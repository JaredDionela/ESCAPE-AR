pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("unityLibrary/unityLibrary/unityLibrary/libs")
        }
    }
}

rootProject.name = "ESCAPE-AR"
include(":app")
// Re-enable Unity library for AR lab access without gating
// TEMPORARILY DISABLED TO BUILD WITHOUT UNITY (for testing new features)
// Uncomment these lines when you need Unity AR features or have freed up disk space:
// include(":unityLibrary")
// include(":unityLibrary:xrmanifest.androidlib")
// project(":unityLibrary").projectDir = file("unityLibrary/unityLibrary/unityLibrary")
// project(":unityLibrary:xrmanifest.androidlib").projectDir = file("unityLibrary/unityLibrary/unityLibrary/xrmanifest.androidlib")

