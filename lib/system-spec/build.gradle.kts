plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    kotlin("plugin.serialization")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}

dependencies {
    // Keep this module dependency-light: SPI + default implementations only.
}
