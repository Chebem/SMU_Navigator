pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
        maven { url = uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") }

    }
    plugins {
        id("com.android.application") version "8.9.3"
        id("org.jetbrains.kotlin.android") version "2.1.0"
        id("com.google.gms.google-services") version "4.4.4"
        id("com.google.firebase.crashlytics") version "3.0.6"
        id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" //
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
 