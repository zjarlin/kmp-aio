plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-koin")
}

val libs = versionCatalogs.named("libs")
val desktopMainClass = "site.addzero.cupertinodemo.MainKt"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("io-github-robinpcrd-cupertino").get())
            implementation(libs.findLibrary("io-github-robinpcrd-cupertino-adaptive").get())
            implementation(libs.findLibrary("io-github-robinpcrd-cupertino-icons-extended").get())
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
