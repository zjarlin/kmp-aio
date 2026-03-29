import site.addzero.gradle.GitDependencysExtension

pluginManagement {
    val localBuildLogicDir = file("checkouts/build-logic")
    if (localBuildLogicDir.resolve("src/main/kotlin").isDirectory) {
        includeBuild(localBuildLogicDir) {
            name = "build-logic"
        }
    }
    val localAddzeroLibJvmDir = file("../addzero-lib-jvm")
    val localAddzeroLibJvmVersion = localAddzeroLibJvmDir
        .resolve("gradle.properties")
        .takeIf { file -> file.isFile }
        ?.readLines()
        ?.firstOrNull { line -> line.startsWith("version=") }
        ?.substringAfter("=")
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: "2026.10329.10127"
    if (localAddzeroLibJvmDir.resolve("settings.gradle.kts").isFile) {
        plugins {
            id("site.addzero.kcp.i18n") version localAddzeroLibJvmVersion
        }
    }
}

val localAddzeroLibJvmDir = file("../addzero-lib-jvm")
val localAddzeroBuildLogicCatalogFile = localAddzeroLibJvmDir.resolve("checkouts/build-logic/gradle/libs.versions.toml")

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}

if (localAddzeroLibJvmDir.resolve("settings.gradle.kts").isFile) {
    extensions.configure<GitDependencysExtension>("implementationRemoteGit") {
        enableZlibs.set(false)
    }
}

if (localAddzeroLibJvmDir.resolve("settings.gradle.kts").isFile) {
    fun remapExternalProject(projectPath: String, relativeDir: String) {
        include(projectPath)
        project(projectPath).projectDir = localAddzeroLibJvmDir.resolve(relativeDir)
    }

    remapExternalProject(":lib:api", "lib/api")
    remapExternalProject(":lib:api:api-music-spi", "lib/api/api-music-spi")
    remapExternalProject(":lib:api:api-netease", "lib/api/api-netease")
    remapExternalProject(":lib:api:api-qqmusic", "lib/api/api-qqmusic")
    remapExternalProject(":lib:api:api-suno", "lib/api/api-suno")
    remapExternalProject(":lib:compose", "lib/compose")
    remapExternalProject(":lib:compose:app-sidebar", "lib/compose/app-sidebar")
    remapExternalProject(":lib:compose:glass-components", "lib/compose/glass-components")
    remapExternalProject(":lib:compose:liquid-glass", "lib/compose/liquid-glass")
    remapExternalProject(":lib:compose:media-playlist-player", "lib/compose/media-playlist-player")
    remapExternalProject(":lib:compose:scaffold-spi", "lib/compose/scaffold-spi")
    remapExternalProject(":lib:tool-kmp", "lib/tool-kmp")
    remapExternalProject(":lib:tool-kmp:network-starter", "lib/tool-kmp/network-starter")
    remapExternalProject(":lib:ksp", "lib/ksp")
    remapExternalProject(":lib:ksp:route", "lib/ksp/route")
    remapExternalProject(":lib:ksp:route:route-core", "lib/ksp/route/route-core")
    remapExternalProject(":lib:ksp:route:route-processor", "lib/ksp/route/route-processor")
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
