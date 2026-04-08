package site.addzero.kcloud.plugins.hostconfig.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.ui.graphics.vector.ImageVector
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogEntityType
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionResponse

enum class CatalogTreeNodeKind {
    PRODUCT,
    DEVICE,
    PROPERTY,
    FEATURE,
    LABEL_GROUP,
    LABEL,
}

data class CatalogTreeNode(
    val id: String,
    val kind: CatalogTreeNodeKind,
    val label: String,
    val caption: String? = null,
    val entityType: CatalogEntityType? = null,
    val entityId: Long? = null,
    val productId: Long? = null,
    val deviceDefinitionId: Long? = null,
    val children: List<CatalogTreeNode> = emptyList(),
)

fun CatalogTreeNodeKind.icon(): ImageVector {
    return when (this) {
        CatalogTreeNodeKind.PRODUCT -> Icons.Outlined.SettingsApplications
        CatalogTreeNodeKind.DEVICE -> Icons.Outlined.DeviceHub
        CatalogTreeNodeKind.PROPERTY -> Icons.Outlined.Memory
        CatalogTreeNodeKind.FEATURE -> Icons.Outlined.CloudQueue
        CatalogTreeNodeKind.LABEL_GROUP,
        CatalogTreeNodeKind.LABEL,
        -> Icons.Outlined.Tag
    }
}

fun buildCatalogTreeNodes(
    products: List<ProductDefinitionTreeResponse>,
    labels: List<LabelDefinitionResponse>,
): List<CatalogTreeNode> {
    return buildList {
        addAll(products.map(ProductDefinitionTreeResponse::toTreeNode))
        if (labels.isNotEmpty()) {
            add(
                CatalogTreeNode(
                    id = LABEL_GROUP_NODE_ID,
                    kind = CatalogTreeNodeKind.LABEL_GROUP,
                    label = "标签字典",
                    caption = "${labels.size} 个标签",
                    children = labels.map(LabelDefinitionResponse::toTreeNode),
                ),
            )
        }
    }
}

fun List<CatalogTreeNode>.findCatalogNode(
    nodeId: String?,
): CatalogTreeNode? {
    if (nodeId == null) {
        return null
    }
    fun search(nodes: List<CatalogTreeNode>): CatalogTreeNode? {
        nodes.forEach { node ->
            if (node.id == nodeId) {
                return node
            }
            search(node.children)?.let { child ->
                return child
            }
        }
        return null
    }
    return search(this)
}

fun productNodeId(productId: Long): String = "product:$productId"

fun deviceNodeId(deviceDefinitionId: Long): String = "device:$deviceDefinitionId"

fun propertyNodeId(propertyDefinitionId: Long): String = "property:$propertyDefinitionId"

fun featureNodeId(featureDefinitionId: Long): String = "feature:$featureDefinitionId"

fun labelNodeId(labelId: Long): String = "label:$labelId"

const val LABEL_GROUP_NODE_ID: String = "label-group"

private fun ProductDefinitionTreeResponse.toTreeNode(): CatalogTreeNode {
    return CatalogTreeNode(
        id = productNodeId(id),
        kind = CatalogTreeNodeKind.PRODUCT,
        label = name,
        caption = code,
        entityType = CatalogEntityType.PRODUCT,
        entityId = id,
        productId = id,
        children = devices.map { device ->
            device.toTreeNode(productId = id)
        },
    )
}

private fun DeviceDefinitionTreeResponse.toTreeNode(
    productId: Long,
): CatalogTreeNode {
    return CatalogTreeNode(
        id = deviceNodeId(id),
        kind = CatalogTreeNodeKind.DEVICE,
        label = name,
        caption = code,
        entityType = CatalogEntityType.DEVICE,
        entityId = id,
        productId = productId,
        deviceDefinitionId = id,
        children = buildList {
            addAll(properties.map { property -> property.toTreeNode(productId, id) })
            addAll(features.map { feature -> feature.toTreeNode(productId, id) })
        },
    )
}

private fun PropertyDefinitionResponse.toTreeNode(
    productId: Long,
    deviceDefinitionId: Long,
): CatalogTreeNode {
    return CatalogTreeNode(
        id = propertyNodeId(id),
        kind = CatalogTreeNodeKind.PROPERTY,
        label = name,
        caption = identifier,
        entityType = CatalogEntityType.PROPERTY,
        entityId = id,
        productId = productId,
        deviceDefinitionId = deviceDefinitionId,
    )
}

private fun FeatureDefinitionResponse.toTreeNode(
    productId: Long,
    deviceDefinitionId: Long,
): CatalogTreeNode {
    return CatalogTreeNode(
        id = featureNodeId(id),
        kind = CatalogTreeNodeKind.FEATURE,
        label = name,
        caption = identifier,
        entityType = CatalogEntityType.FEATURE,
        entityId = id,
        productId = productId,
        deviceDefinitionId = deviceDefinitionId,
    )
}

private fun LabelDefinitionResponse.toTreeNode(): CatalogTreeNode {
    return CatalogTreeNode(
        id = labelNodeId(id),
        kind = CatalogTreeNodeKind.LABEL,
        label = name,
        caption = code,
        entityType = CatalogEntityType.LABEL,
        entityId = id,
    )
}
