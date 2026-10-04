@file:Suppress("UnstableApiUsage")

plugins {
    // AGP 9 已将 Kotlin 插件置于 classpath，此处不能再声明版本号
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.org.jetbrains.kotlin.atomicfu)

}

sourceSets {
    val main by getting
    main.apply {
        kotlin.setSrcDirs(
            listOf(
                "Bilibili-Kotlin-SDK/src/main/kotlin",
                //"IGSPlayer/main/src/main/java",
            )
        )
    }
}
dependencies {
    api(libs.ktor.client.core)
    api(libs.ktor.client.cio)
    api(libs.ktor.client.android)
    api(libs.ktor.client.logging)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.gson)
    api(libs.ktor.serialization.kotlinx.protobuf)

    api(libs.jsoup)

    api(libs.atomicfu)
}