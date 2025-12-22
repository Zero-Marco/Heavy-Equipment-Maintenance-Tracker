plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.hilt.android)
    id("androidx.navigation.safeargs.kotlin")

}

android {
    namespace = "com.example.heaveyequpments"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.heaveyequpments"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
}
configurations.all {
    resolutionStrategy {
        // Exclude the older annotation group which contains the duplicate classes
        exclude(group = "com.intellij", module = "annotations")
    }
}


dependencies {
    // Core & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation("com.google.android.material:material:1.12.0")

    // Lifecycle, Navigation, Room
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.gridlayout)

    ksp(libs.androidx.room.compiler)

    implementation("com.google.code.gson:gson:2.10.1")
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // ADDED: Glide dependency for efficient image loading from URI
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("org.jetbrains:annotations:23.0.0")
}