import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.tasks.Jar

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
}

val codegenContextUiProject = project(":apps:kcloud:plugins:codegen-context:ui")
val generatedApiRootDir = codegenContextUiProject.layout.buildDirectory.dir("generated/ksp/commonMain/kotlin").get()
val generatedApiOutputDir =
    generatedApiRootDir.dir(
        "site/addzero/kcloud/plugins/codegencontext/api/external",
    )
val sharedSourceDir = layout.projectDirectory.dir("../shared/src/commonMain/kotlin")
val sharedComposeSourceDir = codegenContextUiProject.layout.projectDirectory.dir("src/commonMain/kotlin")
val backendServerSourceDir = layout.projectDirectory.dir("src/jvmMain/kotlin")
val mcuConsoleServerGeneratedContractDir =
    rootProject.layout.projectDirectory.dir(
        "apps/kcloud/plugins/mcu-console/server/generated/jvmMain/kotlin",
    )
val mcuConsoleSharedGeneratedContractDir =
    rootProject.layout.projectDirectory.dir(
        "apps/kcloud/plugins/mcu-console/shared/generated/commonMain/kotlin",
    )
val mcuConsoleMetadataOutputDir =
    rootProject.layout.projectDirectory.dir(
        "apps/kcloud/plugins/mcu-console/server/generated/modbus-metadata",
    )
val codegenContextServerJvmJar = tasks.named<Jar>("jvmJar")
val codegenContextServerJvmRuntimeClasspath = configurations.named("jvmRuntimeClasspath")
val kcloudMysqlJdbcUrl = "jdbc:mysql://192.168.31.133:3306/okmy_dics?createDatabaseIfNotExist=true"
val kcloudMysqlUser = "root"
val kcloudMysqlPassword = "test123456"
val modbusMetadataQuery =
    """
    SELECT payload
    FROM codegen_context_modbus_contract
    WHERE consumer_target = 'MCU_CONSOLE'
      AND enabled = 1
      AND selected = 1
      AND transport = '${'$'}{transport}'
    ORDER BY updated_at DESC
    LIMIT 1
    """.trimIndent()

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
val modbusCodegenVersion = libs.findVersion("modbus-runtime").get().requiredVersion

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:codegen-context:shared"))
            implementation(project(":apps:kcloud:plugins:host-config:server"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            implementation("site.addzero:modbus-codegen-model:$modbusCodegenVersion")
            implementation("site.addzero:modbus-codegen-core:$modbusCodegenVersion")
            implementation("site.addzero:modbus-ksp-core:$modbusCodegenVersion")
            implementation("site.addzero:modbus-ksp-kotlin-gateway:$modbusCodegenVersion")
            implementation("site.addzero:modbus-ksp-c-contract:$modbusCodegenVersion")
            implementation("site.addzero:modbus-ksp-markdown:$modbusCodegenVersion")
            implementation(libs.findLibrary("mysql-mysql-connector-java").get())
        }
        jvmTest.dependencies {
            implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
            implementation(libs.findLibrary("mysql-mysql-connector-java").get())
            implementation(libs.findLibrary("org-flywaydb-flyway-core").get())
            implementation(libs.findLibrary("org-flywaydb-flyway-mysql").get())
        }
    }
}

val cleanCodegenContextGeneratedApis by tasks.registering(Delete::class) {
    delete(generatedApiOutputDir.asFile)
}

val generateMcuConsoleContracts by tasks.registering(JavaExec::class) {
    group = "code generation"
    description = "从 codegen-context 已选快照生成 MCU Console 合同源码。"
    dependsOn(codegenContextServerJvmJar)
    inputs.property("jdbcUrl", kcloudMysqlJdbcUrl)
    inputs.property("jdbcUser", kcloudMysqlUser)
    inputs.property("query", modbusMetadataQuery)
    inputs.property("jsonColumn", "payload")
    inputs.property("transport", "rtu,tcp,mqtt")
    outputs.dir(mcuConsoleServerGeneratedContractDir)
    outputs.dir(mcuConsoleSharedGeneratedContractDir)
    outputs.dir(mcuConsoleMetadataOutputDir)
    outputs.upToDateWhen { false }
    classpath(
        files(codegenContextServerJvmJar.flatMap { jar -> jar.archiveFile }),
        codegenContextServerJvmRuntimeClasspath,
    )
    mainClass.set("site.addzero.kcloud.plugins.codegencontext.codegen_context.service.CodegenMetadataBuildCliKt")
    workingDir(rootProject.projectDir)
    args(
        "--driver-class",
        "com.mysql.cj.jdbc.Driver",
        "--jdbc-url",
        kcloudMysqlJdbcUrl,
        "--username",
        kcloudMysqlUser,
        "--password",
        kcloudMysqlPassword,
        "--query",
        modbusMetadataQuery,
        "--json-column",
        "payload",
        "--transport",
        "rtu,tcp,mqtt",
        "--skip-missing-transports",
        "true",
        "--workspace-root",
        rootProject.projectDir.absolutePath,
        "--server-output-root",
        mcuConsoleServerGeneratedContractDir.asFile.absolutePath,
        "--shared-output-root",
        mcuConsoleSharedGeneratedContractDir.asFile.absolutePath,
        "--gateway-output-root",
        mcuConsoleServerGeneratedContractDir.asFile.absolutePath,
        "--c-output-root",
        mcuConsoleMetadataOutputDir.dir("c").asFile.absolutePath,
        "--markdown-output-root",
        mcuConsoleMetadataOutputDir.dir("markdown").asFile.absolutePath,
    )
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    outputs.files(generatedApiFiles)
    dependsOn(cleanCodegenContextGeneratedApis)
}
