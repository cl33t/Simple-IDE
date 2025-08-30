plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "s.ide.compiler"
    compileSdk = 35

    defaultConfig {
        minSdk = 31
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":logging"))
    implementation(project(":utils"))
    implementation(project(":signer"))
    implementation(project(":ui"))

    //noinspection GradleDependency
    implementation("org.eclipse.jdt:ecj:3.18.0")
    implementation("com.android.tools:r8:8.11.18")
    implementation("androidx.annotation:annotation-jvm:1.9.1")
}
