plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.fitblock.game"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fitblock.game"
        minSdk = 24
        // Google Play 2026 Requirement: Target Android 15 (API 35)
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    // --- RELEASE SIGNING CONFIGURATION ---
    // Google Play & Indus Appstore require release-signed AAB/APK
    signingConfigs {
        create("release") {
            // OPTION 1: For local build - create your keystore once
            // Run in terminal: keytool -genkey -v -keystore fitblock-release.keystore -alias fitblock -keyalg RSA -keysize 2048 -validity 10000
            // Then put the file in app/ folder
            
            // OPTION 2: For GitHub Actions - set these as Secrets
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "fitblock-release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "YOUR_STORE_PASSWORD_HERE"
            keyAlias = System.getenv("KEY_ALIAS") ?: "fitblock"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "YOUR_KEY_PASSWORD_HERE"
            
            // If keystore not found, build will still work with debug key for testing
            // But for Play Store upload, you MUST provide a valid release keystore
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    // Required for .aab bundle support
    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-graphics:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    // AdMob for monetization - Required for AD_ID permission
    implementation("com.google.android.gms:play-services-ads:23.0.0")
}
