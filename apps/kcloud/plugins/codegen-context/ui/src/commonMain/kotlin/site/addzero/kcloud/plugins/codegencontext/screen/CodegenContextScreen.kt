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
    val selectedProtocolTemplate = state.protocolTemplates.firstOrNull { item -> item.id == state.draft.protocolTemplateId }

    Row(modifier = Modifier.fillMaxSize()) {
        WorkbenchTreeSidebar(
            items = state.contexts,
            selectedId = state.selectedContextId,
            onNodeClick = { viewModel.selectContext(it.id) },
            modifier = Modifier.fillMaxHeight().weight(0.26f),
            searchPlaceholder = "搜索元数据草稿",
            getId = { it.id },
            getLabel = { it.name.ifBlank { it.code } },
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
                draft = state.draft,
                protocolTemplates = state.protocolTemplates,
                selectedProtocolTemplateDescription = selectedProtocolTemplate?.description,
                saving = state.saving,
                previewing = state.previewing,
                exporting = state.exporting,
                onSelectProtocolTemplate = viewModel::selectProtocolTemplate,
                onUpdateCode = { value -> viewModel.updateDraft { it.copy(code = value) } },
                onUpdateName = { value -> viewModel.updateDraft { it.copy(name = value) } },
                onUpdateDescription = { value -> viewModel.updateDraft { it.copy(description = value) } },
                onUpdateEnabled = { value -> viewModel.updateDraft { it.copy(enabled = value) } },
                onSave = viewModel::save,
                onPreview = viewModel::preview,
                onExport = viewModel::exportSelected,
            )
            DeviceFunctionsPanel(
                draft = state.draft,
                definitions = state.availableContextDefinitions,
                onAddFunction = viewModel::addDeviceFunction,
                onRemoveFunction = viewModel::removeDeviceFunction,
                onUpdateFunction = viewModel::updateDeviceFunction,
                onToggleFunctionProperty = viewModel::toggleFunctionPropertySelection,
                onUpdateBinding = viewModel::updateDeviceFunctionBindingValue,
            )
            ThingPropertiesPanel(
                draft = state.draft,
                definitions = state.availableContextDefinitions,
                onAddProperty = viewModel::addThingProperty,
                onRemoveProperty = viewModel::removeThingProperty,
                onUpdateProperty = viewModel::updateThingProperty,
                onUpdateBinding = viewModel::updateThingPropertyBindingValue,
            )
            ExportWorkbenchPanel(
                draft = state.draft,
                preview = state.preview,
                exportResult = state.exportResult,
                onToggleKotlinTransport = viewModel::toggleKotlinClientTransport,
                onToggleCTransport = viewModel::toggleCExposeTransport,
                onToggleArtifactKind = viewModel::toggleArtifactKind,
                onUpdateFirmwareSync = viewModel::updateFirmwareSync,
                onUpdateRtuDefaults = viewModel::updateRtuDefaults,
                onUpdateTcpDefaults = viewModel::updateTcpDefaults,
                onUpdateMqttDefaults = viewModel::updateMqttDefaults,
            )
        }
    }
}
