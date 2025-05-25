// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.8.8" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {
    repositories {
        google() // Google's Maven repository
        mavenCentral() // Maven Central repository
    }
    dependencies {
        // Add this for Google Sign-In (Google Services plugin)
        classpath("com.google.gms:google-services:4.4.2") // Use the latest version
    }
}