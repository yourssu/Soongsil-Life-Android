plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    kotlin("plugin.serialization")
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {
    implementation(libs.kotlinx.serialization.json)
}