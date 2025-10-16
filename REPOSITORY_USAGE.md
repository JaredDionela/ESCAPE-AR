# 📖 Repository Usage Guide

## Quick Reference for Supabase Repositories

### VideoRepository

**Get all videos for a topic:**
```kotlin
val videoRepo = VideoRepository(supabase)
val result = videoRepo.getVideosByTopic("decantation")

result.onSuccess { videos ->
    // Handle list of videos
}.onFailure { error ->
    // Handle error
}
```

**Track video progress:**
```kotlin
// Update progress (every few seconds while playing)
videoRepo.updateVideoProgress(
    videoId = "video-uuid",
    watchedSeconds = 120,
    completed = false
)

// Mark video as complete
videoRepo.markVideoComplete(videoId = "video-uuid")

// Get progress for resume
val progress = videoRepo.getVideoProgress(videoId).getOrNull()
```

**Get videos with user progress:**
```kotlin
val result = videoRepo.getVideosWithProgress("solar_system")
result.onSuccess { list ->
    list.forEach { (video, progress) ->
        println("${video.title}: ${progress?.watchedSeconds ?: 0} seconds watched")
    }
}
```

**Get DepEd lesson:**
```kotlin
val lesson = videoRepo.getDepEdLessonByTopic("organ_system").getOrNull()
```

---

### SettingsRepository

**Get user settings:**
```kotlin
val settingsRepo = SettingsRepository(supabase)
val settings = settingsRepo.getUserSettings().getOrNull()
```

**Update settings:**
```kotlin
// Update all settings
settingsRepo.updateSettings(
    musicVolume = 0.8f,
    sfxVolume = 0.5f,
    captionsEnabled = true
)

// Update individual settings
settingsRepo.updateMusicVolume(0.8f)
settingsRepo.updateSfxVolume(0.5f)
settingsRepo.toggleCaptions(true)

// Reset to defaults
settingsRepo.resetToDefaults()
```

---

### UserRepository (Existing)

**Update profile:**
```kotlin
val userRepo = UserRepository()

suspend fun updateProfile(name: String, teacher: String?, section: String?) {
    val userId = getCurrentUserId()
    
    supabase.from("profiles").update(
        mapOf(
            "display_name" to name,
            "teacher_name" to teacher,
            "section" to section
        )
    ) {
        filter { eq("id", userId) }
    }
}
```

**Get current user profile:**
```kotlin
val profile = supabase.from("profiles")
    .select {
        filter { eq("id", userId) }
    }
    .decodeSingleOrNull<UserProfile>()
```

---

## ViewModel Integration Examples

### Video Lessons ViewModel

```kotlin
class VideoLessonsViewModel(
    private val videoRepo: VideoRepository
) : ViewModel() {
    
    var videos by mutableStateOf<List<Video>>(emptyList())
        private set
    
    var selectedVideo by mutableStateOf<Video?>(null)
        private set
    
    var videoProgress by mutableStateOf<Map<String, VideoProgress>>(emptyMap())
        private set
    
    fun loadVideos(topic: String) {
        viewModelScope.launch {
            val result = videoRepo.getVideosWithProgress(topic)
            result.onSuccess { list ->
                videos = list.map { it.first }
                videoProgress = list.mapNotNull { (video, progress) ->
                    progress?.let { video.id to it }
                }.toMap()
            }
        }
    }
    
    fun selectVideo(video: Video) {
        selectedVideo = video
    }
    
    fun updateProgress(videoId: String, seconds: Int) {
        viewModelScope.launch {
            videoRepo.updateVideoProgress(videoId, seconds)
        }
    }
    
    fun markComplete(videoId: String) {
        viewModelScope.launch {
            videoRepo.markVideoComplete(videoId)
            // Reload to get updated progress
            selectedVideo?.let { video ->
                val topic = video.topic
                loadVideos(topic)
            }
        }
    }
}
```

### Settings ViewModel

```kotlin
class SettingsViewModel(
    private val settingsRepo: SettingsRepository
) : ViewModel() {
    
    var settings by mutableStateOf<UserSettings?>(null)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    init {
        loadSettings()
    }
    
    fun loadSettings() {
        viewModelScope.launch {
            isLoading = true
            settings = settingsRepo.getUserSettings().getOrNull()
            isLoading = false
        }
    }
    
    fun updateMusicVolume(volume: Float) {
        viewModelScope.launch {
            settingsRepo.updateMusicVolume(volume)
            loadSettings()
        }
    }
    
    fun updateSfxVolume(volume: Float) {
        viewModelScope.launch {
            settingsRepo.updateSfxVolume(volume)
            loadSettings()
        }
    }
    
    fun toggleCaptions(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.toggleCaptions(enabled)
            loadSettings()
        }
    }
    
    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepo.resetToDefaults()
            loadSettings()
        }
    }
}
```

---

## Composable Usage Examples

### Video Player with Progress Tracking

```kotlin
@Composable
fun VideoPlayerScreen(
    viewModel: VideoLessonsViewModel = viewModel()
) {
    val video = viewModel.selectedVideo ?: return
    val progress = viewModel.videoProgress[video.id]
    
    Column {
        // YouTube Player
        YouTubePlayerView(
            videoId = video.youtubeId,
            startSeconds = progress?.watchedSeconds?.toFloat() ?: 0f,
            onProgressUpdate = { seconds ->
                viewModel.updateProgress(video.id, seconds.toInt())
            },
            onVideoEnd = {
                viewModel.markComplete(video.id)
            }
        )
        
        // Video Info
        Text(
            text = video.title,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = video.summary ?: "",
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Progress Indicator
        LinearProgressIndicator(
            progress = progress?.let { 
                it.watchedSeconds.toFloat() / (video.durationSeconds ?: 1)
            } ?: 0f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

### Settings Screen

```kotlin
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val settings = viewModel.settings ?: return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Music Volume")
        Slider(
            value = settings.musicVolume,
            onValueChange = { viewModel.updateMusicVolume(it) },
            valueRange = 0f..1f
        )
        
        Text("SFX Volume")
        Slider(
            value = settings.sfxVolume,
            onValueChange = { viewModel.updateSfxVolume(it) },
            valueRange = 0f..1f
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Captions")
            Switch(
                checked = settings.captionsEnabled,
                onCheckedChange = { viewModel.toggleCaptions(it) }
            )
        }
        
        Button(
            onClick = { viewModel.resetToDefaults() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset to Defaults")
        }
    }
}
```

---

## Error Handling

All repository methods return `Result<T>`, handle appropriately:

```kotlin
viewModelScope.launch {
    val result = videoRepo.getVideosByTopic("decantation")
    
    result.fold(
        onSuccess = { videos ->
            // Update UI with videos
            this@ViewModel.videos = videos
        },
        onFailure = { error ->
            // Show error message
            errorMessage = error.message ?: "Unknown error"
            Log.e(TAG, "Failed to load videos", error)
        }
    )
}
```

---

## Topic Constants

Valid topic values (must match database):
- `"decantation"`
- `"organ_system"`
- `"simple_machines"`
- `"solar_system"`

---

*Quick Reference Version 1.0*
