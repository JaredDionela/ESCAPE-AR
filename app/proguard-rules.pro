# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ===== Unity Integration Rules =====
# Keep all Unity classes - CRITICAL for Unity to work
-keep class com.unity3d.** { *; }
-keep interface com.unity3d.** { *; }
-dontwarn com.unity3d.**

# Keep Unity player specifically
-keep class com.unity3d.player.UnityPlayer { *; }
-keep class com.unity3d.player.UnityPlayerActivity { *; }
-keep class com.unity3d.player.UnityPlayerGameActivity { *; }
-keep class com.unity3d.player.IUnityPlayerLifecycleEvents { *; }

# Keep all Unity native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep classes used by Unity
-keep class com.unity.** { *; }
-keep class bitter.jnibridge.** { *; }
-keepattributes *Annotation*

# Don't obfuscate Unity classes
-keepnames class com.unity3d.** { *; }

# Keep R classes
-keep class **.R$* { *; }
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep our Unity wrapper classes
-keep class com.example.escape_ar.unity.** { *; }
-keepclassmembers class com.example.escape_ar.unity.** { *; }

# Disable optimization for Unity classes
-dontoptimize
-dontobfuscate