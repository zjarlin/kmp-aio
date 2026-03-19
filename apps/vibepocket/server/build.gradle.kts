plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
    id("site.addzero.buildlogic.jvm.jimmer")
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
}

apply(from = rootProject.file("gradle/spring2ktor-ksp-cache-workaround.gradle.kts"))

application {
    mainClass.set("site.addzero.vibepocket.ApplicationKt")
}
val sharedDir = rootDir.resolve("shared/src/commonMain/kotlin").absolutePath
val localMusicSearchSourceDir =
    file("/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/tool-jvm/network-call/music/tool-api-music-search/src/main/kotlin")
val localMusicPlaybackRateSourceDir =
    file("/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/tool-jvm/network-call/music/api-music-spi/src/jvmMain/kotlin")

sourceSets {
    named("main") {
        java.srcDir(localMusicSearchSourceDir)
        java.srcDir(localMusicPlaybackRateSourceDir)
    }
}

ksp {
    arg("isomorphicGenDir", sharedDir)
    arg("springKtor.generatedPackage", "site.addzero.vibepocket.generated.springktor")
}

dependencies {
//    ksp("site.addzero:entity2iso-processor:2026.02.28")

    implementation("site.addzero:tool-api-suno:2026.02.06")
    implementation(project(":lib:api-music-spi"))
    implementation(project(":lib:api-suno"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.57")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.unbescape:unbescape:1.1.6.RELEASE")
    implementation("javazoom:jlayer:1.0.1")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")

    implementation("site.addzero:spring2ktor-server-core:2026.03.13")
    ksp("site.addzero:spring2ktor-server-processor:2026.03.13")
    compileOnly(libs.org.springframework.spring.web)

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
