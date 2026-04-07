@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.SettingsEthernet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigKeyValueRow
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigPanel
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigSectionTitle
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigStatusStrip
import site.addzero.kcloud.plugins.hostconfig.protocols.ProtocolsViewModel

@Route(
    title = "协议模板",
    routePath = "host-config/protocols",
    icon = "Key",
    order = 10.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "宿主配置",
            icon = "SettingsApplications",
            order = 10,
        ),
    ),
)
@Composable
fun ProtocolsScreen() {
    val viewModel = koinViewModel<ProtocolsViewModel>()
    val state = viewModel.screenState

    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        WorkbenchTreeSidebar(
            items = state.protocolTemplates,
            selectedId = state.selectedProtocolTemplateId,
            onNodeClick = { template ->
                viewModel.selectProtocolTemplate(template.id)
            },
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.34f),
            searchPlaceholder = "搜索协议模板",
            getId = { item -> item.id },
            getLabel = { item -> item.name },
            getChildren = { emptyList() },
            getIcon = { Icons.Outlined.SettingsEthernet },
            header = {
                HostConfigSectionTitle("协议模板目录")
                state.errorMessage?.let { message ->
                    HostConfigStatusStrip(message)
                }
                WorkbenchActionButton(
                    text = if (state.loading) "加载中" else "刷新",
                    onClick = viewModel::refresh,
                    variant = WorkbenchButtonVariant.Outline,
                )
            },
        )

        Column(
            modifier = Modifier
                .weight(0.66f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val selected = state.selectedProtocolTemplate
            HostConfigPanel(
                title = selected?.name ?: "暂无协议模板",
                subtitle = selected?.description ?: "这里展示内置协议模板以及对应模块目录。",
            ) {
                HostConfigKeyValueRow("模板编码", selected?.code ?: "-")
                HostConfigKeyValueRow("排序值", selected?.sortIndex?.toString() ?: "-")
                HostConfigKeyValueRow("模块模板数量", state.moduleTemplates.size.toString())
            }

            HostConfigPanel(
                title = "模块模板清单",
                subtitle = "当前协议模板下可直接复用的模块类型。",
            ) {
                if (state.moduleTemplates.isEmpty()) {
                    HostConfigStatusStrip("当前协议模板还没有模块模板。")
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        state.moduleTemplates.forEach { module ->
                            HostConfigPanel(
                                title = module.name,
                                subtitle = module.description,
                            ) {
                                HostConfigKeyValueRow("模板编码", module.code)
                                HostConfigKeyValueRow("归属协议模板", module.protocolTemplateId.toString())
                                HostConfigKeyValueRow("通道数量", module.channelCount?.toString() ?: "-")
                            }
                        }
                    }
                }
            }

            HostConfigPanel(
                title = "使用说明",
                subtitle = "本页只读，用于在项目页创建协议与模块时选型。",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Memory,
                        contentDescription = null,
                    )
                    CupertinoText(
                        text = "协议模板与模块模板都来自插件内置种子数据，不在这里直接编辑。",
                        modifier = Modifier.fillMaxWidth(),
                        style = CupertinoTheme.typography.body,
                    )
                }
            }
        }
    }
}
