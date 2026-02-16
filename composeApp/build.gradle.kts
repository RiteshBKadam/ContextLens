import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
//    alias(libs.plugins.googleGmsGoogleServices)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
    id("app.cash.sqldelight") version "2.0.2"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("com.google.code.gson:gson:2.11.0")
            implementation("app.cash.sqldelight:android-driver:2.0.2")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("app.cash.sqldelight:runtime:2.0.2")



        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.ktor.client.cio)
            implementation("net.java.dev.jna:jna:5.18.1")
            implementation(libs.jna.platform)
            implementation(libs.okhttp)
            implementation("com.google.code.gson:gson:2.13.2")
            implementation(libs.sqlite.driver)
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.coroutines.extensions)
            implementation(libs.androidx.collection)
            implementation("com.google.code.gson:gson:2.13.2")
            implementation("org.json:json:20251224")
            implementation("com.fasterxml.jackson.core:jackson-databind:2.21.0")
            implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.21.0")

        }
    }
}
sqldelight {
    databases {
        create("ContextLensDatabase") {
            packageName.set("com.riteshbkadam.contextlens.db")
        }
    }
}
android {
    namespace = "com.riteshbkadam.contextlens"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.riteshbkadam.contextlens"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.riteshbkadam.contextlens.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.riteshbkadam.contextlens"
            packageVersion = "1.0.0"
        }
    }
}
