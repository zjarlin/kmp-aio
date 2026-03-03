plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jvm-koin")
}

dependencies {
    api(projects.lib.starterSpi)
    implementation(libs.org.flywaydb.flyway.core)
    implementation(libs.io.ktor.ktor.server.core)
}
