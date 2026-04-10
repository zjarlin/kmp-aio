//pluginManagement {
//    repositories {
//        mavenLocal()
//        google()
//        mavenCentral()
//        gradlePluginPortal()
//    }
//    includeBuild("checkouts/build-logic")
//}

rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
plugins {
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
    id("site.addzero.gradle.plugin.modules-buddy") version "+"
//    id("io.gitee.zjarlin.auto-modules") version "0.0.608"
}
//includeBuild("../addzero-lib-jvm")

//includeFlat("addzero-lib-jvm")

