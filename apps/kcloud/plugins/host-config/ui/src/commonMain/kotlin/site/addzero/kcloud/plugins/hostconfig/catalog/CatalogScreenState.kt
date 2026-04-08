package site.addzero.kcloud.plugins.hostconfig.catalog

import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogEntityMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogEntityType
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogFieldOptionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.CatalogMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.DeviceDefinitionTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.FeatureDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.LabelDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.ProductDefinitionTreeResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.PropertyDefinitionResponse
import site.addzero.kcloud.plugins.hostconfig.api.catalog.SpecIotPropertyResponse

data class CatalogScreenState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
    val products: List<ProductDefinitionTreeResponse> = emptyList(),
    val labels: List<LabelDefinitionResponse> = emptyList(),
    val metadata: CatalogMetadataResponse = CatalogMetadataResponse(emptyList()),
    val treeNodes: List<CatalogTreeNode> = emptyList(),
    val selectedNodeId: String? = null,
    val specIotProperties: List<SpecIotPropertyResponse> = emptyList(),
) {
    val optionSets: Map<String, List<CatalogFieldOptionResponse>>
        get() = metadata.optionSets.associate { it.key to it.options }

    val selectedNode: CatalogTreeNode?
        get() = treeNodes.findCatalogNode(selectedNodeId)

    val selectedEntityMetadata: CatalogEntityMetadataResponse?
        get() = selectedNode?.entityType?.let(::metadataFor)

    val selectedProduct: ProductDefinitionTreeResponse?
        get() = when (selectedNode?.entityType) {
            CatalogEntityType.PRODUCT -> selectedNode?.entityId?.let(products::findProduct)
            else -> selectedNode?.productId?.let(products::findProduct)
        }

    val selectedDeviceDefinition: DeviceDefinitionTreeResponse?
        get() = when (selectedNode?.entityType) {
            CatalogEntityType.DEVICE -> selectedNode?.entityId?.let(products::findDeviceDefinition)
            CatalogEntityType.PROPERTY,
            CatalogEntityType.FEATURE,
            -> selectedNode?.deviceDefinitionId?.let(products::findDeviceDefinition)
            else -> null
        }

    val selectedPropertyDefinition: PropertyDefinitionResponse?
        get() = selectedNode
            ?.takeIf { node -> node.entityType == CatalogEntityType.PROPERTY }
            ?.entityId
            ?.let(products::findPropertyDefinition)

    val selectedFeatureDefinition: FeatureDefinitionResponse?
        get() = selectedNode
            ?.takeIf { node -> node.entityType == CatalogEntityType.FEATURE }
            ?.entityId
            ?.let(products::findFeatureDefinition)

    val selectedLabelDefinition: LabelDefinitionResponse?
        get() = selectedNode
            ?.takeIf { node -> node.entityType == CatalogEntityType.LABEL }
            ?.entityId
            ?.let(labels::findLabelDefinition)

    val activeDeviceDefinition: DeviceDefinitionTreeResponse?
        get() = selectedNode?.deviceDefinitionId?.let(products::findDeviceDefinition)
}

fun CatalogScreenState.metadataFor(
    entityType: CatalogEntityType,
): CatalogEntityMetadataResponse? {
    return metadata.entities.firstOrNull { metadataItem ->
        metadataItem.entityType == entityType
    }
}

private fun List<ProductDefinitionTreeResponse>.findProduct(
    productId: Long,
): ProductDefinitionTreeResponse? {
    return firstOrNull { product -> product.id == productId }
}

private fun List<ProductDefinitionTreeResponse>.findDeviceDefinition(
    deviceDefinitionId: Long,
): DeviceDefinitionTreeResponse? {
    return asSequence()
        .flatMap { product -> product.devices.asSequence() }
        .firstOrNull { device -> device.id == deviceDefinitionId }
}

private fun List<ProductDefinitionTreeResponse>.findPropertyDefinition(
    propertyDefinitionId: Long,
): PropertyDefinitionResponse? {
    return asSequence()
        .flatMap { product -> product.devices.asSequence() }
        .flatMap { device -> device.properties.asSequence() }
        .firstOrNull { property -> property.id == propertyDefinitionId }
}

private fun List<ProductDefinitionTreeResponse>.findFeatureDefinition(
    featureDefinitionId: Long,
): FeatureDefinitionResponse? {
    return asSequence()
        .flatMap { product -> product.devices.asSequence() }
        .flatMap { device -> device.features.asSequence() }
        .firstOrNull { feature -> feature.id == featureDefinitionId }
}

private fun List<LabelDefinitionResponse>.findLabelDefinition(
    labelId: Long,
): LabelDefinitionResponse? {
    return firstOrNull { label -> label.id == labelId }
}
