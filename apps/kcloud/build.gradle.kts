@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import java.util.Enumeration
import java.util.jar.JarFile

/**
 * KCloud - 类 Nextcloud 的跨平台同步客户端
 *
 * 支持 WebDAV/S3/SSH 多种存储后端，端到端加密
 */
plugins {
    id("site.addzero.buildlogic.kmp.cmp-aio")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

apply(from = rootProject.file("gradle/spring2ktor-ksp-cache-workaround.gradle.kts"))

val desktopMainClass = "com.kcloud.MainKt"
val libs = versionCatalogs.named("libs")
val ktorVersion = libs.findVersion("ktor").get().requiredVersion
val aggregateGeneratedJvmPath = layout.buildDirectory.dir("generated/source/crossModuleAggregates/jvmMain/kotlin")

@CacheableTask
abstract class GenerateCrossModuleAggregatesTask : DefaultTask() {
    @get:Classpath
    abstract val classpath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputRoot = outputDir.get().asFile
        val classpathFiles = classpath.files
        val springRoutePackages = classpathFiles
            .flatMap { it.findGeneratedClasses("/generated/springktor/GeneratedSpringRoutesKt.class") }
            .map { it.substringBeforeLast(".GeneratedSpringRoutesKt") }
            .distinct()
            .sorted()
        val iocProviders = classpathFiles
            .flatMap { it.findGeneratedClasses("/ThisModuleProvider.class") }
            .distinct()
            .sorted()

        outputRoot.resolve(
            "com/kcloud/app/generated/springktor/aggregate/GeneratedFeatureSpringRoutes.kt"
        ).apply {
            parentFile.mkdirs()
            writeText(
                buildString {
                    appendLine("package com.kcloud.app.generated.springktor.aggregate")
                    appendLine()
                    appendLine("import io.ktor.server.routing.Route")
                    if (springRoutePackages.isNotEmpty()) {
                        appendLine()
                        springRoutePackages.forEachIndexed { index, packageName ->
                            appendLine(
                                "import $packageName.registerGeneratedSpringRoutes as registerAggregatedSpringRoutes$index"
                            )
                        }
                    }
                    appendLine()
                    appendLine("fun Route.registerAggregatedFeatureSpringRoutes() {")
                    springRoutePackages.forEachIndexed { index, _ ->
                        appendLine("    registerAggregatedSpringRoutes$index()")
                    }
                    appendLine("}")
                }
            )
        }

        outputRoot.resolve(
            "com/kcloud/app/generated/ioc/aggregate/GeneratedIocModuleBootstrap.kt"
        ).apply {
            parentFile.mkdirs()
            writeText(
                buildString {
                    appendLine("package com.kcloud.app.generated.ioc.aggregate")
                    appendLine()
                    if (iocProviders.isNotEmpty()) {
                        appendLine("import site.addzero.ioc.spi.IocModuleRegistry")
                        appendLine()
                    }
                    appendLine("fun registerAggregatedIocModules() {")
                    iocProviders.forEach { providerName ->
                        appendLine("    IocModuleRegistry.register($providerName)")
                    }
                    appendLine("}")
                }
            )
        }
    }

    private fun <T> Enumeration<T>.asSequence(): Sequence<T> = sequence {
        while (hasMoreElements()) {
            yield(nextElement())
        }
    }

    private fun File.findGeneratedClasses(suffix: String): List<String> {
        return when {
            isDirectory -> walkTopDown()
                .filter { it.isFile }
                .map { it.relativeTo(this).invariantSeparatorsPath }
                .filter { it.endsWith(suffix) }
                .map { it.removeSuffix(".class").replace('/', '.') }
                .toList()

            isFile && extension == "jar" -> JarFile(this).use { jarFile ->
                jarFile.entries()
                    .asSequence()
                    .filter { !it.isDirectory && it.name.endsWith(suffix) }
                    .map { it.name.removeSuffix(".class").replace('/', '.') }
                    .toList()
            }

            else -> emptyList()
        }
    }
}

ksp {
    arg("springKtor.generatedPackage", "com.kcloud.app.generated.springktor")
}

kotlin {
    jvmToolchain(17)
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":apps:kcloud:features:ai"))
            implementation(project(":apps:kcloud:features:compose"))
            implementation(project(":apps:kcloud:features:desktop-integration"))
            implementation(project(":apps:kcloud:features:dotfiles"))
            implementation(project(":apps:kcloud:features:environment"))
            implementation(project(":apps:kcloud:features:file"))
            implementation(project(":apps:kcloud:features:notes"))
            implementation(project(":apps:kcloud:features:feature-api"))
            implementation(project(":apps:kcloud:features:package-organizer"))
            implementation(project(":apps:kcloud:features:quick-transfer"))
            implementation(project(":apps:kcloud:features:rbac"))
            implementation(project(":apps:kcloud:features:settings"))
            implementation(project(":apps:kcloud:features:server-management"))
            implementation(project(":apps:kcloud:features:ssh"))
            implementation(project(":apps:kcloud:features:transfer-history"))
            implementation(project(":apps:kcloud:features:webdav"))
        }
        jvmMain.dependencies {
            implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")
            implementation(libs.findLibrary("io-ktor-ktor-server-content-negotiation").get())
            implementation(libs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
            implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
            implementation(projects.lib.starterStatuspages)
            implementation(libs.findLibrary("site-addzero-ioc-core").get())
            implementation("site.addzero:spring2ktor-server-core:2026.03.13")
            compileOnly("org.springframework:spring-web:5.3.21")
        }
    }
}

dependencies {
    add("kspJvm", "site.addzero:spring2ktor-server-processor:2026.03.13")
}

val jvmCompileClasspath = configurations.named("jvmCompileClasspath")
val generateCrossModuleAggregates = tasks.register<GenerateCrossModuleAggregatesTask>("generateCrossModuleAggregates") {
    dependsOn(jvmCompileClasspath)
    classpath.from(jvmCompileClasspath)
    outputDir.set(aggregateGeneratedJvmPath)
}

val java17Launcher = extensions.getByType<JavaToolchainService>().launcherFor {
    languageVersion.set(JavaLanguageVersion.of(17))
}

kotlin.sourceSets.named("jvmMain") {
    kotlin.srcDir(aggregateGeneratedJvmPath)
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

tasks.withType<JavaExec>().configureEach {
    javaLauncher.set(java17Launcher)
}

val aggregateConsumerTasks = setOf("kspKotlinJvm", "compileKotlinJvm", "jvmSourcesJar", "jvmJar")
tasks.configureEach {
    if (name in aggregateConsumerTasks) {
        dependsOn(generateCrossModuleAggregates)
    }
}

compose.desktop {
    application {
        mainClass = desktopMainClass
    }
}
