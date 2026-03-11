plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
}

application {
    mainClass.set("com.kcloud.server.ApplicationKt")
}

val libs = versionCatalogs.named("libs")
val ktorVersion = libs.findVersion("ktor").get().requiredVersion

ksp {
    arg("springKtor.generatedPackage", "com.kcloud.server.generated.springktor")
}

dependencies {
    implementation("site.addzero:spring2ktor-server-core:2026.03.10")
    ksp("site.addzero:spring2ktor-server-processor:2026.03.10")
    compileOnly("org.springframework:spring-web:5.3.21")

    // 依赖 kcloud 核心模块
    implementation(project(":apps:kcloud"))
}
