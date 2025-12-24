plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myreadbookapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myreadbookapplication"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Production URL (sẽ được override bởi buildTypes nếu cần)
        //http://192.168.1.205:9000/
        //https://fliply.fly.dev/
        buildConfigField("String", "BASE_URL", "\"https://10.167.82.139:8000/\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // Android core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)



    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
}