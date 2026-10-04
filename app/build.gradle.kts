import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.google.protobuf)
    alias(libs.plugins.google.dagger.hilt.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.kotlin.serialization)
}

/**
 * Release 签名配置。
 *
 * 优先级：
 *  1. 环境变量（CI/GitHub Actions）：KEYSTORE_PATH / KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD
 *  2. keystore/keystore.properties（本地开发，已 gitignore）
 *  3. 都没有 → release 回落到 debug 签名（保证构建不中断，但产物不可发布）
 */
val keystorePropsFile = rootProject.file("keystore/keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

fun signingValue(envName: String, propName: String): String? =
    System.getenv(envName)?.takeIf { it.isNotBlank() } ?: keystoreProps.getProperty(propName)

val releaseStoreFile = signingValue("KEYSTORE_PATH", "storeFile")
val releaseStorePassword = signingValue("KEYSTORE_PASSWORD", "storePassword")
val releaseKeyAlias = signingValue("KEY_ALIAS", "keyAlias")
val releaseKeyPassword = signingValue("KEY_PASSWORD", "keyPassword")

val hasReleaseSigning = !releaseStoreFile.isNullOrBlank() &&
        !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank() &&
        rootProject.file(releaseStoreFile).exists()

android {
    namespace = "cn.spacexc.wearbili.remake"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTool.get()

    experimentalProperties["android.experimental.r8.dex-startup-optimization"] = true

    val releaseNumber = 5
    defaultConfig {
        applicationId = "cn.spacexc.wearbili.remake"
        minSdk = 25
        targetSdk = 36
        versionCode = 49
        versionName = "HotSteel 炽热钢铁"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            buildConfigField("Integer", "releaseNumber", "$releaseNumber")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 有真实签名用真实签名，否则回落 debug（本地快速验证用）
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        debug {
            buildConfigField("Integer", "releaseNumber", "$releaseNumber")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes.apply {
            add("/META-INF/{AL2.0,LGPL2.1}")
            add("META-INF/beans.xml")
        }
    }

    // AGP 9: 使用 variant API 自定义 APK 输出文件名
    androidComponents {
        onVariants { variant ->
            val vName = variant.outputs.firstOrNull()?.versionName?.orNull ?: "unknown"
            val vCode = variant.outputs.firstOrNull()?.versionCode?.orNull ?: 0
            variant.outputs.forEach { output ->
                (output as? com.android.build.api.variant.impl.VariantOutputImpl)?.outputFileName?.set(
                    "Re-WearBili - $vName Ver.$releaseNumber Rel.$vCode.apk"
                )
            }
        }
    }


}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:" + libs.versions.protobuflite.get()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(libs.junit)

    // Compose BOM 统一管理 compose.ui / foundation / runtime（material3 单独覆盖到 Expressive 版本）
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.palette.ktx)
    // AGP 9 内建 Kotlin 后，parcelize 运行时依赖不再自动附带，需显式声明
    implementation(libs.kotlin.parcelize.runtime)

    // Haze 液态玻璃 / 毛玻璃
    implementation(libs.haze)
    implementation(libs.haze.blur)
    implementation(libs.haze.blur.materials)

    implementation(libs.kotlinx.metadata.jvm)
    implementation(project(":app:common"))
    implementation(project(":ijkplayer-java"))
    implementation(project(":ijkplayer-so"))
    implementation(project(":libs"))

    implementation(libs.androidx.datastore.proto)
    implementation(libs.androidx.datastore.core)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.service)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    implementation(libs.androidx.profileinstaller)
    "baselineProfile"(project(":baselineprofile"))
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.work.compiler)
    implementation(libs.hilt.navigation.compose)


    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    implementation(libs.accompanist.placeholder.material)


    implementation(libs.androidx.paging.runtime.ktx)
    //noinspection GradleDependency
    implementation(libs.androidx.paging.compose)


    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // (Java only)
    implementation(libs.androidx.work.runtime)
    // Kotlin + coroutines
    implementation(libs.androidx.work.runtime.ktx)


    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    // optional - Paging 3 Integration
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.room.ktx)

    implementation(libs.okhttp)
    implementation(libs.rxhttp)
    ksp(libs.rxhttp.compiler)


    implementation(libs.photoView)

    implementation(libs.zoomable)

    implementation(libs.jsoup)

    implementation(libs.leancloud.storage)
    implementation(libs.rxjava.rxandroid)
}
