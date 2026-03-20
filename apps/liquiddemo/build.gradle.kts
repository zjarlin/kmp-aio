@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
}

val desktopMainClass = "site.addzero.liquiddemo.MainKt"

kotlin {
    sourceSets {
        commonMain.dependencies {
//           https://github.com/VyacheslavYunzhakov/LiquidGlass?tab=readme-ov-file
            implementation("io.github.fletchmckee.liquid:liquid:1.1.1")
            implementation("io.github.kyant0:backdrop:+")
           implementation("com.mocharealm.gaze:glassy-core:2.0.0")
            implementation("com.mocharealm.gaze:glassy-liquid-effect:2.0.0")
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
