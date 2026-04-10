@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen.protocols

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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.cupertino.workbench.components.panel.CupertinoKeyValueRow
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.hostconfig.protocols.ProtocolsViewModel

@Route(
    value = "字典管理",
    title = "协议字典",
    routePath = "host-config/protocols",
    icon = "Key",
    order = 10.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "系统管理",
            icon = "SettingsApplications",
            order = Int.MAX_VALUE,
        ),
    ),
)
@Composable
/**
 * 处理协议界面。
 */
fun ProtocolsScreen() {
    val viewModel = koinViewModel<ProtocolsViewModel>()
    val state = viewModel.screenState
    val sidebarHeaderSpi = koinInject<ProtocolsSidebarHeaderSpi>()

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
            searchPlaceholder = "搜索协议字典",
            getId = { item -> item.id },
            getLabel = { item -> item.name },
            getChildren = { emptyList() },
            getIcon = { Icons.Outlined.SettingsEthernet },
            header = {
                sidebarHeaderSpi.Render(
                    state = state,
                    viewModel = viewModel,
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
            CupertinoPanel(
                title = selected?.name ?: "暂无协议字典项",
                subtitle = selected?.description ?: "这里展示系统内置协议字典以及对应模块模板。",
            ) {
                CupertinoKeyValueRow("模板编码", selected?.code ?: "-")
                CupertinoKeyValueRow("排序值", selected?.sortIndex?.toString() ?: "-")
                CupertinoKeyValueRow("模块模板数量", state.moduleTemplates.size.toString())
            }

            CupertinoPanel(
                title = "模块模板清单",
                subtitle = "当前协议模板下可直接复用的模块类型。",
            ) {
                if (state.moduleTemplates.isEmpty()) {
                    CupertinoStatusStrip("当前协议模板还没有模块模板。")
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        state.moduleTemplates.forEach { module ->
                            CupertinoPanel(
                                title = module.name,
                                subtitle = module.description,
                            ) {
                                CupertinoKeyValueRow("模板编码", module.code)
                                CupertinoKeyValueRow("归属协议模板", module.protocolTemplateId.toString())
                                CupertinoKeyValueRow("通道数量", module.channelCount?.toString() ?: "-")
                            }
                        }
                    }
                }
            }

            CupertinoPanel(
                title = "使用说明",
                subtitle = "本页只读，用于维护系统协议字典认知，不在工程页创建协议。",
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
                        text = "这里展示的是系统内置协议模板和模块模板，工程页只应做协议关联，不应承担协议创建入口。",
                        modifier = Modifier.fillMaxWidth(),
                        style = CupertinoTheme.typography.body,
                    )
                }
            }
        }
    }
}
