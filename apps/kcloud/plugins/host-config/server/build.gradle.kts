import org.gradle.api.tasks.Delete

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
}

val hostConfigUiProjectDir = layout.projectDirectory.dir("../ui")
val hostConfigSharedProjectDir = layout.projectDirectory.dir("../shared")
val generatedApiRootDir =
    hostConfigUiProjectDir.dir("build/generated/source/controller2api/commonMain/kotlin")

/** API 生成目录。 */
val generatedApiOutputDir =
    generatedApiRootDir.dir(
        "site/addzero/kcloud/plugins/hostconfig/api/external/generated",
    )

/** 共享源码目录。 */
val sharedSourceDir =
    hostConfigSharedProjectDir.dir("src/commonMain/kotlin")

/** 前端源码目录。 */
val sharedComposeSourceDir =
    hostConfigUiProjectDir.dir("src/commonMain/kotlin")

/** 当前 server 源码目录。 */
val backendServerSourceDir = layout.projectDirectory.dir("src/jvmMain/kotlin")

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.hostconfig.api.external.generated")
    arg("apiClientOutputDir", generatedApiOutputDir.asFile.absolutePath)
    arg("apiClientAggregatorObjectName", "Apis")
    arg("apiClientAggregatorStyle", "koin")
    arg("apiClientAggregatorOutputDir", generatedApiOutputDir.asFile.absolutePath)
    arg("sharedSourceDir", sharedSourceDir.asFile.absolutePath)
    arg("sharedComposeSourceDir", sharedComposeSourceDir.asFile.absolutePath)
    arg("backendServerSourceDir", backendServerSourceDir.asFile.absolutePath)
    arg("entity2Iso.enabled", "false")
    arg("entity2Form.enabled", "false")
    arg("entity2Mcp.enabled", "false")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:host-config:shared"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(project(":lib:ktor:starter:starter-spi"))
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            implementation("site.addzero:spec-iot:2026.03.13")
        }
    }
}

val cleanHostConfigGeneratedApis by tasks.registering(Delete::class) {
    delete(generatedApiRootDir.asFile)
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    outputs.dir(generatedApiOutputDir.asFile)
    dependsOn(cleanHostConfigGeneratedApis)
}
