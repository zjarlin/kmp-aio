import site.addzero.gradle.GitDependencysExtension

pluginManagement {
    val localBuildLogicDir = file("checkouts/build-logic")
    if (localBuildLogicDir.resolve("src/main/kotlin").isDirectory) {
        includeBuild(localBuildLogicDir) {
            name = "build-logic"
        }
    }
    val localAddzeroLibJvmDir = file("../addzero-lib-jvm")
    if (localAddzeroLibJvmDir.resolve("settings.gradle.kts").isFile) {
        includeBuild(localAddzeroLibJvmDir) {
            name = "addzero-lib-jvm"
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
    includeBuild(localAddzeroLibJvmDir) {
        name = "addzero-lib-jvm"
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
