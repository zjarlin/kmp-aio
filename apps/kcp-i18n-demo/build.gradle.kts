@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import java.io.File

buildscript {
    val localAddzeroLibJvmDir = listOf(
        rootDir.resolve("../addzero-lib-jvm"),
        rootDir.resolve("addzero-lib-jvm"),
    ).firstOrNull { candidateDir -> candidateDir.resolve("settings.gradle.kts").isFile }
    val localAddzeroLibJvmVersion = localAddzeroLibJvmDir
        ?.resolve("gradle.properties")
        ?.takeIf { file -> file.isFile }
        ?.readLines()
        ?.firstOrNull { line -> line.startsWith("version=") }
        ?.substringAfter("=")
        ?.trim()
        ?.takeIf(String::isNotBlank)
    val resolvedKcpI18nVersion = project.providers.gradleProperty("addzeroKcpI18nVersion")
        .orElse(localAddzeroLibJvmVersion ?: "2026.10330.12238")
        .get()
    val localI18nPluginJar = localAddzeroLibJvmDir
        ?.resolve("lib/kcp/kcp-i18n-gradle-plugin/build/libs")
        ?.takeIf { directory -> directory.isDirectory }
        ?.listFiles()
        ?.firstOrNull { file ->
            file.isFile &&
                file.extension == "jar" &&
                !file.name.endsWith("-sources.jar") &&
                !file.name.endsWith("-javadoc.jar")
        }
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://repo.spring.io/milestone/")
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        if (localI18nPluginJar != null) {
            classpath(files(localI18nPluginJar))
            classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.20-RC")
        } else {
            classpath("site.addzero.kcp.i18n:site.addzero.kcp.i18n.gradle.plugin:$resolvedKcpI18nVersion")
        }
    }
}

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

apply(plugin = "site.addzero.kcp.i18n")

val libs = versionCatalogs.named("libs")
val addzeroLibJvmVersion: String by project
val jdkVersion = libs.findVersion("jdk17").get().requiredVersion.toInt()
val desktopJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(jdkVersion))
}

data class JvmMainEntryPoint(
    val packageName: String,
    val facadeClassName: String,
) {
    val mainClassName
        get() = "$packageName.$facadeClassName"
}

fun File.inferJvmMainEntryPoint(): JvmMainEntryPoint {
    require(isDirectory) {
        "未找到 jvmMain Kotlin 源码目录: $absolutePath"
    }
    val packageRegex = Regex("""^\s*package\s+([A-Za-z0-9_.]+)\s*$""", RegexOption.MULTILINE)
    val mainRegex = Regex("""^\s*fun\s+main\s*\(""", RegexOption.MULTILINE)
    val jvmNameRegex = Regex("""@file:JvmName\("([^"]+)"\)""")
    val candidates = walkTopDown()
        .filter { file -> file.isFile && file.extension == "kt" }
        .mapNotNull { file ->
            val content = file.readText()
            if (!mainRegex.containsMatchIn(content)) {
                return@mapNotNull null
            }
            val packageName = packageRegex.find(content)?.groupValues?.get(1)
                ?: error("文件 ${file.absolutePath} 缺少 package 声明")
            val facadeClassName = jvmNameRegex.find(content)?.groupValues?.get(1)
                ?: "${file.nameWithoutExtension.replaceFirstChar { char -> char.uppercaseChar() }}Kt"
            JvmMainEntryPoint(
                packageName = packageName,
                facadeClassName = facadeClassName,
            )
        }
        .toList()

    require(candidates.size == 1) {
        "期望在 jvmMain 中找到唯一一个 main 入口，实际找到 ${candidates.size} 个: $candidates"
    }
    return candidates.single()
}

val desktopEntryPoint = project.layout.projectDirectory
    .dir("src/jvmMain/kotlin")
    .asFile
    .inferJvmMainEntryPoint()
val desktopMainClass = desktopEntryPoint.mainClassName

extensions.configure<site.addzero.kcp.i18n.gradle.I18NGradleExtension>("i18n") {
    targetLocale.set("en")
    resourceBasePath.set("i18n")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:scaffold-spi:$addzeroLibJvmVersion")
        }
    }
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

tasks.withType<JavaExec>().configureEach {
    if (name == "jvmRun" || name == "runJvm") {
        javaLauncher.set(desktopJavaLauncher)
    }
}

compose.desktop {
    application {
        mainClass = desktopMainClass
    }
}

tasks.register("printDetectedDesktopMainClass") {
    group = "verification"
    description = "打印从 jvmMain 源码推断出的桌面主类"
    doLast {
        println("packageName=${desktopEntryPoint.packageName}")
        println("mainClass=$desktopMainClass")
    }
}
