/* coding-playground-managed
projectId=36e18e9c-fa98-4430-ac25-22c4d33c5048
targetId=d4af533d-440f-424a-9938-fcd79a0adf68
fileId=eac04cf1-c779-44f9-b855-a47046abc917
declarationIds=7cc6e732-36a4-49d5-a3ee-4789c1c2f4f3
metadataHash=a054fb2e78308318cd6b570222cdc28d2a33aff41721560d2f3e6dc0d077402c
contentHash=2a99d9b604a24bb6933b77a244cbc3800dfe99bc1b66ffd2817b9dffba8e2426
*/

package site.addzero.generated.dto

import site.addzero.coding.playground.annotations.GeneratedManagedDeclaration

/**
 * SampleFeature 新增与编辑请求。
 *
 * @author zjarlin
 * @date 2026/03/29
 * @constructor 创建[SampleFeatureRequest]
 * @param [name]
 * @param [code]
 * @param [remark]
 */
@GeneratedManagedDeclaration(declarationId = "7cc6e732-36a4-49d5-a3ee-4789c1c2f4f3", fileId = "eac04cf1-c779-44f9-b855-a47046abc917", presetType = "DATA_CLASS")
data class SampleFeatureRequest(
    val name: String = "",
    val code: String = "",
    val remark: String = ""
)