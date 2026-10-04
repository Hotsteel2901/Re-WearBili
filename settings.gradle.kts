@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("http://4thline.org/m2"){
            isAllowInsecureProtocol = true
        }
        // 已移除：https://androidx.dev/storage/compose-compiler/repository/
        // 该仓库是旧版 Compose 编译器专用，AGP 9 + Kotlin 2.x 内建 compose 插件后不再需要，
        // 且其 TLS 握手在部分网络环境下被拒，会导致依赖解析整体失败。
    }
}
rootProject.name = "WearBili"
include(":app")
include(":app:common")
include(":ijkplayer-java")
include(":ijkplayer-so")
include(":libs")
include(":baselineprofile")