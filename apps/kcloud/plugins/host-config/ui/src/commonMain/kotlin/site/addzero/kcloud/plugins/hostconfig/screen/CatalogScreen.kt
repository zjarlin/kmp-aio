@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogEntityMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogEntityType
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogFieldOptionResponse
import site.addzero.kcloud.plugins.hostconfig.catalog.CatalogFormDraft
import site.addzero.kcloud.plugins.hostconfig.catalog.CatalogScreenState
import site.addzero.kcloud.plugins.hostconfig.catalog.CatalogTreeNodeKind
import site.addzero.kcloud.plugins.hostconfig.catalog.CatalogViewModel
import site.addzero.kcloud.plugins.hostconfig.catalog.CatalogMetadataDetailContent
import site.addzero.kcloud.plugins.hostconfig.catalog.CatalogMetadataFormContent
import site.addzero.kcloud.plugins.hostconfig.catalog.defaultCatalogValues
import site.addzero.kcloud.plugins.hostconfig.catalog.deviceNodeId
import site.addzero.kcloud.plugins.hostconfig.catalog.featureNodeId
import site.addzero.kcloud.plugins.hostconfig.catalog.icon
import site.addzero.kcloud.plugins.hostconfig.catalog.labelNodeId
import site.addzero.kcloud.plugins.hostconfig.catalog.metadataFor
import site.addzero.kcloud.plugins.hostconfig.catalog.productNodeId
import site.addzero.kcloud.plugins.hostconfig.catalog.propertyNodeId
import site.addzero.kcloud.plugins.hostconfig.catalog.toCatalogValues
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigDialog
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigKeyValueRow
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigPanel
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigStatusStrip
import site.addzero.kcloud.plugins.hostconfig.common.orDash

@Route(
    title = "产品目录",
    routePath = "host-config/catalog",
    icon = "Category",
    order = 5.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "元数据配置",
            icon = "SettingsApplications",
            order = -10,
        ),
    ),
)
@Composable
fun CatalogScreen() {
    val viewModel = koinViewModel<CatalogViewModel>()
    val state = viewModel.screenState

    var editorSeed by remember { mutableStateOf<CatalogEditorSeed?>(null) }

    fun openCreate(entityType: CatalogEntityType) {
        viewModel.clearNotice()
        editorSeed = when (entityType) {
            CatalogEntityType.PRODUCT,
            CatalogEntityType.LABEL,
            -> CatalogEditorSeed(
                entityType = entityType,
                initialValues = defaultCatalogValues(entityType),
            )

            CatalogEntityType.DEVICE -> state.selectedProduct?.let { product ->
                CatalogEditorSeed(
                    entityType = entityType,
                    productId = product.id,
                    initialValues = defaultCatalogValues(entityType),
                )
            }

            CatalogEntityType.PROPERTY,
            CatalogEntityType.FEATURE,
            -> state.activeDeviceDefinition?.let { device ->
                CatalogEditorSeed(
                    entityType = entityType,
                    productId = state.selectedProduct?.id,
                    deviceDefinitionId = device.id,
                    initialValues = defaultCatalogValues(entityType),
                )
            }
        }
    }

    fun openEditSelection() {
        val node = state.selectedNode ?: return
        val entityType = node.entityType ?: return
        val currentValues = state.selectedCatalogValues() ?: return
        editorSeed = CatalogEditorSeed(
            entityType = entityType,
            existingId = node.entityId,
            productId = node.productId,
            deviceDefinitionId = node.deviceDefinitionId,
            initialValues = defaultCatalogValues(entityType) + currentValues,
        )
    }

    fun deleteCurrentSelection() {
        when (state.selectedNode?.entityType) {
            CatalogEntityType.PRODUCT -> {
                state.selectedProduct?.let { product ->
                    viewModel.deleteProduct(product.id)
                }
            }

            CatalogEntityType.DEVICE -> {
                state.selectedDeviceDefinition?.let { device ->
                    viewModel.deleteDeviceDefinition(
                        deviceDefinitionId = device.id,
                        productId = state.selectedProduct?.id,
                    )
                }
            }

            CatalogEntityType.PROPERTY -> {
                state.selectedPropertyDefinition?.let { property ->
                    viewModel.deletePropertyDefinition(
                        propertyDefinitionId = property.id,
                        deviceDefinitionId = state.activeDeviceDefinition?.id,
                    )
                }
            }

            CatalogEntityType.FEATURE -> {
                state.selectedFeatureDefinition?.let { feature ->
                    viewModel.deleteFeatureDefinition(
                        featureDefinitionId = feature.id,
                        deviceDefinitionId = state.activeDeviceDefinition?.id,
                    )
                }
            }

            CatalogEntityType.LABEL -> {
                state.selectedLabelDefinition?.let { label ->
                    viewModel.deleteLabel(label.id)
                }
            }

            null -> Unit
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        WorkbenchTreeSidebar(
            items = state.treeNodes,
            selectedId = state.selectedNodeId,
            onNodeClick = { node ->
                viewModel.selectNode(node.id)
            },
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.32f),
            searchPlaceholder = "搜索产品目录",
            getId = { node -> node.id },
            getLabel = { node -> node.label },
            getChildren = { node -> node.children },
            getIcon = { node -> node.kind.icon() },
            header = {
                state.errorMessage?.let { message ->
                    HostConfigStatusStrip(message)
                }
                state.noticeMessage?.let { message ->
                    HostConfigStatusStrip(message)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WorkbenchActionButton(
                        text = "新建产品",
                        onClick = {
                            openCreate(CatalogEntityType.PRODUCT)
                        },
                        variant = WorkbenchButtonVariant.Default,
                    )
                    WorkbenchActionButton(
                        text = "新建标签",
                        onClick = {
                            openCreate(CatalogEntityType.LABEL)
                        },
                        variant = WorkbenchButtonVariant.Outline,
                    )
                    WorkbenchActionButton(
                        text = if (state.loading) "加载中" else "刷新",
                        onClick = viewModel::refresh,
                        variant = WorkbenchButtonVariant.Outline,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(0.68f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CatalogDetailPanel(
                state = state,
                onCreate = ::openCreate,
                onEdit = ::openEditSelection,
                onDelete = ::deleteCurrentSelection,
            )
            CatalogChildrenPanel(
                state = state,
                onSelectNode = viewModel::selectNode,
            )
            if (state.activeDeviceDefinition != null) {
                CatalogSpecPanel(state = state)
            }
        }
    }

    editorSeed?.let { seed ->
        val metadata = state.metadataFor(seed.entityType) ?: return@let
        CatalogEntityEditorDialog(
            title = if (seed.existingId == null) {
                "新建${metadata.title}"
            } else {
                "编辑${metadata.title}"
            },
            metadata = metadata,
            optionSets = state.optionSets,
            initialValues = seed.initialValues,
            saving = state.busy,
            onDismissRequest = {
                editorSeed = null
            },
            onSaveRequested = { draft ->
                val errorMessage = saveCatalogDraft(
                    seed = seed,
                    draft = draft,
                    viewModel = viewModel,
                )
                if (errorMessage == null) {
                    editorSeed = null
                }
                errorMessage
            },
        )
    }
}

@Composable
private fun CatalogDetailPanel(
    state: CatalogScreenState,
    onCreate: (CatalogEntityType) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val selectedMetadata = state.selectedEntityMetadata
    val selectedValues = state.selectedCatalogValues()
    val createTargets = state.resolveCreateTargets()

    HostConfigPanel(
        title = state.selectedNode?.label ?: "请选择左侧节点",
        subtitle = selectedMetadata?.subtitle ?: "这里展示产品定义、设备定义、属性定义、功能定义与标签定义的元数据驱动详情。",
        actions = {
            createTargets.forEach { target ->
                WorkbenchActionButton(
                    text = "新建${target.displayName()}",
                    onClick = {
                        onCreate(target)
                    },
                    variant = WorkbenchButtonVariant.Outline,
                )
            }
            if (state.selectedNode?.entityType != null) {
                WorkbenchActionButton(
                    text = "编辑",
                    onClick = onEdit,
                    variant = WorkbenchButtonVariant.Secondary,
                )
                WorkbenchActionButton(
                    text = "删除",
                    onClick = onDelete,
                    variant = WorkbenchButtonVariant.Destructive,
                )
            }
        },
    ) {
        if (selectedMetadata == null || selectedValues == null) {
            HostConfigStatusStrip("选中一个目录节点后，这里会按后端元数据自动渲染详情与编辑表单。")
            CupertinoText(
                text = "当前这套产品目录模型与工程实例树解耦，适合作为物联网产品/设备/属性/功能/标签的源数据中心。",
                style = CupertinoTheme.typography.body,
            )
        } else {
            CatalogMetadataDetailContent(
                metadata = selectedMetadata,
                values = selectedValues,
            )
        }
    }
}

@Composable
private fun CatalogChildrenPanel(
    state: CatalogScreenState,
    onSelectNode: (String) -> Unit,
) {
    when {
        state.activeDeviceDefinition != null -> {
            val device = state.activeDeviceDefinition
            val propertyMetadata = state.metadataFor(CatalogEntityType.PROPERTY)
            val featureMetadata = state.metadataFor(CatalogEntityType.FEATURE)
            HostConfigPanel(
                title = "设备能力清单",
                subtitle = "当前设备定义下的属性与功能都会复用同一套元数据渲染器。",
            ) {
                if (device == null || (device.properties.isEmpty() && device.features.isEmpty())) {
                    HostConfigStatusStrip("当前设备定义还没有属性或功能。")
                    return@HostConfigPanel
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    propertyMetadata?.let { metadata ->
                        device.properties.forEach { property ->
                            CatalogEntitySummaryCard(
                                title = property.name,
                                subtitle = property.identifier,
                                metadata = metadata,
                                values = property.toCatalogValues(),
                                onClick = {
                                    onSelectNode(propertyNodeId(property.id))
                                },
                            )
                        }
                    }
                    featureMetadata?.let { metadata ->
                        device.features.forEach { feature ->
                            CatalogEntitySummaryCard(
                                title = feature.name,
                                subtitle = feature.identifier,
                                metadata = metadata,
                                values = feature.toCatalogValues(),
                                onClick = {
                                    onSelectNode(featureNodeId(feature.id))
                                },
                            )
                        }
                    }
                }
            }
        }

        state.selectedProduct != null -> {
            val deviceMetadata = state.metadataFor(CatalogEntityType.DEVICE)
            HostConfigPanel(
                title = "设备定义列表",
                subtitle = "产品下挂哪些设备型号，由这里统一呈现。",
            ) {
                val product = state.selectedProduct
                if (product == null || product.devices.isEmpty()) {
                    HostConfigStatusStrip("当前产品还没有设备定义。")
                    return@HostConfigPanel
                }
                if (deviceMetadata == null) {
                    HostConfigStatusStrip("设备定义元数据缺失。")
                    return@HostConfigPanel
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    product.devices.forEach { device ->
                        CatalogEntitySummaryCard(
                            title = device.name,
                            subtitle = device.code,
                            metadata = deviceMetadata,
                            values = device.toCatalogValues(),
                            onClick = {
                                onSelectNode(deviceNodeId(device.id))
                            },
                        )
                    }
                }
            }
        }

        state.selectedLabelDefinition != null -> {
            val productMetadata = state.metadataFor(CatalogEntityType.PRODUCT)
            val label = state.selectedLabelDefinition
            val linkedProducts = state.products.filter { product ->
                product.labels.any { it.id == label?.id }
            }
            HostConfigPanel(
                title = "标签关联产品",
                subtitle = "标签只保留目录侧语义，不直接侵入工程实例点位。",
            ) {
                if (label == null || linkedProducts.isEmpty()) {
                    HostConfigStatusStrip("当前标签还没有关联任何产品。")
                    return@HostConfigPanel
                }
                if (productMetadata == null) {
                    HostConfigStatusStrip("产品定义元数据缺失。")
                    return@HostConfigPanel
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    linkedProducts.forEach { product ->
                        CatalogEntitySummaryCard(
                            title = product.name,
                            subtitle = product.code,
                            metadata = productMetadata,
                            values = product.toCatalogValues(),
                            onClick = {
                                onSelectNode(productNodeId(product.id))
                            },
                        )
                    }
                }
            }
        }

        else -> {
            HostConfigPanel(
                title = "设计说明",
                subtitle = "这块不是工程树副本，而是产品建模目录。",
            ) {
                HostConfigStatusStrip("产品定义负责型号语义，工程树负责现场实例；两者暂时分离，先把元模型做扎实。")
            }
        }
    }
}

@Composable
private fun CatalogSpecPanel(
    state: CatalogScreenState,
) {
    HostConfigPanel(
        title = "spec-iot 属性预览",
        subtitle = "这里展示后端已经转换好的协议消费视图，后面协议层和遥测层可以直接吃这份定义。",
    ) {
        if (state.specIotProperties.isEmpty()) {
            HostConfigStatusStrip("当前设备定义还没有可导出的属性定义。")
            return@HostConfigPanel
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.specIotProperties.forEach { property ->
                HostConfigPanel(
                    title = property.name ?: property.identifier,
                    subtitle = property.identifier,
                ) {
                    val rows = listOf(
                        "值类型" to property.valueType,
                        "单位" to property.unit.orDash(),
                        "长度" to property.length?.toString().orDash(),
                        "扩展属性" to property.attributes.entries
                            .joinToString("；") { (key, value) -> "$key=$value" }
                            .ifBlank { "未设置" },
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rows.forEach { (label, value) ->
                            HostConfigKeyValueRow(label, value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogEntitySummaryCard(
    title: String,
    subtitle: String?,
    metadata: CatalogEntityMetadataResponse,
    values: Map<String, Any?>,
    onClick: () -> Unit,
) {
    HostConfigPanel(
        title = title,
        subtitle = subtitle,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        CatalogMetadataDetailContent(
            metadata = metadata,
            values = values,
        )
    }
}

@Composable
private fun CatalogEntityEditorDialog(
    title: String,
    metadata: CatalogEntityMetadataResponse,
    optionSets: Map<String, List<CatalogFieldOptionResponse>>,
    initialValues: Map<String, Any?>,
    saving: Boolean,
    onDismissRequest: () -> Unit,
    onSaveRequested: (CatalogFormDraft) -> String?,
) {
    var draft by remember(title, initialValues) {
        mutableStateOf(CatalogFormDraft.from(metadata, initialValues))
    }
    var validationMessage by remember(title, initialValues) {
        mutableStateOf<String?>(null)
    }

    HostConfigDialog(
        title = title,
        onDismissRequest = onDismissRequest,
        actions = {
            WorkbenchActionButton(
                text = "取消",
                onClick = onDismissRequest,
                variant = WorkbenchButtonVariant.Outline,
            )
            WorkbenchActionButton(
                text = if (saving) "保存中" else "保存",
                onClick = {
                    val errorMessage = draft.requiredFieldMessage(metadata) ?: onSaveRequested(draft)
                    validationMessage = errorMessage
                },
                variant = WorkbenchButtonVariant.Default,
                enabled = !saving,
            )
        },
    ) {
        validationMessage?.let { message ->
            HostConfigStatusStrip(message)
        }
        CatalogMetadataFormContent(
            metadata = metadata,
            optionSets = optionSets,
            draft = draft,
            onDraftChange = { nextDraft ->
                draft = nextDraft
                validationMessage = null
            },
        )
    }
}

private data class CatalogEditorSeed(
    val entityType: CatalogEntityType,
    val existingId: Long? = null,
    val productId: Long? = null,
    val deviceDefinitionId: Long? = null,
    val initialValues: Map<String, Any?> = emptyMap(),
)

private fun saveCatalogDraft(
    seed: CatalogEditorSeed,
    draft: CatalogFormDraft,
    viewModel: CatalogViewModel,
): String? {
    return when (seed.entityType) {
        CatalogEntityType.PRODUCT -> {
            if (seed.existingId == null) {
                viewModel.createProduct(draft.toProductCreateRequest())
            } else {
                viewModel.updateProduct(seed.existingId, draft.toProductUpdateRequest())
            }
            null
        }

        CatalogEntityType.DEVICE -> {
            val productId = seed.productId ?: return "缺少所属产品，无法保存设备定义"
            if (seed.existingId == null) {
                viewModel.createDeviceDefinition(productId, draft.toDeviceDefinitionCreateRequest())
            } else {
                viewModel.updateDeviceDefinition(seed.existingId, draft.toDeviceDefinitionUpdateRequest())
            }
            null
        }

        CatalogEntityType.PROPERTY -> {
            val deviceDefinitionId = seed.deviceDefinitionId ?: return "缺少所属设备，无法保存属性定义"
            if (seed.existingId == null) {
                val request = draft.toPropertyDefinitionCreateRequest().getOrElse { throwable ->
                    return throwable.message ?: "扩展属性 JSON 解析失败"
                }
                viewModel.createPropertyDefinition(deviceDefinitionId, request)
            } else {
                val request = draft.toPropertyDefinitionUpdateRequest().getOrElse { throwable ->
                    return throwable.message ?: "扩展属性 JSON 解析失败"
                }
                viewModel.updatePropertyDefinition(seed.existingId, request)
            }
            null
        }

        CatalogEntityType.FEATURE -> {
            val deviceDefinitionId = seed.deviceDefinitionId ?: return "缺少所属设备，无法保存功能定义"
            if (seed.existingId == null) {
                viewModel.createFeatureDefinition(deviceDefinitionId, draft.toFeatureDefinitionCreateRequest())
            } else {
                viewModel.updateFeatureDefinition(seed.existingId, draft.toFeatureDefinitionUpdateRequest())
            }
            null
        }

        CatalogEntityType.LABEL -> {
            if (seed.existingId == null) {
                viewModel.createLabel(draft.toLabelDefinitionCreateRequest())
            } else {
                viewModel.updateLabel(seed.existingId, draft.toLabelDefinitionUpdateRequest())
            }
            null
        }
    }
}

private fun CatalogScreenState.selectedCatalogValues(): Map<String, Any?>? {
    return when (selectedNode?.entityType) {
        CatalogEntityType.PRODUCT -> selectedProduct?.toCatalogValues()
        CatalogEntityType.DEVICE -> selectedDeviceDefinition?.toCatalogValues()
        CatalogEntityType.PROPERTY -> selectedPropertyDefinition?.toCatalogValues()
        CatalogEntityType.FEATURE -> selectedFeatureDefinition?.toCatalogValues()
        CatalogEntityType.LABEL -> selectedLabelDefinition?.toCatalogValues()
        null -> null
    }
}

private fun CatalogScreenState.resolveCreateTargets(): List<CatalogEntityType> {
    return when (selectedNode?.kind) {
        CatalogTreeNodeKind.PRODUCT -> listOf(CatalogEntityType.DEVICE)
        CatalogTreeNodeKind.DEVICE,
        CatalogTreeNodeKind.PROPERTY,
        CatalogTreeNodeKind.FEATURE,
        -> listOf(CatalogEntityType.PROPERTY, CatalogEntityType.FEATURE)

        CatalogTreeNodeKind.LABEL,
        CatalogTreeNodeKind.LABEL_GROUP,
        null,
        -> emptyList()
    }
}

private fun CatalogEntityType.displayName(): String {
    return when (this) {
        CatalogEntityType.PRODUCT -> "产品"
        CatalogEntityType.DEVICE -> "设备"
        CatalogEntityType.PROPERTY -> "属性"
        CatalogEntityType.FEATURE -> "功能"
        CatalogEntityType.LABEL -> "标签"
    }
}
