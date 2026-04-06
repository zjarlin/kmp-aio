plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jvm-koin")
}
val libs = versionCatalogs.named("libs")

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

dependencies {
    implementation(libs.findLibrary("io-ktor-ktor-server-core").get())
    implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
    implementation(libs.findLibrary("site-addzero-tool-json").get())
    implementation(libs.findLibrary("site-addzero-tool-sql-executor").get())

    testImplementation(libs.findLibrary("ktor-server-test-host").get())
    testImplementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
}
