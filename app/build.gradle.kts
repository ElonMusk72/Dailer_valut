plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 34
    namespace = "com.aistudio.dialer.app"

    defaultConfig {
        applicationId = "com.aistudio.dialer.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Android Core
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.12.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Google Drive API
    implementation("com.google.api-client:google-api-client-android:1.35.2")
    implementation("com.google.apis:google-api-services-drive:v3-rev20231127-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.11.0")

    // FFmpeg for video processing
    implementation("com.arthenica:mobile-ffmpeg-full:4.4.LTS")

    // JSON parsing for service accounts
    implementation("com.google.code.gson:gson:2.10.1")

    // HTTP requests
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
