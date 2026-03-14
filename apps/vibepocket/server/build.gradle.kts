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
val localMusicSearchSourceDir =
    file("/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/tool-jvm/network-call/music/tool-api-music-search/src/main/kotlin")

sourceSets {
    named("main") {
        java.srcDir(localMusicSearchSourceDir)
    }
}

ksp {
    arg("isomorphicGenDir", sharedDir)
}

dependencies {
//    ksp("site.addzero:entity2iso-processor:2026.02.28")

    implementation("site.addzero:tool-api-suno:2026.02.06")
    implementation(project(":lib:api-music-spi"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.57")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.unbescape:unbescape:1.1.6.RELEASE")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")

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

    implementation(project(":apps:vibepocket:shared"))
    implementation(projects.lib.ktorJimmerPlugin)
    implementation(projects.lib.ktorS3Plugin)
    implementation(libs.org.xerial.sqlite.jdbc.v3)

    // Kotest property testing & assertions for server-side property tests
    testImplementation(libs.io.kotest.kotest.property)
    testImplementation(libs.io.kotest.kotest.assertions.core)
    testImplementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
}
