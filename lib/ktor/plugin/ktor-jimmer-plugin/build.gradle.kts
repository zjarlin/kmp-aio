plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jimmer")
    id("site.addzero.buildlogic.jvm.jvm-koin")
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("hikaricp").get())
//    implementation(project(":lib:ktor:starter:starter-spi"))
//    implementation(libs.findLibrary("io-ktor-ktor-server-core").get())
//    implementation(libs.findLibrary("io-insert-koin-koin-ktor").get())
    implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
    implementation(libs.findLibrary("org-postgresql-postgresql").get())
//    implementation(libs.findLibrary("site-addzero-ioc-core").get())
}
