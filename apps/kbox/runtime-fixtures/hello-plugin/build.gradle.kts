plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:kbox-plugin-api"))
            implementation(libs.findLibrary("io-insert-koin-koin-core").get())
        }
    }
}

val generatePluginManifest = tasks.register("generatePluginManifest") {
    val outputDir = layout.buildDirectory.dir("generated/kbox-runtime-plugin")
    outputs.dir(outputDir)
    doLast {
        val targetDir = outputDir.get().asFile
        targetDir.mkdirs()
        targetDir.resolve("plugin.json").writeText(
            """
            {
              "pluginId": "hello-runtime",
              "name": "Hello Runtime",
              "version": "0.1.0",
              "hostApiVersion": "1.0",
              "entryClass": "site.addzero.kbox.runtime.fixture.hello.HelloRuntimePlugin",
              "description": "运行时插件示例",
              "capabilities": ["SCREEN"]
            }
            """.trimIndent(),
        )
    }
}

tasks.register<Copy>("packageRuntimePlugin") {
    dependsOn("jvmJar", generatePluginManifest)
    into(layout.buildDirectory.dir("runtime-plugin/hello-runtime"))
    from(layout.buildDirectory.file("generated/kbox-runtime-plugin/plugin.json"))
    from(tasks.named("jvmJar")) {
        into("lib")
    }
}
