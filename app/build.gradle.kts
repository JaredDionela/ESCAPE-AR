plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

// --- Supabase secrets loading (avoid hardcoding / typos) ---
// Values are stored in root local.properties (NOT committed) as:
// SUPABASE_URL=https://your-project-id.supabase.co
// SUPABASE_ANON_KEY=eyJ... (anon public key)
import java.util.Properties

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val supabaseUrl: String = (localProps.getProperty("SUPABASE_URL") ?: "").trim()
val supabaseAnonKey: String = (localProps.getProperty("SUPABASE_ANON_KEY") ?: "").trim()

if (supabaseUrl.isBlank()) {
    logger.warn("[Supabase] SUPABASE_URL missing in local.properties – BuildConfig will contain blank, backend calls will be disabled.")
} else if (!supabaseUrl.startsWith("https://") || !supabaseUrl.contains(".supabase.co")) {
    logger.warn("[Supabase] SUPABASE_URL appears malformed: $supabaseUrl")
}

android {
    namespace = "com.example.escape_ar"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.escape_ar"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // BuildConfig injection for Supabase (loaded from local.properties)
    buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
    buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
    }

    signingConfigs {
        create("release") {
            // For now, using debug keystore for quick release build
            // In production, replace with your actual keystore
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true // ensure BuildConfig class generated
    }
    
    // Updated packaging DSL (AGP 8+)
    packaging {
        jniLibs.pickFirsts += listOf("**/libc++_shared.so", "**/libjsc.so")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Supabase + Ktor (real backend integration)
    // NOTE: Using supabase-kt version 2.5.4 (pre-3.x API).
    // In 2.x the auth module artifact is 'gotrue-kt'. (It was renamed to 'auth-kt' in 3.0.0.)
    val supabaseVersion = "2.5.4"
    implementation(platform("io.github.jan-tennert.supabase:bom:$supabaseVersion"))
    implementation("io.github.jan-tennert.supabase:supabase-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    // Align Ktor with supabase-kt (2.x uses Ktor 2.3.12)
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-logging:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-utils:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    
    // Animation
    implementation("androidx.compose.animation:animation:1.6.8")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    
    // YouTube Player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
    
    // Unity as a Library (enabled)
    // TEMPORARILY DISABLED TO BUILD WITHOUT UNITY (for testing new features)
    // Uncomment when you need Unity AR features or have freed up disk space:
    // implementation(project(":unityLibrary"))
    // Explicit dependencies required for subclassing UnityPlayerGameActivity
    implementation("androidx.games:games-activity:3.0.5")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}