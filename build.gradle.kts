// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
        google()
    }
}

plugins {
    alias(androidx.plugins.android.application) apply false
    alias(androidx.plugins.android.library) apply false
    alias(dandanplay.plugins.application) apply false
    alias(dandanplay.plugins.library) apply false
    alias(dandanplay.plugins.router) apply false
    alias(kotlinx.plugins.jvm) apply false
    alias(kotlinx.plugins.kapt) apply false
    alias(kotlinx.plugins.kotlin) apply false
    alias(kotlinx.plugins.ksp) apply false
    alias(kotlinx.plugins.parcelize) apply false
    alias(libs.plugins.router.agp) apply false
}