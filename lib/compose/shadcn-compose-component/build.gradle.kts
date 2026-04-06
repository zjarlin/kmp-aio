@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-datetime")
}
val libs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        implementation("site.addzero:tool-expect:2025.09.30")
        implementation(libs.findLibrary("io-coil-kt-coil3-coil-compose").get())
        implementation(libs.findLibrary("io-coil-kt-coil3-coil-network-ktor3").get())
    }
}
