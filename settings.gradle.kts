pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
        maven { url = uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") }

    }
    plugins {
        id("com.android.application") version "8.9.2"
        id("org.jetbrains.kotlin.android") version "2.1.0"
        id("com.google.gms.google-services") version "4.4.0"
        id("com.google.firebase.crashlytics") version "2.9.9"
        id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" // 👈 REQUIRED
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven { url = uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") }

    }
}

rootProject.name = "SMUNavigator"
include(":app")
 