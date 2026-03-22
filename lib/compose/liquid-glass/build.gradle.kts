plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ContextParameters")
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.io.github.kyant0.backdrop)
            implementation(libs.io.github.fletchmckee.liquid.liquid)
            implementation(libs.io.github.kyant0.shapes)
            implementation("org.jetbrains:annotations:26.0.2-1")
        }
    }
}
