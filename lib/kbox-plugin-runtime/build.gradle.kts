plugins {
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}

dependencies {
    implementation(project(":lib:kbox-core"))
    implementation(project(":lib:kbox-plugin-api"))
}

tasks.named<Test>("test") {
    dependsOn(":lib:kbox-plugin-runtime-fixture-hello:packageRuntimePlugin")
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty(
        "kbox.runtimeFixtureDir",
        rootProject.file("lib/kbox-plugin-runtime-fixture-hello/build/runtime-plugin/hello-runtime").absolutePath,
    )
}
