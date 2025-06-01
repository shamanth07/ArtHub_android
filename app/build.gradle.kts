plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.arthub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.arthub"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {


    implementation("com.google.firebase:firebase-auth:22.0.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.inappmessaging)
    implementation (libs.appcompat.v161)

    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")// for map api
    implementation ("com.google.android.libraries.places:places:3.2.0")// for place api







    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.playServicesMaps)
    implementation(libs.firebase.storage)
    implementation(libs.playServicesMaps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}