plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "s.ide.utils"
    compileSdk = 35

    defaultConfig {
        minSdk = 31
        resValue("string", "app_name", rootProject.name)
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
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("androidx.core:core-ktx:1.17.0")

    implementation("io.github.Rosemoe.sora-editor:editor:0.23.6")
    implementation("io.github.Rosemoe.sora-editor:language-java:0.23.6")

    implementation(project(":format"))
}
