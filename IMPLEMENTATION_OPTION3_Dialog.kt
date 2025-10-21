// OPTION 3: Dialog with Instructions
// This is the quickest fix - no dependencies needed!
// Replace the onLessonClick implementation in LessonsScreen.kt

// Add these imports at the top if not already present:
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

// Add this state variable inside the LessonsScreen composable:
var showVideoDialog by remember { mutableStateOf(false) }
var selectedLesson by remember { mutableStateOf<Lesson?>(null) }

// Replace the onLessonClick code with this:
onLessonClick = {
    selectedLesson = it
    showVideoDialog = true
}

// Add this Dialog at the end of the LessonsScreen composable (before the last closing brace):

if (showVideoDialog && selectedLesson != null) {
    AlertDialog(
        onDismissRequest = { showVideoDialog = false },
        icon = {
            Text("📺", style = MaterialTheme.typography.displaySmall)
        },
        title = {
            Text("Watch Video Lesson")
        },
        text = {
            Column {
                Text(
                    text = selectedLesson!!.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "This will open YouTube. After watching, use your device's back button or home button to return to ESCAPE-AR.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Tip: Swipe down from the top of the screen and tap on the ESCAPE-AR notification to return quickly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showVideoDialog = false
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=${selectedLesson!!.youtubeVideoId}")
                    )
                    context.startActivity(intent)
                }
            ) {
                Text("Open YouTube")
            }
        },
        dismissButton = {
            TextButton(onClick = { showVideoDialog = false }) {
                Text("Cancel")
            }
        }
    )
}

// That's it! This solution:
// ✅ No dependencies needed
// ✅ 5-minute implementation
// ✅ Educates users on how to return
// ✅ Professional appearance
// ✅ Users can cancel if they changed their mind

// BONUS: Enhanced version with more options
/*
if (showVideoDialog && selectedLesson != null) {
    AlertDialog(
        onDismissRequest = { showVideoDialog = false },
        title = { Text("Choose How to Watch") },
        text = {
            Column {
                Text("Open this video in:")
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = {
                        showVideoDialog = false
                        // Open in YouTube app
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("vnd.youtube:${selectedLesson!!.youtubeVideoId}")
                        )
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // YouTube app not installed, use browser
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=${selectedLesson!!.youtubeVideoId}")
                            )
                            context.startActivity(browserIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📱 YouTube App")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = {
                        showVideoDialog = false
                        // Open in browser
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/watch?v=${selectedLesson!!.youtubeVideoId}")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🌐 Browser")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "💡 Use the back button to return to ESCAPE-AR",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { showVideoDialog = false }) {
                Text("Cancel")
            }
        }
    )
}
*/
