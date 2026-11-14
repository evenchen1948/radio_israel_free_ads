plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Release signing comes from environment variables so no secret is ever stored in the repo.
// Without them, release builds are produced unsigned.
val releaseKeystoreFile: String? = System.getenv("RADIO_KEYSTORE_FILE")

android {
    namespace = "com.israeliradio.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.israeliradio.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (releaseKeystoreFile != null) {
            create("release") {
                storeFile = file(releaseKeystoreFile)
                storePassword = System.getenv("RADIO_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RADIO_KEY_ALIAS")
                keyPassword = System.getenv("RADIO_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // R8: shrink, optimize and obfuscate code, and drop unused resources.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.session)
    implementation(libs.kotlinx.serialization.json)
}
