@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.codegencontext.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextWorkbenchTab
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel
import site.addzero.kcloud.plugins.codegencontext.screen.contexts.CodegenContextSidebarHeaderSpi

@Route(
    title = "元数据建模与导出",
    routePath = "codegen-context/contexts",
    icon = "Code",
    order = 40.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "元数据配置",
            icon = "SettingsApplications",
            order = -10,
        ),
        defaultInScene = false,
    ),
)
@Composable
fun CodegenContextScreen() {
    val viewModel = koinViewModel<CodegenContextViewModel>()
    val state = viewModel.screenState
    val sidebarHeaderSpi = koinInject<CodegenContextSidebarHeaderSpi>()

    Row(modifier = Modifier.fillMaxSize()) {
        WorkbenchTreeSidebar(
            items = state.contexts,
            selectedId = state.selectedContextId,
            onNodeClick = { viewModel.selectContext(it.id) },
            modifier = Modifier.fillMaxHeight().weight(0.26f),
            searchPlaceholder = "搜索建模配置",
            getId = { it.id },
            getLabel = { it.name.ifBlank { it.code } },
            getChildren = { emptyList() },
            getIcon = { Icons.Outlined.DataObject },
            header = {
                sidebarHeaderSpi.Render(
                    state = state,
                    viewModel = viewModel,
                )
            },
        )

        Column(
            modifier =
                Modifier
                    .weight(0.74f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.errorMessage?.let { CupertinoStatusStrip(text = it, tone = Color(0xFFFFE8E6)) }
            state.statusMessage?.let { CupertinoStatusStrip(text = it) }
            ContextDraftPanel(
                state = state,
                viewModel = viewModel,
            )
            WorkbenchTabsPanel(
                state = state,
                viewModel = viewModel,
            )
            MetadataPreviewSummaryPanel(state = state)
            when (state.selectedWorkbenchTab) {
                CodegenContextWorkbenchTab.DEVICE_FUNCTIONS ->
                    DeviceFunctionsPanel(
                        state = state,
                        viewModel = viewModel,
                    )

                CodegenContextWorkbenchTab.THING_PROPERTIES ->
                    ThingPropertiesPanel(
                        state = state,
                        viewModel = viewModel,
                    )
            }
            ExportWorkbenchPanel(
                state = state,
                viewModel = viewModel,
            )
        }
    }
}
