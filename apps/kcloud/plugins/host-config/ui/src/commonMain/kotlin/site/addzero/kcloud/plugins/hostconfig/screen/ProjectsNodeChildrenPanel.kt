package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoSectionTitle
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import org.koin.compose.koinInject
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
    val interactiveSurfaceSpi = koinInject<ProjectsInteractiveSurfaceSpi>()
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
                    columns = listOf("协议", "模板", "轮询(ms)", "设备数", "通信摘要", "排序"),
                    rows = projectTree.protocols.map { protocol ->
                        ChildNodeTableRow(
                            cells = listOf(
                                protocol.displayName(),
                                protocol.protocolTemplateName,
                                protocol.pollingIntervalMs.toString(),
                                protocol.devices.size.toString(),
                                protocol.transportConfig
                                    ?.toSummary(resolveProtocolTemplateMetadata(state, protocol.protocolTemplateId))
                                    .orEmpty()
                                    .ifBlank { "-" },
                                protocol.sortIndex.toString(),
                            ),
                            onClick = {
                                onSelectNode(ProjectsViewModel.buildProtocolNodeId(projectTree.id, protocol.id))
                            },
                        )
                    },
                    interactiveSurfaceSpi = interactiveSurfaceSpi,
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
                    columns = listOf("设备", "类型", "站号", "模块数", "标签数", "排序"),
                    rows = protocol.devices.map { device ->
                        ChildNodeTableRow(
                            cells = listOf(
                                device.name,
                                device.deviceTypeName,
                                device.stationNo.toString(),
                                device.modules.size.toString(),
                                device.tags.size.toString(),
                                device.sortIndex.toString(),
                            ),
                            onClick = {
                                onSelectNode(ProjectsViewModel.buildDeviceNodeId(projectId, device.id))
                            },
                        )
                    },
                    interactiveSurfaceSpi = interactiveSurfaceSpi,
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
                        columns = listOf("模块", "模板", "模板编码", "排序"),
                        rows = device.modules.map { module ->
                            ChildNodeTableRow(
                                cells = listOf(
                                    module.name,
                                    module.moduleTemplateName,
                                    module.moduleTemplateCode,
                                    module.sortIndex.toString(),
                                ),
                                onClick = {
                                    onSelectNode(ProjectsViewModel.buildModuleNodeId(projectId, module.id))
                                },
                            )
                        },
                        interactiveSurfaceSpi = interactiveSurfaceSpi,
                    )
                }
                if (state.tagPage.d.isNotEmpty()) {
                    ChildNodeTableSection(
                        title = "标签",
                        subtitle = "分页偏移 ${state.tagOffset} / 共 ${state.tagPage.t} 条",
                        columns = listOf("点名", "开关", "寄存器类型", "寄存器地址", "数据类型", "BACnet类型", "BACnet地址"),
                        rows = state.tagPage.d.map { tag ->
                            ChildNodeTableRow(
                                cells = listOf(
                                    tag.name,
                                    if (tag.enabled) "开" else "关",
                                    tag.registerTypeName,
                                    tag.registerAddress.toString(),
                                    tag.dataTypeName,
                                    tag.forwardRegisterTypeName ?: "-",
                                    tag.forwardRegisterAddress?.toString() ?: "-",
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
                        interactiveSurfaceSpi = interactiveSurfaceSpi,
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
                    columns = listOf("显示文本", "原始值", "排序"),
                    rows = tag.valueTexts.map { item ->
                        ChildNodeTableRow(
                            cells = listOf(
                                item.displayText,
                                item.rawValue,
                                item.sortIndex.toString(),
                            ),
                        )
                    },
                    interactiveSurfaceSpi = interactiveSurfaceSpi,
                )
            }
        }
    }
}

private data class ChildNodeTableRow(
    val cells: List<String>,
    val onClick: (() -> Unit)? = null,
)

@Composable
private fun ChildNodeTableSection(
    title: String,
    columns: List<String>,
    rows: List<ChildNodeTableRow>,
    interactiveSurfaceSpi: ProjectsInteractiveSurfaceSpi,
    subtitle: String? = null,
) {
    val horizontalScrollState = rememberScrollState()
    val tableShape = RoundedCornerShape(16.dp)
    val columnWidth = 156.dp

    CupertinoSectionTitle(title)
    subtitle?.takeIf { it.isNotBlank() }?.let { text ->
        CupertinoText(
            text = text,
            style = CupertinoTheme.typography.footnote,
            color = CupertinoTheme.colorScheme.secondaryLabel,
        )
        Spacer(modifier = Modifier.height(6.dp))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
            .clip(tableShape)
            .border(
                width = 1.dp,
                color = CupertinoTheme.colorScheme.separator.copy(alpha = 0.28f),
                shape = tableShape,
            ),
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = columnWidth * columns.size)
                .background(CupertinoTheme.colorScheme.secondarySystemGroupedBackground),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CupertinoTheme.colorScheme.tertiarySystemGroupedBackground)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
                columns.forEach { column ->
                    ChildNodeTableCell(
                        text = column,
                        width = columnWidth,
                        emphasis = true,
                    )
                }
            }
            rows.forEachIndexed { index, row ->
                interactiveSurfaceSpi.Render(
                    modifier = Modifier.fillMaxWidth(),
                    actions = remember(row.onClick) {
                        ProjectsInteractiveSurfaceActions(onClick = row.onClick)
                    },
                ) { interactiveModifier ->
                    Row(
                        modifier = interactiveModifier
                            .fillMaxWidth()
                            .background(
                                if (index % 2 == 0) {
                                    CupertinoTheme.colorScheme.secondarySystemGroupedBackground
                                } else {
                                    CupertinoTheme.colorScheme.systemGroupedBackground
                                },
                            )
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                    ) {
                        row.cells.forEach { cell ->
                            ChildNodeTableCell(
                                text = cell,
                                width = columnWidth,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChildNodeTableCell(
    text: String,
    width: Dp,
    emphasis: Boolean = false,
) {
    CupertinoText(
        text = text,
        modifier = Modifier
            .width(width)
            .defaultMinSize(minHeight = 24.dp)
            .padding(horizontal = 8.dp),
        style = if (emphasis) CupertinoTheme.typography.subhead else CupertinoTheme.typography.body,
        color = if (emphasis) {
            CupertinoTheme.colorScheme.label
        } else {
            CupertinoTheme.colorScheme.secondaryLabel
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
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
