rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
//includeBuild("../addzero-lib-jvm")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("site.addzero.gradle.plugin.modules-buddy") version "+"
    id("site.addzero.gradle.plugin.repo-buddy") version "2025.11.30"
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "2026.02.25"

}
