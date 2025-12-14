// In C:/Users/lenovo/AndroidStudioProjects/ChroniCare/app/build.gradle.kts
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.chronicare"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.chronicare"
        minSdk = 24
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

    // --- THIS IS THE FIX ---
    // Make Java and Kotlin target the same JVM version.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Changed from 11 to 17
        targetCompatibility = JavaVersion.VERSION_17 // Changed from 11 to 17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        // The old, redundant kotlinOptions block has been removed.
    }
    // --- END OF FIX ---

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Firebase - Import the Bill of Materials (BoM)
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    implementation("com.google.firebase:firebase-firestore-ktx")

    // Compose - Import the Bill of Materials (BoM)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation(libs.androidx.material.icons.extended)
// Use the correct alias from TOML\
    implementation("androidx.compose.material:material-icons-core")



    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coil for Image Loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug dependencies
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
