import org.gradle.api.tasks.Delete

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
}

val hostConfigUiProjectDir = layout.projectDirectory.dir("../ui")
val hostConfigSharedProjectDir = layout.projectDirectory.dir("../shared")

/** API 生成目录。 */
val generatedApiOutputDir =
    hostConfigUiProjectDir.dir(
        "src/commonMain/kotlin/site/addzero/kcloud/plugins/hostconfig/api/external",
    )

/** API 聚合入口生成目录。 */
val generatedApiAggregatorOutputDir =
    hostConfigUiProjectDir.dir(
        "build/generated/source/controller2api/commonMain/kotlin/site/addzero/kcloud/plugins/hostconfig/api/external",
    )

/** 共享源码目录。 */
val sharedSourceDir =
    hostConfigSharedProjectDir.dir("src/commonMain/kotlin")

/** 前端源码目录。 */
val sharedComposeSourceDir =
    hostConfigUiProjectDir.dir("src/commonMain/kotlin")

/** 当前 server 源码目录。 */
val backendServerSourceDir = layout.projectDirectory.dir("src/jvmMain/kotlin")

val generatedApiFiles =
    listOf(
        "CatalogApi.kt",
        "CloudAccessApi.kt",
        "GatewayConfigApi.kt",
        "ProjectApi.kt",
        "ProjectUploadApi.kt",
        "TagApi.kt",
        "TemplateApi.kt",
        "ProjectConfigApi.kt",
    ).map { fileName ->
        generatedApiOutputDir.file(fileName).asFile
    } +
        listOf(
            generatedApiOutputDir.file("Apis.kt").asFile,
            generatedApiOutputDir.file("ApisModule.kt").asFile,
            generatedApiAggregatorOutputDir.file("Apis.kt").asFile,
            generatedApiAggregatorOutputDir.file("ApisModule.kt").asFile,
        )

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.hostconfig.api.external")
    arg("apiClientOutputDir", generatedApiOutputDir.asFile.absolutePath)
    arg("apiClientAggregatorObjectName", "Apis")
    arg("apiClientAggregatorStyle", "koin")
    arg("apiClientAggregatorOutputDir", generatedApiAggregatorOutputDir.asFile.absolutePath)
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
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            implementation("site.addzero:spec-iot:2026.03.13")
        }
    }
}

val cleanHostConfigGeneratedApis by tasks.registering(Delete::class) {
    delete(generatedApiFiles)
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    outputs.files(generatedApiFiles)
    dependsOn(cleanHostConfigGeneratedApis)
}
