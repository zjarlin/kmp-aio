import org.gradle.api.tasks.Delete

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
}

val libs = versionCatalogs.named("libs")

val hostConfigApiProject = project(":apps:kcloud:plugins:host-config:api")
val hostConfigApiProjectDir = layout.projectDirectory.dir("../api")
val hostConfigSharedProjectDir = layout.projectDirectory.dir("../shared")
val generatedApiRootDir =
    hostConfigApiProject.layout.buildDirectory.dir("generated/source/controller2api/commonMain/kotlin")

/** API 生成目录。 */
val generatedApiOutputDir =
    generatedApiRootDir.map { root ->
        root.dir("site/addzero/kcloud/plugins/hostconfig/api/external/generated")
    }
val generatedApiBridgeSourceDir =
    generatedApiRootDir.map { root ->
        root.dir("site/addzero/kcloud/plugins/hostconfig/api/external")
    }

/** 共享源码目录。 */
val sharedSourceDir =
    hostConfigSharedProjectDir.dir("src/commonMain/kotlin")

/** 前端源码目录。 */
val sharedComposeSourceDir =
    hostConfigApiProjectDir.dir("src/commonMain/kotlin")

/** 当前 server 源码目录。 */
val backendServerSourceDir = layout.projectDirectory.dir("src/jvmMain/kotlin")

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.hostconfig.api.external.generated")
    arg("apiClientOutputDir", generatedApiOutputDir.get().asFile.absolutePath)
    arg("apiClientBridgePackageName", "site.addzero.kcloud.plugins.hostconfig.api.external")
    arg("apiClientBridgeOutputDir", generatedApiBridgeSourceDir.get().asFile.absolutePath)
    arg("apiClientBridgeFileName", "HostConfigGeneratedApiClients.kt")
    arg("sharedSourceDir", sharedSourceDir.asFile.absolutePath)
    arg("sharedComposeSourceDir", sharedComposeSourceDir.asFile.absolutePath)
    arg("backendServerSourceDir", backendServerSourceDir.asFile.absolutePath)
    arg("entity2Iso.enabled", "false")
    arg("entity2Form.enabled", "false")
    arg("entity2Mcp.enabled", "false")
}

kotlin {
    dependencies{
        implementation(project(":lib:kmp-exception"))
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:host-config:shared"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(project(":lib:ktor:starter:starter-spi"))
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            implementation(libs.findLibrary("site-addzero-tool-sql-executor").get())
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
            implementation(libs.findLibrary("spec-iot").get())
        }
        jvmTest.dependencies {
            implementation("junit:junit:4.13.2")
            implementation("io.mockk:mockk:1.13.8")
            implementation("com.intelligt.modbus:jamod:1.0.0") // 常用modbus库
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
        }
    }
}

val cleanHostConfigGeneratedApis by tasks.registering(Delete::class) {
    delete(generatedApiRootDir.get().asFile)
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    outputs.dir(generatedApiRootDir)
    dependsOn(cleanHostConfigGeneratedApis)
}
