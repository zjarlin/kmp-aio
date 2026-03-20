@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}

val desktopMainClass = "site.addzero.liquiddemo.MainKt"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.fletchmckee.liquid:liquid:1.1.1")
        }
    }
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

compose.desktop {
    application {
        mainClass = desktopMainClass
    }
}
