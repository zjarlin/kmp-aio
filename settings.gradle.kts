pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://repo.spring.io/milestone/")
        maven(url = "https://plugins.gradle.org/m2/")
    }
    val localBuildLogicDir = file("checkouts/build-logic")
    val fallbackBuildLogicDir = file("../addzero-lib-jvm/checkouts/build-logic")
    val resolvedBuildLogicDir = listOf(localBuildLogicDir, fallbackBuildLogicDir)
        .firstOrNull { buildLogicDir -> buildLogicDir.resolve("src/main/kotlin").isDirectory }
    if (resolvedBuildLogicDir != null) {
        includeBuild(resolvedBuildLogicDir) {
            name = "build-logic"
        }
    }
    val localAddzeroLibJvmDir = file("../addzero-lib-jvm")
    val useLocalAddzeroLibJvmPluginBuild = providers.gradleProperty("useLocalAddzeroLibJvmPluginBuild")
        .map(String::toBoolean)
        .orElse(false)
        .get()
    if (useLocalAddzeroLibJvmPluginBuild && localAddzeroLibJvmDir.resolve("settings.gradle.kts").isFile) {
        includeBuild(localAddzeroLibJvmDir) {
            name = "addzero-lib-jvm-plugin-build"
        }
    }
    val localAddzeroLibJvmVersion = localAddzeroLibJvmDir
        .resolve("gradle.properties")
        .takeIf { file -> file.isFile }
        ?.readLines()
        ?.firstOrNull { line -> line.startsWith("version=") }
        ?.substringAfter("=")
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: "2026.10329.10127"
    plugins {
        id("site.addzero.kcp.i18n") version localAddzeroLibJvmVersion
        id("site.addzero.ksp.modbus-rtu") version localAddzeroLibJvmVersion
        id("site.addzero.ksp.modbus-tcp") version localAddzeroLibJvmVersion
    }
}

val localAddzeroLibJvmDir = file("../addzero-lib-jvm")
val localAddzeroBuildLogicCatalogFile = localAddzeroLibJvmDir.resolve("checkouts/build-logic/gradle/libs.versions.toml")
val addzeroLibJvmVersion = providers.gradleProperty("addzeroLibJvmVersion").orNull ?: "2026.10329.10127"

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "+" apply false
}

val ignoredModuleScanDirNames = setOf("build", "generated", "out", "node_modules")

fun File.shouldSkipModuleScan(rootDir: File): Boolean {
    if (!isDirectory) return true
    if (this != rootDir && name.startsWith(".")) return true
    if (name in ignoredModuleScanDirNames) return true
    if (this == rootDir) return false

    val relativePath = relativeTo(rootDir).invariantSeparatorsPath
    if (relativePath == "checkouts" || relativePath.startsWith("checkouts/")) return true
    if (relativePath.split('/').any { segment -> segment.startsWith(".") }) return true
    return false
}

fun File.toGradleProjectPath(rootDir: File): String =
    relativeTo(rootDir)
        .invariantSeparatorsPath
        .split('/')
        .filter(String::isNotBlank)
        .joinToString(separator = ":", prefix = ":")

fun Settings.includeLocalModules() {
    val root = rootDir
    val includedModules = root
        .walkTopDown()
        .onEnter { dir -> !dir.shouldSkipModuleScan(root) }
        .filter { dir -> dir != root && dir.isDirectory && dir.resolve("build.gradle.kts").isFile }
        .map { dir -> dir.toGradleProjectPath(root) }
        .toList()

    includedModules.forEach { moduleName ->
        include(moduleName)
        println("📦find module: $moduleName")
    }

    fun sample(list: List<String>): String =
        if (list.isEmpty()) "-" else list.take(5).joinToString(", ") + if (list.size > 5) ", ..." else ""

    println(
        """
================ Modules Summary ================
📦 Modules included: ${includedModules.size} (${sample(includedModules)})
================================================
""".trimIndent(),
    )
}

includeLocalModules()

val hasLocalAddzeroLibJvm = localAddzeroLibJvmDir.resolve("settings.gradle.kts").isFile

if (!hasLocalAddzeroLibJvm) {
    apply(plugin = "site.addzero.gradle.plugin.addzero-git-dependency")
}

if (hasLocalAddzeroLibJvm) {
    fun remapExternalProject(projectPath: String, relativeDir: String) {
        include(projectPath)
        project(projectPath).projectDir = localAddzeroLibJvmDir.resolve(relativeDir)
    }

    remapExternalProject(":lib:api", "lib/api")
    remapExternalProject(":lib:api:api-music-spi", "lib/api/api-music-spi")
    remapExternalProject(":lib:api:api-netease", "lib/api/api-netease")
    remapExternalProject(":lib:api:api-qqmusic", "lib/api/api-qqmusic")
    remapExternalProject(":lib:api:api-suno", "lib/api/api-suno")
    remapExternalProject(":lib:kcp", "lib/kcp")
    remapExternalProject(":lib:kcp:kcp-i18n", "lib/kcp/kcp-i18n")
    remapExternalProject(":lib:kcp:kcp-i18n-runtime", "lib/kcp/kcp-i18n-runtime")
    remapExternalProject(":lib:compose", "lib/compose")
    remapExternalProject(":lib:compose:app-sidebar", "lib/compose/app-sidebar")
    remapExternalProject(":lib:compose:compose-icon-map", "lib/compose/compose-icon-map")
    remapExternalProject(":lib:compose:compose-native-component-high-level", "lib/compose/compose-native-component-high-level")
    remapExternalProject(":lib:compose:compose-native-component-button", "lib/compose/compose-native-component-button")
    remapExternalProject(":lib:compose:compose-native-component-searchbar", "lib/compose/compose-native-component-searchbar")
    remapExternalProject(":lib:compose:compose-native-component-tree", "lib/compose/compose-native-component-tree")
    remapExternalProject(":lib:compose:glass-components", "lib/compose/glass-components")
    remapExternalProject(":lib:compose:liquid-glass", "lib/compose/liquid-glass")
    remapExternalProject(":lib:compose:media-playlist-player", "lib/compose/media-playlist-player")
    remapExternalProject(":lib:compose:scaffold-spi", "lib/compose/scaffold-spi")
    remapExternalProject(":lib:compose:shadcn-compose-component", "lib/compose/shadcn-compose-component")
    remapExternalProject(":lib:tool-kmp", "lib/tool-kmp")
    remapExternalProject(":lib:tool-kmp:tool-coll", "lib/tool-kmp/tool-coll")
    remapExternalProject(":lib:tool-kmp:network-starter", "lib/tool-kmp/network-starter")
    remapExternalProject(":lib:tool-kmp:tool-tree", "lib/tool-kmp/tool-tree")
    remapExternalProject(":lib:tool-jvm", "lib/tool-jvm")
    remapExternalProject(":lib:tool-jvm:tool-serial", "lib/tool-jvm/tool-serial")
    remapExternalProject(":lib:tool-jvm:tool-modbus", "lib/tool-jvm/tool-modbus")
    remapExternalProject(":lib:tool-jvm:tool-stm32-bootloader", "lib/tool-jvm/tool-stm32-bootloader")
    remapExternalProject(":lib:ksp", "lib/ksp")
    remapExternalProject(":lib:ksp:common", "lib/ksp/common")
    remapExternalProject(":lib:ksp:common:ksp-support-jdbc", "lib/ksp/common/ksp-support-jdbc")
    remapExternalProject(":lib:ksp:metadata", "lib/ksp/metadata")
    remapExternalProject(":lib:ksp:metadata:controller2api-processor", "lib/ksp/metadata/controller2api-processor")
    remapExternalProject(":lib:ksp:metadata:entity2form", "lib/ksp/metadata/entity2form")
    remapExternalProject(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-core", "lib/ksp/metadata/ksp-dsl-builder/ksp-dsl-builder-core")
    remapExternalProject(":lib:lsi", "lib/lsi")
    remapExternalProject(":lib:lsi:lsi-core", "lib/lsi/lsi-core")
    remapExternalProject(":lib:ksp:route", "lib/ksp/route")
    remapExternalProject(":lib:ksp:route:route-core", "lib/ksp/route/route-core")
    remapExternalProject(":lib:ksp:route:route-processor", "lib/ksp/route/route-processor")
    remapExternalProject(":lib:ksp:metadata:jimmer-entity-spi", "lib/ksp/metadata/jimmer-entity-spi")
    remapExternalProject(":lib:ksp:metadata:entity2iso-processor", "lib/ksp/metadata/entity2iso-processor")
    remapExternalProject(":lib:ksp:metadata:entity2form:entity2form-processor", "lib/ksp/metadata/entity2form/entity2form-processor")
    remapExternalProject(":lib:ksp:metadata:entity2mcp-processor", "lib/ksp/metadata/entity2mcp-processor")
    remapExternalProject(":lib:ksp:metadata:jimmer-entity-external-processor", "lib/ksp/metadata/jimmer-entity-external-processor")
    remapExternalProject(":lib:ksp:metadata:modbus:modbus-runtime", "lib/ksp/metadata/modbus/modbus-runtime")
    remapExternalProject(":lib:ksp:metadata:modbus:modbus-ksp-core", "lib/ksp/metadata/modbus/modbus-ksp-core")
    remapExternalProject(
        ":lib:ksp:metadata:modbus:modbus-ksp-kotlin-gateway",
        "lib/ksp/metadata/modbus/modbus-ksp-kotlin-gateway",
    )
    remapExternalProject(
        ":lib:ksp:metadata:modbus:modbus-ksp-c-contract",
        "lib/ksp/metadata/modbus/modbus-ksp-c-contract",
    )
    remapExternalProject(
        ":lib:ksp:metadata:modbus:modbus-ksp-keil-sync",
        "lib/ksp/metadata/modbus/modbus-ksp-keil-sync",
    )
    remapExternalProject(
        ":lib:ksp:metadata:modbus:modbus-ksp-markdown",
        "lib/ksp/metadata/modbus/modbus-ksp-markdown",
    )
    remapExternalProject(":lib:ksp:metadata:modbus:modbus-ksp-rtu", "lib/ksp/metadata/modbus/modbus-ksp-rtu")
    remapExternalProject(":lib:ksp:metadata:modbus:modbus-ksp-tcp", "lib/ksp/metadata/modbus/modbus-ksp-tcp")

    gradle.beforeProject {
        configurations.configureEach {
            resolutionStrategy.dependencySubstitution {
                substitute(module("site.addzero:network-starter"))
                    .using(project(":lib:tool-kmp:network-starter"))
                substitute(module("site.addzero:compose-icon-map"))
                    .using(project(":lib:compose:compose-icon-map"))
                substitute(module("site.addzero:compose-native-component-high-level"))
                    .using(project(":lib:compose:compose-native-component-high-level"))
                substitute(module("site.addzero:compose-native-component-button"))
                    .using(project(":lib:compose:compose-native-component-button"))
                substitute(module("site.addzero:compose-native-component-searchbar"))
                    .using(project(":lib:compose:compose-native-component-searchbar"))
                substitute(module("site.addzero:compose-native-component-tree"))
                    .using(project(":lib:compose:compose-native-component-tree"))
                substitute(module("site.addzero:scaffold-spi:$addzeroLibJvmVersion"))
                    .using(project(":lib:compose:scaffold-spi"))
                substitute(module("site.addzero:shadcn-compose-component"))
                    .using(project(":lib:compose:shadcn-compose-component"))
                substitute(module("site.addzero:route-core"))
                    .using(project(":lib:ksp:route:route-core"))
                substitute(module("site.addzero:route-processor"))
                    .using(project(":lib:ksp:route:route-processor"))
                substitute(module("site.addzero:controller2api-processor"))
                    .using(project(":lib:ksp:metadata:controller2api-processor"))
                substitute(module("site.addzero:ksp-support-jdbc"))
                    .using(project(":lib:ksp:common:ksp-support-jdbc"))
                substitute(module("site.addzero:ksp-dsl-builder-core"))
                    .using(project(":lib:ksp:metadata:ksp-dsl-builder:ksp-dsl-builder-core"))
                substitute(module("site.addzero:lsi-core"))
                    .using(project(":lib:lsi:lsi-core"))
                substitute(module("site.addzero:tool-coll"))
                    .using(project(":lib:tool-kmp:tool-coll"))
                substitute(module("site.addzero:tool-serial"))
                    .using(project(":lib:tool-jvm:tool-serial"))
                substitute(module("site.addzero:tool-modbus"))
                    .using(project(":lib:tool-jvm:tool-modbus"))
                substitute(module("site.addzero:tool-stm32-bootloader"))
                    .using(project(":lib:tool-jvm:tool-stm32-bootloader"))
                substitute(module("site.addzero:kcp-i18n"))
                    .using(project(":lib:kcp:kcp-i18n"))
                substitute(module("site.addzero:kcp-i18n-runtime"))
                    .using(project(":lib:kcp:kcp-i18n-runtime"))
                substitute(module("site.addzero:jimmer-entity-spi"))
                    .using(project(":lib:ksp:metadata:jimmer-entity-spi"))
                substitute(module("site.addzero:entity2iso-processor"))
                    .using(project(":lib:ksp:metadata:entity2iso-processor"))
                substitute(module("site.addzero:entity2form-processor"))
                    .using(project(":lib:ksp:metadata:entity2form:entity2form-processor"))
                substitute(module("site.addzero:entity2mcp-processor"))
                    .using(project(":lib:ksp:metadata:entity2mcp-processor"))
                substitute(module("site.addzero:jimmer-entity-external-processor"))
                    .using(project(":lib:ksp:metadata:jimmer-entity-external-processor"))
                substitute(module("site.addzero:modbus-runtime"))
                    .using(project(":lib:ksp:metadata:modbus:modbus-runtime"))
                substitute(module("site.addzero:modbus-ksp-rtu"))
                    .using(project(":lib:ksp:metadata:modbus:modbus-ksp-rtu"))
                substitute(module("site.addzero:modbus-ksp-tcp"))
                    .using(project(":lib:ksp:metadata:modbus:modbus-ksp-tcp"))
            }
        }
    }
}

dependencyResolutionManagement {
    if (localAddzeroBuildLogicCatalogFile.isFile) {
        versionCatalogs {
            create("libs") {
                from(files(localAddzeroBuildLogicCatalogFile))
            }
        }
    }
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven(url = "https://repo.spring.io/milestone/")
        maven(url = "https://plugins.gradle.org/m2/")
    }
}
