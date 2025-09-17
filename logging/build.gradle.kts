plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "s.ide.logging"
    compileSdk = 35

    defaultConfig {
        minSdk = 31
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":utils"))
    implementation("commons-io:commons-io:2.20.0")
    implementation("androidx.annotation:annotation-jvm:1.9.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-android:2.9.3")
    //noinspection KtxExtensionAvailable
    implementation("androidx.lifecycle:lifecycle-livedata-core:2.9.4")
    implementation("androidx.core:core-ktx:1.17.0")
}
