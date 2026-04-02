plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:config-center"))
            implementation(libs.findLibrary("com-squareup-kotlinpoet").get())
            implementation(libs.findLibrary("com-squareup-kotlinpoet-ksp").get())
        }
    }
}
