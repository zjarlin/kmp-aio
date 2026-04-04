pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://repo.spring.io/milestone/")
        maven(url = "https://plugins.gradle.org/m2/")
    }
    val localAddzeroLibJvmCandidateDirs = listOf(
        file("../addzero-lib-jvm"),
        file("addzero-lib-jvm"),
    )
    val localBuildLogicDir = file("checkouts/build-logic")
    val fallbackBuildLogicDirs = localAddzeroLibJvmCandidateDirs.map { it.resolve("checkouts/build-logic") }
    val resolvedBuildLogicDir = listOf(localBuildLogicDir, *fallbackBuildLogicDirs.toTypedArray())
        .distinctBy { it.absoluteFile.normalize().path }
        .firstOrNull { buildLogicDir -> buildLogicDir.resolve("src/main/kotlin").isDirectory }
    if (resolvedBuildLogicDir != null) {
        includeBuild(resolvedBuildLogicDir) {
            name = "build-logic"
        }
    }
    val localAddzeroLibJvmDir = localAddzeroLibJvmCandidateDirs
        .firstOrNull { candidateDir -> candidateDir.resolve("settings.gradle.kts").isFile }
    val useLocalAddzeroLibJvmPluginBuild = providers.gradleProperty("useLocalAddzeroLibJvmPluginBuild")
        .map(String::toBoolean)
        .orElse(false)
        .get()
    if (useLocalAddzeroLibJvmPluginBuild && localAddzeroLibJvmDir != null) {
        includeBuild(localAddzeroLibJvmDir) {
            name = "addzero-lib-jvm-plugin-build"
        }
    }
    val localAddzeroLibJvmVersion = localAddzeroLibJvmDir
        ?.resolve("gradle.properties")
        ?.takeIf { file -> file.isFile }
        ?.readLines()
        ?.firstOrNull { line -> line.startsWith("version=") }
        ?.substringAfter("=")
        ?.trim()
        ?.takeIf(String::isNotBlank)
    val resolvedKcpI18nVersion = providers.gradleProperty("addzeroKcpI18nVersion")
        .orElse(localAddzeroLibJvmVersion ?: "2026.10330.12238")
        .get()
    val resolvedAddzeroLibJvmVersion = providers.gradleProperty("addzeroLibJvmVersion")
        .orElse(localAddzeroLibJvmVersion ?: "2026.04.04")
        .get()
    plugins {
        id("site.addzero.kcp.all-object-jvm-static") version resolvedAddzeroLibJvmVersion
        id("site.addzero.kcp.i18n") version resolvedKcpI18nVersion
        id("site.addzero.kcp.multireceiver") version resolvedAddzeroLibJvmVersion
        id("site.addzero.kcp.reified") version resolvedAddzeroLibJvmVersion
        id("site.addzero.kcp.spread-pack") version resolvedAddzeroLibJvmVersion
        id("site.addzero.kcp.transform-overload") version resolvedAddzeroLibJvmVersion
        id("site.addzero.ksp.modbus-rtu") version resolvedAddzeroLibJvmVersion
        id("site.addzero.ksp.modbus-tcp") version resolvedAddzeroLibJvmVersion
    }
}

val localAddzeroLibJvmCandidateDirs = listOf(
    file("../addzero-lib-jvm"),
    file("addzero-lib-jvm"),
)
val localAddzeroLibJvmDir = localAddzeroLibJvmCandidateDirs
    .firstOrNull { candidateDir -> candidateDir.resolve("settings.gradle.kts").isFile }
val preferredBuildLogicCatalogFile = listOf(
    file("checkouts/build-logic/gradle/libs.versions.toml"),
    localAddzeroLibJvmDir?.resolve("checkouts/build-logic/gradle/libs.versions.toml"),
).firstOrNull { catalogFile -> catalogFile?.isFile == true }
val localAddzeroLibJvmVersion = localAddzeroLibJvmDir
    ?.resolve("gradle.properties")
    ?.takeIf { file -> file.isFile }
    ?.readLines()
    ?.firstOrNull { line -> line.startsWith("version=") }
    ?.substringAfter("=")
    ?.trim()
    ?.takeIf(String::isNotBlank)
val addzeroLibJvmVersion = providers.gradleProperty("addzeroLibJvmVersion")
    .orElse(localAddzeroLibJvmVersion ?: "2026.04.04")
    .get()
val hasLocalAddzeroLibJvm = localAddzeroLibJvmDir != null
val hasLocalBuildLogicCheckout = file("checkouts/build-logic/settings.gradle.kts").isFile
val includeKcpI18nDemo = providers.gradleProperty("includeKcpI18nDemo")
    .map(String::toBoolean)
    .orElse(false)
    .get()
val includeLiquidDemo = providers.gradleProperty("includeLiquidDemo")
    .map(String::toBoolean)
    .orElse(hasLocalAddzeroLibJvm)
    .get()
val optionalModuleScanDecisions = mapOf(
    "apps/kcp-i18n-demo" to includeKcpI18nDemo,
    "apps/liquiddemo" to includeLiquidDemo,
)

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
    if (relativePath == "addzero-lib-jvm" || relativePath.startsWith("addzero-lib-jvm/")) return true
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
        .filter { dir ->
            dir != root &&
                dir.isDirectory &&
                dir.resolve("build.gradle.kts").isFile &&
                optionalModuleScanDecisions[dir.relativeTo(root).invariantSeparatorsPath] != false
        }
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

if (!hasLocalAddzeroLibJvm && !hasLocalBuildLogicCheckout) {
    apply(plugin = "site.addzero.gradle.plugin.addzero-git-dependency")
}

if (hasLocalAddzeroLibJvm) {
    val resolvedLocalAddzeroLibJvmDir = checkNotNull(localAddzeroLibJvmDir)

    fun remapExternalProject(projectPath: String, relativeDir: String) {
        include(projectPath)
        project(projectPath).projectDir = resolvedLocalAddzeroLibJvmDir.resolve(relativeDir)
    }

    remapExternalProject(":lib:api", "lib/api")
    remapExternalProject(":lib:api:api-music-spi", "lib/api/api-music-spi")
    remapExternalProject(":lib:api:api-netease", "lib/api/api-netease")
    remapExternalProject(":lib:api:api-qqmusic", "lib/api/api-qqmusic")
    remapExternalProject(":lib:api:api-suno", "lib/api/api-suno")
    remapExternalProject(":lib:kcp", "lib/kcp")
    remapExternalProject(":lib:kcp:kcp-i18n", "lib/kcp/kcp-i18n")
    remapExternalProject(":lib:kcp:kcp-i18n-runtime", "lib/kcp/kcp-i18n-runtime")
    remapExternalProject(":lib:kcp:spread-pack:kcp-spread-pack-annotations", "lib/kcp/spread-pack/kcp-spread-pack-annotations")
    remapExternalProject(":lib:kcp:spread-pack:kcp-spread-pack-plugin", "lib/kcp/spread-pack/kcp-spread-pack-plugin")
    remapExternalProject(
        ":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin",
        "lib/kcp/spread-pack/kcp-spread-pack-gradle-plugin",
    )
    remapExternalProject(":lib:kcp:spread-pack:kcp-spread-pack-ide-plugin", "lib/kcp/spread-pack/kcp-spread-pack-ide-plugin")
    remapExternalProject(":lib:compose", "lib/compose")
    remapExternalProject(":lib:compose:app-sidebar", "lib/compose/app-sidebar")
    remapExternalProject(":lib:compose:app-sidebar-cupertino-adapter", "lib/compose/app-sidebar-cupertino-adapter")
    remapExternalProject(":lib:compose:compose-apple-corner", "lib/compose/compose-apple-corner")
    remapExternalProject(":lib:compose:compose-cupertino-workbench", "lib/compose/compose-cupertino-workbench")
    remapExternalProject(":lib:compose:compose-crud-spi", "lib/compose/compose-crud-spi")
    remapExternalProject(":lib:compose:compose-eventbus", "lib/compose/compose-eventbus")
    remapExternalProject(":lib:compose:compose-icon-map", "lib/compose/compose-icon-map")
    remapExternalProject(":lib:compose:compose-klibs-component", "lib/compose/compose-klibs-component")
    remapExternalProject(":lib:compose:compose-model-component", "lib/compose/compose-model-component")
    remapExternalProject(":lib:compose:compose-native-component-assist", "lib/compose/compose-native-component-assist")
    remapExternalProject(":lib:compose:compose-native-component-autocomplet", "lib/compose/compose-native-component-autocomplet")
    remapExternalProject(":lib:compose:compose-native-component-high-level", "lib/compose/compose-native-component-high-level")
    remapExternalProject(":lib:compose:compose-native-component-button", "lib/compose/compose-native-component-button")
    remapExternalProject(":lib:compose:compose-native-component-card", "lib/compose/compose-native-component-card")
    remapExternalProject(":lib:compose:compose-native-component-chat", "lib/compose/compose-native-component-chat")
    remapExternalProject(":lib:compose:compose-native-component-ext", "lib/compose/compose-native-component-ext")
    remapExternalProject(":lib:compose:compose-native-component-form", "lib/compose/compose-native-component-form")
    remapExternalProject(":lib:compose:compose-native-component-glass", "lib/compose/compose-native-component-glass")
    remapExternalProject(":lib:compose:compose-native-component-hook", "lib/compose/compose-native-component-hook")
    remapExternalProject(
        ":lib:compose:compose-native-component-knowledgegraph",
        "lib/compose/compose-native-component-knowledgegraph",
    )
    remapExternalProject(":lib:compose:compose-native-component-searchbar", "lib/compose/compose-native-component-searchbar")
    remapExternalProject(":lib:compose:compose-native-component-select", "lib/compose/compose-native-component-select")
    remapExternalProject(":lib:compose:compose-native-component-sheet", "lib/compose/compose-native-component-sheet")
    remapExternalProject(":lib:compose:compose-native-component-table", "lib/compose/compose-native-component-table")
    remapExternalProject(":lib:compose:compose-native-component-table-core", "lib/compose/compose-native-component-table-core")
    remapExternalProject(":lib:compose:compose-native-component-table-pro", "lib/compose/compose-native-component-table-pro")
    remapExternalProject(":lib:compose:compose-native-component-text", "lib/compose/compose-native-component-text")
    remapExternalProject(":lib:compose:compose-native-component-tree", "lib/compose/compose-native-component-tree")
    remapExternalProject(":lib:compose:compose-native-component-toast", "lib/compose/compose-native-component-toast")
    remapExternalProject(":lib:compose:compose-sheet-spi", "lib/compose/compose-sheet-spi")
    remapExternalProject(":lib:compose:compose-workbench-design", "lib/compose/compose-workbench-design")
    remapExternalProject(":lib:compose:compose-workbench-shell", "lib/compose/compose-workbench-shell")
    remapExternalProject(":lib:compose:compose-zh-fonts", "lib/compose/compose-zh-fonts")
    remapExternalProject(":lib:compose:glass-components", "lib/compose/glass-components")
    remapExternalProject(":lib:compose:liquid-glass", "lib/compose/liquid-glass")
    remapExternalProject(":lib:compose:media-playlist-player", "lib/compose/media-playlist-player")
    remapExternalProject(":lib:compose:scaffold-spi", "lib/compose/scaffold-spi")
    remapExternalProject(":lib:compose:shadcn-compose-component", "lib/compose/shadcn-compose-component")
    remapExternalProject(":lib:gradle-plugin", "lib/gradle-plugin")
    remapExternalProject(":lib:gradle-plugin:project-plugin", "lib/gradle-plugin/project-plugin")
    remapExternalProject(
        ":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base",
        "lib/gradle-plugin/project-plugin/gradle-ksp-consumer-base",
    )
    remapExternalProject(":lib:tool-kmp", "lib/tool-kmp")
    remapExternalProject(":lib:tool-kmp:tool-array", "lib/tool-kmp/tool-array")
    remapExternalProject(":lib:tool-kmp:tool-boolean", "lib/tool-kmp/tool-boolean")
    remapExternalProject(":lib:tool-kmp:tool-coll", "lib/tool-kmp/tool-coll")
    remapExternalProject(":lib:tool-kmp:tool-enum", "lib/tool-kmp/tool-enum")
    remapExternalProject(":lib:tool-kmp:network-starter", "lib/tool-kmp/network-starter")
    remapExternalProject(":lib:tool-kmp:tool-model", "lib/tool-kmp/tool-model")
    remapExternalProject(":lib:tool-kmp:tool-regex", "lib/tool-kmp/tool-regex")
    remapExternalProject(":lib:tool-kmp:tool-str", "lib/tool-kmp/tool-str")
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
    remapExternalProject(":lib:lsi:lsi-ksp", "lib/lsi/lsi-ksp")
    remapExternalProject(":lib:kcp:multireceiver:kcp-multireceiver-annotations", "lib/kcp/multireceiver/kcp-multireceiver-annotations")
    remapExternalProject(":lib:ksp:route", "lib/ksp/route")
    remapExternalProject(":lib:ksp:route:route-core", "lib/ksp/route/route-core")
    remapExternalProject(":lib:ksp:route:route-processor", "lib/ksp/route/route-processor")
    remapExternalProject(":lib:ksp:metadata:jimmer-entity-spi", "lib/ksp/metadata/jimmer-entity-spi")
    remapExternalProject(
        ":lib:ksp:metadata:compose-props:compose-props-annotations",
        "lib/ksp/metadata/compose-props/compose-props-annotations",
    )
    remapExternalProject(
        ":lib:ksp:metadata:compose-props:compose-props-gradle-plugin",
        "lib/ksp/metadata/compose-props/compose-props-gradle-plugin",
    )
    remapExternalProject(
        ":lib:ksp:metadata:compose-props:compose-props-processor",
        "lib/ksp/metadata/compose-props/compose-props-processor",
    )
    remapExternalProject(":lib:ksp:metadata:entity2iso-processor", "lib/ksp/metadata/entity2iso-processor")
    remapExternalProject(":lib:ksp:metadata:entity2form:entity2form-processor", "lib/ksp/metadata/entity2form/entity2form-processor")
    remapExternalProject(":lib:ksp:metadata:entity2mcp-processor", "lib/ksp/metadata/entity2mcp-processor")
    remapExternalProject(":lib:ksp:metadata:jimmer-entity-external-processor", "lib/ksp/metadata/jimmer-entity-external-processor")
    remapExternalProject(":lib:ksp:metadata:multireceiver-processor", "lib/ksp/metadata/multireceiver-processor")
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
                substitute(module("site.addzero:app-sidebar-cupertino-adapter"))
                    .using(project(":lib:compose:app-sidebar-cupertino-adapter"))
                substitute(module("site.addzero:compose-cupertino-workbench"))
                    .using(project(":lib:compose:compose-cupertino-workbench"))
                substitute(module("site.addzero:compose-native-component-high-level"))
                    .using(project(":lib:compose:compose-native-component-high-level"))
                substitute(module("site.addzero:compose-native-component-button"))
                    .using(project(":lib:compose:compose-native-component-button"))
                substitute(module("site.addzero:compose-native-component-chat"))
                    .using(project(":lib:compose:compose-native-component-chat"))
                substitute(module("site.addzero:compose-native-component-searchbar"))
                    .using(project(":lib:compose:compose-native-component-searchbar"))
                substitute(module("site.addzero:compose-native-component-text"))
                    .using(project(":lib:compose:compose-native-component-text"))
                substitute(module("site.addzero:compose-native-component-tree"))
                    .using(project(":lib:compose:compose-native-component-tree"))
                substitute(module("site.addzero:compose-workbench-design"))
                    .using(project(":lib:compose:compose-workbench-design"))
                substitute(module("site.addzero:compose-workbench-shell"))
                    .using(project(":lib:compose:compose-workbench-shell"))
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
                substitute(module("site.addzero:tool-array"))
                    .using(project(":lib:tool-kmp:tool-array"))
                substitute(module("site.addzero:tool-boolean"))
                    .using(project(":lib:tool-kmp:tool-boolean"))
                substitute(module("site.addzero:tool-coll"))
                    .using(project(":lib:tool-kmp:tool-coll"))
                substitute(module("site.addzero:tool-enum"))
                    .using(project(":lib:tool-kmp:tool-enum"))
                substitute(module("site.addzero:tool-model"))
                    .using(project(":lib:tool-kmp:tool-model"))
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
                substitute(module("site.addzero:kcp-spread-pack-annotations"))
                    .using(project(":lib:kcp:spread-pack:kcp-spread-pack-annotations"))
                substitute(module("site.addzero:kcp-spread-pack-plugin"))
                    .using(project(":lib:kcp:spread-pack:kcp-spread-pack-plugin"))
                substitute(module("site.addzero:kcp-spread-pack-gradle-plugin"))
                    .using(project(":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin"))
                substitute(module("site.addzero:kcp-spread-pack-ide-plugin"))
                    .using(project(":lib:kcp:spread-pack:kcp-spread-pack-ide-plugin"))
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
    if (preferredBuildLogicCatalogFile?.isFile == true) {
        versionCatalogs {
            create("libs") {
                from(files(preferredBuildLogicCatalogFile))
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
