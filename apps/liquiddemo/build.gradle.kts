@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}

val desktopMainClass = "site.addzero.liquiddemo.MainKt"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:app-sidebar"))
            implementation(project(":lib:compose:workbench-shell"))
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
