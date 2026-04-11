import org.gradle.api.tasks.Delete

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
}

val libs = versionCatalogs.named("libs")

configurations.matching { configuration ->
    configuration.name == "kspJvm"
}.configureEach {
    exclude(group = "site.addzero", module = "entity2form-processor")
    exclude(group = "site.addzero", module = "entity2form-processor-jvm")
    exclude(group = "site.addzero", module = "entity2iso-processor")
    exclude(group = "site.addzero", module = "entity2iso-processor-jvm")
}

val hostConfigApiProject = project(":apps:kcloud:plugins:host-config:api")
val hostConfigApiProjectDir = layout.projectDirectory.dir("../api")
val hostConfigSharedProjectDir = layout.projectDirectory.dir("../shared")
val hostConfigUiProjectDir = layout.projectDirectory.dir("../ui")
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

/** 同构体生成源码目录。 */
val generatedIsoSourceDir =
    hostConfigApiProjectDir.dir("generated/commonMain/kotlin")

/** 同构体生成包。 */
val generatedIsoPackage = "site.addzero.kcloud.plugins.hostconfig.generated.isomorphic"

/** 同构体生成源码输出目录。 */
val generatedIsoOutputDir =
    generatedIsoSourceDir.dir("site/addzero/kcloud/plugins/hostconfig/generated/isomorphic")

/** 前端表单生成源码目录。 */
val generatedUiFormSourceDir =
    hostConfigUiProjectDir.dir("generated/commonMain/kotlin")

/** 当前 server 源码目录。 */
val backendServerSourceDir = layout.projectDirectory.dir("src/jvmMain/kotlin")

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.hostconfig.api.external.generated")
    arg("apiClientOutputDir", generatedApiOutputDir.get().asFile.absolutePath)
    arg("apiClientAggregatorOutputDir", generatedApiOutputDir.get().asFile.absolutePath)
    arg("sharedSourceDir", generatedIsoSourceDir.asFile.absolutePath)
    arg("sharedComposeSourceDir", generatedUiFormSourceDir.asFile.absolutePath)
    arg("backendServerSourceDir", backendServerSourceDir.asFile.absolutePath)
    arg("isomorphicPkg", generatedIsoPackage)
    arg("isomorphicGenDir", generatedIsoOutputDir.asFile.absolutePath)
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
    add("kspJvm", project(":lib:ksp:controller2iso2dataprovider-processor-local"))
    add("kspJvm", project(":lib:ksp:entity2form-processor-local"))
    add("kspJvm", project(":lib:ksp:entity2iso-processor-local"))
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
    delete(generatedIsoSourceDir.asFile)
    delete(generatedUiFormSourceDir.asFile)
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    outputs.dir(generatedApiRootDir)
    outputs.dir(generatedIsoSourceDir)
    outputs.dir(generatedUiFormSourceDir)
    dependsOn(cleanHostConfigGeneratedApis)
}
