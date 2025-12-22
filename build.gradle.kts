// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    val nav_version = "2.9.6" // Use the same version as the dependencies
    id("androidx.navigation.safeargs.kotlin") version nav_version apply false

}
