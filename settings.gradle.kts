pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    if (file("checkouts/build-logic/settings.gradle.kts").isFile) {
        includeBuild("checkouts/build-logic")
    }
}

plugins {
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "+" apply false
}

val buildLogicCatalogFile = file("checkouts/build-logic/gradle/libs.versions.toml")
val buildLogicSettingsFile = file("checkouts/build-logic/settings.gradle.kts")
val excludedTopLevelDirs = setOf("build", "checkouts", "kotlin-js-store")
val excludedDirNames = setOf("build", "node_modules")

dependencyResolutionManagement {
    if (buildLogicCatalogFile.isFile) {
        versionCatalogs {
            create("libs") {
                from(files(buildLogicCatalogFile))
            }
        }
    }
}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

if (!buildLogicSettingsFile.isFile) {
    apply(plugin = "site.addzero.gradle.plugin.addzero-git-dependency")
}

fun java.io.File.shouldPrune(rootDir: java.io.File): Boolean {
    if (!isDirectory || absolutePath == rootDir.absolutePath) {
        return false
    }
    val relativePath = relativeTo(rootDir).invariantSeparatorsPath
    val segments = relativePath.split('/').filter { it.isNotBlank() }
    if (segments.isEmpty()) {
        return false
    }
    if (segments.any { it.startsWith(".") }) {
        return true
    }
    if (segments.first() in excludedTopLevelDirs) {
        return true
    }
    return segments.any { it in excludedDirNames }
}

fun java.io.File.toGradleProjectPath(rootDir: java.io.File): String =
    relativeTo(rootDir)
        .invariantSeparatorsPath
        .split('/')
        .filter { it.isNotBlank() }
        .joinToString(separator = ":", prefix = ":")

fun collectModuleDirs(
    currentDir: java.io.File,
    rootDir: java.io.File,
    result: MutableList<java.io.File>,
) {
    if (currentDir.shouldPrune(rootDir)) {
        return
    }
    if (currentDir != rootDir && currentDir.resolve("build.gradle.kts").isFile) {
        result += currentDir
    }
    currentDir.listFiles()
        ?.asSequence()
        ?.filter { it.isDirectory }
        ?.sortedBy { it.name }
        ?.forEach { child ->
            collectModuleDirs(child, rootDir, result)
        }
}

buildList {
    collectModuleDirs(rootDir, rootDir, this)
}.forEach { moduleDir ->
    include(moduleDir.toGradleProjectPath(rootDir))
}

//includeBuild("../addzero-lib-jvm")

//includeFlat("addzero-lib-jvm")
