plugins {
    kotlin("multiplatform") version "2.3.20" apply false
    id("io.insert-koin.compiler.plugin") version "0.3.0" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
}
