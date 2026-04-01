@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
    id("site.addzero.buildlogic.kmp.cmp-kbox-aio")
}

val addzeroLibJvmVersion: String by project
val desktopMainClass = "site.addzero.kbox.MainKt"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kbox:shared"))
            implementation("site.addzero:scaffold-spi:$addzeroLibJvmVersion")
            implementation(project(":lib:kbox-plugin-api"))
        }
        jvmMain.dependencies {
            implementation(project(":lib:kbox-core"))
            implementation(project(":lib:kbox-ssh"))
            implementation(project(":lib:kbox-plugin-runtime"))
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

tasks.named<Test>("jvmTest") {
    dependsOn(":apps:kbox:runtime-fixtures:hello-plugin:packageRuntimePlugin")
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty(
        "kbox.runtimeFixtureDir",
        rootProject.file("apps/kbox/runtime-fixtures/hello-plugin/build/runtime-plugin/hello-runtime").absolutePath,
    )
}
