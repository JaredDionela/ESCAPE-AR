// OPTION 2: Custom Tabs Implementation
// Replace the onLessonClick implementation in LessonsScreen.kt

// Add this import at the top of LessonsScreen.kt:
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

// Replace the existing onLessonClick code (lines 193-200) with this:

onLessonClick = {
    // Open YouTube video in Custom Tab (Chrome Custom Tabs)
    // This keeps the user in your app context with a back button
    try {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(false)
            .build()
        
        customTabsIntent.launchUrl(
            context,
            "https://www.youtube.com/watch?v=${lesson.youtubeVideoId}".toUri()
        )
    } catch (e: Exception) {
        // Fallback to regular intent if Custom Tabs not available
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/watch?v=${lesson.youtubeVideoId}")
        )
        context.startActivity(intent)
    }
}

// STEP 1: Add dependency to app/build.gradle.kts:
/*
dependencies {
    // Existing dependencies...
    implementation("androidx.browser:browser:1.7.0")
}
*/

// STEP 2: Sync Gradle

// STEP 3: Replace the onLessonClick code with the code above

// That's it! Custom Tabs provides:
// ✅ Back button returns to your app
// ✅ Feels more integrated (appears as overlay)
// ✅ Fast and lightweight
// ✅ Smooth transitions
// ✅ Users don't feel like they left your app
