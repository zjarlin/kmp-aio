plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-koin")
}

val libs = versionCatalogs.named("libs")
val desktopMainClass = "site.addzero.fluentdemo.MainKt"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("io-github-compose-fluent-fluent").get())
            implementation(libs.findLibrary("io-github-compose-fluent-fluent-icons-extended").get())
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
