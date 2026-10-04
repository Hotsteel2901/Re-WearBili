plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.google.protobuf)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace  = "cn.spacexc.wearbili.common"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTool.get()

    defaultConfig {
        minSdk = 25
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

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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

    ksp(libs.hilt.compiler)
}