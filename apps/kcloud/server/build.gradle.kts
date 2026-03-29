plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server")
}

// Keep the enabled server plugin set centralized here. Some plugin modules are UI-only or
// support-only, so server wiring should remain an explicit opt-in list instead of a blind scan.
val serverPluginProjects = listOf(
    "mcu-console",
    "system/ai-chat",
    "system/knowledge-base",
    "system/plugin-market",
    "system/rbac",
    "vibepocket",
).map { pluginId ->
    project(":apps:kcloud:plugins:${pluginId.replace("/", ":")}")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            // Starter 模块（引入即生效）
            implementation(project(":lib:config-center:spec"))
            implementation(project(":lib:config-center:ktor"))
            implementation(project(":lib:config-center:runtime-jvm"))
            implementation(project(":lib:ktor:starter:starter-spi"))
            implementation(project(":lib:ktor:starter:starter-koin"))
            implementation(project(":lib:ktor:starter:starter-serialization"))
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            implementation(project(":lib:ktor:starter:starter-banner"))
            implementation(project(":lib:ktor:starter:starter-openapi"))
            implementation(project(":lib:ktor:starter:starter-flyway"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            // <managed:plugin-market-server-deps:start>
            serverPluginProjects.forEach { implementation(it) }
            // <managed:plugin-market-server-deps:end>
        }
    }
}
val serverMainClass = "site.addzero.kcloud.ApplicationKt"

kotlin.jvm().mainRun {
    mainClass.set(serverMainClass)
}

tasks.named<JavaExec>("runJvm") {
    mainClass.set(serverMainClass)
}
