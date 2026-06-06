import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun String.asBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.mrmustard.activelistening"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.mrmustard.activelistening"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "DEVEXPERT_API_KEY",
            (
                localProperties.getProperty("devexpert.apiKey")
                    ?: localProperties.getProperty("openai.apiKey")
                    ?: System.getenv("DEVEXPERT_API_KEY")
                    ?: ""
                ).asBuildConfigString(),
        )
        buildConfigField(
            "String",
            "DEVEXPERT_GUIDANCE_MODEL",
            (
                localProperties.getProperty("devexpert.guidanceModel")
                    ?: localProperties.getProperty("openai.guidanceModel")
                    ?: "mimo-v2.5"
                ).asBuildConfigString(),
        )
        buildConfigField(
            "String",
            "DEVEXPERT_BASE_URL",
            (
                localProperties.getProperty("devexpert.baseUrl")
                    ?: "https://inference.devexpert.io/v1/"
                ).asBuildConfigString(),
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

configurations.all {
    resolutionStrategy.force(libs.kotlin.metadata.jvm)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.hilt.android)
    implementation(libs.media3.exoplayer)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(platform(libs.openai.client.bom))
    implementation(libs.openai.client)
    runtimeOnly(libs.ktor.client.okhttp)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.room.compiler)
    ksp(libs.kotlin.metadata.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
