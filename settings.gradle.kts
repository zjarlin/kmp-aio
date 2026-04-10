pluginManagement {
    repositories {
        mavenLocal {
            content {
                includeGroupByRegex("site\\.addzero(\\..+)?")
            }
        }
        maven(url = "https://mirrors.tencent.com/nexus/repository/maven-public/")
        maven(url = "https://mirrors.tencent.com/nexus/repository/gradle-plugins/")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal {
            content {
                includeGroupByRegex("site\\.addzero(\\..+)?")
            }
        }
        maven(url = "https://mirrors.tencent.com/nexus/repository/maven-public/")
        maven(url = "https://mirrors.tencent.com/nexus/repository/gradle-plugins/")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("site.addzero.gradle.plugin.repo-buddy") version "2026.04.11"
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
    id("site.addzero.gradle.plugin.modules-buddy") version "+"
//    id("io.gitee.zjarlin.auto-modules") version "0.0.608"
}
//includeBuild("../addzero-lib-jvm")

//includeFlat("addzero-lib-jvm")
