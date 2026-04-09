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
    title = "功能定义",
    routePath = "codegen-context/contexts",
    icon = "Code",
    order = 40.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "宿主配置",
            icon = "SettingsApplications",
            order = 10,
        ),
        defaultInScene = false,
    ),
)
@Composable
fun CodegenContextScreen() {
    val viewModel = koinViewModel<CodegenContextViewModel>()
    val state = viewModel.screenState
    val selectedProtocolTemplate = state.protocolTemplates.firstOrNull { it.id == state.editor.protocolTemplateId }

    Row(modifier = Modifier.fillMaxSize()) {
        WorkbenchTreeSidebar(
            items = state.contexts,
            selectedId = state.selectedContextId,
            onNodeClick = { viewModel.selectContext(it.id) },
            modifier = Modifier.fillMaxHeight().weight(0.28f),
            searchPlaceholder = "搜索上下文",
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
            GenerationSettingsPanel(
                state = state.editor.generationSettings,
                selectedProtocolTemplate = selectedProtocolTemplate,
                viewModel = viewModel,
            )
            ContextDefinitionsPanel(
                definitions = state.editor.availableContextDefinitions,
                selectedProtocolTemplate = selectedProtocolTemplate,
            )
            PropertyPoolPanel(editor = state.editor, viewModel = viewModel)
            MethodWorkbenchPanel(editor = state.editor, viewModel = viewModel)
        }
    }
}
