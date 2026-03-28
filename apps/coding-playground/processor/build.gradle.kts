plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:coding-playground:annotations"))
            implementation(libs.findLibrary("com-google-devtools-ksp-symbol-processing-api").get())
            implementation(libs.findLibrary("com-squareup-kotlinpoet").get())
            implementation(libs.findLibrary("com-squareup-kotlinpoet-ksp").get())
        }
    }
}
