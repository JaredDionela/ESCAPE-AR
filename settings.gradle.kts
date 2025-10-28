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
            dirs("unityLibrary/unityLibrary/libs")
        }
    }
}

rootProject.name = "ESCAPE-AR"
include(":app")
// Unity AR Integration
include(":unityLibrary")
include(":unityLibrary:xrmanifest.androidlib")
project(":unityLibrary").projectDir = file("unityLibrary/unityLibrary")
project(":unityLibrary:xrmanifest.androidlib").projectDir = file("unityLibrary/unityLibrary/xrmanifest.androidlib")

