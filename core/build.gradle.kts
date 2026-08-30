plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}