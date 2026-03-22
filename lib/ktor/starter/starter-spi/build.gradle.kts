plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.findLibrary("io-ktor-ktor-server-core").get())
}
