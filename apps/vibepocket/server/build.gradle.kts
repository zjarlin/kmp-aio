plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
    id("site.addzero.buildlogic.jvm.jimmer")
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
}
application {
    mainClass.set("site.addzero.vibepocket.ApplicationKt")
}
val sharedDir = rootDir.resolve("shared/src/commonMain/kotlin").absolutePath

ksp {
    arg("isomorphicGenDir", sharedDir)
}

dependencies {
//    ksp("site.addzero:entity2iso-processor:2026.02.28")

    implementation("site.addzero:tool-api-music-search:2026.01.20")
    implementation("site.addzero:tool-api-suno:2026.02.06")

    // @Bean KSP processor（用于 routes 聚合）
    implementation(libs.site.addzero.ioc.core)
    ksp(libs.site.addzero.ioc.processor)

    // Starter 模块（引入即生效）
    implementation(projects.lib.starterKoin)
    implementation(projects.lib.starterSerialization)
    implementation(projects.lib.starterStatuspages)
    implementation(projects.lib.starterBanner)
    implementation(projects.lib.starterOpenapi)
    implementation(projects.lib.starterFlyway)

    // 业务 lib 模块
    implementation(projects.shared)
    implementation(projects.lib.ktorJimmerPlugin)
    implementation(projects.lib.ktorS3Plugin)
    implementation(libs.org.xerial.sqlite.jdbc.v3)

    // Kotest property testing & assertions for server-side property tests
    testImplementation(libs.io.kotest.kotest.property)
    testImplementation(libs.io.kotest.kotest.assertions.core)
    testImplementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
}
