package site.addzero.coding.playground.server.generation

import org.koin.core.annotation.Single
import site.addzero.coding.playground.server.util.renderTemplateString
import site.addzero.coding.playground.shared.dto.ContextAggregateDto
import site.addzero.coding.playground.shared.dto.GenerationTargetMetaDto
import site.addzero.coding.playground.shared.dto.RelationKind
import site.addzero.coding.playground.shared.dto.RenderedTemplateDto
import site.addzero.coding.playground.shared.dto.TemplateMetaDto
import site.addzero.coding.playground.shared.service.TemplateRenderer
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readText

@Single
class TemplateRendererImpl(
    private val builtinTemplateCatalog: BuiltinTemplateCatalog,
    private val irCompiler: MetadataIrCompiler,
) : TemplateRenderer {
    override suspend fun render(
        template: TemplateMetaDto,
        context: ContextAggregateDto,
        target: GenerationTargetMetaDto,
        variables: Map<String, String>,
    ): RenderedTemplateDto {
        val relativePath = renderTemplateString(template.relativeOutputPath, variables)
        val fileName = renderTemplateString(template.fileNameTemplate, variables)
        val ir = irCompiler.compile(context)
        val content = when (template.key) {
            "descriptor-runtime" -> renderDescriptorRuntime()
            "jimmer-entity" -> renderJimmerEntity(ir, variables)
            "crud-dtos" -> renderCrudDtos(ir, variables)
            "repository-skeleton" -> renderRepositorySkeleton(ir, variables)
            "service-skeleton" -> renderServiceSkeleton(ir, variables)
            "route-skeleton" -> renderRouteSkeleton(ir, variables)
            "client-api" -> renderClientApi(ir, variables)
            "client-state" -> renderClientState(ir, variables)
            "client-screen" -> renderClientScreen(ir, variables)
            "koin-module" -> renderServerKoinModule(variables)
            "client-koin-module" -> renderClientKoinModule(variables)
            "client-workbench" -> renderClientWorkbench(ir, variables)
            "metadata-object" -> renderMetadataObject(context, ir, variables)
            "metadata-index" -> renderMetadataIndex(variables)
            "plugin-root-gradle" -> renderPluginRootGradle()
            "plugin-settings-gradle" -> renderPluginSettingsGradle(variables)
            "plugin-server-gradle" -> renderPluginServerGradle(variables)
            "plugin-client-gradle" -> renderPluginClientGradle()
            "plugin-spi-gradle" -> renderPluginSpiGradle()
            "settings-snippet" -> renderSettingsSnippet(variables)
            else -> renderTemplateString(template.body, variables)
        }
        return RenderedTemplateDto(
            templateId = template.id,
            templateKey = template.key,
            relativePath = relativePath,
            fileName = fileName,
            content = content.trimEnd() + "\n",
        )
    }

    private fun renderJimmerEntity(
        context: GenerationContextIr,
        variables: Map<String, String>,
    ): String {
        val entity = entity(context, variables)
        val packageName = variables.getValue("packageName")
        return buildString {
            appendLine("package $packageName.model")
            appendLine()
            appendLine("import org.babyfish.jimmer.sql.*")
            appendLine()
            appendLine("@Entity")
            appendLine("@Table(name = \"${entity.tableName}\")")
            appendLine("interface ${entity.kotlinName} {")
            val idField = entity.idField
            if (idField == null) {
                appendLine("    @Id")
                appendLine("    val id: String")
            } else {
                appendLine("    @Id")
                appendLine("    val ${idField.kotlinName}: ${idField.kotlinType}")
            }
            entity.fields
                .filterNot { it.idField }
                .forEach { field ->
                    appendLine()
                    if (field.keyField) {
                        appendLine("    @Key")
                    }
                    appendLine("    val ${field.kotlinName}: ${field.kotlinType}")
                }
            entity.relations.forEach { relation ->
                appendLine()
                when (RelationKind.valueOf(relation.kind)) {
                    RelationKind.MANY_TO_ONE -> {
                        appendLine("    @ManyToOne")
                        appendLine("    @JoinColumn(name = \"${relation.kotlinName}_id\")")
                        appendLine("    val ${relation.kotlinName}: ${relation.targetEntityName}${if (relation.nullable) "?" else ""}")
                    }
                    RelationKind.ONE_TO_ONE -> {
                        appendLine("    @OneToOne")
                        appendLine("    @JoinColumn(name = \"${relation.kotlinName}_id\")")
                        appendLine("    val ${relation.kotlinName}: ${relation.targetEntityName}${if (relation.nullable) "?" else ""}")
                    }
                    RelationKind.ONE_TO_MANY -> {
                        appendLine("    @OneToMany(mappedBy = \"${relation.mappedBy ?: entity.camelName}\")")
                        appendLine("    val ${relation.kotlinName}: List<${relation.targetEntityName}>")
                    }
                    RelationKind.MANY_TO_MANY -> {
                        appendLine("    @ManyToMany")
                        appendLine("    val ${relation.kotlinName}: List<${relation.targetEntityName}>")
                    }
                }
            }
            appendLine("}")
        }
    }

    private fun renderCrudDtos(
        context: GenerationContextIr,
        variables: Map<String, String>,
    ): String {
        val entity = entity(context, variables)
        val packageName = variables.getValue("packageName")
        return buildString {
            appendLine("package $packageName.dto")
            appendLine()
            appendLine("import kotlinx.serialization.Serializable")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class ${entity.kotlinName}Query(")
            if (entity.payloadFields.isEmpty()) {
                appendLine("    val keyword: String? = null,")
            } else {
                entity.payloadFields.forEachIndexed { index, field ->
                    appendLine("    val ${field.kotlinName}: ${field.kotlinNullableType} = null${if (index == entity.payloadFields.lastIndex) "" else ","}")
                }
            }
            appendLine(")")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class ${entity.kotlinName}PageRequest(")
            appendLine("    val pageIndex: Int = 0,")
            appendLine("    val pageSize: Int = 20,")
            appendLine("    val query: ${entity.kotlinName}Query = ${entity.kotlinName}Query(),")
            appendLine(")")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class ${entity.kotlinName}PageResponse(")
            appendLine("    val items: List<${entity.kotlinName}Response> = emptyList(),")
            appendLine("    val totalCount: Long = 0,")
            appendLine(")")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class Create${entity.kotlinName}Request(")
            appendRequestFields(entity)
            appendLine(")")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class Update${entity.kotlinName}Request(")
            appendRequestFields(entity)
            appendLine(")")
            appendLine()
            appendLine("@Serializable")
            appendLine("data class ${entity.kotlinName}Response(")
            entity.fields.forEachIndexed { index, field ->
                appendLine("    val ${field.kotlinName}: ${field.kotlinType}${if (index == entity.fields.lastIndex) "" else ","}")
            }
            if (entity.fields.isEmpty()) {
                appendLine("    val id: String,")
            }
            appendLine(")")
        }
    }

    private fun StringBuilder.appendRequestFields(entity: GenerationEntityIr) {
        if (entity.payloadFields.isEmpty()) {
            appendLine("    val placeholder: String = \"\",")
            return
        }
        entity.payloadFields.forEachIndexed { index, field ->
            appendLine("    val ${field.kotlinName}: ${field.kotlinType}${if (index == entity.payloadFields.lastIndex) "" else ","}")
        }
    }

    private fun renderRepositorySkeleton(
        context: GenerationContextIr,
        variables: Map<String, String>,
    ): String {
        val entity = entity(context, variables)
        val idField = entity.idField
        val packageName = variables.getValue("packageName")
        return """
            package $packageName.service

            import org.babyfish.jimmer.sql.kt.KSqlClient
            import org.koin.core.annotation.Single
            import $packageName.dto.${entity.kotlinName}PageRequest
            import $packageName.dto.${entity.kotlinName}PageResponse
            import $packageName.dto.${entity.kotlinName}Response
            import $packageName.dto.Create${entity.kotlinName}Request
            import $packageName.dto.Update${entity.kotlinName}Request

            @Single
            class ${entity.kotlinName}Repository(
                private val sqlClient: KSqlClient,
            ) {
                fun page(request: ${entity.kotlinName}PageRequest): ${entity.kotlinName}PageResponse {
                    return ${entity.kotlinName}PageResponse()
                }

                fun get(id: String): ${entity.kotlinName}Response {
                    return stub${entity.kotlinName}Response(id)
                }

                fun create(request: Create${entity.kotlinName}Request): ${entity.kotlinName}Response {
                    return stub${entity.kotlinName}Response().copy(
                        ${entity.payloadFields.joinToString(",\n            ") { "${it.kotlinName} = request.${it.kotlinName}" }}
                    )
                }

                fun update(id: String, request: Update${entity.kotlinName}Request): ${entity.kotlinName}Response {
                    return stub${entity.kotlinName}Response(id).copy(
                        ${entity.payloadFields.joinToString(",\n            ") { "${it.kotlinName} = request.${it.kotlinName}" }}
                    )
                }

                fun delete(id: String) {
                }

                private fun stub${entity.kotlinName}Response(id: String = "stub"): ${entity.kotlinName}Response {
                    return ${entity.kotlinName}Response(
                        ${entity.fields.joinToString(",\n                ") { field ->
                            if (field.idField && idField != null) {
                                "${field.kotlinName} = ${idLiteral(field, "id")}"
                            } else {
                                "${field.kotlinName} = ${placeholderLiteral(field)}"
                            }
                        }}
                    )
                }
            }
        """.trimIndent()
    }

    private fun renderServiceSkeleton(
        context: GenerationContextIr,
        variables: Map<String, String>,
    ): String {
        val entity = entity(context, variables)
        val packageName = variables.getValue("packageName")
        return """
            package $packageName.service

            import org.koin.core.annotation.Single
            import $packageName.dto.${entity.kotlinName}PageRequest
            import $packageName.dto.${entity.kotlinName}PageResponse
            import $packageName.dto.${entity.kotlinName}Response
            import $packageName.dto.Create${entity.kotlinName}Request
            import $packageName.dto.Update${entity.kotlinName}Request

            @Single
            class ${entity.kotlinName}Service(
                private val repository: ${entity.kotlinName}Repository,
            ) {
                fun page(request: ${entity.kotlinName}PageRequest): ${entity.kotlinName}PageResponse = repository.page(request)

                fun get(id: String): ${entity.kotlinName}Response = repository.get(id)

                fun create(request: Create${entity.kotlinName}Request): ${entity.kotlinName}Response = repository.create(request)

                fun update(id: String, request: Update${entity.kotlinName}Request): ${entity.kotlinName}Response = repository.update(id, request)

                fun delete(id: String) {
                    repository.delete(id)
                }
            }
        """.trimIndent()
    }

    private fun renderRouteSkeleton(
        context: GenerationContextIr,
        variables: Map<String, String>,
    ): String {
        val entity = entity(context, variables)
        val packageName = variables.getValue("packageName")
        val contextCode = variables.getValue("contextCode")
        return """
            package $packageName.route

            import org.koin.mp.KoinPlatform
            import org.springframework.web.bind.annotation.DeleteMapping
            import org.springframework.web.bind.annotation.GetMapping
            import org.springframework.web.bind.annotation.PathVariable
            import org.springframework.web.bind.annotation.PostMapping
            import org.springframework.web.bind.annotation.PutMapping
            import org.springframework.web.bind.annotation.RequestBody
            import $packageName.dto.${entity.kotlinName}PageRequest
            import $packageName.dto.${entity.kotlinName}PageResponse
            import $packageName.dto.${entity.kotlinName}Response
            import $packageName.dto.Create${entity.kotlinName}Request
            import $packageName.dto.Update${entity.kotlinName}Request
            import $packageName.service.${entity.kotlinName}Service

            @PostMapping("/api/$contextCode/${entity.entityPath}/page")
            suspend fun list${entity.kotlinName}s(
                @RequestBody request: ${entity.kotlinName}PageRequest,
            ): ${entity.kotlinName}PageResponse {
                return KoinPlatform.getKoin().get<${entity.kotlinName}Service>().page(request)
            }

            @GetMapping("/api/$contextCode/${entity.entityPath}/{id}")
            suspend fun get${entity.kotlinName}(
                @PathVariable id: String,
            ): ${entity.kotlinName}Response {
                return KoinPlatform.getKoin().get<${entity.kotlinName}Service>().get(id)
            }

            @PostMapping("/api/$contextCode/${entity.entityPath}")
            suspend fun create${entity.kotlinName}(
                @RequestBody request: Create${entity.kotlinName}Request,
            ): ${entity.kotlinName}Response {
                return KoinPlatform.getKoin().get<${entity.kotlinName}Service>().create(request)
            }

            @PutMapping("/api/$contextCode/${entity.entityPath}/{id}")
            suspend fun update${entity.kotlinName}(
                @PathVariable id: String,
                @RequestBody request: Update${entity.kotlinName}Request,
            ): ${entity.kotlinName}Response {
                return KoinPlatform.getKoin().get<${entity.kotlinName}Service>().update(id, request)
            }

            @DeleteMapping("/api/$contextCode/${entity.entityPath}/{id}")
            suspend fun delete${entity.kotlinName}(
                @PathVariable id: String,
            ) {
                KoinPlatform.getKoin().get<${entity.kotlinName}Service>().delete(id)
            }
        """.trimIndent()
    }

    private fun renderClientApi(
        context: GenerationContextIr,
        variables: Map<String, String>,
    ): String {
        val entity = entity(context, variables)
        val packageName = variables.getValue("packageName")
        val contextCode = variables.getValue("contextCode")
        return """
            package $packageName.client.api

            import io.ktor.client.HttpClient
            import io.ktor.client.call.body
            import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
            import io.ktor.client.request.delete
            import io.ktor.client.request.get
            import io.ktor.client.request.post
            import io.ktor.client.request.put
            import io.ktor.client.request.setBody
            import io.ktor.http.ContentType
            import io.ktor.http.contentType
            import io.ktor.serialization.kotlinx.json.json
            import org.koin.core.annotation.Single
            import $packageName.dto.${entity.kotlinName}PageRequest
            import $packageName.dto.${entity.kotlinName}PageResponse
            import $packageName.dto.${entity.kotlinName}Response
            import $packageName.dto.Create${entity.kotlinName}Request
            import $packageName.dto.Update${entity.kotlinName}Request

            @Single
            class ${entity.kotlinName}Api(
                private val httpClient: HttpClient,
            ) {
                private val baseUrl: String = ""

                suspend fun page(request: ${entity.kotlinName}PageRequest): ${entity.kotlinName}PageResponse {
                    return httpClient.post("${'$'}baseUrl/api/$contextCode/${entity.entityPath}/page") {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body()
                }

                suspend fun get(id: String): ${entity.kotlinName}Response {
                    return httpClient.get("${'$'}baseUrl/api/$contextCode/${entity.entityPath}/${'$'}id").body()
                }

                suspend fun create(request: Create${entity.kotlinName}Request): ${entity.kotlinName}Response {
                    return httpClient.post("${'$'}baseUrl/api/$contextCode/${entity.entityPath}") {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body()
                }

                suspend fun update(id: String, request: Update${entity.kotlinName}Request): ${entity.kotlinName}Response {
                    return httpClient.put("${'$'}baseUrl/api/$contextCode/${entity.entityPath}/${'$'}id") {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body()
                }

                suspend fun delete(id: String) {
                    httpClient.delete("${'$'}baseUrl/api/$contextCode/${entity.entityPath}/${'$'}id")
                }
            }
        """.trimIndent()
    }

    private fun renderClientState(
        context: GenerationContextIr,
        variables: Map<String, String>,
    ): String {
        val entity = entity(context, variables)
        val createArgs = if (entity.payloadFields.isEmpty()) {
            "placeholder = \"\""
        } else {
            entity.payloadFields.joinToString(",\n                                ") { "${it.kotlinName} = ${placeholderLiteral(it)}" }
        }
        val updateArgs = if (entity.payloadFields.isEmpty()) {
            "placeholder = \"\""
        } else {
            entity.payloadFields.joinToString(",\n                                ") { "${it.kotlinName} = ${placeholderLiteral(it)}" }
        }
        return """
            package ${variables.getValue("packageName")}.client.state

            import androidx.compose.runtime.getValue
            import androidx.compose.runtime.mutableStateOf
            import androidx.compose.runtime.setValue
            import org.koin.core.annotation.Single
            import ${variables.getValue("packageName")}.client.api.${entity.kotlinName}Api
            import ${variables.getValue("packageName")}.dto.${entity.kotlinName}PageRequest
            import ${variables.getValue("packageName")}.dto.${entity.kotlinName}PageResponse
            import ${variables.getValue("packageName")}.dto.${entity.kotlinName}Response
            import ${variables.getValue("packageName")}.dto.Create${entity.kotlinName}Request
            import ${variables.getValue("packageName")}.dto.Update${entity.kotlinName}Request

            @Single
            class ${entity.kotlinName}CrudState(
                private val api: ${entity.kotlinName}Api,
            ) {
                var page by mutableStateOf(${entity.kotlinName}PageResponse())
                    private set
                var selected by mutableStateOf<${entity.kotlinName}Response?>(null)
                    private set
                var loading by mutableStateOf(false)
                    private set
                var message by mutableStateOf("准备就绪")
                    private set

                suspend fun refresh() {
                    loading = true
                    runCatching {
                        page = api.page(${entity.kotlinName}PageRequest())
                        message = "列表已刷新"
                    }.onFailure {
                        message = it.message ?: "列表刷新失败"
                    }
                    loading = false
                }

                suspend fun load(id: String) {
                    loading = true
                    runCatching {
                        selected = api.get(id)
                        message = "详情已加载"
                    }.onFailure {
                        message = it.message ?: "详情加载失败"
                    }
                    loading = false
                }

                suspend fun createPlaceholder() {
                    loading = true
                    runCatching {
                        selected = api.create(
                            Create${entity.kotlinName}Request(
                                $createArgs
                            ),
                        )
                        message = "已执行创建占位调用"
                        refresh()
                    }.onFailure {
                        message = it.message ?: "创建失败"
                    }
                    loading = false
                }

                suspend fun updatePlaceholder(id: String) {
                    loading = true
                    runCatching {
                        selected = api.update(
                            id = id,
                            request = Update${entity.kotlinName}Request(
                                $updateArgs
                            ),
                        )
                        message = "已执行更新占位调用"
                        refresh()
                    }.onFailure {
                        message = it.message ?: "更新失败"
                    }
                    loading = false
                }

                suspend fun delete(id: String) {
                    loading = true
                    runCatching {
                        api.delete(id)
                        selected = null
                        message = "已执行删除占位调用"
                        refresh()
                    }.onFailure {
                        message = it.message ?: "删除失败"
                    }
                    loading = false
                }
            }
        """.trimIndent()
    }

    private fun renderClientScreen(
        context: GenerationContextIr,
        variables: Map<String, String>,
    ): String {
        val entity = entity(context, variables)
        val idProperty = entity.idField?.kotlinName ?: "id"
        return """
            package ${variables.getValue("packageName")}.client.ui

            import androidx.compose.foundation.layout.Arrangement
            import androidx.compose.foundation.layout.Column
            import androidx.compose.foundation.layout.Row
            import androidx.compose.foundation.layout.fillMaxHeight
            import androidx.compose.foundation.layout.fillMaxSize
            import androidx.compose.foundation.layout.fillMaxWidth
            import androidx.compose.foundation.layout.padding
            import androidx.compose.foundation.lazy.LazyColumn
            import androidx.compose.foundation.lazy.items
            import androidx.compose.material3.Button
            import androidx.compose.material3.Card
            import androidx.compose.material3.MaterialTheme
            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.LaunchedEffect
            import androidx.compose.runtime.rememberCoroutineScope
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.unit.dp
            import kotlinx.coroutines.launch
            import org.koin.compose.koinInject
            import ${variables.getValue("packageName")}.client.state.${entity.kotlinName}CrudState

            @Composable
            fun ${entity.kotlinName}CrudScreen(
                state: ${entity.kotlinName}CrudState = koinInject(),
            ) {
                val scope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    state.refresh()
                }
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { scope.launch { state.refresh() } }) {
                            Text("刷新${entity.name}列表")
                        }
                        Button(onClick = { scope.launch { state.createPlaceholder() } }) {
                            Text("创建占位记录")
                        }
                        Text(state.message, style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Card(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("${entity.name} 列表", style = MaterialTheme.typography.titleMedium)
                                Text("总数: ${'$'}{state.page.totalCount}")
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(state.page.items) { item ->
                                        Card(modifier = Modifier.fillMaxWidth()) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Text(item.toString(), style = MaterialTheme.typography.bodySmall)
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(onClick = { scope.launch { state.load(item.$idProperty.toString()) } }) {
                                                        Text("详情")
                                                    }
                                                    Button(onClick = { scope.launch { state.updatePlaceholder(item.$idProperty.toString()) } }) {
                                                        Text("更新")
                                                    }
                                                    Button(onClick = { scope.launch { state.delete(item.$idProperty.toString()) } }) {
                                                        Text("删除")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Card(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text("${entity.name} 详情 / 编辑壳层", style = MaterialTheme.typography.titleMedium)
                                Text(state.selected?.toString() ?: "尚未选择记录")
                                Text("表单与校验逻辑由业务方在该壳层上继续补充。", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        """.trimIndent()
    }

    private fun renderServerKoinModule(variables: Map<String, String>): String {
        return """
            package ${variables.getValue("packageName")}.di

            import org.koin.core.annotation.ComponentScan
            import org.koin.core.annotation.Configuration
            import org.koin.core.annotation.Module

            @Module
            @Configuration("${variables.getValue("contextCode")}")
            @ComponentScan("${variables.getValue("packageName")}")
            class ${variables.getValue("ContextName")}ServerModule
        """.trimIndent()
    }

    private fun renderClientKoinModule(variables: Map<String, String>): String {
        return """
            package ${variables.getValue("packageName")}.client.di

            import io.ktor.client.HttpClient
            import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
            import io.ktor.serialization.kotlinx.json.json
            import org.koin.core.annotation.ComponentScan
            import org.koin.core.annotation.Configuration
            import org.koin.core.annotation.Module
            import org.koin.core.annotation.Single

            @Module
            @Configuration("${variables.getValue("contextCode")}-client")
            @ComponentScan("${variables.getValue("packageName")}.client")
            class ${variables.getValue("ContextName")}ClientModule {
                @Single
                fun provideClientHttpClient(): HttpClient {
                    return HttpClient {
                        install(ContentNegotiation) {
                            json()
                        }
                    }
                }
            }
        """.trimIndent()
    }

    private fun renderClientWorkbench(
        context: GenerationContextIr,
        variables: Map<String, String>,
    ): String {
        val packageName = variables.getValue("packageName")
        val entries = context.entities.joinToString(",\n                    ") { "\"${it.code}\" to \"${it.name}\"" }
        val branches = context.entities.joinToString("\n                    ") {
            "\"${it.code}\" -> ${it.kotlinName}CrudScreen()"
        }
        return """
            package $packageName.client.ui

            import androidx.compose.foundation.layout.Arrangement
            import androidx.compose.foundation.layout.Column
            import androidx.compose.foundation.layout.Row
            import androidx.compose.foundation.layout.fillMaxSize
            import androidx.compose.foundation.layout.padding
            import androidx.compose.material3.FilterChip
            import androidx.compose.material3.MaterialTheme
            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.getValue
            import androidx.compose.runtime.mutableStateOf
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.setValue
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.unit.dp

            object ${variables.getValue("ContextName")}ClientFeature {
                val key: String = "${variables.getValue("contextCode")}"
                val title: String = "${variables.getValue("contextName")}"

                @Composable
                fun Render() {
                    ${variables.getValue("ContextName")}ClientWorkbench()
                }
            }

            @Composable
            fun ${variables.getValue("ContextName")}ClientWorkbench() {
                val entries = listOf(
                    $entries
                )
                var selected by remember { mutableStateOf(entries.firstOrNull()?.first.orEmpty()) }
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("${variables.getValue("contextName")} CRUD 工作台", style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        entries.forEach { (key, title) ->
                            FilterChip(
                                selected = selected == key,
                                onClick = { selected = key },
                                label = { Text(title) },
                            )
                        }
                    }
                    when (selected) {
                        $branches
                        else -> Text("暂无可渲染实体")
                    }
                }
            }
        """.trimIndent()
    }

    private fun renderMetadataObject(
        context: ContextAggregateDto,
        ir: GenerationContextIr,
        variables: Map<String, String>,
    ): String {
        val packageName = variables.getValue("packageName")
        val modelDescriptors = ir.entities.joinToString(",\n            ") { entity ->
            val fieldDescriptors = entity.fields.joinToString(",\n                    ") { field ->
                """
                FieldDescriptor(
                    name = "${field.name}",
                    code = "${field.code}",
                    type = site.addzero.coding.playground.shared.dto.FieldType.${fieldTypeLiteral(field)},
                    nullable = ${field.nullable},
                    list = ${field.list},
                    idField = ${field.idField},
                    keyField = ${field.keyField},
                )
                """.trimIndent()
            }
            val relationDescriptors = entity.relations.joinToString(",\n                    ") { relation ->
                """
                RelationDescriptor(
                    name = "${relation.name}",
                    code = "${relation.code}",
                    kind = site.addzero.coding.playground.shared.dto.RelationKind.${relation.kind},
                    targetModel = "${relation.targetEntityName}",
                    nullable = ${relation.nullable},
                )
                """.trimIndent()
            }
            """
            ModelDescriptor(
                name = "${entity.kotlinName}",
                code = "${entity.code}",
                tableName = "${entity.tableName}",
                fields = listOf(
                    ${fieldDescriptors.ifBlank { "" }}
                ),
                relations = listOf(
                    ${relationDescriptors.ifBlank { "" }}
                ),
            )
            """.trimIndent()
        }
        val dtoDescriptors = ir.dtos.joinToString(",\n            ") { dto ->
            val fieldDescriptors = dto.fields.joinToString(",\n                    ") { field ->
                """
                DtoFieldDescriptor(
                    name = "${field.name}",
                    code = "${field.code}",
                    type = site.addzero.coding.playground.shared.dto.FieldType.${dtoFieldTypeLiteral(field)},
                    nullable = ${field.nullable},
                    list = ${field.list},
                    sourcePath = ${field.sourcePath?.let { "\"$it\"" } ?: "null"},
                )
                """.trimIndent()
            }
            """
            DtoDescriptor(
                name = "${dto.kotlinName}",
                code = "${dto.code}",
                kind = site.addzero.coding.playground.shared.dto.DtoKind.${dto.kind},
                sourceModel = ${dto.sourceModelName?.let { "\"$it\"" } ?: "null"},
                fields = listOf(
                    ${fieldDescriptors.ifBlank { "" }}
                ),
            )
            """.trimIndent()
        }
        val templateDescriptors = context.templates.joinToString(",\n            ") { template ->
            """
            TemplateDescriptor(
                name = "${template.name}",
                key = "${template.key}",
                outputKind = site.addzero.coding.playground.shared.dto.TemplateOutputKind.${template.outputKind.name},
                relativeOutputPath = "${template.relativeOutputPath}",
                fileNameTemplate = "${template.fileNameTemplate}",
            )
            """.trimIndent()
        }
        return """
            package $packageName.metadata

            import site.addzero.coding.playground.shared.descriptor.ContextMetadataDescriptor
            import site.addzero.coding.playground.shared.descriptor.ContextMetadataView
            import site.addzero.coding.playground.shared.descriptor.DtoDescriptor
            import site.addzero.coding.playground.shared.descriptor.DtoFieldDescriptor
            import site.addzero.coding.playground.shared.descriptor.FieldDescriptor
            import site.addzero.coding.playground.shared.descriptor.ModelDescriptor
            import site.addzero.coding.playground.shared.descriptor.RelationDescriptor
            import site.addzero.coding.playground.shared.descriptor.TemplateDescriptor

            object ${variables.getValue("ContextName")}Metadata : ContextMetadataView {
                private val descriptor = ContextMetadataDescriptor(
                    name = "${context.context.name}",
                    code = "${context.context.code}",
                    models = listOf(
                        ${modelDescriptors.ifBlank { "" }}
                    ),
                    dtos = listOf(
                        ${dtoDescriptors.ifBlank { "" }}
                    ),
                    templates = listOf(
                        ${templateDescriptors.ifBlank { "" }}
                    ),
                )

                fun descriptor(): ContextMetadataDescriptor = descriptor

                override fun models(): List<ModelDescriptor> = descriptor.models

                override fun dtos(): List<DtoDescriptor> = descriptor.dtos

                override fun templates(): List<TemplateDescriptor> = descriptor.templates

                override fun findModel(name: String): ModelDescriptor? =
                    descriptor.models.firstOrNull { it.name == name || it.code == name }

                override fun findDto(name: String): DtoDescriptor? =
                    descriptor.dtos.firstOrNull { it.name == name || it.code == name }
            }
        """.trimIndent()
    }

    private fun renderMetadataIndex(variables: Map<String, String>): String {
        return """
            package ${variables.getValue("packageName")}.metadata

            import site.addzero.coding.playground.shared.descriptor.ContextMetadataDescriptor

            object GeneratedMetadataIndex : site.addzero.coding.playground.shared.descriptor.GeneratedMetadataIndex {
                override fun contexts(): List<ContextMetadataDescriptor> = listOf(${variables.getValue("ContextName")}Metadata.descriptor())

                override fun findContext(name: String): ContextMetadataDescriptor? =
                    contexts().firstOrNull { it.name == name || it.code == name }
            }
        """.trimIndent()
    }

    private fun renderDescriptorRuntime(): String {
        return locateRepoRoot()
            .resolve("apps/coding-playground/shared/src/commonMain/kotlin/site/addzero/coding/playground/shared/descriptor/GeneratedMetadataDescriptors.kt")
            .takeIf(Path::exists)
            ?.readText()
            ?: error("Unable to locate GeneratedMetadataDescriptors.kt source")
    }

    private fun renderPluginRootGradle(): String {
        return """
            plugins {
                base
            }

            description = "Generated plugin aggregate"
        """.trimIndent()
    }

    private fun renderPluginSettingsGradle(variables: Map<String, String>): String {
        return """
            rootProject.name = "${variables.getValue("contextCode")}-generated-plugin"

            include(":server")
            include(":client")
            include(":spi")
        """.trimIndent()
    }

    private fun renderPluginServerGradle(variables: Map<String, String>): String {
        return """
            plugins {
                id("site.addzero.buildlogic.jvm.kotlin-convention")
                id("site.addzero.buildlogic.jvm.jimmer")
                id("site.addzero.buildlogic.jvm.jvm-koin")
                id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
                id("site.addzero.buildlogic.jvm.jvm-json-withtool")
            }

            ksp {
                arg("springKtor.generatedPackage", "${variables.getValue("packageName")}.generated.springktor")
            }

            dependencies {
                implementation(project(":spi"))
                implementation("site.addzero:spring2ktor-server-core:2026.03.13")
                compileOnly(libs.findLibrary("org-springframework-spring-web").get())
                ksp("site.addzero:spring2ktor-server-processor:2026.03.13")
            }
        """.trimIndent()
    }

    private fun renderPluginClientGradle(): String {
        return """
            plugins {
                id("site.addzero.buildlogic.kmp.kmp-core")
                id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
                id("site.addzero.buildlogic.kmp.kmp-koin")
                id("site.addzero.buildlogic.kmp.kmp-ktor-client")
                id("site.addzero.buildlogic.kmp.kmp-json-withtool")
                id("org.jetbrains.compose")
                id("org.jetbrains.kotlin.plugin.compose")
            }

            val libs = versionCatalogs.named("libs")

            kotlin {
                sourceSets {
                    commonMain.dependencies {
                        implementation(project(":spi"))
                        implementation(libs.findLibrary("org-jetbrains-compose-runtime-runtime").get())
                        implementation(libs.findLibrary("org-jetbrains-compose-foundation-foundation").get())
                        implementation(libs.findLibrary("org-jetbrains-compose-material3-material3").get())
                        implementation(libs.findLibrary("org-jetbrains-compose-material-material-icons-extended").get())
                        implementation(libs.findLibrary("org-jetbrains-androidx-lifecycle-lifecycle-runtime-compose").get())
                        implementation(libs.findLibrary("org-jetbrains-androidx-lifecycle-lifecycle-viewmodel-compose").get())
                    }
                }
            }
        """.trimIndent()
    }

    private fun renderPluginSpiGradle(): String {
        return """
            plugins {
                id("site.addzero.buildlogic.kmp.kmp-core")
                id("site.addzero.buildlogic.kmp.kmp-json-withtool")
            }
        """.trimIndent()
    }

    private fun renderSettingsSnippet(variables: Map<String, String>): String {
        return """includeBuild("plugins/${variables.getValue("contextCode")}")"""
    }

    private fun locateRepoRoot(): Path {
        var current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize()
        repeat(10) {
            if (current.resolve("settings.gradle.kts").exists() && current.resolve("apps").exists()) {
                return current
            }
            current = current.parent ?: return@repeat
        }
        error("Unable to locate repository root from '${System.getProperty("user.dir")}'")
    }

    private fun entity(context: GenerationContextIr, variables: Map<String, String>): GenerationEntityIr {
        val entityCode = variables.getValue("entityCode")
        return context.entities.first { it.code == entityCode }
    }

    private fun placeholderLiteral(field: GenerationFieldIr): String {
        return when {
            field.list -> "emptyList()"
            field.kotlinType == "String" || field.kotlinType == "String?" -> "\"\""
            field.kotlinType == "Boolean" || field.kotlinType == "Boolean?" -> "false"
            field.kotlinType == "Int" || field.kotlinType == "Int?" -> "0"
            field.kotlinType == "Long" || field.kotlinType == "Long?" -> "0L"
            field.kotlinType == "Double" || field.kotlinType == "Double?" -> "0.0"
            else -> "\"\""
        }
    }

    private fun idLiteral(field: GenerationFieldIr, idVariable: String): String {
        return when (field.kotlinType.removeSuffix("?")) {
            "String" -> idVariable
            "Long" -> "${idVariable}.toLongOrNull() ?: 0L"
            "Int" -> "${idVariable}.toIntOrNull() ?: 0"
            else -> idVariable
        }
    }

    private fun fieldTypeLiteral(field: GenerationFieldIr): String {
        return field.sourceType
    }

    private fun dtoFieldTypeLiteral(field: GenerationDtoFieldIr): String {
        return field.sourceType
    }
}
