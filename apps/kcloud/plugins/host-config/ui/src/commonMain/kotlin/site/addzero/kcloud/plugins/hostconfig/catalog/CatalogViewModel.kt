package site.addzero.kcloud.plugins.hostconfig.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.external.CatalogApi

@KoinViewModel
class CatalogViewModel(
    private val catalogApi: CatalogApi,
) : ViewModel() {
    var screenState by mutableStateOf(CatalogScreenState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loadPage()
        }
    }

    fun clearNotice() {
        screenState = screenState.copy(
            noticeMessage = null,
            errorMessage = null,
        )
    }

    fun selectNode(
        nodeId: String,
    ) {
        val node = screenState.treeNodes.findCatalogNode(nodeId) ?: return
        viewModelScope.launch {
            applySelection(node.id)
        }
    }

    fun createProduct(
        request: ProductDefinitionCreateRequest,
    ) {
        mutate("产品定义已创建") {
            val created = catalogApi.createProduct(request)
            productNodeId(created.id)
        }
    }

    fun updateProduct(
        productId: Long,
        request: ProductDefinitionUpdateRequest,
    ) {
        mutate("产品定义已更新") {
            catalogApi.updateProduct(productId, request)
            productNodeId(productId)
        }
    }

    fun deleteProduct(
        productId: Long,
    ) {
        mutate("产品定义已删除") {
            catalogApi.deleteProduct(productId)
            null
        }
    }

    fun createDeviceDefinition(
        productId: Long,
        request: DeviceDefinitionCreateRequest,
    ) {
        mutate("设备定义已创建") {
            val created = catalogApi.createDeviceDefinition(productId, request)
            deviceNodeId(created.id)
        }
    }

    fun updateDeviceDefinition(
        deviceDefinitionId: Long,
        request: DeviceDefinitionUpdateRequest,
    ) {
        mutate("设备定义已更新") {
            catalogApi.updateDeviceDefinition(deviceDefinitionId, request)
            deviceNodeId(deviceDefinitionId)
        }
    }

    fun deleteDeviceDefinition(
        deviceDefinitionId: Long,
        productId: Long?,
    ) {
        mutate("设备定义已删除") {
            catalogApi.deleteDeviceDefinition(deviceDefinitionId)
            productId?.let(::productNodeId)
        }
    }

    fun createPropertyDefinition(
        deviceDefinitionId: Long,
        request: PropertyDefinitionCreateRequest,
    ) {
        mutate("属性定义已创建") {
            val created = catalogApi.createPropertyDefinition(deviceDefinitionId, request)
            propertyNodeId(created.id)
        }
    }

    fun updatePropertyDefinition(
        propertyDefinitionId: Long,
        request: PropertyDefinitionUpdateRequest,
    ) {
        mutate("属性定义已更新") {
            catalogApi.updatePropertyDefinition(propertyDefinitionId, request)
            propertyNodeId(propertyDefinitionId)
        }
    }

    fun deletePropertyDefinition(
        propertyDefinitionId: Long,
        deviceDefinitionId: Long?,
    ) {
        mutate("属性定义已删除") {
            catalogApi.deletePropertyDefinition(propertyDefinitionId)
            deviceDefinitionId?.let(::deviceNodeId)
        }
    }

    fun createFeatureDefinition(
        deviceDefinitionId: Long,
        request: FeatureDefinitionCreateRequest,
    ) {
        mutate("功能定义已创建") {
            val created = catalogApi.createFeatureDefinition(deviceDefinitionId, request)
            featureNodeId(created.id)
        }
    }

    fun updateFeatureDefinition(
        featureDefinitionId: Long,
        request: FeatureDefinitionUpdateRequest,
    ) {
        mutate("功能定义已更新") {
            catalogApi.updateFeatureDefinition(featureDefinitionId, request)
            featureNodeId(featureDefinitionId)
        }
    }

    fun deleteFeatureDefinition(
        featureDefinitionId: Long,
        deviceDefinitionId: Long?,
    ) {
        mutate("功能定义已删除") {
            catalogApi.deleteFeatureDefinition(featureDefinitionId)
            deviceDefinitionId?.let(::deviceNodeId)
        }
    }

    fun createLabel(
        request: LabelDefinitionCreateRequest,
    ) {
        mutate("标签定义已创建") {
            val created = catalogApi.createLabel(request)
            labelNodeId(created.id)
        }
    }

    fun updateLabel(
        labelId: Long,
        request: LabelDefinitionUpdateRequest,
    ) {
        mutate("标签定义已更新") {
            catalogApi.updateLabel(labelId, request)
            labelNodeId(labelId)
        }
    }

    fun deleteLabel(
        labelId: Long,
    ) {
        mutate("标签定义已删除") {
            catalogApi.deleteLabel(labelId)
            LABEL_GROUP_NODE_ID
        }
    }

    private fun mutate(
        successMessage: String,
        action: suspend () -> String?,
    ) {
        viewModelScope.launch {
            screenState = screenState.copy(
                busy = true,
                errorMessage = null,
            )
            runCatching {
                val preferredNodeId = action()
                loadPage(
                    preferredNodeId = preferredNodeId,
                    noticeMessage = successMessage,
                    preserveBusy = true,
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    busy = false,
                    errorMessage = throwable.message ?: "目录操作失败",
                )
            }
        }
    }

    private suspend fun loadPage(
        preferredNodeId: String? = screenState.selectedNodeId,
        noticeMessage: String? = screenState.noticeMessage,
        preserveBusy: Boolean = false,
    ) {
        screenState = screenState.copy(
            loading = true,
            busy = preserveBusy,
            errorMessage = null,
        )
        runCatching {
            val snapshot = catalogApi.getSnapshot()
            val treeNodes = buildCatalogTreeNodes(snapshot.products, snapshot.labels)
            val selectedNodeId = preferredNodeId
                ?.takeIf { nodeId -> treeNodes.findCatalogNode(nodeId) != null }
                ?: treeNodes.firstOrNull()?.id
            val specIotProperties = loadSpecIotProperties(treeNodes, selectedNodeId)
            screenState = CatalogScreenState(
                loading = false,
                busy = false,
                products = snapshot.products,
                labels = snapshot.labels,
                metadata = snapshot.metadata,
                treeNodes = treeNodes,
                selectedNodeId = selectedNodeId,
                specIotProperties = specIotProperties,
                noticeMessage = noticeMessage,
            )
        }.onFailure { throwable ->
            screenState = screenState.copy(
                loading = false,
                busy = false,
                errorMessage = throwable.message ?: "加载产品目录失败",
            )
        }
    }

    private suspend fun applySelection(
        selectedNodeId: String?,
    ) {
        val resolvedNodeId = selectedNodeId
            ?.takeIf { nodeId -> screenState.treeNodes.findCatalogNode(nodeId) != null }
        val specIotProperties = loadSpecIotProperties(screenState.treeNodes, resolvedNodeId)
        screenState = screenState.copy(
            selectedNodeId = resolvedNodeId,
            specIotProperties = specIotProperties,
        )
    }

    private suspend fun loadSpecIotProperties(
        treeNodes: List<CatalogTreeNode>,
        selectedNodeId: String?,
    ) = treeNodes
        .findCatalogNode(selectedNodeId)
        ?.deviceDefinitionId
        ?.let { deviceDefinitionId ->
            catalogApi.listSpecIotProperties(deviceDefinitionId)
        }
        .orEmpty()
}
