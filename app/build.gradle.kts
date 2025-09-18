import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {

    namespace = "s.ide"
    compileSdk = 36

    defaultConfig {
        minSdk = 31
        targetSdk = 35
        versionCode = verCode()
        versionName = verName()
        resValue("string", "app_name", rootProject.name)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }


    kotlinOptions {
        jvmTarget = "17"
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }

    packaging {
        jniLibs.useLegacyPackaging = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-android:2.9.4")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation(platform("io.github.Rosemoe.sora-editor:bom:0.23.6"))
    implementation("io.github.Rosemoe.sora-editor:editor")
    implementation("io.github.Rosemoe.sora-editor:language-textmate")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("commons-io:commons-io:2.20.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")

    implementation("com.android.tools:r8:8.11.18")
    implementation(project(":compiler"))
    implementation(project(":signer"))
    implementation(project(":logging"))
    implementation(project(":utils"))
    implementation(project(":ui"))
    implementation("androidx.activity:activity:1.11.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

}

fun verName(): String {
    val date = Date()
    val formattedDate = SimpleDateFormat("yyMMdd").format(date)
    return formattedDate
}

fun verCode(): Int {
    val properties = Properties()
    val propertiesFile = file("version.properties")
    if (propertiesFile.exists()) {
        properties.load(FileInputStream(propertiesFile))
    }
    val versionCode = properties.getProperty("VERSION_CODE", "0").toInt() + 1
    properties.setProperty("VERSION_CODE", versionCode.toString())
    properties.store(FileOutputStream(propertiesFile), null)
    return versionCode
}
