plugins {
    id("com.android.application") version "9.3.2"
    id("org.jetbrains.kotlin.android") version "1.9.24"
}

android {
    namespace = "technology.ezequieldevteam.ettoolbox"
    compileSdk = 36

    defaultConfig {
        applicationId = "technology.ezequieldevteam.ettoolbox"
        minSdk = 26
        targetSdk = 36
        versionCode = 6
        versionName = "0.4.2"
    }

    signingConfigs {
        create("ett") {
            val storeFile = System.getenv("SIGNING_STORE_FILE")?.let { rootProject.file(it) } ?: rootProject.file("ci/signing/ettbox.jks")
            val storePassword = System.getenv("SIGNING_STORE_PASSWORD") ?: "ettbox2026"
            val keyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: "ettoolbox"
            val keyPassword = System.getenv("SIGNING_KEY_PASSWORD") ?: "ettbox2026"

            storeFile = storeFile
            storePassword = storePassword
            keyAlias = keyAlias
            keyPassword = keyPassword
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("ett")
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("ett")
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
}