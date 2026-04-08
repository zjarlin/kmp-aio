plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-server-core")
    id("site.addzero.ksp.modbus-rtu")
}

val libs = versionCatalogs.named("libs")
val generatedMetadataDir = layout.projectDirectory.dir("generated/modbus-metadata")
val generatedMetadataDbFile = generatedMetadataDir.file("codegen-context.sqlite").asFile
val generatedMetadataTransportMarkerFile = generatedMetadataDir.file("selected-transport.txt").asFile
val useCodegenContextMetadata =
    generatedMetadataDbFile.exists() &&
        generatedMetadataTransportMarkerFile.exists() &&
        generatedMetadataTransportMarkerFile.readText().trim().equals("rtu", ignoreCase = true)

/** Api生成目录 */
val generatedApiOutputDir = project(":apps:kcloud:plugins:mcu-console:ui") .projectDir .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/mcuconsole/api/external") .absolutePath

/** 同构体生成目录 */
val generatedIsoPackage = "site.addzero.kcloud.plugins.mcuconsole"

/** 同构体生成包 */
val generatedIsoOutputDir = project(":apps:kcloud:plugins:mcu-console:shared") .projectDir .resolve("generated/commonMain/kotlin/site/addzero/kcloud/plugins/mcuconsole") .absolutePath

/** 共享目录 */
val sharedSourceDir = project(":apps:kcloud:plugins:mcu-console:shared") .projectDir .resolve("src/commonMain/kotlin") .absolutePath
/** 共享前端目录 */
val sharedComposeSourceDir = project(":apps:kcloud:plugins:mcu-console:ui") .projectDir .resolve("src/commonMain/kotlin") .absolutePath
//val generatedJvmRouteSourceDir = layout.buildDirectory.dir("generated/ksp/jvm/jvmMain/kotlin")

/** 当前server源码目录 */
val backendServerSourceDir = projectDir.resolve("src/jvmMain/kotlin").absolutePath
val generatedContractSourceDir = layout.projectDirectory.dir("generated/jvmMain/kotlin")

//dependencies {
//}

ksp {
    arg("apiClientPackageName", "site.addzero.kcloud.plugins.mcuconsole.api.external")
    arg("apiClientOutputDir", generatedApiOutputDir)
    arg("sharedSourceDir", sharedSourceDir)
    arg("sharedComposeSourceDir", sharedComposeSourceDir)
    arg("backendServerSourceDir", backendServerSourceDir)

    arg("isomorphicPkg", generatedIsoPackage)
    arg("isomorphicGenDir", generatedIsoOutputDir)
    arg("isomorphicPackageName", generatedIsoPackage)
    arg("isomorphicClassSuffix", "Iso")
    arg("isomorphicSerializableEnabled", "true")
    arg("entity2Iso.enabled", "true")
    arg("entity2Form.enabled", "false")
    arg("entity2Mcp.enabled", "false")
    arg("formPackageName", "site.addzero.kcloud.plugins.mcuconsole.generated.forms")
    arg("enumOutputPackage", "site.addzero.kcloud.plugins.mcuconsole.generated.enums")
    arg("iso2DataProviderPackage", "site.addzero.kcloud.plugins.mcuconsole.generated.forms.dataprovider")
    arg("mcpPackageName", "site.addzero.kcloud.plugins.mcuconsole.generated.mcp")
}

modbusRtu {
    codegenModes.set(listOf("server"))
    contractPackages.set(
        listOf(
            "site.addzero.kcloud.plugins.mcuconsole.modbus.device",
        ),
    )
    metadataProviders.set(
        if (useCodegenContextMetadata) {
            listOf("database")
        } else {
            listOf("interfaces")
        },
    )
    if (useCodegenContextMetadata) {
        databaseDriverClass.set("org.sqlite.JDBC")
        databaseJdbcUrl.set("jdbc:sqlite:${generatedMetadataDbFile.absolutePath}")
        databaseQuery.set(
            """
            SELECT payload
            FROM codegen_context_modbus_contract
            WHERE consumer_target = 'MCU_CONSOLE'
              AND enabled = 1
              AND selected = 1
              AND transport = '${'$'}{transport}'
            ORDER BY updated_at DESC
            LIMIT 1
            """.trimIndent(),
        )
        databaseJsonColumn.set("payload")
    }
}

dependencies {
    add("kspJvm", libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
}

kotlin {
    sourceSets {
        jvmMain {
            kotlin.srcDir(generatedContractSourceDir)
            dependencies {
                implementation(project(":apps:kcloud:plugins:mcu-console:shared"))
                implementation(libs.findLibrary("modbus-runtime").get())
                implementation(libs.findLibrary("tool-stm32-bootloader").get())
                implementation(libs.findLibrary("tool-serial").get())
                implementation(project(":lib:ktor:plugin:ktor-jimmer-plugin"))
                implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-datetime").get())
            }
        }
    }
}
