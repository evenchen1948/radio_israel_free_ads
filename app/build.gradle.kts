import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Release signing reads the upload keystore location and passwords from keystore.properties in
// the project root. That file and the keystore are git-ignored, so no secret is stored in the repo.
// The RADIO_KEYSTORE_* environment variables are used when the file is absent (for example on CI).
// With neither, release builds are produced unsigned.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun signingValue(property: String, envVariable: String): String? =
    keystoreProperties.getProperty(property) ?: System.getenv(envVariable)

val releaseKeystoreFile: String? = signingValue("storeFile", "RADIO_KEYSTORE_FILE")

android {
    namespace = "io.github.evenchen1948.radioil"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.evenchen1948.radioil"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (releaseKeystoreFile != null) {
            create("release") {
                // A relative path is resolved against the project root.
                storeFile = rootProject.file(releaseKeystoreFile)
                storePassword = signingValue("storePassword", "RADIO_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "RADIO_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "RADIO_KEY_PASSWORD")
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
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
}
