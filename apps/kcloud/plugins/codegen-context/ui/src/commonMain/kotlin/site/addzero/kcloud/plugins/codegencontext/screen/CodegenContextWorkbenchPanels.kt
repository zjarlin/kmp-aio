package site.addzero.kcloud.plugins.codegencontext.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.components.field.CupertinoBooleanField
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.form.CupertinoFormGrid
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.codegencontext.api.context.CodegenContextDefinitionDto
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextEditorState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel
import site.addzero.kcloud.plugins.codegencontext.context.CodegenMethodEditorState
import site.addzero.kcloud.plugins.codegencontext.context.CodegenPropertyEditorState

@Composable
internal fun PropertyPoolPanel(
    editor: CodegenContextEditorState,
    viewModel: CodegenContextViewModel,
) {
    CupertinoPanel(
        title = "物模型",
        subtitle = "把寄存器、线圈、状态位等能力先抽象成可复用物模型字段，再被不同设备功能引用。",
        actions = {
            WorkbenchActionButton(
                text = "剪贴板导入",
                onClick = viewModel::importPropertiesFromClipboard,
                variant = WorkbenchButtonVariant.Outline,
            )
            WorkbenchActionButton(text = "新增字段", onClick = viewModel::addProperty)
        },
    ) {
        CupertinoStatusStrip("支持多行导入。每一行一个中文字段描述；属性名和默认类型都可以先留空，保存时统一补齐。")
        if (editor.properties.isEmpty()) {
            CupertinoStatusStrip("当前还没有物模型字段。先从剪贴板导入一批中文字段描述会更高效。")
            return@CupertinoPanel
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            editor.properties.forEachIndexed { propertyIndex, property ->
                PropertyEditorCard(
                    propertyIndex = propertyIndex,
                    property = property,
                    usageCount = editor.methods.count { method -> property.editorKey in method.selectedPropertyKeys },
                    definitions = editor.propertyDefinitions(),
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun PropertyEditorCard(
    propertyIndex: Int,
    property: CodegenPropertyEditorState,
    usageCount: Int,
    definitions: List<CodegenContextDefinitionDto>,
    viewModel: CodegenContextViewModel,
) {
    CupertinoPanel(
        title = property.name.ifBlank { property.propertyName.ifBlank { "未命名物模型字段" } },
        subtitle = property.signaturePreview(),
        actions = {
            WorkbenchActionButton(
                text = "删除字段",
                onClick = { viewModel.removeProperty(propertyIndex) },
                variant = WorkbenchButtonVariant.Destructive,
            )
        },
    ) {
        CupertinoStatusStrip("当前被 $usageCount 个设备功能引用。")
        CupertinoStatusStrip(property.modbusSummary())
        CupertinoFormGrid {
            item {
                CupertinoTextField(
                    label = "字段描述",
                    value = property.name,
                    onValueChange = { viewModel.updatePropertyName(propertyIndex, it) },
                    placeholder = "例如 设备地址",
                    description = "直接输入字段含义即可；属性名留空时由服务端自动生成。",
                )
            }
            item {
                CupertinoTextField(
                    label = "属性名",
                    value = property.propertyName,
                    onValueChange = { value -> viewModel.updateProperty(propertyIndex) { it.copy(propertyName = value) } },
                    placeholder = "留空后由服务端自动生成",
                    description = "需要强制指定字段名时再填写。",
                )
            }
            item {
                CupertinoTextField(
                    label = "Kotlin 类型",
                    value = property.typeName,
                    onValueChange = { value -> viewModel.updateProperty(propertyIndex) { it.copy(typeName = value) } },
                    description = "留空时由服务端按 transportType 推断，也可以手动覆盖。",
                )
            }
            item {
                CupertinoBooleanField(
                    label = "可空",
                    checked = property.nullable,
                    onCheckedChange = { checked -> viewModel.updateProperty(propertyIndex) { it.copy(nullable = checked) } },
                    description = "控制生成实体字段是否可空。",
                )
            }
            item {
                CupertinoTextField(
                    label = "默认值字面量",
                    value = property.defaultLiteral,
                    onValueChange = { value -> viewModel.updateProperty(propertyIndex) { it.copy(defaultLiteral = value) } },
                    description = "例如 0、false、\"\"。",
                )
            }
            fullWidth {
                CupertinoTextField(
                    label = "字段备注",
                    value = property.description,
                    onValueChange = { value -> viewModel.updateProperty(propertyIndex) { it.copy(description = value) } },
                    singleLine = false,
                )
            }
        }
        BindingEditorSection(
            title = "物模型字段上下文",
            definitions = definitions,
            bindings = property.bindings,
            onValueChange = { definitionCode, paramCode, value ->
                viewModel.updatePropertyBindingValue(propertyIndex, definitionCode, paramCode, value)
            },
        )
    }
}

@Composable
internal fun MethodWorkbenchPanel(
    editor: CodegenContextEditorState,
    viewModel: CodegenContextViewModel,
) {
    CupertinoPanel(
        title = "设备功能",
        subtitle = "功能像定义 Kotlin 接口方法一样维护：填中文语义、补充方法名、绑定物模型字段，再配置协议上下文。",
        actions = {
            WorkbenchActionButton(
                text = "剪贴板导入",
                onClick = viewModel::importMethodsFromClipboard,
                variant = WorkbenchButtonVariant.Outline,
            )
            WorkbenchActionButton(
                text = "新增功能",
                onClick = viewModel::addMethod,
                imageVector = Icons.Outlined.Code,
            )
        },
    ) {
        CupertinoStatusStrip("支持多行导入。每一行一个中文功能描述；methodName 留空时由服务端统一生成。")
        if (editor.methods.isEmpty()) {
            CupertinoStatusStrip("当前还没有设备功能。建议先复制一批中文功能描述直接导入。")
            return@CupertinoPanel
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            editor.methods.forEachIndexed { methodIndex, method ->
                MethodEditorCard(methodIndex, method, editor, viewModel)
            }
        }
    }
}

@Composable
private fun MethodEditorCard(
    methodIndex: Int,
    method: CodegenMethodEditorState,
    editor: CodegenContextEditorState,
    viewModel: CodegenContextViewModel,
) {
    CupertinoPanel(
        title = method.name.ifBlank { method.effectiveMethodName() },
        subtitle = method.signaturePreview(),
        actions = {
            WorkbenchActionButton(
                text = "删除功能",
                onClick = { viewModel.removeMethod(methodIndex) },
                variant = WorkbenchButtonVariant.Destructive,
            )
        },
    ) {
        CupertinoStatusStrip("请求实体：${method.requestModelPreview()} · 响应实体：${method.responseModelPreview()}")
        CupertinoStatusStrip(method.modbusSummary())
        CupertinoStatusStrip("当前界面只维护功能语义和绑定关系，真正的合法类型名由服务端在保存时统一校正。")
        CupertinoFormGrid {
            item {
                CupertinoTextField(
                    label = "功能描述",
                    value = method.name,
                    onValueChange = { viewModel.updateMethodName(methodIndex, it) },
                    placeholder = "例如 读取 Flash 持久化配置",
                    description = "直接输入方法语义即可；methodName 留空时由服务端自动生成。",
                )
            }
            item {
                CupertinoTextField(
                    label = "方法名",
                    value = method.methodName,
                    onValueChange = { value -> viewModel.updateMethod(methodIndex) { it.copy(methodName = value) } },
                    placeholder = "留空后由服务端自动生成",
                    description = "需要手工指定 Kotlin 方法名时再填写。",
                )
            }
            fullWidth {
                CupertinoTextField(
                    label = "功能备注",
                    value = method.description,
                    onValueChange = { value -> viewModel.updateMethod(methodIndex) { it.copy(description = value) } },
                    singleLine = false,
                )
            }
        }
        PropertyTransferSection(
            method = method,
            properties = editor.properties,
            onToggle = { viewModel.toggleMethodPropertySelection(methodIndex, it) },
        )
        BindingEditorSection(
            title = "设备功能上下文",
            definitions = editor.methodDefinitions(),
            bindings = method.bindings,
            onValueChange = { definitionCode, paramCode, value ->
                viewModel.updateMethodBindingValue(methodIndex, definitionCode, paramCode, value)
            },
        )
    }
}

@Composable
private fun PropertyTransferSection(
    method: CodegenMethodEditorState,
    properties: List<CodegenPropertyEditorState>,
    onToggle: (String) -> Unit,
) {
    val selectedKeys = method.selectedPropertyKeys.toSet()
    val selectedProperties = properties.filter { it.editorKey in selectedKeys }
    val availableProperties = properties.filterNot { it.editorKey in selectedKeys }
    CupertinoPanel(
        title = "功能关联字段",
        subtitle = "左侧是未绑定的物模型字段，右侧是当前设备功能使用的字段集合。通过穿梭框复用物模型。",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PropertyBucket(
                title = "可选字段",
                subtitle = "点击“加入”绑定到当前设备功能",
                properties = availableProperties,
                actionText = "加入",
                onAction = { onToggle(it.editorKey) },
                modifier = Modifier.weight(1f),
            )
            Box(modifier = Modifier.width(8.dp).fillMaxHeight())
            PropertyBucket(
                title = "已绑定字段",
                subtitle = "这些字段会进入该功能关联的请求或响应模型，模型名按约定自动生成。",
                properties = selectedProperties,
                actionText = "移除",
                onAction = { onToggle(it.editorKey) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PropertyBucket(
    title: String,
    subtitle: String,
    properties: List<CodegenPropertyEditorState>,
    actionText: String,
    onAction: (CodegenPropertyEditorState) -> Unit,
    modifier: Modifier = Modifier,
) {
    CupertinoPanel(title = title, subtitle = subtitle, modifier = modifier) {
        if (properties.isEmpty()) {
            CupertinoStatusStrip("暂无字段。")
            return@CupertinoPanel
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            properties.forEach { property ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        CupertinoText(
                            text = property.name.ifBlank { property.propertyName },
                            style = CupertinoTheme.typography.body,
                        )
                        CupertinoText(
                            text = property.signaturePreview(),
                            style = CupertinoTheme.typography.footnote,
                            color = CupertinoTheme.colorScheme.secondaryLabel,
                        )
                        property.description.takeIf { it.isNotBlank() }?.let { description ->
                            CupertinoText(
                                text = description,
                                style = CupertinoTheme.typography.footnote,
                                color = CupertinoTheme.colorScheme.secondaryLabel,
                            )
                        }
                    }
                    WorkbenchActionButton(
                        text = actionText,
                        onClick = { onAction(property) },
                        variant = WorkbenchButtonVariant.Outline,
                    )
                }
            }
        }
    }
}
