// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    alias(libs.plugins.ksp) apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
}