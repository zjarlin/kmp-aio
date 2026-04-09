import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.tasks.Jar
import java.io.File

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
}

val codegenContextApiProject = project(":apps:kcloud:plugins:codegen-context:api")
val generatedApiRootDir = codegenContextApiProject.layout.buildDirectory.dir("generated/source/controller2api/commonMain/kotlin")
val generatedApiPackageRootDir =
    generatedApiRootDir.map { root ->
        root.dir("site/addzero/kcloud/plugins/codegencontext/api/external")
    }
val generatedApiOutputDir =
    generatedApiPackageRootDir.map { root ->
        root.dir("generated")
    }
val generatedApiBridgeSourceDir =
    generatedApiPackageRootDir
val sharedSourceDir = layout.projectDirectory.dir("../shared/src/commonMain/kotlin")
val sharedComposeSourceDir = codegenContextApiProject.layout.projectDirectory.dir("src/commonMain/kotlin")
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
        "../t/Docs/generated/modbus-metadata",
    )
val codegenContextServerJvmJar = tasks.named<Jar>("jvmJar")
val codegenContextServerJvmRuntimeClasspath = configurations.named("jvmRuntimeClasspath")
val defaultCodegenContextSqliteFile = layout.buildDirectory.file("generated/codegen-context-metadata.sqlite").get().asFile
val codegenContextSqliteFile =
    providers.gradleProperty("codegenContextSqliteFile").orNull?.let(::File) ?: defaultCodegenContextSqliteFile
val useCodegenContextSqlite =
    providers.gradleProperty("codegenContextDb").orNull?.equals("sqlite", ignoreCase = true) == true ||
        codegenContextSqliteFile.exists()
val kcloudMysqlJdbcUrl = "jdbc:mysql://192.168.31.133:3306/okmy_dics?createDatabaseIfNotExist=true"
val kcloudMysqlUser = "root"
val kcloudMysqlPassword = "test123456"
val codegenMetadataDriverClass = if (useCodegenContextSqlite) "org.sqlite.JDBC" else "com.mysql.cj.jdbc.Driver"
val codegenMetadataJdbcUrl =
    if (useCodegenContextSqlite) {
        "jdbc:sqlite:${codegenContextSqliteFile.absolutePath}"
    } else {
        kcloudMysqlJdbcUrl
    }
val codegenMetadataUser = if (useCodegenContextSqlite) null else kcloudMysqlUser
val codegenMetadataPassword = if (useCodegenContextSqlite) null else kcloudMysqlPassword
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

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.codegencontext.api.external.generated")
    arg("apiClientOutputDir", generatedApiOutputDir.get().asFile.absolutePath)
    arg("apiClientBridgePackageName", "site.addzero.kcloud.plugins.codegencontext.api.external")
    arg("apiClientBridgeOutputDir", generatedApiBridgeSourceDir.get().asFile.absolutePath)
    arg("apiClientBridgeFileName", "CodegenContextApiClients.kt")
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
    dependencies{
        implementation(project(":lib:kmp-exception"))
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":apps:kcloud:plugins:codegen-context:shared"))
            implementation(project(":apps:kcloud:plugins:host-config:server"))
            implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
            implementation(project(":lib:ktor:starter:starter-statuspages"))
            implementation(libs.findLibrary("site-addzero-tool-jvmstr").get())
            implementation("site.addzero:modbus-codegen-model:$modbusCodegenVersion")
            implementation("site.addzero:modbus-codegen-core:$modbusCodegenVersion")
            implementation("site.addzero:modbus-ksp-core:$modbusCodegenVersion")
            implementation("site.addzero:modbus-ksp-kotlin-gateway:$modbusCodegenVersion")
            implementation("site.addzero:modbus-ksp-c-contract:$modbusCodegenVersion")
            implementation("site.addzero:modbus-ksp-markdown:$modbusCodegenVersion")
            implementation(libs.findLibrary("mysql-mysql-connector-java").get())
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
        }
        jvmTest.dependencies {
            implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
            implementation(libs.findLibrary("mysql-mysql-connector-java").get())
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
            implementation(libs.findLibrary("org-flywaydb-flyway-core").get())
            implementation(libs.findLibrary("org-flywaydb-flyway-mysql").get())
        }
    }
}

val cleanCodegenContextGeneratedApis by tasks.registering(Delete::class) {
    delete(generatedApiRootDir.get().asFile)
}

val generateMcuConsoleContracts by tasks.registering(JavaExec::class) {
    group = "code generation"
    description = "从 codegen-context 已选快照生成 MCU Console 合同源码。"
    dependsOn(codegenContextServerJvmJar)
    inputs.property("driverClass", codegenMetadataDriverClass)
    inputs.property("jdbcUrl", codegenMetadataJdbcUrl)
    inputs.property("jdbcUser", codegenMetadataUser ?: "")
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
    val cliArgs =
        mutableListOf(
            "--driver-class",
            codegenMetadataDriverClass,
            "--jdbc-url",
            codegenMetadataJdbcUrl,
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
    codegenMetadataUser?.let {
        cliArgs += listOf("--username", it)
    }
    codegenMetadataPassword?.let {
        cliArgs += listOf("--password", it)
    }
    args(
        cliArgs,
    )
}

tasks.matching { task ->
    task.name == "kspKotlinJvm"
}.configureEach {
    outputs.dir(generatedApiRootDir)
    dependsOn(cleanCodegenContextGeneratedApis)
}
