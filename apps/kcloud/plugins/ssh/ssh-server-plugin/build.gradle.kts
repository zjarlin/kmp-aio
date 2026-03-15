plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

val libs = versionCatalogs.named("libs")

ksp {
    arg("springKtor.generatedPackage", "com.kcloud.plugins.ssh.server.generated.springktor")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:plugin-api"))
            implementation(project(":apps:kcloud:plugins:ssh:ssh-plugin"))
            implementation(project(":lib:kcloud-core"))
            implementation("site.addzero:spring2ktor-server-core:2026.03.10")
            implementation(libs.findLibrary("com-hierynomus-sshj").get())
            compileOnly("org.springframework:spring-web:5.3.21")
        }
    }
}

dependencies {
    add("kspJvm", "site.addzero:spring2ktor-server-processor:2026.03.10")
}
