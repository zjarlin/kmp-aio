plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jimmer")
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
}

tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(
                org.gradle.jvm.toolchain.JavaLanguageVersion.of(
                    org.gradle.api.JavaVersion.current().majorVersion,
                ),
            )
        },
    )
}

ksp {
    arg("springKtor.generatedPackage", "site.addzero.coding.playground.server.generated.springktor")
}

dependencies {
    implementation(project(":apps:coding-playground:shared"))

    implementation(libs.io.ktor.ktor.server.core)
    implementation("site.addzero:spring2ktor-server-core:2026.03.13")
    compileOnly(libs.org.springframework.spring.web)
    implementation(libs.org.jetbrains.kotlin.kotlin.scripting.jsr223)
    implementation(libs.org.jetbrains.kotlin.kotlin.compiler.embeddable)

    implementation(libs.org.xerial.sqlite.jdbc.v3)

    ksp("site.addzero:spring2ktor-server-processor:2026.03.13")
}
