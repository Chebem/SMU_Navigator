plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.smunavigator2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smunavigator2"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.1.0" // ✅ Match Kotlin plugin
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true // ✅ Enable desugaring
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }

    buildToolsVersion = "35.0.1"

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }
}

dependencies {
    // ✅ Latest Compose BOM
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    //KakaoMap
    implementation ("com.kakao.maps.open:android:2.12.8")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")




    // Core
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-appcheck:17.0.0")
    implementation ("com.google.firebase:firebase-appcheck-playintegrity:17.0.0")

    // UI Libraries
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0-beta01")
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("com.github.ismaeldivita:chip-navigation-bar:1.3.2")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.androidx.activity)
    implementation(libs.play.services.maps)
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    //Calender
    implementation ("com.kizitonwose.calendar:view:2.6.2")
    // Add this under dependencies
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    implementation ("com.google.android.material:material:1.11.0")
    implementation ("com.tbuonomo:dotsindicator:4.3")


    implementation ("de.hdodenhof:circleimageview:3.1.0")






    // Java 8+ Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation ("com.google.android.gms:play-services-location:21.0.1")
}

// ✅ Global resolution if needed
configurations.all {
    resolutionStrategy {
        force("androidx.core:core-ktx:1.16.0")
        force("androidx.appcompat:appcompat:1.6.1")
        force("com.google.android.material:material:1.11.0")
    }
}
