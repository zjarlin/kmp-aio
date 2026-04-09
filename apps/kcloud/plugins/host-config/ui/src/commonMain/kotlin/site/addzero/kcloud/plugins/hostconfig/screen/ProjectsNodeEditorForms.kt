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
import site.addzero.cupertino.workbench.components.form.CupertinoFormSection
import site.addzero.cupertino.workbench.components.panel.CupertinoKeyValueRow
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
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
    val templateCode = protocolTemplates.firstOrNull { item -> item.id == draft.protocolTemplateId }?.code

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
        title = "通信配置",
        subtitle = "模块通信参数已经上提到协议层，这里只保留协议级连接参数。",
    ) {
        when (templateCode) {
            "MODBUS_RTU_CLIENT" -> {
                item { CupertinoTextField("串口", draft.portName, { onDraftChange(draft.copy(portName = it)) }, placeholder = "例如 COM3") }
                item { CupertinoTextField("波特率", draft.baudRate, { onDraftChange(draft.copy(baudRate = it)) }) }
                item { CupertinoTextField("数据位", draft.dataBits, { onDraftChange(draft.copy(dataBits = it)) }) }
                item { CupertinoTextField("停止位", draft.stopBits, { onDraftChange(draft.copy(stopBits = it)) }) }
                item {
                    CupertinoSelectionField(
                        label = "校验位",
                        options = Parity.entries.map { option -> CupertinoOption(option, option.label()) },
                        selectedValue = draft.parity,
                        onSelected = { onDraftChange(draft.copy(parity = it ?: Parity.NONE)) },
                    )
                }
                item {
                    CupertinoTextField("响应超时(ms)", draft.responseTimeoutMs, { onDraftChange(draft.copy(responseTimeoutMs = it)) })
                }
            }

            "MODBUS_TCP_CLIENT" -> {
                item { CupertinoTextField("主机地址", draft.host, { onDraftChange(draft.copy(host = it)) }, placeholder = "例如 192.168.1.10") }
                item { CupertinoTextField("TCP 端口", draft.tcpPort, { onDraftChange(draft.copy(tcpPort = it)) }, placeholder = "默认 502") }
                item {
                    CupertinoTextField("响应超时(ms)", draft.responseTimeoutMs, { onDraftChange(draft.copy(responseTimeoutMs = it)) })
                }
            }

            else -> {
                fullWidth {
                    CupertinoStatusStrip("当前协议模板没有额外通信字段。")
                }
            }
        }
    }
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
    CupertinoPanel(title = "绑定协议", subtitle = "模块创建后会挂到这个协议实例下。") {
        CupertinoKeyValueRow("承载协议", protocolName.orDash())
        CupertinoKeyValueRow("协议字典", protocolTemplateName.orDash())
    }
    CupertinoFormSection(
        title = "模块信息",
        subtitle = "模块表单只维护硬件语义、模板归属和排序。",
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
    }
    CupertinoPanel(
        title = "提示",
        subtitle = "模块通信参数已提升到协议层，这里只维护模块模板和排序。",
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
    CupertinoFormSection(
        title = "基础信息",
        subtitle = "设备标识、轮询节奏与启停状态默认放在首屏。",
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
            CupertinoTextField("请求间隔(ms)", draft.requestIntervalMs, { onDraftChange(draft.copy(requestIntervalMs = it)) })
        }
        item {
            CupertinoTextField("写值间隔(ms)", draft.writeIntervalMs, { onDraftChange(draft.copy(writeIntervalMs = it)) })
        }
        item {
            CupertinoBooleanField(
                label = "禁用设备",
                checked = draft.disabled,
                onCheckedChange = { checked -> onDraftChange(draft.copy(disabled = checked)) },
            )
        }
    }
    CupertinoFormSection(
        title = "字节序",
        subtitle = "不同设备协议常会在这里出现字节序差异。",
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
    }
    CupertinoFormSection(
        title = "批量读取",
        subtitle = "把模拟量和数字量的起点与长度并排配置，便于核对。",
        twoColumnMinWidth = 640.dp,
    ) {
        item { CupertinoTextField("模拟量起点", draft.batchAnalogStart, { onDraftChange(draft.copy(batchAnalogStart = it)) }) }
        item { CupertinoTextField("模拟量长度", draft.batchAnalogLength, { onDraftChange(draft.copy(batchAnalogLength = it)) }) }
        item { CupertinoTextField("数字量起点", draft.batchDigitalStart, { onDraftChange(draft.copy(batchDigitalStart = it)) }) }
        item { CupertinoTextField("数字量长度", draft.batchDigitalLength, { onDraftChange(draft.copy(batchDigitalLength = it)) }) }
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
