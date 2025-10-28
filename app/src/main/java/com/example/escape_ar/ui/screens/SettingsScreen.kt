package com.example.escape_ar.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.escape_ar.BuildConfig
import com.example.escape_ar.data.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onBackgroundMusicToggle: (Boolean) -> Unit,
    onMusicVolumeChange: (Float) -> Unit,
    onSoundEffectsToggle: (Boolean) -> Unit,
    onEffectsVolumeChange: (Float) -> Unit,
    onWifiOnlyToggle: (Boolean) -> Unit,
    onClearCache: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { com.example.escape_ar.utils.AudioManager.getInstance(context) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    // Load saved preferences
    val sharedPrefs = context.getSharedPreferences("escape_ar_settings", Context.MODE_PRIVATE)
    var backgroundMusicEnabled by remember { 
        mutableStateOf(sharedPrefs.getBoolean("background_music", true)) 
    }
    var musicVolume by remember {
        mutableStateOf(sharedPrefs.getFloat("music_volume", 0.5f))
    }
    var soundEffectsEnabled by remember { 
        mutableStateOf(sharedPrefs.getBoolean("sound_effects", true)) 
    }
    var effectsVolume by remember {
        mutableStateOf(sharedPrefs.getFloat("effects_volume", 0.7f))
    }
    var wifiOnlyDownload by remember { 
        mutableStateOf(sharedPrefs.getBoolean("wifi_only", false)) 
    }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Function to save settings to Supabase
    fun saveSettingsToSupabase() {
        scope.launch {
            try {
                isSaving = true
                val userId = SupabaseConfig.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    SupabaseConfig.client.from("profiles").update(
                        {
                            set("background_music", backgroundMusicEnabled)
                            set("music_volume", musicVolume)
                            set("sound_effects", soundEffectsEnabled)
                            set("effects_volume", effectsVolume)
                            set("wifi_only", wifiOnlyDownload)
                        }
                    ) {
                        filter {
                            eq("id", userId)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SettingsScreen", "Error saving to Supabase: ${e.message}")
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Audio Settings Section
            SettingsSection(title = "Audio") {
                // Background Music Toggle
                SwitchSettingItem(
                    icon = Icons.Default.MusicNote,
                    title = "Background Music",
                    subtitle = "Play music in the app",
                    checked = backgroundMusicEnabled,
                    onCheckedChange = { enabled ->
                        audioManager.playButtonClick()
                        backgroundMusicEnabled = enabled
                        sharedPrefs.edit().putBoolean("background_music", enabled).apply()
                        onBackgroundMusicToggle(enabled)
                        saveSettingsToSupabase()
                    }
                )
                
                // Music Volume Slider
                if (backgroundMusicEnabled) {
                    SliderSettingItem(
                        icon = Icons.Default.VolumeUp,
                        title = "Music Volume",
                        value = musicVolume,
                        onValueChange = { volume ->
                            musicVolume = volume
                            sharedPrefs.edit().putFloat("music_volume", volume).apply()
                            onMusicVolumeChange(volume)
                        },
                        onValueChangeFinished = {
                            saveSettingsToSupabase()
                        }
                    )
                }
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // Sound Effects Toggle
                SwitchSettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Sound Effects",
                    subtitle = "Enable button and action sounds",
                    checked = soundEffectsEnabled,
                    onCheckedChange = { enabled ->
                        // Play sound before toggling (so it plays even when enabling)
                        if (enabled) audioManager.playButtonClick()
                        soundEffectsEnabled = enabled
                        sharedPrefs.edit().putBoolean("sound_effects", enabled).apply()
                        onSoundEffectsToggle(enabled)
                        saveSettingsToSupabase()
                    }
                )
                
                // Effects Volume Slider
                if (soundEffectsEnabled) {
                    SliderSettingItem(
                        icon = Icons.Default.VolumeUp,
                        title = "Effects Volume",
                        value = effectsVolume,
                        onValueChange = { volume ->
                            effectsVolume = volume
                            sharedPrefs.edit().putFloat("effects_volume", volume).apply()
                            onEffectsVolumeChange(volume)
                        },
                        onValueChangeFinished = {
                            saveSettingsToSupabase()
                        }
                    )
                }
            }

            Divider()

            // Data & AR Settings Section
            SettingsSection(title = "Data & AR") {
                SwitchSettingItem(
                    icon = Icons.Default.Wifi,
                    title = "Wi-Fi Only Downloads",
                    subtitle = "Download AR modules only on Wi-Fi",
                    checked = wifiOnlyDownload,
                    onCheckedChange = { enabled ->
                        audioManager.playButtonClick()
                        wifiOnlyDownload = enabled
                        sharedPrefs.edit().putBoolean("wifi_only", enabled).apply()
                        onWifiOnlyToggle(enabled)
                        saveSettingsToSupabase()
                    }
                )

                SettingItem(
                    icon = Icons.Default.DeleteOutline,
                    title = "Clear AR Content Cache",
                    subtitle = "Free up storage space",
                    onClick = { 
                        audioManager.playButtonClick()
                        showClearCacheDialog = true 
                    }
                )
            }

            Divider()

            // Legal & Support Section
            SettingsSection(title = "Legal & Support") {
                SettingItem(
                    icon = Icons.Default.Description,
                    title = "Terms & Conditions",
                    subtitle = "View terms of service",
                    onClick = { 
                        audioManager.playButtonClick()
                        navController.navigate("settings_terms")
                    }
                )

                SettingItem(
                    icon = Icons.Default.Gavel,
                    title = "Privacy Policy",
                    subtitle = "View privacy policy",
                    onClick = { 
                        audioManager.playButtonClick()
                        navController.navigate("settings_terms")
                    }
                )
                
                SettingItem(
                    icon = Icons.Default.Help,
                    title = "Help Center",
                    subtitle = "Get help and report bugs",
                    onClick = {
                        audioManager.playButtonClick()
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:jrdnether@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Project E.S.C.A.P.E - Help Request")
                                putExtra(Intent.EXTRA_TEXT, "App Version: ${BuildConfig.VERSION_NAME}\n\nDescribe your issue:\n\n")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.util.Log.e("SettingsScreen", "No email client found: ${e.message}")
                            // Show a toast or snackbar as fallback
                            android.widget.Toast.makeText(
                                context,
                                "No email app found. Please email: jrdnether@gmail.com",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
                
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "About Project E.S.C.A.P.E",
                    subtitle = "Version ${BuildConfig.VERSION_NAME}",
                    onClick = { 
                        audioManager.playButtonClick()
                        navController.navigate("about")
                    }
                )
            }
            
            // Show saving indicator
            if (isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text(
                            text = "Saving settings...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Clear Cache Confirmation Dialog
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { 
                audioManager.playButtonClick()
                showClearCacheDialog = false 
            },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            title = { Text("Clear AR Content Cache?") },
            text = { Text("This will delete all downloaded AR module data. You'll need to re-download them when you use AR features again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        audioManager.playButtonClick()
                        onClearCache()
                        showClearCacheDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    audioManager.playButtonClick()
                    showClearCacheDialog = false 
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SwitchSettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun SliderSettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
                )
            )
        }
    }
}
