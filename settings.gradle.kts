rootProject.name = rootDir.name
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("site.addzero.gradle.plugin.repo-buddy") version "+"
    id("site.addzero.gradle.plugin.addzero-git-dependency") version "+"
    id("site.addzero.gradle.plugin.modules-buddy") version "+"
}

val localAddzeroLibJvmDir = file("../addzero-lib-jvm")
if (localAddzeroLibJvmDir.resolve("settings.gradle.kts").isFile) {
    includeBuild(localAddzeroLibJvmDir)
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven(url = "https://repo.spring.io/milestone/")
        maven(url = "https://plugins.gradle.org/m2/")
    }
}
