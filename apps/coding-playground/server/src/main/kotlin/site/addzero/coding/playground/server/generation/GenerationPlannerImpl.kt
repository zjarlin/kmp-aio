package site.addzero.coding.playground.server.generation

import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.entity.toDto
import site.addzero.coding.playground.server.service.MetadataPersistenceSupport
import site.addzero.coding.playground.server.util.renderTemplateString
import site.addzero.coding.playground.server.util.toPascalIdentifier
import site.addzero.coding.playground.shared.dto.ContextAggregateDto
import site.addzero.coding.playground.shared.dto.GeneratedFileDto
import site.addzero.coding.playground.shared.dto.GenerationPlanDto
import site.addzero.coding.playground.shared.dto.GenerationPlanFileDto
import site.addzero.coding.playground.shared.dto.GenerationRequestDto
import site.addzero.coding.playground.shared.dto.GenerationResultDto
import site.addzero.coding.playground.shared.dto.GenerationScaffoldMode
import site.addzero.coding.playground.shared.service.CompositeBuildIntegrator
import site.addzero.coding.playground.shared.service.EtlWrapperExecutor
import site.addzero.coding.playground.shared.service.GenerationPlanner
import site.addzero.coding.playground.shared.service.PathVariableResolver
import site.addzero.coding.playground.shared.service.TemplateRenderer
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Single
class GenerationPlannerImpl(
    private val support: MetadataPersistenceSupport,
    private val pathVariableResolver: PathVariableResolver,
    private val templateRenderer: TemplateRenderer,
    private val etlWrapperExecutor: EtlWrapperExecutor,
    private val compositeBuildIntegrator: CompositeBuildIntegrator,
    private val builtinTemplateCatalog: BuiltinTemplateCatalog,
    private val scaffoldBootstrapper: KcloudStyleScaffoldBootstrapper,
) : GenerationPlanner {
    override suspend fun plan(request: GenerationRequestDto): GenerationPlanDto {
        val target = support.targetOrThrow(request.targetId).toDto(support.json, support.templateIdsForTarget(request.targetId))
        if (target.contextId != request.contextId) {
            error("Generation request context does not match target context")
        }
        val context = support.buildContextAggregate(request.contextId)
        val resolvedOutputRoot = pathVariableResolver.resolve(
            target.outputRoot,
            target.variables + builtins(context, target.outputRoot),
        )
        val scaffoldMode = scaffoldBootstrapper.detectMode(Paths.get(resolvedOutputRoot))
        val selectedTemplateIds = when {
            request.templateIds.isNotEmpty() -> request.templateIds
            target.templateIds.isNotEmpty() -> target.templateIds
            else -> context.templates.filter { it.enabled }.map { it.id }
        }.toSet()
        val templates = context.templates
            .filter { it.enabled && (selectedTemplateIds.isEmpty() || it.id in selectedTemplateIds) }
            .sortedBy { it.orderIndex }
        val files = mutableListOf<GenerationPlanFileDto>()
        templates.forEach { template ->
            when (builtinTemplateCatalog.scopeOf(template.key)) {
                BuiltinTemplateScope.ENTITY -> {
                    context.entities.forEach { entity ->
                        val variables = fileVariables(context, target.packageName, entity.code, resolvedOutputRoot)
                        files += GenerationPlanFileDto(
                            templateId = template.id,
                            templateKey = template.key,
                            entityCode = entity.code,
                            relativePath = renderTemplateString(template.relativeOutputPath, variables),
                            fileName = renderTemplateString(template.fileNameTemplate, variables),
                            outputKind = template.outputKind,
                            description = "${template.name} for ${entity.name}",
                        )
                    }
                }
                else -> {
                    val variables = fileVariables(context, target.packageName, null, resolvedOutputRoot)
                    files += GenerationPlanFileDto(
                        templateId = template.id,
                        templateKey = template.key,
                        entityCode = null,
                        relativePath = renderTemplateString(template.relativeOutputPath, variables),
                        fileName = renderTemplateString(template.fileNameTemplate, variables),
                        outputKind = template.outputKind,
                        description = template.name,
                    )
                }
            }
        }
        return GenerationPlanDto(
            request = request,
            target = target,
            context = context,
            scaffoldMode = scaffoldMode,
            files = files,
        )
    }

    override suspend fun generate(request: GenerationRequestDto): GenerationResultDto {
        val plan = plan(request)
        if (request.previewOnly) {
            return GenerationResultDto(plan = plan, files = emptyList())
        }
        val outputRoot = pathVariableResolver.resolve(
            plan.target.outputRoot,
            plan.target.variables + builtins(plan.context, plan.target.outputRoot),
        )
        val outputRootPath = Paths.get(outputRoot).toAbsolutePath().normalize()
        val selectedTemplates = plan.context.templates.associateBy { it.id }
        val generatedFiles = mutableListOf<GeneratedFileDto>()
        generatedFiles += scaffoldBootstrapper.prepareRootScaffold(outputRootPath, plan.target, plan.context)

        plan.files.forEach { filePlan ->
            val template = selectedTemplates.getValue(filePlan.templateId)
            val variables = fileVariables(plan.context, plan.target.packageName, filePlan.entityCode, outputRoot) + plan.target.variables
            var rendered = templateRenderer.render(template, plan.context, plan.target, variables)
            val wrapperId = template.etlWrapperId
            if (plan.target.enableEtl && wrapperId != null) {
                val wrapper = support.etlWrapperOrThrow(wrapperId).toDto()
                rendered = etlWrapperExecutor.apply(wrapper, rendered, template, plan.target, variables)
            }
            val absolutePath = outputRootPath
                .resolve(rendered.relativePath)
                .resolve(rendered.fileName)
                .normalize()
            absolutePath.parent?.createDirectories()
            absolutePath.writeText(rendered.content)
            generatedFiles += GeneratedFileDto(
                absolutePath = absolutePath.toString(),
                relativePath = outputRootPath.relativize(absolutePath).toString(),
                templateKey = rendered.templateKey,
                content = rendered.content,
                etlApplied = plan.target.enableEtl && wrapperId != null,
            )
        }
        val integrations = if (plan.target.autoIntegrateCompositeBuild) {
            listOf(
                compositeBuildIntegrator.integrate(
                    targetRoot = outputRoot,
                    includeBuildPath = "plugins/${plan.context.context.code}",
                    marker = plan.target.managedMarker,
                ),
            )
        } else {
            emptyList()
        }
        return GenerationResultDto(
            plan = plan,
            files = generatedFiles,
            integrations = integrations,
        )
    }

    private fun fileVariables(
        context: ContextAggregateDto,
        packageName: String,
        entityCode: String?,
        outputRoot: String,
    ): Map<String, String> {
        val contextName = context.context.name.toPascalIdentifier()
        val contextCode = context.context.code
        val packagePath = packageName.replace('.', '/')
        val entity = entityCode?.let { code -> context.entities.firstOrNull { it.code == code } }
        val entityName = entity?.name?.toPascalIdentifier()
        return buildMap {
            put("packageName", packageName)
            put("packagePath", packagePath)
            put("contextCode", contextCode)
            put("contextName", context.context.name)
            put("ContextName", contextName)
            put("targetRoot", outputRoot)
            if (entity != null && entityName != null) {
                put("EntityName", entityName)
                put("entityName", entityName.replaceFirstChar(Char::lowercaseChar))
                put("entityCode", entity.code)
                put("entityPath", entity.code.lowercase() + "s")
            } else {
                put("entityPath", contextCode.lowercase())
            }
        }
    }

    private fun builtins(context: ContextAggregateDto, outputRoot: String): Map<String, String> {
        return mapOf(
            "TARGET_ROOT" to outputRoot,
            "CONTEXT_CODE" to context.context.code,
            "HOME" to System.getProperty("user.home"),
            "PLAYGROUND_REPO_ROOT" to locateRepoRoot().toString(),
            "BUILD_LOGIC_PATH" to locateRepoRoot().resolve("checkouts/build-logic").toString(),
        )
    }

    private fun locateRepoRoot(): Path {
        var current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize()
        repeat(10) {
            if (current.resolve("settings.gradle.kts").toFile().exists() && current.resolve("apps").toFile().exists()) {
                return current
            }
            current = current.parent ?: return@repeat
        }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize()
    }
}
