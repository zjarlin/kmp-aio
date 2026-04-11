package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import org.koin.compose.koinInject
import site.addzero.component.table.original.TableOriginal
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.component.table.original.entity.TableLayoutConfig
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoSectionTitle
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.hostconfig.api.template.ModuleTemplateOptionResponse
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigModuleBoard
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind
import site.addzero.kcloud.plugins.hostconfig.common.resolveModuleBoardModel
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsScreenState
import site.addzero.kcloud.plugins.hostconfig.projects.ProjectsViewModel
import site.addzero.kcloud.plugins.hostconfig.projects.displayName
import site.addzero.kcloud.plugins.hostconfig.screen.projects.ProjectsInteractiveSurfaceActions
import site.addzero.kcloud.plugins.hostconfig.screen.projects.ProjectsNodeChildrenPanelActions
import site.addzero.kcloud.plugins.hostconfig.screen.projects.ProjectsNodeChildrenPanelActionsSpi
import site.addzero.kcloud.plugins.hostconfig.screen.projects.ProjectsInteractiveSurfaceSpi

/**
 * 处理nodechildrenpanel。
 *
 * @param state 状态。
 * @param onSelectNode on选择node。
 * @param onPrevTagPage onprev标签分页。
 * @param onNextTagPage onnext标签分页。
 */
@Composable
internal fun NodeChildrenPanel(
    state: ProjectsScreenState,
    onSelectNode: (String) -> Unit,
    onPrevTagPage: () -> Unit,
    onNextTagPage: () -> Unit,
) {
    val panelActionsSpi = koinInject<ProjectsNodeChildrenPanelActionsSpi>()
    val nodeKind = resolveSelectedNodeKind(state)
    val actions = remember(onPrevTagPage, onNextTagPage) {
        ProjectsNodeChildrenPanelActions(
            onPrevTagPage = onPrevTagPage,
            onNextTagPage = onNextTagPage,
        )
    }
    CupertinoPanel(
        title = if (nodeKind == HostConfigNodeKind.TAG) "子项信息" else "下级节点",
        actions = {
            panelActionsSpi.Render(state = state, actions = actions)
        },
    ) {
        when (nodeKind) {
            null -> CupertinoStatusStrip("左侧树选择后，这里会显示当前节点的 children 信息。")

            HostConfigNodeKind.PROJECT -> {
                val projectTree = state.selectedProjectTree
                if (projectTree == null) {
                    CupertinoStatusStrip("当前工程树还没有加载完成。")
                    return@CupertinoPanel
                }
                if (projectTree.protocols.isEmpty()) {
                    CupertinoStatusStrip("当前工程还没有下级节点。")
                    return@CupertinoPanel
                }
                ChildNodeTableSection(
                    title = "协议",
                    columns = listOf(
                        ChildNodeTableColumn("protocol", "协议", 188f),
                        ChildNodeTableColumn("template", "模板", 150f),
                        ChildNodeTableColumn("polling", "轮询(ms)", 116f),
                        ChildNodeTableColumn("deviceCount", "设备数", 96f),
                        ChildNodeTableColumn("summary", "通信摘要", 220f),
                        ChildNodeTableColumn("sortIndex", "排序", 88f),
                    ),
                    rows = projectTree.protocols.map { protocol ->
                        ChildNodeTableRow(
                            id = protocol.id.toString(),
                            cells = mapOf(
                                "protocol" to protocol.displayName(),
                                "template" to protocol.protocolTemplateName,
                                "polling" to protocol.pollingIntervalMs.toString(),
                                "deviceCount" to protocol.devices.size.toString(),
                                "summary" to protocol.transportConfig
                                    ?.toSummary(resolveProtocolTemplateMetadata(state, protocol.protocolTemplateId))
                                    .orEmpty()
                                    .ifBlank { "-" },
                                "sortIndex" to protocol.sortIndex.toString(),
                            ),
                            onClick = {
                                onSelectNode(ProjectsViewModel.buildProtocolNodeId(projectTree.id, protocol.id))
                            },
                        )
                    },
                )
            }

            HostConfigNodeKind.PROTOCOL -> {
                val projectId = state.selectedProjectId ?: return@CupertinoPanel
                val protocol = state.selectedProtocol
                if (protocol == null || protocol.devices.isEmpty()) {
                    CupertinoStatusStrip("当前协议还没有下级设备。")
                    return@CupertinoPanel
                }
                ChildNodeTableSection(
                    title = "设备",
                    columns = listOf(
                        ChildNodeTableColumn("name", "设备", 176f),
                        ChildNodeTableColumn("deviceType", "类型", 132f),
                        ChildNodeTableColumn("stationNo", "站号", 92f),
                        ChildNodeTableColumn("moduleCount", "模块数", 92f),
                        ChildNodeTableColumn("tagCount", "标签数", 92f),
                        ChildNodeTableColumn("sortIndex", "排序", 88f),
                    ),
                    rows = protocol.devices.map { device ->
                        ChildNodeTableRow(
                            id = device.id.toString(),
                            cells = mapOf(
                                "name" to device.name,
                                "deviceType" to device.deviceTypeName,
                                "stationNo" to device.stationNo.toString(),
                                "moduleCount" to device.modules.size.toString(),
                                "tagCount" to device.tags.size.toString(),
                                "sortIndex" to device.sortIndex.toString(),
                            ),
                            onClick = {
                                onSelectNode(ProjectsViewModel.buildDeviceNodeId(projectId, device.id))
                            },
                        )
                    },
                )
            }

            HostConfigNodeKind.MODULE -> {
                CupertinoStatusStrip("当前模块没有下级节点。标签可从模块操作菜单发起创建。")
            }

            HostConfigNodeKind.DEVICE -> {
                val projectId = state.selectedProjectId ?: return@CupertinoPanel
                val deviceId = state.activeDeviceId ?: return@CupertinoPanel
                val device = state.selectedDevice
                if (device == null) {
                    CupertinoStatusStrip("当前设备详情尚未加载完成。")
                    return@CupertinoPanel
                }
                if (device.modules.isEmpty() && state.tagPage.d.isEmpty()) {
                    CupertinoStatusStrip("当前设备还没有模块和标签。")
                    return@CupertinoPanel
                }
                if (device.modules.isNotEmpty()) {
                    ChildNodeTableSection(
                        title = "模块",
                        columns = listOf(
                            ChildNodeTableColumn("name", "模块", 176f),
                            ChildNodeTableColumn("templateName", "模板", 164f),
                            ChildNodeTableColumn("templateCode", "模板编码", 176f),
                            ChildNodeTableColumn("sortIndex", "排序", 88f),
                        ),
                        rows = device.modules.map { module ->
                            ChildNodeTableRow(
                                id = module.id.toString(),
                                cells = mapOf(
                                    "name" to module.name,
                                    "templateName" to module.moduleTemplateName,
                                    "templateCode" to module.moduleTemplateCode,
                                    "sortIndex" to module.sortIndex.toString(),
                                ),
                                onClick = {
                                    onSelectNode(ProjectsViewModel.buildModuleNodeId(projectId, module.id))
                                },
                            )
                        },
                    )
                }
                if (state.tagPage.d.isNotEmpty()) {
                    ChildNodeTableSection(
                        title = "标签",
                        subtitle = "分页偏移 ${state.tagOffset} / 共 ${state.tagPage.t} 条",
                        columns = listOf(
                            ChildNodeTableColumn("name", "点名", 188f),
                            ChildNodeTableColumn("enabled", "开关", 88f),
                            ChildNodeTableColumn("registerType", "寄存器类型", 132f),
                            ChildNodeTableColumn("registerAddress", "寄存器地址", 132f),
                            ChildNodeTableColumn("dataType", "数据类型", 132f),
                            ChildNodeTableColumn("forwardRegisterType", "BACnet类型", 148f),
                            ChildNodeTableColumn("forwardRegisterAddress", "BACnet地址", 148f),
                        ),
                        rows = state.tagPage.d.map { tag ->
                            ChildNodeTableRow(
                                id = tag.id.toString(),
                                cells = mapOf(
                                    "name" to tag.name,
                                    "enabled" to if (tag.enabled) "开" else "关",
                                    "registerType" to tag.registerTypeName,
                                    "registerAddress" to tag.registerAddress.toString(),
                                    "dataType" to tag.dataTypeName,
                                    "forwardRegisterType" to (tag.forwardRegisterTypeName ?: "-"),
                                    "forwardRegisterAddress" to (tag.forwardRegisterAddress?.toString() ?: "-"),
                                ),
                                onClick = {
                                    onSelectNode(
                                        ProjectsViewModel.buildTagNodeId(
                                            projectId = projectId,
                                            deviceId = deviceId,
                                            tagId = tag.id,
                                        ),
                                    )
                                },
                            )
                        },
                    )
                }
            }

            HostConfigNodeKind.TAG -> {
                val tag = state.selectedTagDetail
                if (tag == null) {
                    CupertinoStatusStrip("当前标签详情尚未加载完成。")
                    return@CupertinoPanel
                }
                if (tag.valueTexts.isEmpty()) {
                    CupertinoStatusStrip("当前标签没有值文本子项。")
                    return@CupertinoPanel
                }
                ChildNodeTableSection(
                    title = "值文本",
                    columns = listOf(
                        ChildNodeTableColumn("displayText", "显示文本", 188f),
                        ChildNodeTableColumn("rawValue", "原始值", 148f),
                        ChildNodeTableColumn("sortIndex", "排序", 88f),
                    ),
                    rows = tag.valueTexts.map { item ->
                        ChildNodeTableRow(
                            id = item.id.toString(),
                            cells = mapOf(
                                "displayText" to item.displayText,
                                "rawValue" to item.rawValue,
                                "sortIndex" to item.sortIndex.toString(),
                            ),
                        )
                    },
                )
            }
        }
    }
}

private data class ChildNodeTableColumn(
    val key: String,
    val label: String,
    val widthDp: Float = 148f,
)

private data class ChildNodeTableRow(
    val id: String,
    val cells: Map<String, String>,
    val onClick: (() -> Unit)? = null,
)

@Composable
private fun ChildNodeTableSection(
    title: String,
    columns: List<ChildNodeTableColumn>,
    rows: List<ChildNodeTableRow>,
    subtitle: String? = null,
) {
    val hasRowAction = rows.any { row ->
        row.onClick != null
    }
    val layoutConfig = remember(hasRowAction) {
        TableLayoutConfig(
            indexColumnWidthDp = 56f,
            actionColumnWidthDp = 112f,
            headerHeightDp = 50f,
            rowHeightDp = 54f,
            defaultColumnWidthDp = 148f,
            enableAutoWidth = true,
            autoWidthMinDp = 88f,
            autoWidthMaxDp = 240f,
        )
    }
    val columnConfigs = remember(columns) {
        columns.mapIndexed { index, column ->
            ColumnConfig(
                key = column.key,
                comment = column.label,
                width = column.widthDp,
                order = index,
                showFilter = false,
                showSort = false,
            )
        }
    }
    val tableHeight = remember(rows.size, layoutConfig) {
        val visibleRowCount = rows.size.coerceIn(1, 6)
        (layoutConfig.headerHeightDp + (layoutConfig.rowHeightDp * visibleRowCount) + 2f).dp
    }

    CupertinoSectionTitle(title)
    subtitle?.takeIf { it.isNotBlank() }?.let { text ->
        CupertinoText(
            text = text,
            style = CupertinoTheme.typography.footnote,
            color = CupertinoTheme.colorScheme.secondaryLabel,
        )
        Spacer(modifier = Modifier.height(6.dp))
    }
    TableOriginal(
        data = rows,
        columns = columns,
        getColumnKey = { column ->
            column.key
        },
        getRowId = { row ->
            row.id
        },
        columnConfigs = columnConfigs,
        layoutConfig = layoutConfig,
        getColumnLabel = { column ->
            CupertinoText(
                text = column.label,
                style = CupertinoTheme.typography.subhead,
                color = CupertinoTheme.colorScheme.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        getCellContent = { row, column ->
            CupertinoText(
                text = row.cells[column.key].orEmpty(),
                style = CupertinoTheme.typography.footnote,
                color = CupertinoTheme.colorScheme.secondaryLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        rowActionSlot = if (hasRowAction) {
            { row ->
                val onClick = row.onClick
                if (onClick != null) {
                    WorkbenchActionButton(
                        text = "查看",
                        onClick = onClick,
                        variant = WorkbenchButtonVariant.Outline,
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }
        } else {
            null
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(tableHeight),
    )
}

/**
 * 处理项目模块rack。
 *
 * @param projectId 项目 ID。
 * @param modules 模块。
 * @param moduleTemplates 模块模板。
 * @param onSelectNode on选择node。
 */
@Composable
internal fun ProjectModuleRack(
    projectId: Long?,
    modules: List<site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode>,
    moduleTemplates: List<ModuleTemplateOptionResponse>,
    onSelectNode: (String) -> Unit,
) {
    val interactiveSurfaceSpi = koinInject<ProjectsInteractiveSurfaceSpi>()
    val rackShape = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rackShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CupertinoTheme.colorScheme.tertiarySystemGroupedBackground,
                        CupertinoTheme.colorScheme.secondarySystemGroupedBackground,
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = CupertinoTheme.colorScheme.separator.copy(alpha = 0.35f),
                shape = rackShape,
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CupertinoText(text = "模块总览", style = CupertinoTheme.typography.headline)
            CupertinoText(
                text = "${modules.size} 个模块",
                style = CupertinoTheme.typography.footnote,
                color = CupertinoTheme.colorScheme.secondaryLabel,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            modules.forEachIndexed { index, module ->
                Column(
                    modifier = Modifier.width(296.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CupertinoText(
                            text = "模块 ${index + 1}",
                            style = CupertinoTheme.typography.caption1,
                            color = CupertinoTheme.colorScheme.secondaryLabel,
                        )
                        CupertinoText(
                            text = "挂在 1 台设备下",
                            style = CupertinoTheme.typography.caption2,
                            color = CupertinoTheme.colorScheme.tertiaryLabel,
                        )
                    }
                    interactiveSurfaceSpi.Render(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CupertinoTheme.colorScheme.secondarySystemGroupedBackground)
                            .border(
                                width = 1.dp,
                                color = CupertinoTheme.colorScheme.separator.copy(alpha = 0.28f),
                                shape = RoundedCornerShape(18.dp),
                            )
                            .padding(8.dp),
                        enabled = projectId != null,
                        actions = ProjectsInteractiveSurfaceActions(
                            onClick = {
                                projectId?.let { safeProjectId ->
                                    onSelectNode(
                                        ProjectsViewModel.buildModuleNodeId(
                                            projectId = safeProjectId,
                                            moduleId = module.id,
                                        ),
                                    )
                                }
                            },
                        ),
                    ) { interactiveModifier ->
                        Box(modifier = interactiveModifier) {
                            HostConfigModuleBoard(
                                model = resolveModuleBoardModel(module = module, moduleTemplates = moduleTemplates),
                                compact = true,
                            )
                        }
                    }
                }
            }
        }
    }
}
