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
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.cupertino.workbench.button.WorkbenchActionButton
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.cupertino.workbench.sidebar.WorkbenchTreeSidebar
import site.addzero.kcloud.plugins.codegencontext.context.CodegenContextViewModel

@Route(
    title = "元数据管理",
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
    val selectedProtocolTemplate = state.protocolTemplates.firstOrNull { it.id == state.editor.protocolTemplateId }
    var selectedTab by remember { mutableStateOf(CodegenWorkbenchTab.THING_MODEL) }

    Row(modifier = Modifier.fillMaxSize()) {
        WorkbenchTreeSidebar(
            items = state.contexts,
            selectedId = state.selectedContextId,
            onNodeClick = { viewModel.selectContext(it.id) },
            modifier = Modifier.fillMaxHeight().weight(0.28f),
            searchPlaceholder = "搜索元数据上下文",
            getId = { it.id },
            getLabel = { it.name },
            getChildren = { emptyList() },
            getIcon = { Icons.Outlined.DataObject },
            header = {
                WorkbenchActionButton(
                    text = if (state.loading) "加载中" else "刷新",
                    onClick = viewModel::refresh,
                    imageVector = Icons.Outlined.Refresh,
                    variant = WorkbenchButtonVariant.Outline,
                )
                WorkbenchActionButton(text = "新建", onClick = viewModel::newContext)
                WorkbenchActionButton(
                    text = "删除",
                    onClick = viewModel::deleteSelected,
                    enabled = state.selectedContextId != null && !state.deleting,
                    variant = WorkbenchButtonVariant.Destructive,
                )
            },
        )

        Column(
            modifier = Modifier
                .weight(0.72f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.errorMessage?.let { CupertinoStatusStrip(text = it, tone = Color(0xFFFFE8E6)) }
            state.statusMessage?.let { CupertinoStatusStrip(it) }
            if (state.generatedFiles.isNotEmpty()) {
                GeneratedFilesPanel(state.generatedFiles)
            }
            ContextSummaryPanel(
                state = state.editor,
                selectedProtocolTemplate = selectedProtocolTemplate,
                protocolTemplates = state.protocolTemplates,
                viewModel = viewModel,
                saving = state.saving,
                generating = state.generating,
            )
            MetadataOverviewStrip(
                editor = state.editor,
                selectedProtocolTemplate = selectedProtocolTemplate,
            )
            WorkbenchTabSwitcher(
                selectedTab = selectedTab,
                editor = state.editor,
                onSelected = { selectedTab = it },
            )
            when (selectedTab) {
                CodegenWorkbenchTab.THING_MODEL -> PropertyPoolPanel(editor = state.editor, viewModel = viewModel)
                CodegenWorkbenchTab.DEVICE_FUNCTION -> MethodWorkbenchPanel(editor = state.editor, viewModel = viewModel)
                CodegenWorkbenchTab.CONTEXT -> {
                    ContextDefinitionsPanel(
                        definitions = state.editor.availableContextDefinitions,
                        selectedProtocolTemplate = selectedProtocolTemplate,
                    )
                    GenerationSettingsPanel(
                        state = state.editor.generationSettings,
                        selectedProtocolTemplate = selectedProtocolTemplate,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}
