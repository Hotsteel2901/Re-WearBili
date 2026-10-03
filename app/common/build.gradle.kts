plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.google.protobuf)
    kotlin("kapt")
}

android {
    namespace  = "cn.spacexc.wearbili.common"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTool.get()

    defaultConfig {
        minSdk = 21
        lint.targetSdk = 36

    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        create("benchmarkRelease") {
        }
        create("nonMinifiedRelease") {
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// kotlinOptions { jvmTarget = ... } 在 KGP 2.2+ 是 error 级别弃用，改用 compilerOptions DSL
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(libs.versions.jvm.target.get()))
    }
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.androidx.ui.graphics)

    //api(libs.bilibili.sdk)
    //api(project(":libs"))
    api(project(":libs"))
    api(libs.hilt.android)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.serialization.kotlinx.protobuf)

    implementation(libs.atomicfu)

    api(libs.protobuf.javalite)
    api(libs.protobuf.kotlin.lite)

    api(libs.gson)

    implementation(libs.zxing.core)

    kapt(libs.hilt.compiler)
}