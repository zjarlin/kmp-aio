package site.addzero.kcloud.plugins.codegencontext.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import site.addzero.cupertino.workbench.components.field.CupertinoOption
import site.addzero.cupertino.workbench.components.field.CupertinoSelectionField
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.form.CupertinoFormGrid
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextParamDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextBindingEditorState
import site.addzero.kcloud.plugins.codegencontext.context.inputPlaceholder
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenContextValueType

@Composable
internal fun BindingEditorSection(
    title: String,
    definitions: List<CodegenContextDefinitionDto>,
    bindings: List<CodegenContextBindingEditorState>,
    onValueChange: (definitionCode: String, paramCode: String, value: String) -> Unit,
) {
    CupertinoPanel(
        title = title,
        subtitle = "这些 context 参数决定协议层如何理解当前方法或字段。",
    ) {
        if (definitions.isEmpty()) {
            CupertinoStatusStrip("当前协议模板没有可编辑的上下文定义。")
            return@CupertinoPanel
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            definitions.forEach { definition ->
                val binding = bindings.firstOrNull { it.definitionId == definition.id || it.definitionCode == definition.code }
                CupertinoPanel(title = definition.name, subtitle = definition.description ?: definition.code) {
                    if (definition.bindingTargetMode.name == "MULTIPLE") {
                        CupertinoStatusStrip("当前定义支持多组绑定，V1 先按单组参数编辑。")
                    }
                    if (definition.params.isEmpty()) {
                        CupertinoStatusStrip("该定义没有额外参数。")
                    } else {
                        CupertinoFormGrid {
                            definition.params.forEach { param ->
                                val currentValue = binding
                                    ?.values
                                    ?.firstOrNull { it.paramDefinitionId == param.id || it.paramCode == param.code }
                                    ?.value
                                    .orEmpty()
                                if (param.valueType == CodegenContextValueType.TEXT) {
                                    fullWidth {
                                        BindingValueField(param, currentValue) { next ->
                                            onValueChange(definition.code, param.code, next)
                                        }
                                    }
                                } else {
                                    item {
                                        BindingValueField(param, currentValue) { next ->
                                            onValueChange(definition.code, param.code, next)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BindingValueField(
    param: CodegenContextParamDefinitionDto,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val description = bindingFieldDescription(param)
    when (param.valueType) {
        CodegenContextValueType.ENUM -> CupertinoSelectionField(
            label = param.name,
            options = param.enumOptions.map { CupertinoOption(it, it) },
            selectedValue = value.takeIf { it.isNotBlank() },
            onSelected = { onValueChange(it.orEmpty()) },
            description = description,
            allowClear = !param.required,
        )

        CodegenContextValueType.BOOLEAN -> CupertinoSelectionField(
            label = param.name,
            options = listOf(CupertinoOption("true", "true"), CupertinoOption("false", "false")),
            selectedValue = value.takeIf { it.isNotBlank() },
            onSelected = { onValueChange(it.orEmpty()) },
            description = description,
            allowClear = !param.required,
        )

        else -> CupertinoTextField(
            label = param.name,
            value = value,
            onValueChange = onValueChange,
            singleLine = param.valueType != CodegenContextValueType.TEXT,
            placeholder = param.placeholder ?: param.valueType.inputPlaceholder(),
            description = description,
        )
    }
}
