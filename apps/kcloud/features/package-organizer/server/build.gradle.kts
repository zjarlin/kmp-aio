plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

apply(from = rootProject.file("gradle/spring2ktor-ksp-cache-workaround.gradle.kts"))

ksp {
    arg("springKtor.generatedPackage", "com.kcloud.features.packages.server.generated.springktor")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:features:feature-api"))
            implementation(project(":apps:kcloud:features:package-organizer:client"))
            implementation("site.addzero:spring2ktor-server-core:2026.03.13")
            implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json)
            compileOnly("org.springframework:spring-web:5.3.21")
        }
    }
}

dependencies {
    add("kspJvm", "site.addzero:spring2ktor-server-processor:2026.03.13")
}
