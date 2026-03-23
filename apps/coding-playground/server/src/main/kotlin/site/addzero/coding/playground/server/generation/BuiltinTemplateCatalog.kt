package site.addzero.coding.playground.server.generation

import org.koin.core.annotation.Single
import site.addzero.coding.playground.shared.dto.CreateTemplateMetaRequest
import site.addzero.coding.playground.shared.dto.TemplateOutputKind

enum class BuiltinTemplateScope {
    CONTEXT,
    ENTITY,
}

data class BuiltinTemplateSpec(
    val key: String,
    val name: String,
    val scope: BuiltinTemplateScope,
    val outputKind: TemplateOutputKind,
    val relativeOutputPath: String,
    val fileNameTemplate: String,
    val body: String = "",
    val description: String? = null,
)

@Single
class BuiltinTemplateCatalog {
    val templates: List<BuiltinTemplateSpec> = listOf(
        BuiltinTemplateSpec(
            key = "descriptor-runtime",
            name = "Descriptor Runtime",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/spi/src/commonMain/kotlin/site/addzero/coding/playground/shared/descriptor",
            fileNameTemplate = "GeneratedMetadataDescriptors.kt",
            body = "builtin:descriptor-runtime",
        ),
        BuiltinTemplateSpec(
            key = "jimmer-entity",
            name = "Jimmer Entity",
            scope = BuiltinTemplateScope.ENTITY,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/server/src/main/kotlin/{{packagePath}}/model",
            fileNameTemplate = "{{EntityName}}.kt",
            body = "builtin:jimmer-entity",
        ),
        BuiltinTemplateSpec(
            key = "crud-dtos",
            name = "CRUD DTOs",
            scope = BuiltinTemplateScope.ENTITY,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/spi/src/commonMain/kotlin/{{packagePath}}/dto",
            fileNameTemplate = "{{EntityName}}Dtos.kt",
            body = "builtin:crud-dtos",
        ),
        BuiltinTemplateSpec(
            key = "repository-skeleton",
            name = "Repository Skeleton",
            scope = BuiltinTemplateScope.ENTITY,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/server/src/main/kotlin/{{packagePath}}/service",
            fileNameTemplate = "{{EntityName}}Repository.kt",
            body = "builtin:repository-skeleton",
        ),
        BuiltinTemplateSpec(
            key = "service-skeleton",
            name = "Service Skeleton",
            scope = BuiltinTemplateScope.ENTITY,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/server/src/main/kotlin/{{packagePath}}/service",
            fileNameTemplate = "{{EntityName}}Service.kt",
            body = "builtin:service-skeleton",
        ),
        BuiltinTemplateSpec(
            key = "route-skeleton",
            name = "Route Skeleton",
            scope = BuiltinTemplateScope.ENTITY,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/server/src/main/kotlin/{{packagePath}}/route",
            fileNameTemplate = "{{EntityName}}Routes.kt",
            body = "builtin:route-skeleton",
        ),
        BuiltinTemplateSpec(
            key = "client-api",
            name = "Client API",
            scope = BuiltinTemplateScope.ENTITY,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/client/src/commonMain/kotlin/{{packagePath}}/client/api",
            fileNameTemplate = "{{EntityName}}Api.kt",
            body = "builtin:client-api",
        ),
        BuiltinTemplateSpec(
            key = "client-state",
            name = "Client State",
            scope = BuiltinTemplateScope.ENTITY,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/client/src/commonMain/kotlin/{{packagePath}}/client/state",
            fileNameTemplate = "{{EntityName}}CrudState.kt",
            body = "builtin:client-state",
        ),
        BuiltinTemplateSpec(
            key = "client-screen",
            name = "Client Screen",
            scope = BuiltinTemplateScope.ENTITY,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/client/src/commonMain/kotlin/{{packagePath}}/client/ui",
            fileNameTemplate = "{{EntityName}}CrudScreen.kt",
            body = "builtin:client-screen",
        ),
        BuiltinTemplateSpec(
            key = "koin-module",
            name = "Server Koin Module",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/server/src/main/kotlin/{{packagePath}}/di",
            fileNameTemplate = "{{ContextName}}ServerModule.kt",
            body = "builtin:koin-module",
        ),
        BuiltinTemplateSpec(
            key = "client-koin-module",
            name = "Client Koin Module",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/client/src/commonMain/kotlin/{{packagePath}}/client/di",
            fileNameTemplate = "{{ContextName}}ClientModule.kt",
            body = "builtin:client-koin-module",
        ),
        BuiltinTemplateSpec(
            key = "client-workbench",
            name = "Client Workbench",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/client/src/commonMain/kotlin/{{packagePath}}/client/ui",
            fileNameTemplate = "{{ContextName}}ClientWorkbench.kt",
            body = "builtin:client-workbench",
        ),
        BuiltinTemplateSpec(
            key = "metadata-object",
            name = "Metadata Object",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/spi/src/commonMain/kotlin/{{packagePath}}/metadata",
            fileNameTemplate = "{{ContextName}}Metadata.kt",
            body = "builtin:metadata-object",
        ),
        BuiltinTemplateSpec(
            key = "metadata-index",
            name = "Metadata Index",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.KOTLIN_SOURCE,
            relativeOutputPath = "plugins/{{contextCode}}/spi/src/commonMain/kotlin/{{packagePath}}/metadata",
            fileNameTemplate = "GeneratedMetadataIndex.kt",
            body = "builtin:metadata-index",
        ),
        BuiltinTemplateSpec(
            key = "plugin-root-gradle",
            name = "Plugin Root Gradle",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.GRADLE_KTS,
            relativeOutputPath = "plugins/{{contextCode}}",
            fileNameTemplate = "build.gradle.kts",
            body = "builtin:plugin-root-gradle",
        ),
        BuiltinTemplateSpec(
            key = "plugin-settings-gradle",
            name = "Plugin Settings Gradle",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.GRADLE_KTS,
            relativeOutputPath = "plugins/{{contextCode}}",
            fileNameTemplate = "settings.gradle.kts",
            body = "builtin:plugin-settings-gradle",
        ),
        BuiltinTemplateSpec(
            key = "plugin-server-gradle",
            name = "Plugin Server Gradle",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.GRADLE_KTS,
            relativeOutputPath = "plugins/{{contextCode}}/server",
            fileNameTemplate = "build.gradle.kts",
            body = "builtin:plugin-server-gradle",
        ),
        BuiltinTemplateSpec(
            key = "plugin-client-gradle",
            name = "Plugin Client Gradle",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.GRADLE_KTS,
            relativeOutputPath = "plugins/{{contextCode}}/client",
            fileNameTemplate = "build.gradle.kts",
            body = "builtin:plugin-client-gradle",
        ),
        BuiltinTemplateSpec(
            key = "plugin-spi-gradle",
            name = "Plugin SPI Gradle",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.GRADLE_KTS,
            relativeOutputPath = "plugins/{{contextCode}}/spi",
            fileNameTemplate = "build.gradle.kts",
            body = "builtin:plugin-spi-gradle",
        ),
        BuiltinTemplateSpec(
            key = "settings-snippet",
            name = "Settings Fragment",
            scope = BuiltinTemplateScope.CONTEXT,
            outputKind = TemplateOutputKind.SETTINGS_SNIPPET,
            relativeOutputPath = "",
            fileNameTemplate = "coding-playground.settings.fragment.kts",
            body = "builtin:settings-snippet",
        ),
    )

    fun createRequests(contextId: String): List<CreateTemplateMetaRequest> {
        return templates.map { template ->
            CreateTemplateMetaRequest(
                contextId = contextId,
                name = template.name,
                key = template.key,
                description = template.description,
                outputKind = template.outputKind,
                body = template.body,
                relativeOutputPath = template.relativeOutputPath,
                fileNameTemplate = template.fileNameTemplate,
                tags = listOf("builtin", template.scope.name.lowercase()),
                enabled = true,
                managedByGenerator = true,
            )
        }
    }

    fun scopeOf(key: String): BuiltinTemplateScope? {
        return templates.firstOrNull { it.key == key }?.scope
    }
}
