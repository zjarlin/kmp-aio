import org.gradle.api.tasks.Delete

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
}

val libs = versionCatalogs.named("libs")

val hostConfigApiProject = project(":apps:kcloud:plugins:host-config:api")
val hostConfigApiProjectDir = layout.projectDirectory.dir("../api")
val hostConfigSharedProjectDir = layout.projectDirectory.dir("../shared")
val hostConfigUiProject = project(":apps:kcloud:plugins:host-config:ui")
val generatedApiRootDir =
    hostConfigApiProject.layout.buildDirectory.dir("generated/source/controller2api/commonMain/kotlin")

/** API 生成目录。 */
val generatedApiOutputDir =
    generatedApiRootDir.map { root ->
        root.dir("site/addzero/kcloud/plugins/hostconfig/api/external/generated")
    }

/** 共享源码目录。 */
val sharedSourceDir =
    hostConfigSharedProjectDir.dir("src/commonMain/kotlin")

/** 同构体生成包。 */
val generatedIsoPackage = "site.addzero.kcloud.plugins.hostconfig.generated.isomorphic"

/** 同构体生成目录。 */
val generatedIsoOutputDir =
    hostConfigApiProject
        .layout
        .buildDirectory
        .dir("generated/ksp/commonMain/kotlin")

/** 前端生成源码目录。 */
val sharedComposeSourceDir =
    hostConfigUiProject
        .layout
        .buildDirectory
        .dir("generated/ksp/commonMain/kotlin")

/** 当前 server 源码目录。 */
val backendServerSourceDir = layout.projectDirectory.dir("src/jvmMain/kotlin")

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.hostconfig.api.external.generated")
    arg("apiClientOutputDir", generatedApiOutputDir.get().asFile.absolutePath)
    arg("apiClientAggregatorOutputDir", generatedApiOutputDir.get().asFile.absolutePath)
    arg("sharedSourceDir", sharedSourceDir.asFile.absolutePath)
    arg("sharedComposeSourceDir", sharedComposeSourceDir.get().asFile.absolutePath)
    arg("backendServerSourceDir", backendServerSourceDir.asFile.absolutePath)
    arg("isomorphicPkg", generatedIsoPackage)
    arg("isomorphicGenDir", generatedIsoOutputDir.get().asFile.absolutePath)
    arg("isomorphicPackageName", generatedIsoPackage)
    arg("isomorphicClassSuffix", "Iso")
    arg("isomorphicSerializableEnabled", "true")
    arg("entity2Iso.enabled", "true")
    arg("entity2Form.enabled", "true")
    arg("entity2Mcp.enabled", "false")
    arg("formPackageName", "site.addzero.kcloud.plugins.hostconfig.generated.forms")
    arg("enumOutputPackage", "site.addzero.kcloud.plugins.hostconfig.model.enums")
    arg("iso2DataProviderPackage", "site.addzero.kcloud.plugins.hostconfig.generated.forms.dataprovider")
}

dependencies {
    add("kspJvm", libs.findLibrary("site-addzero-controller2iso2dataprovider-processor").get())
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
    delete(generatedIsoOutputDir.get().asFile)
    delete(sharedComposeSourceDir.get().asFile)
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    outputs.dir(generatedApiRootDir)
    dependsOn(cleanHostConfigGeneratedApis)
}
