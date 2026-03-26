import java.io.File

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
}

val localBuildLogicDir = file("checkouts/build-logic")
require(localBuildLogicDir.isDirectory) {
    "Missing local build logic checkout at ${localBuildLogicDir.absolutePath}"
}

includeBuild(localBuildLogicDir)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("checkouts/build-logic/gradle/libs.versions.toml"))
        }
    }
}

listOf("apps", "lib")
    .map(::file)
    .filter(File::isDirectory)
    .flatMap { root -> findProjectDirectories(root).asSequence() }
    .sortedBy { it.relativeTo(rootDir).invariantSeparatorsPath }
    .forEach { projectDir ->
        include(projectDir.toProjectPath())
    }

fun findProjectDirectories(root: File): List<File> {
    val result = mutableListOf<File>()

    fun walk(current: File) {
        if (!current.isDirectory) {
            return
        }
        if (current.name.startsWith(".")) {
            return
        }
        if (current.name == "build" || current.name == "out") {
            return
        }
        if (File(current, "build.gradle.kts").isFile) {
            result += current
        }
        current.listFiles()
            ?.filter(File::isDirectory)
            ?.sortedBy(File::getName)
            ?.forEach(::walk)
    }

    walk(root)
    return result
}

fun File.toProjectPath(): String {
    return relativeTo(rootDir)
        .invariantSeparatorsPath
        .split('/')
        .filter(String::isNotBlank)
        .joinToString(separator = ":", prefix = ":")
}
