package com.example.escape_ar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.escape_ar.utils.AudioManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTermsScreen(navController: NavController) {
    val context = LocalContext.current
    val audioManager = remember { AudioManager.getInstance(context) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Privacy") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            audioManager.playButtonClick()
                            navController.popBackStack()
                        }
                    ) {
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Terms of Service Section
            Text(
                text = "Terms of Service",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Last Updated: October 2025",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TermsSection(
                title = "1. Acceptance of Terms",
                content = "By accessing and using Project E.S.C.A.P.E, you accept and agree to be bound by the terms and provision of this agreement. If you do not agree to these terms, please do not use this application."
            )
            
            TermsSection(
                title = "2. Educational Use",
                content = "This application is designed for educational purposes. The content provided is meant to supplement, not replace, formal education. Users should consult with their teachers and educational institutions for comprehensive learning."
            )
            
            TermsSection(
                title = "3. User Accounts",
                content = "You are responsible for maintaining the confidentiality of your account credentials. You agree to accept responsibility for all activities that occur under your account. Please notify us immediately of any unauthorized use of your account."
            )
            
            TermsSection(
                title = "4. Content Accuracy",
                content = "While we strive to provide accurate and up-to-date educational content, we make no representations or warranties about the completeness, accuracy, or reliability of any content. Educational content is subject to change and updates."
            )
            
            TermsSection(
                title = "5. AR Safety",
                content = "When using Augmented Reality features, please be aware of your surroundings. Always use AR features in a safe environment. We are not responsible for injuries or accidents that occur while using AR features."
            )
            
            TermsSection(
                title = "6. Privacy",
                content = "Your privacy is important to us. We collect only necessary information to provide our services. Your personal information will not be shared with third parties without your consent, except as required by law."
            )
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Privacy Policy Section
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            TermsSection(
                title = "Information We Collect",
                content = "We collect information that you provide directly to us, including your name, email address, and learning progress. We also collect device information and usage data to improve our services."
            )
            
            TermsSection(
                title = "How We Use Your Information",
                content = "We use your information to:\n• Provide and improve our educational services\n• Track your learning progress\n• Communicate important updates\n• Ensure account security\n• Analyze app usage to enhance user experience"
            )
            
            TermsSection(
                title = "Data Security",
                content = "We implement appropriate security measures to protect your personal information. However, no method of transmission over the internet is 100% secure. We cannot guarantee absolute security."
            )
            
            TermsSection(
                title = "Children's Privacy",
                content = "Our service is designed for students. We comply with applicable laws regarding children's privacy. Parents and guardians should supervise their children's use of the application."
            )
            
            TermsSection(
                title = "Your Rights",
                content = "You have the right to access, update, or delete your personal information. You can also opt-out of certain data collection. Contact us at jrdnether@gmail.com to exercise these rights."
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Questions or Concerns?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "If you have any questions about these Terms or our Privacy Policy, please contact us at:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "jrdnether@gmail.com",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TermsSection(title: String, content: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
