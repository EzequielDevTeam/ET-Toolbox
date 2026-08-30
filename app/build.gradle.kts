plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "technology.ezequieldevteam.ettoolbox"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "technology.ezequieldevteam.ettoolbox"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.libsu)
}