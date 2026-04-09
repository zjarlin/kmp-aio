package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.components.field.CupertinoBooleanField
import site.addzero.cupertino.workbench.components.field.CupertinoOption
import site.addzero.cupertino.workbench.components.field.CupertinoSelectionField
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.form.CupertinoFormGrid
import site.addzero.cupertino.workbench.components.form.CupertinoFormGridScope
import site.addzero.cupertino.workbench.components.form.CupertinoFormSection
import site.addzero.cupertino.workbench.components.panel.CupertinoKeyValueRow
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldKey
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldMetadataResponse
import site.addzero.kcloud.plugins.hostconfig.api.template.ProtocolTransportFieldWidget
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder2
import site.addzero.kcloud.plugins.hostconfig.model.enums.ByteOrder4
import site.addzero.kcloud.plugins.hostconfig.model.enums.FloatOrder
import site.addzero.kcloud.plugins.hostconfig.model.enums.Parity
import site.addzero.kcloud.plugins.hostconfig.model.enums.PointType
import site.addzero.kcloud.plugins.hostconfig.common.label
import site.addzero.kcloud.plugins.hostconfig.common.orDash

@Composable
/**
 * 处理项目editorform。
 *
 * @param draft draft。
 * @param onDraftChange ondraftchange。
 * @param showSortShortcut show排序shortcut。
 * @param onUpdateSort on更新排序。
 */
internal fun ProjectEditorForm(
    draft: ProjectDraft,
    onDraftChange: (ProjectDraft) -> Unit,
    showSortShortcut: Boolean = false,
    onUpdateSort: (() -> Unit)? = null,
) {
    CupertinoFormSection(
        title = "基础信息",
        subtitle = "工程名称、排序和说明文案默认首屏可见。",
    ) {
        item {
            CupertinoTextField("工程名称", draft.name, { onDraftChange(draft.copy(name = it)) })
        }
        item {
            CupertinoTextField("排序", draft.sortIndex, { onDraftChange(draft.copy(sortIndex = it)) })
        }
        fullWidth {
            CupertinoTextField(
                label = "工程描述",
                value = draft.description,
                onValueChange = { onDraftChange(draft.copy(description = it)) },
                singleLine = false,
            )
        }
        fullWidth {
            CupertinoTextField(
                label = "备注",
                value = draft.remark,
                onValueChange = { onDraftChange(draft.copy(remark = it)) },
                singleLine = false,
            )
        }
    }

    if (showSortShortcut && onUpdateSort != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            WorkbenchActionButton(
                text = "仅更新排序",
                onClick = onUpdateSort,
                variant = WorkbenchButtonVariant.Secondary,
            )
        }
    }
}

@Composable
/**
 * 处理协议editorform。
 *
 * @param protocolTemplates 协议模板。
 * @param existing existing。
 * @param draft draft。
 * @param onDraftChange ondraftchange。
 */
internal fun ProtocolEditorForm(
    protocolTemplates: List<site.addzero.kcloud.plugins.hostconfig.api.template.TemplateOptionResponse>,
    existing: site.addzero.kcloud.plugins.hostconfig.api.project.ProtocolTreeNode?,
    draft: ProtocolDraft,
    onDraftChange: (ProtocolDraft) -> Unit,
) {
    val template = protocolTemplates.firstOrNull { item -> item.id == draft.protocolTemplateId }
    val transportForm = template?.metadata?.transportForm

    if (existing == null) {
        CupertinoFormSection(
            title = "协议基本信息",
            subtitle = "先定义协议实例名称、模板与轮询节奏。",
        ) {
            item {
                CupertinoTextField("协议名称", draft.name, { onDraftChange(draft.copy(name = it)) })
            }
            item {
                CupertinoSelectionField(
                    label = "协议模板",
                    options = protocolTemplates.map { item -> CupertinoOption(item.id, item.name, item.description) },
                    selectedValue = draft.protocolTemplateId,
                    onSelected = { onDraftChange(draft.copy(protocolTemplateId = it)) },
                )
            }
            item {
                CupertinoTextField("轮询时间(ms)", draft.pollingIntervalMs, { onDraftChange(draft.copy(pollingIntervalMs = it)) })
            }
            item {
                CupertinoTextField("排序", draft.sortIndex, { onDraftChange(draft.copy(sortIndex = it)) })
            }
        }
    } else {
        CupertinoPanel(
            title = "协议关联",
            subtitle = "工程侧只维护协议字典关联和通信参数，不再编辑字典主数据。",
        ) {
            CupertinoKeyValueRow(
                "协议字典",
                existing.protocolTemplateName.takeIf { it.isNotBlank() } ?: existing.name,
            )
            CupertinoKeyValueRow("模板编码", existing.protocolTemplateCode)
        }
        CupertinoFormSection(
            title = "运行参数",
            subtitle = "这里调整轮询节奏和具体链路参数。",
        ) {
            item {
                CupertinoTextField("轮询时间(ms)", draft.pollingIntervalMs, { onDraftChange(draft.copy(pollingIntervalMs = it)) })
            }
            item {
                CupertinoTextField("排序", draft.sortIndex, { onDraftChange(draft.copy(sortIndex = it)) })
            }
        }
    }

    CupertinoFormSection(
        title = transportForm?.title ?: "通信配置",
        subtitle = transportForm?.subtitle ?: "模块通信参数已经上提到协议层，这里只保留协议级连接参数。",
    ) {
        val fields = transportForm?.fields.orEmpty()
        if (fields.isEmpty()) {
            fullWidth {
                CupertinoStatusStrip("当前协议模板没有额外通信字段。")
            }
        } else {
            fields.forEach { field ->
                renderProtocolTransportField(
                    field = field,
                    draft = draft,
                    onDraftChange = onDraftChange,
                )
            }
            fields.mapNotNull { field ->
                field.helperText?.takeIf { helper -> helper.isNotBlank() }?.let { helper ->
                    field.label to helper
                }
            }.forEach { (label, helperText) ->
                fullWidth {
                    CupertinoStatusStrip("$label：$helperText")
                }
            }
        }
    }
}

private fun CupertinoFormGridScope.renderProtocolTransportField(
    field: ProtocolTransportFieldMetadataResponse,
    draft: ProtocolDraft,
    onDraftChange: (ProtocolDraft) -> Unit,
) {
    when (field.widget) {
        ProtocolTransportFieldWidget.SELECT -> {
            item {
                CupertinoSelectionField(
                    label = field.label,
                    options = field.toCupertinoOptions(),
                    selectedValue = draft.transportFieldValue(field.key).ifBlank { null },
                    onSelected = { selected ->
                        onDraftChange(
                            draft.withTransportFieldValue(
                                key = field.key,
                                value = selected.orEmpty(),
                            ),
                        )
                    },
                )
            }
        }

        ProtocolTransportFieldWidget.TEXT,
        ProtocolTransportFieldWidget.NUMBER -> {
            item {
                CupertinoTextField(
                    label = field.label,
                    value = draft.transportFieldValue(field.key),
                    onValueChange = { value ->
                        onDraftChange(
                            draft.withTransportFieldValue(
                                key = field.key,
                                value = value,
                            ),
                        )
                    },
                    placeholder = field.placeholder,
                )
            }
        }
    }
}

/**
 * 转换协议传输字段选项。
 */
private fun ProtocolTransportFieldMetadataResponse.toCupertinoOptions(): List<CupertinoOption<String>> {
    return options.map { option ->
        CupertinoOption(
            value = option.value,
            label = optionLabel(option.value),
            caption = option.description,
        )
    }
}

/**
 * 生成协议传输字段选项标签。
 *
 * @param rawValue 原始值。
 */
private fun ProtocolTransportFieldMetadataResponse.optionLabel(
    rawValue: String,
): String {
    if (key != ProtocolTransportFieldKey.PARITY) {
        return options.firstOrNull { option -> option.value == rawValue }?.label ?: rawValue
    }
    return Parity.entries
        .firstOrNull { option -> option.name == rawValue }
        ?.label()
        ?: rawValue
}

@Composable
/**
 * 处理模块editorform。
 *
 * @param protocolName 协议名称。
 * @param protocolTemplateName 协议模板名称。
 * @param templates 模板。
 * @param draft draft。
 * @param onDraftChange ondraftchange。
 */
internal fun ModuleEditorForm(
    protocolName: String,
    protocolTemplateName: String,
    templates: List<CupertinoOption<Long>>,
    draft: ModuleDraft,
    onDraftChange: (ModuleDraft) -> Unit,
) {
    if (templates.isEmpty()) {
        CupertinoStatusStrip("当前协议模板下没有可用模块模板。")
        return
    }
    CupertinoPanel(title = "接入摘要", subtitle = "模块会挂到当前协议实例下，通信能力继承协议侧配置。") {
        CupertinoKeyValueRow("承载协议", protocolName.orDash())
        CupertinoKeyValueRow("协议字典", protocolTemplateName.orDash())
        CupertinoKeyValueRow("可选模板数", templates.size.toString())
    }
    CupertinoFormSection(
        title = "基础配置",
        subtitle = "先确定模块模板，再补名称与排序，桌面端保持双栏录入。",
        twoColumnMinWidth = 620.dp,
    ) {
        item { CupertinoTextField("模块名称", draft.name, { onDraftChange(draft.copy(name = it)) }) }
        item {
            CupertinoSelectionField(
                label = "模块模板",
                options = templates,
                selectedValue = draft.moduleTemplateId,
                onSelected = { onDraftChange(draft.copy(moduleTemplateId = it)) },
            )
        }
        item { CupertinoTextField("排序", draft.sortIndex, { onDraftChange(draft.copy(sortIndex = it)) }) }
        fullWidth {
            CupertinoStatusStrip("模块不再单独保存串口、波特率等链路参数，这些运行参数统一跟随所属协议。")
        }
    }
    CupertinoPanel(
        title = "维护说明",
        subtitle = "创建后如果需要调整通信链路，请回到协议节点修改。",
    ) {
        CupertinoStatusStrip("如果要改串口、波特率、校验位和超时，请编辑所属协议。")
    }
}

@Composable
/**
 * 处理设备editorform。
 *
 * @param deviceTypes 设备类型。
 * @param draft draft。
 * @param onDraftChange ondraftchange。
 */
internal fun DeviceEditorForm(
    deviceTypes: List<CupertinoOption<Long>>,
    draft: DeviceDraft,
    onDraftChange: (DeviceDraft) -> Unit,
) {
    CupertinoPanel(
        title = "接入摘要",
        subtitle = "先锁定设备类型和站号，再校准轮询节奏、字节序与批量区间。",
    ) {
        CupertinoKeyValueRow(
            "设备类型",
            deviceTypes.firstOrNull { option -> option.value == draft.deviceTypeId }?.label.orDash(),
        )
        CupertinoKeyValueRow("站号", draft.stationNo.ifBlank { "1" })
        CupertinoKeyValueRow("当前状态", if (draft.disabled) "已禁用" else "启用")
    }
    CupertinoFormSection(
        title = "基础信息",
        subtitle = "设备标识与主寻址信息保持靠前，便于录入时快速核对。",
        twoColumnMinWidth = 640.dp,
    ) {
        item { CupertinoTextField("设备名称", draft.name, { onDraftChange(draft.copy(name = it)) }) }
        item {
            CupertinoSelectionField(
                label = "设备类型",
                options = deviceTypes,
                selectedValue = draft.deviceTypeId,
                onSelected = { onDraftChange(draft.copy(deviceTypeId = it)) },
            )
        }
        item { CupertinoTextField("站号", draft.stationNo, { onDraftChange(draft.copy(stationNo = it)) }) }
        item { CupertinoTextField("排序", draft.sortIndex, { onDraftChange(draft.copy(sortIndex = it)) }) }
        item {
            CupertinoBooleanField(
                label = "禁用设备",
                checked = draft.disabled,
                onCheckedChange = { checked -> onDraftChange(draft.copy(disabled = checked)) },
            )
        }
        fullWidth {
            CupertinoStatusStrip("设备类型决定后续标签建模的默认语义，建议在创建阶段就选准。")
        }
    }
    CupertinoFormSection(
        title = "通讯节奏",
        subtitle = "把轮询与写值节奏放在一组，减少跨区对照。",
        twoColumnMinWidth = 640.dp,
    ) {
        item {
            CupertinoTextField("请求间隔(ms)", draft.requestIntervalMs, { onDraftChange(draft.copy(requestIntervalMs = it)) })
        }
        item {
            CupertinoTextField("写值间隔(ms)", draft.writeIntervalMs, { onDraftChange(draft.copy(writeIntervalMs = it)) })
        }
        fullWidth {
            CupertinoStatusStrip("请求间隔控制采集频率，写值间隔用于限流下发动作。")
        }
    }
    CupertinoFormSection(
        title = "字节序与批量读取",
        subtitle = "字节序和批量区间一起配置，更接近现场调试时的核对方式。",
        twoColumnMinWidth = 640.dp,
    ) {
        item {
            CupertinoSelectionField(
                label = "2 字节顺序",
                options = ByteOrder2.entries.map { CupertinoOption(it, it.label()) },
                selectedValue = draft.byteOrder2,
                onSelected = { onDraftChange(draft.copy(byteOrder2 = it)) },
                allowClear = true,
            )
        }
        item {
            CupertinoSelectionField(
                label = "4 字节顺序",
                options = ByteOrder4.entries.map { CupertinoOption(it, it.label()) },
                selectedValue = draft.byteOrder4,
                onSelected = { onDraftChange(draft.copy(byteOrder4 = it)) },
                allowClear = true,
            )
        }
        item {
            CupertinoSelectionField(
                label = "浮点顺序",
                options = FloatOrder.entries.map { CupertinoOption(it, it.label()) },
                selectedValue = draft.floatOrder,
                onSelected = { onDraftChange(draft.copy(floatOrder = it)) },
                allowClear = true,
            )
        }
        item { CupertinoTextField("模拟量起点", draft.batchAnalogStart, { onDraftChange(draft.copy(batchAnalogStart = it)) }) }
        item { CupertinoTextField("模拟量长度", draft.batchAnalogLength, { onDraftChange(draft.copy(batchAnalogLength = it)) }) }
        item { CupertinoTextField("数字量起点", draft.batchDigitalStart, { onDraftChange(draft.copy(batchDigitalStart = it)) }) }
        item { CupertinoTextField("数字量长度", draft.batchDigitalLength, { onDraftChange(draft.copy(batchDigitalLength = it)) }) }
        fullWidth {
            CupertinoStatusStrip("如果现场设备支持连续寄存器扫描，优先把起点和长度按真实块边界配置。")
        }
    }
}

@Composable
/**
 * 处理标签editorform。
 *
 * @param dataTypes 数据类型。
 * @param registerTypes 寄存器类型。
 * @param draft draft。
 * @param onDraftChange ondraftchange。
 */
internal fun TagEditorForm(
    dataTypes: List<CupertinoOption<Long>>,
    registerTypes: List<CupertinoOption<Long>>,
    draft: TagDraft,
    onDraftChange: (TagDraft) -> Unit,
) {
    CupertinoFormSection(
        title = "基础信息",
        subtitle = "标签命名、说明和启用状态优先集中展示。",
    ) {
        item { CupertinoTextField("标签名称", draft.name, { onDraftChange(draft.copy(name = it)) }) }
        item { CupertinoTextField("排序", draft.sortIndex, { onDraftChange(draft.copy(sortIndex = it)) }) }
        fullWidth {
            CupertinoTextField(
                label = "描述",
                value = draft.description,
                onValueChange = { onDraftChange(draft.copy(description = it)) },
                singleLine = false,
            )
        }
        item {
            CupertinoBooleanField(
                label = "启用标签",
                checked = draft.enabled,
                onCheckedChange = { checked -> onDraftChange(draft.copy(enabled = checked)) },
            )
        }
        item {
            CupertinoBooleanField(
                label = "启用线性转换",
                checked = draft.scalingEnabled,
                onCheckedChange = { checked -> onDraftChange(draft.copy(scalingEnabled = checked)) },
            )
        }
    }
    CupertinoFormSection(
        title = "寄存器与数据语义",
        subtitle = "把数据类型、寄存器类型和地址放在同一区域，录入时更容易对照。",
    ) {
        item {
            CupertinoSelectionField(
                label = "数据类型",
                options = dataTypes,
                selectedValue = draft.dataTypeId,
                onSelected = { onDraftChange(draft.copy(dataTypeId = it)) },
            )
        }
        item {
            CupertinoSelectionField(
                label = "寄存器类型",
                options = registerTypes,
                selectedValue = draft.registerTypeId,
                onSelected = { onDraftChange(draft.copy(registerTypeId = it)) },
            )
        }
        item { CupertinoTextField("寄存器地址", draft.registerAddress, { onDraftChange(draft.copy(registerAddress = it)) }) }
        item {
            CupertinoSelectionField(
                label = "标签类型",
                options = PointType.entries.map { CupertinoOption(it, it.label()) },
                selectedValue = draft.pointType,
                onSelected = { onDraftChange(draft.copy(pointType = it)) },
                allowClear = true,
            )
        }
        item { CupertinoTextField("防抖时间", draft.debounceMs, { onDraftChange(draft.copy(debounceMs = it)) }) }
        item { CupertinoTextField("默认值", draft.defaultValue, { onDraftChange(draft.copy(defaultValue = it)) }) }
        item { CupertinoTextField("异常值", draft.exceptionValue, { onDraftChange(draft.copy(exceptionValue = it)) }) }
    }

    if (draft.scalingEnabled) {
        CupertinoFormSection(
            title = "线性转换",
            subtitle = "原始值与工程值成对放置，方便快速比对。",
        ) {
            item { CupertinoTextField("偏移量", draft.scalingOffset, { onDraftChange(draft.copy(scalingOffset = it)) }) }
            item { CupertinoTextField("原始最小值", draft.rawMin, { onDraftChange(draft.copy(rawMin = it)) }) }
            item { CupertinoTextField("原始最大值", draft.rawMax, { onDraftChange(draft.copy(rawMax = it)) }) }
            item { CupertinoTextField("工程最小值", draft.engMin, { onDraftChange(draft.copy(engMin = it)) }) }
            item { CupertinoTextField("工程最大值", draft.engMax, { onDraftChange(draft.copy(engMax = it)) }) }
        }
    }

    CupertinoPanel(title = "数字量文本", subtitle = "对应历史配置里的值文本映射。") {
        draft.valueTexts.forEachIndexed { index, valueTextDraft ->
            CupertinoPanel(title = "映射 ${index + 1}") {
                CupertinoFormGrid {
                    item {
                        CupertinoTextField(
                            label = "原始值",
                            value = valueTextDraft.rawValue,
                            onValueChange = { text ->
                                onDraftChange(
                                    draft.copy(
                                        valueTexts = draft.valueTexts.toMutableList().apply {
                                            this[index] = this[index].copy(rawValue = text)
                                        },
                                    ),
                                )
                            },
                        )
                    }
                    item {
                        CupertinoTextField(
                            label = "显示文本",
                            value = valueTextDraft.displayText,
                            onValueChange = { text ->
                                onDraftChange(
                                    draft.copy(
                                        valueTexts = draft.valueTexts.toMutableList().apply {
                                            this[index] = this[index].copy(displayText = text)
                                        },
                                    ),
                                )
                            },
                        )
                    }
                    fullWidth {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            WorkbenchActionButton(
                                text = "删除映射",
                                onClick = {
                                    onDraftChange(
                                        draft.copy(
                                            valueTexts = draft.valueTexts.toMutableList().apply {
                                                removeAt(index)
                                            },
                                        ),
                                    )
                                },
                                variant = WorkbenchButtonVariant.Destructive,
                            )
                        }
                    }
                }
            }
        }
        WorkbenchActionButton(
            text = "新增一行",
            onClick = {
                onDraftChange(draft.copy(valueTexts = draft.valueTexts + TagValueTextDraft("", "")))
            },
            variant = WorkbenchButtonVariant.Outline,
        )
    }
}
