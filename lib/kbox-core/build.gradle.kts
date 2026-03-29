plugins {
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-json")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("site-addzero-tool-io").get())
    implementation(libs.findLibrary("org-yaml-snakeyaml").get())
}
