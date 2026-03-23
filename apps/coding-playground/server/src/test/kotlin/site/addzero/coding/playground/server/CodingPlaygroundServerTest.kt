package site.addzero.coding.playground.server

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.dialect.SQLiteDialect
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import org.opentest4j.TestAbortedException
import org.sqlite.SQLiteDataSource
import site.addzero.coding.playground.server.config.PlaygroundJdbcTransactionContext
import site.addzero.coding.playground.server.config.SqliteLocalDateTimeScalarProvider
import site.addzero.coding.playground.server.config.initDatabase
import site.addzero.coding.playground.server.domain.PlaygroundValidationException
import site.addzero.coding.playground.server.generation.BuiltinTemplateCatalog
import site.addzero.coding.playground.server.generation.CompositeBuildIntegratorImpl
import site.addzero.coding.playground.server.generation.EtlWrapperExecutorImpl
import site.addzero.coding.playground.server.generation.GenerationPlannerImpl
import site.addzero.coding.playground.server.generation.KcloudStyleScaffoldBootstrapper
import site.addzero.coding.playground.server.generation.MetadataIrCompiler
import site.addzero.coding.playground.server.generation.TemplateRendererImpl
import site.addzero.coding.playground.server.service.ContextMetaServiceImpl
import site.addzero.coding.playground.server.service.DtoMetaServiceImpl
import site.addzero.coding.playground.server.service.EntityMetaServiceImpl
import site.addzero.coding.playground.server.service.EtlWrapperMetaServiceImpl
import site.addzero.coding.playground.server.service.GenerationTargetMetaServiceImpl
import site.addzero.coding.playground.server.service.MetadataPersistenceSupport
import site.addzero.coding.playground.server.service.MetadataSnapshotServiceImpl
import site.addzero.coding.playground.server.service.PathVariableResolverImpl
import site.addzero.coding.playground.server.service.ProjectMetaServiceImpl
import site.addzero.coding.playground.server.service.TemplateMetaServiceImpl
import site.addzero.coding.playground.shared.dto.CreateBoundedContextMetaRequest
import site.addzero.coding.playground.shared.dto.CreateDtoFieldMetaRequest
import site.addzero.coding.playground.shared.dto.CreateDtoMetaRequest
import site.addzero.coding.playground.shared.dto.CreateEntityMetaRequest
import site.addzero.coding.playground.shared.dto.CreateEtlWrapperMetaRequest
import site.addzero.coding.playground.shared.dto.CreateFieldMetaRequest
import site.addzero.coding.playground.shared.dto.CreateGenerationTargetMetaRequest
import site.addzero.coding.playground.shared.dto.CreateProjectMetaRequest
import site.addzero.coding.playground.shared.dto.CreateRelationMetaRequest
import site.addzero.coding.playground.shared.dto.CreateTemplateMetaRequest
import site.addzero.coding.playground.shared.dto.DtoKind
import site.addzero.coding.playground.shared.dto.FieldType
import site.addzero.coding.playground.shared.dto.GenerationRequestDto
import site.addzero.coding.playground.shared.dto.GenerationScaffoldMode
import site.addzero.coding.playground.shared.dto.MetadataSearchRequest
import site.addzero.coding.playground.shared.dto.RelationKind
import site.addzero.coding.playground.shared.dto.ReorderRequestDto
import site.addzero.coding.playground.shared.dto.TemplateOutputKind
import site.addzero.coding.playground.shared.dto.UpdateBoundedContextMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateDtoFieldMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateDtoMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateEntityMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateEtlWrapperMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateFieldMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateGenerationTargetMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateProjectMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateRelationMetaRequest
import site.addzero.coding.playground.shared.dto.UpdateTemplateMetaRequest
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.concurrent.TimeUnit
import javax.sql.DataSource
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.test.assertFailsWith

class CodingPlaygroundServerTest {
    @Test
    fun metadataCrudSearchAndReorderWorkAcrossAggregates() = withRuntime { runtime ->
        runBlocking {
            val project = runtime.projectService.create(
                CreateProjectMetaRequest(
                    name = "Playground Admin",
                    slug = "playground-admin",
                    description = "metadata studio",
                    tags = listOf("studio"),
                ),
            )
            assertEquals(1, runtime.projectService.list().size)

            val context = runtime.contextService.create(
                CreateBoundedContextMetaRequest(
                    projectId = project.id,
                    name = "Catalog",
                    code = "catalog",
                    description = "Catalog domain",
                    tags = listOf("core"),
                ),
            )
            assertTrue(runtime.templateService.list(MetadataSearchRequest(contextId = context.id)).isNotEmpty())

            val userEntity = runtime.entityService.create(
                CreateEntityMetaRequest(
                    contextId = context.id,
                    name = "User",
                    code = "user",
                    tableName = "users",
                    description = "User aggregate",
                    tags = listOf("account"),
                ),
            )
            val userIdField = runtime.entityService.createField(
                CreateFieldMetaRequest(
                    entityId = userEntity.id,
                    name = "User Id",
                    code = "id",
                    type = FieldType.UUID,
                    idField = true,
                ),
            )
            val userNameField = runtime.entityService.createField(
                CreateFieldMetaRequest(
                    entityId = userEntity.id,
                    name = "Display Name",
                    code = "displayName",
                    type = FieldType.STRING,
                    searchable = true,
                ),
            )

            val orderEntity = runtime.entityService.create(
                CreateEntityMetaRequest(
                    contextId = context.id,
                    name = "Order",
                    code = "order",
                    tableName = "orders",
                ),
            )
            val relation = runtime.entityService.createRelation(
                CreateRelationMetaRequest(
                    contextId = context.id,
                    sourceEntityId = orderEntity.id,
                    targetEntityId = userEntity.id,
                    name = "Order Owner",
                    code = "owner",
                    kind = RelationKind.MANY_TO_ONE,
                    sourceFieldName = "owner",
                ),
            )

            val dto = runtime.dtoService.create(
                CreateDtoMetaRequest(
                    contextId = context.id,
                    entityId = userEntity.id,
                    name = "User Command",
                    code = "userCommand",
                    kind = DtoKind.REQUEST,
                ),
            )
            val dtoField = runtime.dtoService.createField(
                CreateDtoFieldMetaRequest(
                    dtoId = dto.id,
                    entityFieldId = userNameField.id,
                    name = "Display Name",
                    code = "displayName",
                    type = FieldType.STRING,
                ),
            )
            val dtoFieldExtra = runtime.dtoService.createField(
                CreateDtoFieldMetaRequest(
                    dtoId = dto.id,
                    name = "Nick Name",
                    code = "nickName",
                    type = FieldType.STRING,
                ),
            )

            val etl = runtime.etlWrapperService.create(
                CreateEtlWrapperMetaRequest(
                    projectId = project.id,
                    name = "Banner Wrapper",
                    key = "banner-wrapper",
                    scriptBody = "return \"/*\" + content + \"*/\"",
                ),
            )
            val customTemplate = runtime.templateService.create(
                CreateTemplateMetaRequest(
                    contextId = context.id,
                    etlWrapperId = etl.id,
                    name = "Readme Template",
                    key = "readme-template",
                    outputKind = TemplateOutputKind.MARKDOWN,
                    body = "# {{ContextName}}",
                    relativeOutputPath = "docs",
                    fileNameTemplate = "README.md",
                    tags = listOf("custom"),
                    managedByGenerator = false,
                ),
            )
            val secondTemplate = runtime.templateService.create(
                CreateTemplateMetaRequest(
                    contextId = context.id,
                    name = "Notes Template",
                    key = "notes-template",
                    outputKind = TemplateOutputKind.TEXT,
                    body = "notes",
                    relativeOutputPath = "docs",
                    fileNameTemplate = "notes.txt",
                    managedByGenerator = false,
                ),
            )

            val target = runtime.targetService.create(
                CreateGenerationTargetMetaRequest(
                    projectId = project.id,
                    contextId = context.id,
                    name = "Local Target",
                    key = "local-target",
                    outputRoot = runtime.root.resolve("generated").absolutePathString(),
                    packageName = "site.addzero.generated.catalog",
                    templateIds = listOf(customTemplate.id, secondTemplate.id),
                ),
            )

            val updatedProject = runtime.projectService.update(
                project.id,
                UpdateProjectMetaRequest(
                    name = "Playground Console",
                    slug = "playground-console",
                    description = "updated",
                    tags = listOf("studio", "updated"),
                ),
            )
            val updatedContext = runtime.contextService.update(
                context.id,
                UpdateBoundedContextMetaRequest(
                    name = "Catalog Core",
                    code = "catalog",
                    description = "core domain",
                    tags = listOf("core", "modeling"),
                ),
            )
            val updatedEntity = runtime.entityService.update(
                userEntity.id,
                UpdateEntityMetaRequest(
                    name = "Customer",
                    code = "customer",
                    tableName = "customers",
                    description = "customer aggregate",
                ),
            )
            val updatedField = runtime.entityService.updateField(
                userNameField.id,
                UpdateFieldMetaRequest(
                    name = "Full Name",
                    code = "fullName",
                    type = FieldType.STRING,
                    searchable = true,
                    keyField = true,
                ),
            )
            val updatedRelation = runtime.entityService.updateRelation(
                relation.id,
                UpdateRelationMetaRequest(
                    name = "Customer Owner",
                    code = "customerOwner",
                    kind = RelationKind.MANY_TO_ONE,
                    sourceFieldName = "customerOwner",
                ),
            )
            val updatedDto = runtime.dtoService.update(
                dto.id,
                UpdateDtoMetaRequest(
                    entityId = userEntity.id,
                    name = "Customer Command",
                    code = "customerCommand",
                    kind = DtoKind.REQUEST,
                ),
            )
            val updatedDtoField = runtime.dtoService.updateField(
                dtoField.id,
                UpdateDtoFieldMetaRequest(
                    entityFieldId = updatedField.id,
                    name = "Full Name",
                    code = "fullName",
                    type = FieldType.STRING,
                    sourcePath = "entity.fullName",
                ),
            )
            val updatedEtl = runtime.etlWrapperService.update(
                etl.id,
                UpdateEtlWrapperMetaRequest(
                    name = "Comment Wrapper",
                    key = "comment-wrapper",
                    scriptBody = "return \"// \" + content",
                ),
            )
            val updatedTemplate = runtime.templateService.update(
                customTemplate.id,
                UpdateTemplateMetaRequest(
                    etlWrapperId = updatedEtl.id,
                    name = "Catalog Readme",
                    key = "catalog-readme",
                    outputKind = TemplateOutputKind.MARKDOWN,
                    body = "# Catalog",
                    relativeOutputPath = "docs/catalog",
                    fileNameTemplate = "README.catalog.md",
                    managedByGenerator = false,
                ),
            )
            val updatedTarget = runtime.targetService.update(
                target.id,
                UpdateGenerationTargetMetaRequest(
                    name = "Target A",
                    key = "target-a",
                    outputRoot = runtime.root.resolve("generated-two").absolutePathString(),
                    packageName = "site.addzero.generated.catalog.v2",
                    templateIds = listOf(updatedTemplate.id, secondTemplate.id),
                ),
            )

            assertEquals("Playground Console", updatedProject.name)
            assertEquals("Catalog Core", updatedContext.name)
            assertEquals("Customer", updatedEntity.name)
            assertEquals("fullName", updatedField.code)
            assertEquals("customerOwner", updatedRelation.code)
            assertEquals("Customer Command", updatedDto.name)
            assertEquals("entity.fullName", updatedDtoField.sourcePath)
            assertEquals("comment-wrapper", updatedEtl.key)
            assertEquals("catalog-readme", updatedTemplate.key)
            assertEquals("target-a", updatedTarget.key)

            val reorderedFields = runtime.entityService.reorderFields(
                userEntity.id,
                ReorderRequestDto(listOf(updatedField.id, userIdField.id)),
            )
            assertEquals(updatedField.id, reorderedFields.first().id)

            val reorderedDtoFields = runtime.dtoService.reorderFields(
                dto.id,
                ReorderRequestDto(listOf(dtoFieldExtra.id, updatedDtoField.id)),
            )
            assertEquals(dtoFieldExtra.id, reorderedDtoFields.first().id)

            val reorderedTemplates = runtime.templateService.reorder(
                context.id,
                ReorderRequestDto(listOf(secondTemplate.id, updatedTemplate.id)),
            )
            assertEquals(secondTemplate.id, reorderedTemplates.first { it.id == secondTemplate.id }.id)

            val entitySearch = runtime.entityService.list(
                MetadataSearchRequest(
                    contextId = context.id,
                    query = "cust",
                ),
            )
            assertTrue(entitySearch.any { it.id == userEntity.id })

            val projectTree = runtime.projectService.tree(project.id)
            assertEquals(2, projectTree.contexts.first().entities.size)
            assertTrue(projectTree.etlWrappers.any { it.id == updatedEtl.id })
        }
    }

    @Test
    fun deleteGuardsAndCascadeRulesAreApplied() = withRuntime { runtime ->
        runBlocking {
            val fixture = runtime.seedFixture()

            val entityCheck = runtime.entityService.deleteCheck(fixture.userEntityId)
            assertFalse(entityCheck.allowed)
            assertTrue(entityCheck.reasons.any { it.contains("relation", ignoreCase = true) })
            assertFailsWith<PlaygroundValidationException> {
                runtime.entityService.delete(fixture.userEntityId)
            }

            val builtinTemplateId = runtime.templateService
                .list(MetadataSearchRequest(contextId = fixture.contextId))
                .first { it.key == "jimmer-entity" }
                .id
            val templateCheck = runtime.templateService.deleteCheck(builtinTemplateId)
            assertFalse(templateCheck.allowed)
            assertFailsWith<PlaygroundValidationException> {
                runtime.templateService.delete(builtinTemplateId)
            }

            runtime.targetService.delete(fixture.targetId)
            runtime.dtoService.deleteField(fixture.dtoFieldId)
            runtime.dtoService.delete(fixture.dtoId)
            runtime.entityService.deleteRelation(fixture.relationId)
            runtime.entityService.deleteField(fixture.userNameFieldId)
            runtime.entityService.deleteField(fixture.userIdFieldId)
            runtime.entityService.delete(fixture.userEntityId)
            runtime.entityService.deleteField(fixture.orderIdFieldId)
            runtime.entityService.delete(fixture.orderEntityId)
            runtime.contextService.delete(fixture.contextId)
            runtime.projectService.delete(fixture.projectId)

            assertTrue(runtime.projectService.list().isEmpty())
        }
    }

    @Test
    fun snapshotRoundTripPreservesMetadataGraph() = withRuntime { source ->
        runBlocking {
            val fixture = source.seedFixture()
            val snapshot = source.snapshotService.exportProject(fixture.projectId)
            assertTrue(snapshot.entities.isNotEmpty())

            withRuntime { target ->
                runBlocking {
                    val result = target.snapshotService.importSnapshot(snapshot)
                    assertTrue(result.createdIds.isNotEmpty())

                    val importedTree = target.projectService.tree(fixture.projectId)
                    assertEquals(snapshot.projects.single().name, importedTree.project.name)
                    assertEquals(snapshot.contexts.single().code, importedTree.contexts.single().context.code)
                    assertEquals(snapshot.entities.size, importedTree.contexts.single().entities.size)
                    assertEquals(snapshot.templates.size, importedTree.contexts.single().templates.size)
                    assertEquals(snapshot.generationTargets.size, importedTree.contexts.single().generationTargets.size)
                }
            }
        }
    }

    @Test
    fun resolverEtlAndCompositeIntegrationBehaveDeterministically() = withRuntime { runtime ->
        val previous = System.getProperty("PLAYGROUND_TEST_OVERRIDE")
        try {
            System.setProperty("PLAYGROUND_TEST_OVERRIDE", "system-value")
            val resolved = runtime.pathResolver.resolve(
                rawPath = "\${PLAYGROUND_TEST_OVERRIDE}/\$HOME/demo",
                variables = mapOf("PLAYGROUND_TEST_OVERRIDE" to "custom-value"),
            )
            assertContains(resolved, "custom-value")
            assertContains(resolved, System.getProperty("user.home"))

            val relative = runtime.pathResolver.resolve("docs/output", emptyMap())
            assertTrue(relative.endsWith("docs/output"))

            assertFailsWith<IllegalArgumentException> {
                runtime.pathResolver.resolve("\${MISSING_VAR}/broken", emptyMap())
            }

            runBlocking {
                val rendered = runtime.etlExecutor.apply(
                    wrapper = runtime.etlWrapperService.create(
                        CreateEtlWrapperMetaRequest(
                            projectId = runtime.projectService.create(CreateProjectMetaRequest("Tmp", "tmp")).id,
                            name = "Wrap",
                            key = "wrap",
                            scriptBody = "return \"/*\" + content + \"*/\"",
                        ),
                    ),
                    rendered = site.addzero.coding.playground.shared.dto.RenderedTemplateDto(
                        templateId = "t",
                        templateKey = "k",
                        relativePath = "docs",
                        fileName = "readme.md",
                        content = "payload",
                    ),
                    template = null,
                    target = null,
                    variables = emptyMap(),
                )
                assertEquals("/*payload*/", rendered.content)
            }

            runBlocking {
                val brokenWrapper = runtime.etlWrapperService.create(
                    CreateEtlWrapperMetaRequest(
                        projectId = runtime.projectService.create(CreateProjectMetaRequest("Tmp Broken", "tmp-broken")).id,
                        name = "Broken Wrap",
                        key = "broken-wrap",
                        scriptBody = "return variables.getValue(\"missing\")",
                    ),
                )
                assertFailsWith<IllegalStateException> {
                    runtime.etlExecutor.apply(
                        wrapper = brokenWrapper,
                        rendered = site.addzero.coding.playground.shared.dto.RenderedTemplateDto(
                            templateId = "t",
                            templateKey = "k",
                            relativePath = "docs",
                            fileName = "broken.md",
                            content = "payload",
                        ),
                        template = null,
                        target = null,
                        variables = emptyMap(),
                    )
                }
            }

            runBlocking {
                val targetRoot = runtime.root.resolve("integration-root").absolutePathString()
                val first = runtime.compositeIntegrator.integrate(targetRoot, "plugins/catalog", "TEST_MARKER")
                val second = runtime.compositeIntegrator.integrate(targetRoot, "plugins/catalog", "TEST_MARKER")
                val content = runtime.root.resolve("integration-root/settings.gradle.kts").readText()
                assertTrue(first.changed)
                assertFalse(second.changed)
                assertEquals(1, Regex("includeBuild\\(\"plugins/catalog\"\\)").findAll(content).count())
            }
        } finally {
            if (previous == null) {
                System.clearProperty("PLAYGROUND_TEST_OVERRIDE")
            } else {
                System.setProperty("PLAYGROUND_TEST_OVERRIDE", previous)
            }
        }
    }

    @Test
    fun targetAndEtlDeleteCheckValidateAndConflictPathsAreReadable() = withRuntime { runtime ->
        runBlocking {
            val fixture = runtime.seedFixture()
            val targetValidation = runtime.targetService.validate(fixture.targetId)
            assertTrue(targetValidation.isEmpty())
            assertTrue(runtime.targetService.deleteCheck(fixture.targetId).allowed)

            val project = runtime.projectService.get(fixture.projectId)
            val etl = runtime.etlWrapperService.create(
                CreateEtlWrapperMetaRequest(
                    projectId = fixture.projectId,
                    name = "Audit ETL",
                    key = "audit-etl",
                    scriptBody = "return content + \"\\n// audited\"",
                ),
            )
            val template = runtime.templateService.create(
                CreateTemplateMetaRequest(
                    contextId = fixture.contextId,
                    etlWrapperId = etl.id,
                    name = "Audit Template",
                    key = "audit-template",
                    outputKind = TemplateOutputKind.TEXT,
                    body = "audit",
                    relativeOutputPath = "docs",
                    fileNameTemplate = "audit.txt",
                    managedByGenerator = false,
                ),
            )

            assertTrue(runtime.etlWrapperService.validate(etl.id).isEmpty())
            val etlDeleteCheck = runtime.etlWrapperService.deleteCheck(etl.id)
            assertFalse(etlDeleteCheck.allowed)
            assertTrue(etlDeleteCheck.reasons.any { it.contains("template", ignoreCase = true) })

            runtime.templateService.delete(template.id)
            assertTrue(runtime.etlWrapperService.deleteCheck(etl.id).allowed)

            val conflictRoot = runtime.root.resolve("integration-conflict")
            conflictRoot.createDirectories()
            conflictRoot.resolve("settings.gradle.kts").writeText(
                """
                rootProject.name = "${project.slug}"

                includeBuild("plugins/catalog")
                """.trimIndent() + "\n",
            )
            val error = assertFailsWith<IllegalStateException> {
                runtime.compositeIntegrator.integrate(
                    targetRoot = conflictRoot.absolutePathString(),
                    includeBuildPath = "plugins/catalog",
                    marker = "TEST_MARKER",
                )
            }
            assertContains(error.message.orEmpty(), "outside marker")
        }
    }

    @Test
    fun generationProducesCrudSkeletonAndGeneratedModulesCompile() = withRuntime { runtime ->
        runBlocking {
            val fixture = runtime.seedFixture()
            val result = runtime.generationPlanner.generate(
                GenerationRequestDto(
                    targetId = fixture.targetId,
                    contextId = fixture.contextId,
                ),
            )

            assertEquals(GenerationScaffoldMode.NEW_ROOT, result.plan.scaffoldMode)
            val fileNames = result.files.map { Paths.get(it.absolutePath).fileName.toString() }.toSet()
            assertContains(fileNames, "User.kt")
            assertContains(fileNames, "UserDtos.kt")
            assertContains(fileNames, "UserRepository.kt")
            assertContains(fileNames, "UserService.kt")
            assertContains(fileNames, "UserRoutes.kt")
            assertContains(fileNames, "UserApi.kt")
            assertContains(fileNames, "UserCrudState.kt")
            assertContains(fileNames, "UserCrudScreen.kt")
            assertContains(fileNames, "CatalogClientWorkbench.kt")
            assertContains(fileNames, "CatalogMetadata.kt")
            assertContains(fileNames, "GeneratedMetadataIndex.kt")
            assertContains(fileNames, "settings.gradle.kts")

            val routeFile = result.files.first { it.templateKey == "route-skeleton" }
            assertContains(routeFile.content, "suspend fun listUsers")
            assertContains(routeFile.content, "suspend fun createUser")
            val metadataFile = result.files.first { it.templateKey == "metadata-object" }
            assertContains(metadataFile.content, "object CatalogMetadata")
            assertContains(metadataFile.content, "fun findModel")
            val clientWorkbenchFile = result.files.first { it.templateKey == "client-workbench" }
            assertContains(clientWorkbenchFile.content, "object CatalogClientFeature")
            assertContains(clientWorkbenchFile.content, "UserCrudScreen")

            val targetSettings = runtime.root.resolve("generated-target/settings.gradle.kts").readText()
            assertContains(targetSettings, "CODING_PLAYGROUND")
            assertContains(targetSettings, "includeBuild(\"plugins/catalog\")")

            val pluginRoot = runtime.root.resolve("generated-target/plugins/catalog")
            runtime.prepareStandaloneGeneratedPluginBuild(pluginRoot)
            try {
                runtime.runGradle(
                    workingDir = pluginRoot,
                    arguments = listOf(
                        "--no-daemon",
                        "--offline",
                        "--no-configuration-cache",
                        "-Dorg.gradle.caching=false",
                        ":server:compileKotlin",
                        ":client:compileCommonMainKotlinMetadata",
                        ":spi:compileCommonMainKotlinMetadata",
                    ),
                )
            } catch (ex: NestedGradleBuildException) {
                val message = ex.message.orEmpty()
                if (
                    message.contains("Remote host terminated the handshake", ignoreCase = true) ||
                    message.contains("No cached version", ignoreCase = true) ||
                    message.contains("Could not resolve all artifacts", ignoreCase = true)
                ) {
                    throw TestAbortedException("Standalone Gradle smoke compile skipped because required external artifacts were unavailable in cache", ex)
                }
                throw ex
            }
        }
    }
}

private inline fun withRuntime(block: (PlaygroundTestRuntime) -> Unit) {
    PlaygroundTestRuntime.create().use(block)
}

private data class FixtureIds(
    val projectId: String,
    val contextId: String,
    val userEntityId: String,
    val userIdFieldId: String,
    val userNameFieldId: String,
    val orderEntityId: String,
    val orderIdFieldId: String,
    val relationId: String,
    val dtoId: String,
    val dtoFieldId: String,
    val targetId: String,
)

private class PlaygroundTestRuntime private constructor(
    val root: Path,
    private val dataSource: DataSource,
    val support: MetadataPersistenceSupport,
    val projectService: ProjectMetaServiceImpl,
    val contextService: ContextMetaServiceImpl,
    val entityService: EntityMetaServiceImpl,
    val dtoService: DtoMetaServiceImpl,
    val templateService: TemplateMetaServiceImpl,
    val targetService: GenerationTargetMetaServiceImpl,
    val etlWrapperService: EtlWrapperMetaServiceImpl,
    val snapshotService: MetadataSnapshotServiceImpl,
    val pathResolver: PathVariableResolverImpl,
    val etlExecutor: EtlWrapperExecutorImpl,
    val compositeIntegrator: CompositeBuildIntegratorImpl,
    val generationPlanner: GenerationPlannerImpl,
) : AutoCloseable {
    companion object {
        fun create(): PlaygroundTestRuntime {
            val root = Files.createTempDirectory("coding-playground-test")
            val dataSource = SQLiteDataSource().apply {
                url = "jdbc:sqlite:${root.resolve("playground.db").absolutePathString()}"
            }
            initDatabase(dataSource)
            val json = Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            }
            val sqlClient = newKSqlClient {
                setDialect(SQLiteDialect())
                setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.LOWER_CASE)
                addScalarProvider(SqliteLocalDateTimeScalarProvider)
                setConnectionManager {
                    val transactionalConnection = PlaygroundJdbcTransactionContext.connectionOrNull()
                    if (transactionalConnection != null) {
                        proceed(transactionalConnection)
                    } else {
                        dataSource.connection.use { proceed(it) }
                    }
                }
            }
            val catalog = BuiltinTemplateCatalog()
            val support = MetadataPersistenceSupport(
                sqlClient = sqlClient,
                dataSource = dataSource,
                json = json,
                builtinTemplateCatalog = catalog,
            )
            val pathResolver = PathVariableResolverImpl()
            val etlExecutor = EtlWrapperExecutorImpl()
            val compositeIntegrator = CompositeBuildIntegratorImpl()
            val irCompiler = MetadataIrCompiler()
            val scaffoldBootstrapper = KcloudStyleScaffoldBootstrapper()
            val generationPlanner = GenerationPlannerImpl(
                support = support,
                pathVariableResolver = pathResolver,
                templateRenderer = TemplateRendererImpl(catalog, irCompiler),
                etlWrapperExecutor = etlExecutor,
                compositeBuildIntegrator = compositeIntegrator,
                builtinTemplateCatalog = catalog,
                scaffoldBootstrapper = scaffoldBootstrapper,
            )
            return PlaygroundTestRuntime(
                root = root,
                dataSource = dataSource,
                support = support,
                projectService = ProjectMetaServiceImpl(support),
                contextService = ContextMetaServiceImpl(support),
                entityService = EntityMetaServiceImpl(support),
                dtoService = DtoMetaServiceImpl(support),
                templateService = TemplateMetaServiceImpl(support),
                targetService = GenerationTargetMetaServiceImpl(support),
                etlWrapperService = EtlWrapperMetaServiceImpl(support),
                snapshotService = MetadataSnapshotServiceImpl(support),
                pathResolver = pathResolver,
                etlExecutor = etlExecutor,
                compositeIntegrator = compositeIntegrator,
                generationPlanner = generationPlanner,
            )
        }
    }

    suspend fun seedFixture(): FixtureIds {
        val project = projectService.create(CreateProjectMetaRequest("Seed Project", "seed-project"))
        val context = contextService.create(
            CreateBoundedContextMetaRequest(
                projectId = project.id,
                name = "Catalog",
                code = "catalog",
            ),
        )
        val userEntity = entityService.create(
            CreateEntityMetaRequest(
                contextId = context.id,
                name = "User",
                code = "user",
                tableName = "users",
            ),
        )
        val userIdField = entityService.createField(
            CreateFieldMetaRequest(
                entityId = userEntity.id,
                name = "User Id",
                code = "id",
                type = FieldType.UUID,
                idField = true,
            ),
        )
        val userNameField = entityService.createField(
            CreateFieldMetaRequest(
                entityId = userEntity.id,
                name = "User Name",
                code = "name",
                type = FieldType.STRING,
            ),
        )
        val orderEntity = entityService.create(
            CreateEntityMetaRequest(
                contextId = context.id,
                name = "Order",
                code = "order",
                tableName = "orders",
            ),
        )
        val orderIdField = entityService.createField(
            CreateFieldMetaRequest(
                entityId = orderEntity.id,
                name = "Order Id",
                code = "id",
                type = FieldType.UUID,
                idField = true,
            ),
        )
        val relation = entityService.createRelation(
            CreateRelationMetaRequest(
                contextId = context.id,
                sourceEntityId = orderEntity.id,
                targetEntityId = userEntity.id,
                name = "Owner",
                code = "owner",
                kind = RelationKind.MANY_TO_ONE,
                sourceFieldName = "owner",
            ),
        )
        val dto = dtoService.create(
            CreateDtoMetaRequest(
                contextId = context.id,
                entityId = userEntity.id,
                name = "UserRequest",
                code = "userRequest",
                kind = DtoKind.REQUEST,
            ),
        )
        val dtoField = dtoService.createField(
            CreateDtoFieldMetaRequest(
                dtoId = dto.id,
                entityFieldId = userNameField.id,
                name = "User Name",
                code = "name",
                type = FieldType.STRING,
            ),
        )
        val target = targetService.create(
            CreateGenerationTargetMetaRequest(
                projectId = project.id,
                contextId = context.id,
                name = "Generated Target",
                key = "generated-target",
                outputRoot = root.resolve("generated-target").absolutePathString(),
                packageName = "site.addzero.generated.catalog",
                autoIntegrateCompositeBuild = true,
            ),
        )
        return FixtureIds(
            projectId = project.id,
            contextId = context.id,
            userEntityId = userEntity.id,
            userIdFieldId = userIdField.id,
            userNameFieldId = userNameField.id,
            orderEntityId = orderEntity.id,
            orderIdFieldId = orderIdField.id,
            relationId = relation.id,
            dtoId = dto.id,
            dtoFieldId = dtoField.id,
            targetId = target.id,
        )
    }

    fun prepareStandaloneGeneratedPluginBuild(pluginRoot: Path) {
        pluginRoot.createDirectories()
        pluginRoot.resolve("gradle").createDirectories()
        val versionCatalog = sequenceOf(
            locateRepoRoot().resolve("gradle/libs.versions.toml"),
            locateRepoRoot().resolve("checkouts/build-logic/gradle/libs.versions.toml"),
        ).firstOrNull { it.exists() } ?: error("Unable to locate libs.versions.toml for standalone generated build")
        Files.copy(
            versionCatalog,
            pluginRoot.resolve("gradle/libs.versions.toml"),
            REPLACE_EXISTING,
        )
        val existingSettings = pluginRoot.resolve("settings.gradle.kts").readText()
        pluginRoot.resolve("settings.gradle.kts").writeText(
            """
            pluginManagement {
                includeBuild("${normalizeForGradle(locateRepoRoot().resolve("checkouts/build-logic"))}")
                repositories {
                    gradlePluginPortal()
                    google()
                    mavenCentral()
                    maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
                    maven("https://mirrors.tencent.com/nexus/repository/gradle-plugins/")
                }
            }

            dependencyResolutionManagement {
                repositories {
                    google()
                    mavenCentral()
                    maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
                    maven("https://mirrors.tencent.com/nexus/repository/gradle-plugins/")
                }
            }

            $existingSettings
            """.trimIndent() + "\n",
        )
        val gradleProperties = locateRepoRoot().resolve("gradle.properties")
        if (gradleProperties.exists()) {
            Files.copy(
                gradleProperties,
                pluginRoot.resolve("gradle.properties"),
                REPLACE_EXISTING,
            )
        }
    }

    fun runGradle(workingDir: Path, arguments: List<String>) {
        val process = ProcessBuilder(listOf(locateRepoRoot().resolve("gradlew").absolutePathString()) + arguments)
            .directory(workingDir.toFile())
            .redirectErrorStream(true)
            .apply {
                environment()["JAVA_HOME"] = System.getProperty("java.home")
                environment()["PATH"] = "${System.getProperty("java.home")}/bin:${environment()["PATH"].orEmpty()}"
                environment()["GRADLE_USER_HOME"] = locateRepoRoot().resolve(".gradle-user-home").absolutePathString()
            }
            .start()
        val output = process.inputStream.bufferedReader().readText()
        if (!process.waitFor(180, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            fail("Nested Gradle build timed out for ${workingDir.absolutePathString()}")
        }
        if (process.exitValue() != 0) {
            throw NestedGradleBuildException(
                "Nested Gradle build failed with exit code ${process.exitValue()}\n$output",
            )
        }
    }

    override fun close() {
        runCatching { dataSource.connection.close() }
        runCatching { deleteDirectoryRecursively(root) }
    }
}

private fun locateRepoRoot(): Path {
    var current = Paths.get(System.getProperty("user.dir")).toAbsolutePath()
    repeat(8) {
        if (current.resolve("gradlew").exists() && current.resolve("settings.gradle.kts").exists()) {
            return current
        }
        current = current.parent ?: return@repeat
    }
    error("Unable to locate repository root from ${System.getProperty("user.dir")}")
}

private fun normalizeForGradle(path: Path): String {
    return path.toAbsolutePath().normalize().toString().replace("\\", "/")
}

private class NestedGradleBuildException(message: String) : IllegalStateException(message)

private fun deleteDirectoryRecursively(root: Path) {
    if (!root.exists()) {
        return
    }
    Files.walk(root)
        .sorted(Comparator.reverseOrder())
        .forEach { Files.deleteIfExists(it) }
}
