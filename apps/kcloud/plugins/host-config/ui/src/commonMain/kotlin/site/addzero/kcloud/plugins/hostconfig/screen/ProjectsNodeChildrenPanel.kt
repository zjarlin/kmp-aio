package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.components.panel.CupertinoKeyValueRow
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

@Composable
/**
 * 处理nodechildrenpanel。
 *
 * @param state 状态。
 * @param onSelectNode on选择node。
 * @param onPrevTagPage onprev标签分页。
 * @param onNextTagPage onnext标签分页。
 */
internal fun NodeChildrenPanel(
    state: ProjectsScreenState,
    onSelectNode: (String) -> Unit,
    onPrevTagPage: () -> Unit,
    onNextTagPage: () -> Unit,
) {
    val nodeKind = resolveSelectedNodeKind(state)
    CupertinoPanel(
        title = if (nodeKind == HostConfigNodeKind.TAG) "子项信息" else "下级节点",
        actions = {
            if (nodeKind == HostConfigNodeKind.DEVICE) {
                WorkbenchActionButton(
                    text = "上一页",
                    onClick = onPrevTagPage,
                    variant = WorkbenchButtonVariant.Outline,
                    enabled = state.tagOffset > 0,
                )
                WorkbenchActionButton(
                    text = "下一页",
                    onClick = onNextTagPage,
                    variant = WorkbenchButtonVariant.Outline,
                    enabled = state.tagOffset + state.tagSize < state.tagPage.t.toInt(),
                )
            }
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
                CupertinoSectionTitle("协议")
                projectTree.protocols.forEach { protocol ->
                    val templateMetadata = resolveProtocolTemplateMetadata(state, protocol.protocolTemplateId)
                    ChildNodeCard(
                        title = protocol.displayName(),
                        subtitle = "${protocol.protocolTemplateName} · ${protocol.protocolTemplateCode}",
                        onClick = {
                            onSelectNode(ProjectsViewModel.buildProtocolNodeId(projectTree.id, protocol.id))
                        },
                    ) {
                        CupertinoKeyValueRow("轮询间隔(ms)", protocol.pollingIntervalMs.toString())
                        CupertinoKeyValueRow("承载模块", protocol.modules.size.toString())
                        CupertinoKeyValueRow("排序", protocol.sortIndex.toString())
                        renderTransportConfigRows(protocol.transportConfig, templateMetadata)
                    }
                }
            }

            HostConfigNodeKind.PROTOCOL -> {
                val projectId = state.selectedProjectId ?: return@CupertinoPanel
                val protocol = state.selectedProtocol
                if (protocol == null || protocol.modules.isEmpty()) {
                    CupertinoStatusStrip("当前协议还没有下级模块。")
                    return@CupertinoPanel
                }
                protocol.modules.forEach { module ->
                    ChildNodeCard(
                        title = module.name,
                        subtitle = "${module.moduleTemplateName} · ${module.moduleTemplateCode}",
                        onClick = {
                            onSelectNode(ProjectsViewModel.buildModuleNodeId(projectId, module.id))
                        },
                    ) {
                        HostConfigModuleBoard(
                            model = resolveModuleBoardModel(module = module, moduleTemplates = state.moduleTemplates),
                            compact = true,
                        )
                        CupertinoKeyValueRow("设备数量", module.devices.size.toString())
                        CupertinoKeyValueRow("排序", module.sortIndex.toString())
                    }
                }
            }

            HostConfigNodeKind.MODULE -> {
                val projectId = state.selectedProjectId ?: return@CupertinoPanel
                val module = state.selectedModule
                if (module == null || module.devices.isEmpty()) {
                    CupertinoStatusStrip("当前模块还没有下级设备。")
                    return@CupertinoPanel
                }
                module.devices.forEach { device ->
                    ChildNodeCard(
                        title = device.name,
                        subtitle = device.deviceTypeName,
                        onClick = {
                            onSelectNode(ProjectsViewModel.buildDeviceNodeId(projectId, device.id))
                        },
                    ) {
                        CupertinoKeyValueRow("站号", device.stationNo.toString())
                        CupertinoKeyValueRow("标签数量", device.tags.size.toString())
                        CupertinoKeyValueRow("禁用", if (device.disabled) "是" else "否")
                    }
                }
            }

            HostConfigNodeKind.DEVICE -> {
                val projectId = state.selectedProjectId ?: return@CupertinoPanel
                val deviceId = state.activeDeviceId ?: return@CupertinoPanel
                CupertinoKeyValueRow("分页状态", "偏移 ${state.tagOffset} / 共 ${state.tagPage.t} 条")
                if (state.tagPage.d.isEmpty()) {
                    CupertinoStatusStrip("当前设备还没有标签。")
                    return@CupertinoPanel
                }
                state.tagPage.d.forEach { tag ->
                    ChildNodeCard(
                        title = tag.name,
                        subtitle = "${tag.registerTypeName} / ${tag.registerAddress}",
                        onClick = {
                            onSelectNode(
                                ProjectsViewModel.buildTagNodeId(
                                    projectId = projectId,
                                    deviceId = deviceId,
                                    tagId = tag.id,
                                ),
                            )
                        },
                    ) {
                        CupertinoKeyValueRow("数据类型", tag.dataTypeName)
                        CupertinoKeyValueRow("启用", if (tag.enabled) "是" else "否")
                        CupertinoKeyValueRow("值文本条目", tag.valueTexts.size.toString())
                    }
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
                tag.valueTexts.forEach { item ->
                    ChildNodeCard(
                        title = item.displayText,
                        subtitle = "排序 ${item.sortIndex}",
                    ) {
                        CupertinoKeyValueRow("原始值", item.rawValue)
                        CupertinoKeyValueRow("显示文本", item.displayText)
                    }
                }
            }
        }
    }
}

@Composable
/**
 * 处理childnodecard。
 *
 * @param title title。
 * @param subtitle subtitle。
 * @param onClick onclick。
 * @param content content。
 */
private fun ChildNodeCard(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .let { modifier ->
                if (onClick == null) modifier else modifier.clickable(onClick = onClick)
            },
    ) {
        CupertinoPanel(title = title, subtitle = subtitle) {
            content()
        }
    }
}

@Composable
/**
 * 处理项目模块rack。
 *
 * @param projectId 项目 ID。
 * @param modules 模块。
 * @param moduleTemplates 模块模板。
 * @param onSelectNode on选择node。
 */
internal fun ProjectModuleRack(
    projectId: Long?,
    modules: List<site.addzero.kcloud.plugins.hostconfig.api.project.ModuleTreeNode>,
    moduleTemplates: List<ModuleTemplateOptionResponse>,
    onSelectNode: (String) -> Unit,
) {
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
                            text = "${module.devices.size} 台设备",
                            style = CupertinoTheme.typography.caption2,
                            color = CupertinoTheme.colorScheme.tertiaryLabel,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CupertinoTheme.colorScheme.secondarySystemGroupedBackground)
                            .border(
                                width = 1.dp,
                                color = CupertinoTheme.colorScheme.separator.copy(alpha = 0.28f),
                                shape = RoundedCornerShape(18.dp),
                            )
                            .padding(8.dp)
                            .clickable(
                                enabled = projectId != null,
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
                    ) {
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
