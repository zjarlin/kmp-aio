plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

apply(from = rootProject.file("gradle/spring2ktor-ksp-cache-workaround.gradle.kts"))

ksp {
    arg("springKtor.generatedPackage", "site.addzero.remotecompose.server.generated.springktor")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":apps:remotecompose:shared"))
        }
        jvmMain.dependencies {
            implementation("site.addzero:spring2ktor-server-core:2026.03.13")
            compileOnly(libs.org.springframework.spring.web)
        }
    }
}

dependencies {
    add("kspJvm", "site.addzero:spring2ktor-server-processor:2026.03.13")
}
