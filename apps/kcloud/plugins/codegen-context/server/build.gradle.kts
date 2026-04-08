import org.gradle.api.tasks.Delete

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
}

val codegenContextUiProjectDir = layout.projectDirectory.dir("../ui")
val generatedApiRootDir = codegenContextUiProjectDir.dir("generated/commonMain/kotlin")
val generatedApiOutputDir =
    generatedApiRootDir.dir(
        "site/addzero/kcloud/plugins/codegencontext/api/external",
    )
val sharedSourceDir = layout.projectDirectory.dir("../shared/src/commonMain/kotlin")
val sharedComposeSourceDir = codegenContextUiProjectDir.dir("src/commonMain/kotlin")
val backendServerSourceDir = layout.projectDirectory.dir("src/jvmMain/kotlin")

val generatedApiFiles =
    listOf(
        "CodegenContextApi.kt",
        "CodegenTemplateApi.kt",
    ).map { fileName ->
        generatedApiOutputDir.file(fileName).asFile
    }

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.codegencontext.api.external")
    arg("apiClientOutputDir", generatedApiOutputDir.asFile.absolutePath)
    arg("sharedSourceDir", sharedSourceDir.asFile.absolutePath)
    arg("sharedComposeSourceDir", sharedComposeSourceDir.asFile.absolutePath)
    arg("backendServerSourceDir", backendServerSourceDir.asFile.absolutePath)
    arg("entity2Iso.enabled", "false")
    arg("entity2Form.enabled", "false")
    arg("entity2Mcp.enabled", "false")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:codegen-context:shared"))
            implementation(project(":apps:kcloud:plugins:host-config:server"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(project(":lib:ktor:starter:starter-statuspages"))
        }
        jvmTest.dependencies {
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
            implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
        }
    }
}

val cleanCodegenContextGeneratedApis by tasks.registering(Delete::class) {
    delete(generatedApiOutputDir.asFile)
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    outputs.files(generatedApiFiles)
    dependsOn(cleanCodegenContextGeneratedApis)
}
