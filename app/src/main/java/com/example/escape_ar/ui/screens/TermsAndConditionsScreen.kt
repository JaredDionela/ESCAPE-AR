package com.example.escape_ar.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TermsAndConditionsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var isChecked by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Terms and Conditions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Divider(modifier = Modifier.padding(bottom = 16.dp))

            // Scrollable Terms Content
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Welcome to Project E.S.C.A.P.E!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Last Updated: October 2025",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TermsSection(
                        title = "1. Acceptance of Terms",
                        content = "By accessing and using the Project E.S.C.A.P.E application, you accept and agree to be bound by the terms and provision of this agreement. If you do not agree to these terms, please do not use this application."
                    )

                    TermsSection(
                        title = "2. Use of Application",
                        content = "This application is intended for educational purposes. You agree to use this app in compliance with all applicable laws and regulations. The AR features require camera access to function properly."
                    )

                    TermsSection(
                        title = "3. User Accounts",
                        content = "You are responsible for maintaining the confidentiality of your account credentials. Any activities that occur under your account are your responsibility. Please notify us immediately of any unauthorized access."
                    )

                    TermsSection(
                        title = "4. Privacy and Data Collection",
                        content = "We collect and store your email, name, and learning progress to provide you with a personalized learning experience. Your data is securely stored and will never be shared with third parties without your consent."
                    )

                    TermsSection(
                        title = "5. AR Content and Safety",
                        content = "When using AR features, please ensure you are in a safe environment. Be aware of your surroundings and avoid using AR mode while walking or in hazardous locations. We are not liable for injuries resulting from improper use."
                    )

                    TermsSection(
                        title = "6. Intellectual Property",
                        content = "All content, including but not limited to text, graphics, 3D models, videos, and AR experiences, are the property of Project E.S.C.A.P.E and are protected by copyright laws."
                    )

                    TermsSection(
                        title = "7. Modifications to Service",
                        content = "We reserve the right to modify or discontinue the service at any time without prior notice. We may also update these terms and conditions periodically."
                    )

                    TermsSection(
                        title = "8. Limitation of Liability",
                        content = "The app is provided 'as is' without warranties of any kind. We are not liable for any damages arising from the use or inability to use this application."
                    )

                    TermsSection(
                        title = "9. Contact Information",
                        content = "For questions or concerns about these terms, please contact us through the Help Center in the app settings."
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Agreement Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I have read and agree to the Terms and Conditions",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Accept Button
            Button(
                onClick = {
                    // Save acceptance in SharedPreferences
                    val sharedPrefs = context.getSharedPreferences("escape_ar_settings", Context.MODE_PRIVATE)
                    sharedPrefs.edit().putBoolean("terms_accepted", true).apply()
                    
                    // Navigate to auth screen
                    navController.navigate("auth") {
                        popUpTo("terms_and_conditions") { inclusive = true }
                    }
                },
                enabled = isChecked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Accept and Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            textAlign = TextAlign.Justify
        )
    }
}
