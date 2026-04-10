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
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextBindingDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextParamDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.model.enums.CodegenContextValueType

@Composable
internal fun BindingEditorSection(
    title: String,
    definitions: List<CodegenContextDefinitionDto>,
    bindings: List<CodegenContextBindingDto>,
    onValueChange: (definitionCode: String, paramCode: String, value: String) -> Unit,
) {
    CupertinoPanel(
        title = title,
        subtitle = "这里直接编辑 definition 参数原值，不在前端灌协议默认值。",
    ) {
        if (definitions.isEmpty()) {
            CupertinoStatusStrip("当前协议模板没有可编辑的上下文定义。")
            return@CupertinoPanel
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            definitions.forEach { definition ->
                val binding = bindings.firstOrNull { item -> item.definitionId == definition.id || item.definitionCode == definition.code }
                CupertinoPanel(title = definition.name, subtitle = definition.description ?: definition.code) {
                    if (definition.params.isEmpty()) {
                        CupertinoStatusStrip("该定义没有额外参数。")
                    } else {
                        CupertinoFormGrid {
                            definition.params.forEach { param ->
                                val value =
                                    binding
                                        ?.values
                                        ?.firstOrNull { item -> item.paramDefinitionId == param.id || item.paramCode == param.code }
                                        ?.value
                                        .orEmpty()
                                if (param.valueType == CodegenContextValueType.TEXT) {
                                    fullWidth {
                                        BindingValueField(param, value) { next ->
                                            onValueChange(definition.code, param.code, next)
                                        }
                                    }
                                } else {
                                    item {
                                        BindingValueField(param, value) { next ->
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
    when (param.valueType) {
        CodegenContextValueType.ENUM ->
            CupertinoSelectionField(
                label = param.name,
                options = param.enumOptions.map { option -> CupertinoOption(option, option) },
                selectedValue = value.takeIf(String::isNotBlank),
                onSelected = { next -> onValueChange(next.orEmpty()) },
                allowClear = !param.required,
                description = param.description,
            )

        CodegenContextValueType.BOOLEAN ->
            CupertinoSelectionField(
                label = param.name,
                options = listOf(CupertinoOption("true", "true"), CupertinoOption("false", "false")),
                selectedValue = value.takeIf(String::isNotBlank),
                onSelected = { next -> onValueChange(next.orEmpty()) },
                allowClear = !param.required,
                description = param.description,
            )

        else ->
            CupertinoTextField(
                label = param.name,
                value = value,
                onValueChange = onValueChange,
                singleLine = param.valueType != CodegenContextValueType.TEXT,
                placeholder = param.placeholder ?: defaultPlaceholder(param.valueType),
                description = param.description,
            )
    }
}

private fun defaultPlaceholder(
    valueType: CodegenContextValueType,
): String =
    when (valueType) {
        CodegenContextValueType.STRING -> "输入字符串"
        CodegenContextValueType.TEXT -> "输入多行文本"
        CodegenContextValueType.INT -> "输入整数"
        CodegenContextValueType.LONG -> "输入长整数"
        CodegenContextValueType.DECIMAL -> "输入小数"
        CodegenContextValueType.BOOLEAN -> "true / false"
        CodegenContextValueType.ENUM -> "选择枚举值"
        CodegenContextValueType.PATH -> "/abs/path"
    }
