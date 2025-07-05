plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

apply(from = "${rootProject.projectDir}/publish.gradle")

android {

    namespace = "com.ramcosta.composedestinations.wear"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    defaultConfig {
        minSdk = 25

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles.add(File("consumer-rules.pro"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        compose = true
    }

}

kotlin {
    jvmToolchain(11)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=com.ramcosta.composedestinations.annotation.internal.InternalDestinationsApi"
        )
    }
}

dependencies {
    api(project(mapOf("path" to ":compose-destinations")))

    api(libs.wear.compose.navigation)
}
