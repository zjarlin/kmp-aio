plugins {
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-json")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(project(":lib:config-center:spec"))
    implementation(project(":lib:config-center:client"))
    implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
    implementation(libs.findLibrary("org-yaml-snakeyaml").get())
}

