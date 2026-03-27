package site.addzero.coding.playground.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import site.addzero.coding.playground.PlaygroundUiLanguage
import site.addzero.coding.playground.PlaygroundWorkbenchState
import site.addzero.coding.playground.shared.dto.*

@Composable
fun PlaygroundApp(state: PlaygroundWorkbenchState) {
    val scope = rememberCoroutineScope()
    val tabs = listOf("module", "types", "globals", "functions", "metadata", "compile", "snapshot", "preview")
    var selectedTab by remember { mutableStateOf("module") }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF345778),
            secondary = Color(0xFF5B7289),
            surface = Color(0xFFF4F6F8),
            background = Color(0xFFECEFF3),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LeftExplorer(
                state = state,
                onRefresh = { scope.launch { state.refreshAll() } },
                onNewModule = {
                    scope.launch {
                        state.saveModule(
                            selectedId = null,
                            request = CreateLlvmModuleRequest(
                                name = "sample-module",
                                sourceFilename = "sample.ll",
                                targetTriple = "x86_64-unknown-linux-gnu",
                                dataLayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128",
                                description = "New LLVM IR module",
                            ),
                        )
                    }
                },
                modifier = Modifier.width(260.dp).fillMaxHeight(),
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                WorkbenchHeader(state = state, onToggleLanguage = state::toggleLanguage)
                PrimaryTabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = tabs[index] == selectedTab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    when (tab) {
                                        "module" -> tr(state, "模块", "Module")
                                        "types" -> tr(state, "类型", "Types")
                                        "globals" -> tr(state, "全局", "Globals")
                                        "functions" -> tr(state, "函数", "Functions")
                                        "metadata" -> tr(state, "Metadata", "Metadata")
                                        "compile" -> tr(state, "编译", "Compile")
                                        "snapshot" -> tr(state, "快照", "Snapshot")
                                        else -> tr(state, "预览", "Preview")
                                    },
                                )
                            },
                        )
                    }
                }
                when (selectedTab) {
                    "module" -> ModuleTab(state, scope)
                    "types" -> TypeTab(state, scope)
                    "globals" -> GlobalTab(state, scope)
                    "functions" -> FunctionTab(state, scope)
                    "metadata" -> MetadataTab(state, scope)
                    "compile" -> CompileTab(state, scope)
                    "snapshot" -> SnapshotTab(state, scope)
                    else -> PreviewTab(state, scope)
                }
            }
        }
    }
}

@Composable
private fun LeftExplorer(
    state: PlaygroundWorkbenchState,
    onRefresh: () -> Unit,
    onNewModule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surface).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(tr(state, "LLVM IR 工作台", "LLVM IR Workbench"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { state.searchQuery = it },
            label = { Text(tr(state, "全文检索", "Search")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRefresh) { Text(tr(state, "刷新", "Refresh")) }
            OutlinedButton(onClick = onNewModule) { Text(tr(state, "新模块", "New")) }
        }
        HorizontalDivider()
        Text(tr(state, "模块树", "Module Tree"), fontWeight = FontWeight.SemiBold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            items(state.modules) { module ->
                val selected = module.id == state.selectedModuleId
                Surface(
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                    tonalElevation = if (selected) 2.dp else 0.dp,
                    modifier = Modifier.fillMaxWidth().clickable { state.selectModule(module.id) },
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(module.name, fontWeight = FontWeight.SemiBold)
                        Text(module.targetTriple, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
        HorizontalDivider()
        Text(state.statusMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun WorkbenchHeader(
    state: PlaygroundWorkbenchState,
    onToggleLanguage: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    state.modules.firstOrNull { it.id == state.selectedModuleId }?.name ?: tr(state, "未选择模块", "No Module Selected"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    tr(state, "SQLite + Jimmer + Compose Desktop", "SQLite + Jimmer + Compose Desktop"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            TextButton(onClick = onToggleLanguage) {
                Text(if (state.uiLanguage == PlaygroundUiLanguage.ZH_CN) "EN" else "中文")
            }
        }
    }
}

@Composable
private fun ModuleTab(state: PlaygroundWorkbenchState, scope: CoroutineScope) {
    val selected = state.modules.firstOrNull { it.id == state.selectedModuleId }
    var name by remember(selected?.id) { mutableStateOf(selected?.name.orEmpty()) }
    var sourceFilename by remember(selected?.id) { mutableStateOf(selected?.sourceFilename ?: "sample.ll") }
    var targetTriple by remember(selected?.id) { mutableStateOf(selected?.targetTriple ?: "x86_64-unknown-linux-gnu") }
    var dataLayout by remember(selected?.id) { mutableStateOf(selected?.dataLayout ?: "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128") }
    var moduleAsm by remember(selected?.id) { mutableStateOf(selected?.moduleAsm.orEmpty()) }
    var description by remember(selected?.id) { mutableStateOf(selected?.description.orEmpty()) }
    var moduleFlags by remember(selected?.id) { mutableStateOf(selected?.moduleFlags?.entries?.joinToString("\n") { "${it.key}=${it.value}" }.orEmpty()) }

    EditorSurface {
        FormSection(title = tr(state, "模块信息", "Module")) {
            LabeledField(tr(state, "名称", "Name")) {
                OutlinedTextField(name, { name = it }, modifier = Modifier.fillMaxWidth())
            }
            LabeledField(tr(state, "源文件名", "Source Filename")) {
                OutlinedTextField(sourceFilename, { sourceFilename = it }, modifier = Modifier.fillMaxWidth())
            }
            LabeledField(tr(state, "Target Triple", "Target Triple")) {
                OutlinedTextField(targetTriple, { targetTriple = it }, modifier = Modifier.fillMaxWidth())
            }
            LabeledField(tr(state, "Data Layout", "Data Layout")) {
                OutlinedTextField(dataLayout, { dataLayout = it }, modifier = Modifier.fillMaxWidth())
            }
            LabeledField(tr(state, "Module ASM", "Module ASM")) {
                OutlinedTextField(moduleAsm, { moduleAsm = it }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
            LabeledField(tr(state, "描述", "Description")) {
                OutlinedTextField(description, { description = it }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
            LabeledField(tr(state, "Module Flags (key=value)", "Module Flags")) {
                OutlinedTextField(moduleFlags, { moduleFlags = it }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
            ActionRow(
                onPrimary = {
                    scope.launch {
                        state.saveModule(
                            selected?.id,
                            CreateLlvmModuleRequest(
                                name = name,
                                sourceFilename = sourceFilename,
                                targetTriple = targetTriple,
                                dataLayout = dataLayout,
                                moduleAsm = moduleAsm.ifBlank { null },
                                moduleFlags = parseMapText(moduleFlags),
                                description = description.ifBlank { null },
                            ),
                        )
                    }
                },
                onSecondary = { scope.launch { state.validateSelectedModule() } },
                primaryLabel = tr(state, "保存模块", "Save Module"),
                secondaryLabel = tr(state, "校验模块", "Validate"),
            )
            if (selected != null) {
                OutlinedButton(onClick = { scope.launch { state.removeSelectedModule() } }) {
                    Text(tr(state, "删除模块", "Delete Module"))
                }
            }
        }
    }
}

@Composable
private fun TypeTab(state: PlaygroundWorkbenchState, scope: CoroutineScope) {
    val selected = state.types.firstOrNull { it.id == state.selectedTypeId }
    val members = state.typeMembers.filter { it.typeId == selected?.id }
    var name by remember(selected?.id) { mutableStateOf(selected?.name ?: "i32") }
    var symbol by remember(selected?.id) { mutableStateOf(selected?.symbol ?: "MyType") }
    var kind by remember(selected?.id) { mutableStateOf(selected?.kind ?: LlvmTypeKind.INTEGER) }
    var primitiveWidth by remember(selected?.id) { mutableStateOf(selected?.primitiveWidth?.toString().orEmpty()) }
    var definitionText by remember(selected?.id) { mutableStateOf(selected?.definitionText.orEmpty()) }

    SplitEditor(
        left = {
            ExplorerList(
                title = tr(state, "类型列表", "Type List"),
                items = state.types,
                selectedId = state.selectedTypeId,
                itemId = { it.id },
                itemTitle = { it.symbol },
                itemSubtitle = { it.kind.name },
                onSelect = state::selectType,
            )
        },
        right = {
            FormSection(title = tr(state, "类型编辑", "Type Editor")) {
                LabeledField(tr(state, "名称", "Name")) { OutlinedTextField(name, { name = it }, modifier = Modifier.fillMaxWidth()) }
                LabeledField(tr(state, "符号", "Symbol")) { OutlinedTextField(symbol, { symbol = it }, modifier = Modifier.fillMaxWidth()) }
                LabeledField(tr(state, "类型种类", "Type Kind")) {
                    EnumSelector(kind, LlvmTypeKind.entries, onSelected = { kind = it })
                }
                LabeledField(tr(state, "位宽 / 长度", "Width / Length")) {
                    OutlinedTextField(primitiveWidth, { primitiveWidth = it }, modifier = Modifier.fillMaxWidth())
                }
                LabeledField(tr(state, "定义文本", "Definition Text")) {
                    OutlinedTextField(definitionText, { definitionText = it }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
                ActionRow(
                    onPrimary = {
                        scope.launch {
                            state.saveType(
                                selected?.id,
                                CreateLlvmTypeRequest(
                                    moduleId = state.selectedModuleId ?: return@launch,
                                    name = name,
                                    symbol = symbol,
                                    kind = kind,
                                    primitiveWidth = primitiveWidth.toIntOrNull(),
                                    arrayLength = primitiveWidth.toIntOrNull(),
                                    definitionText = definitionText.ifBlank { null },
                                ),
                            )
                        }
                    },
                    onSecondary = { scope.launch { state.removeSelectedType() } },
                    primaryLabel = tr(state, "保存类型", "Save Type"),
                    secondaryLabel = tr(state, "删除类型", "Delete Type"),
                )
                Text(tr(state, "成员", "Members"), fontWeight = FontWeight.SemiBold)
                members.forEach {
                    Text("${it.orderIndex}. ${it.name}: ${it.memberTypeText}", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
    )
}

@Composable
private fun GlobalTab(state: PlaygroundWorkbenchState, scope: CoroutineScope) {
    val selected = remember(state.globals) { state.globals.firstOrNull() }
    var name by remember(selected?.id) { mutableStateOf(selected?.name ?: "globalValue") }
    var symbol by remember(selected?.id) { mutableStateOf(selected?.symbol ?: "gValue") }
    var typeText by remember(selected?.id) { mutableStateOf(selected?.typeText ?: "i32") }
    var initializer by remember(selected?.id) { mutableStateOf(selected?.initializerText ?: "0") }
    var constant by remember(selected?.id) { mutableStateOf(selected?.constant ?: false) }

    SplitEditor(
        left = {
            ExplorerList(
                title = tr(state, "全局列表", "Globals"),
                items = state.globals,
                selectedId = selected?.id,
                itemId = { it.id },
                itemTitle = { it.symbol },
                itemSubtitle = { it.typeText },
                onSelect = {},
            )
        },
        right = {
            FormSection(title = tr(state, "全局变量", "Global Variable")) {
                LabeledField(tr(state, "名称", "Name")) { OutlinedTextField(name, { name = it }, modifier = Modifier.fillMaxWidth()) }
                LabeledField(tr(state, "符号", "Symbol")) { OutlinedTextField(symbol, { symbol = it }, modifier = Modifier.fillMaxWidth()) }
                LabeledField(tr(state, "类型", "Type")) { OutlinedTextField(typeText, { typeText = it }, modifier = Modifier.fillMaxWidth()) }
                LabeledField(tr(state, "初始化", "Initializer")) { OutlinedTextField(initializer, { initializer = it }, modifier = Modifier.fillMaxWidth()) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = constant, onCheckedChange = { constant = it })
                    Text(tr(state, "常量", "Constant"))
                }
                Button(onClick = {
                    scope.launch {
                        state.saveGlobal(
                            selected?.id,
                            CreateLlvmGlobalVariableRequest(
                                moduleId = state.selectedModuleId ?: return@launch,
                                name = name,
                                symbol = symbol,
                                typeText = typeText,
                                initializerText = initializer,
                                constant = constant,
                            ),
                        )
                    }
                }) { Text(tr(state, "保存全局", "Save Global")) }
            }
        },
    )
}

@Composable
private fun FunctionTab(state: PlaygroundWorkbenchState, scope: CoroutineScope) {
    val selected = state.functions.firstOrNull { it.id == state.selectedFunctionId }
    val selectedBlock = state.blocks.firstOrNull { it.id == state.selectedBlockId }
    val selectedInstruction = state.instructions.firstOrNull { it.id == state.selectedInstructionId }
    var functionName by remember(selected?.id) { mutableStateOf(selected?.name ?: "main") }
    var functionSymbol by remember(selected?.id) { mutableStateOf(selected?.symbol ?: "main") }
    var returnType by remember(selected?.id) { mutableStateOf(selected?.returnTypeText ?: "i32") }
    var declarationOnly by remember(selected?.id) { mutableStateOf(selected?.declarationOnly ?: false) }
    var blockName by remember(selectedBlock?.id) { mutableStateOf(selectedBlock?.name ?: "entry") }
    var blockLabel by remember(selectedBlock?.id) { mutableStateOf(selectedBlock?.label ?: "entry") }
    var opcode by remember(selectedInstruction?.id) { mutableStateOf(selectedInstruction?.opcode ?: LlvmInstructionOpcode.RET) }
    var resultSymbol by remember(selectedInstruction?.id) { mutableStateOf(selectedInstruction?.resultSymbol.orEmpty()) }
    var instructionType by remember(selectedInstruction?.id) { mutableStateOf(selectedInstruction?.typeText ?: "i32") }
    var suffix by remember(selectedInstruction?.id) { mutableStateOf(selectedInstruction?.textSuffix.orEmpty()) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SplitEditor(
            left = {
                ExplorerList(
                    title = tr(state, "函数列表", "Functions"),
                    items = state.functions,
                    selectedId = state.selectedFunctionId,
                    itemId = { it.id },
                    itemTitle = { it.symbol },
                    itemSubtitle = { it.returnTypeText },
                    onSelect = state::selectFunction,
                )
            },
            right = {
                FormSection(title = tr(state, "函数编辑", "Function Editor")) {
                    LabeledField(tr(state, "名称", "Name")) { OutlinedTextField(functionName, { functionName = it }, modifier = Modifier.fillMaxWidth()) }
                    LabeledField(tr(state, "符号", "Symbol")) { OutlinedTextField(functionSymbol, { functionSymbol = it }, modifier = Modifier.fillMaxWidth()) }
                    LabeledField(tr(state, "返回类型", "Return Type")) { OutlinedTextField(returnType, { returnType = it }, modifier = Modifier.fillMaxWidth()) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = declarationOnly, onCheckedChange = { declarationOnly = it })
                        Text(tr(state, "仅声明", "Declaration Only"))
                    }
                    ActionRow(
                        onPrimary = {
                            scope.launch {
                                state.saveFunction(
                                    selected?.id,
                                    CreateLlvmFunctionRequest(
                                        moduleId = state.selectedModuleId ?: return@launch,
                                        name = functionName,
                                        symbol = functionSymbol,
                                        returnTypeText = returnType,
                                        declarationOnly = declarationOnly,
                                    ),
                                )
                            }
                        },
                        onSecondary = { scope.launch { state.removeSelectedFunction() } },
                        primaryLabel = tr(state, "保存函数", "Save Function"),
                        secondaryLabel = tr(state, "删除函数", "Delete Function"),
                    )
                }
            },
        )
        SplitEditor(
            left = {
                ExplorerList(
                    title = tr(state, "基本块", "Basic Blocks"),
                    items = state.blocks.filter { it.functionId == state.selectedFunctionId },
                    selectedId = state.selectedBlockId,
                    itemId = { it.id },
                    itemTitle = { it.label },
                    itemSubtitle = { it.name },
                    onSelect = state::selectBlock,
                )
            },
            right = {
                FormSection(title = tr(state, "Block / Instruction", "Block / Instruction")) {
                    LabeledField(tr(state, "Block 名称", "Block Name")) { OutlinedTextField(blockName, { blockName = it }, modifier = Modifier.fillMaxWidth()) }
                    LabeledField(tr(state, "Block 标签", "Block Label")) { OutlinedTextField(blockLabel, { blockLabel = it }, modifier = Modifier.fillMaxWidth()) }
                    ActionRow(
                        onPrimary = { scope.launch { state.saveBlock(selectedBlock?.id, blockName, blockLabel) } },
                        onSecondary = { scope.launch { state.removeSelectedBlock() } },
                        primaryLabel = tr(state, "保存 Block", "Save Block"),
                        secondaryLabel = tr(state, "删除 Block", "Delete Block"),
                    )
                    HorizontalDivider()
                    Text(tr(state, "指令", "Instructions"), fontWeight = FontWeight.SemiBold)
                    ExplorerList(
                        title = "",
                        items = state.instructions.filter { it.blockId == state.selectedBlockId },
                        selectedId = state.selectedInstructionId,
                        itemId = { it.id },
                        itemTitle = { it.opcode.name },
                        itemSubtitle = { it.resultSymbol ?: it.textSuffix ?: "" },
                        onSelect = state::selectInstruction,
                    )
                    LabeledField(tr(state, "Opcode", "Opcode")) { EnumSelector(opcode, LlvmInstructionOpcode.entries, onSelected = { opcode = it }) }
                    LabeledField(tr(state, "结果符号", "Result Symbol")) { OutlinedTextField(resultSymbol, { resultSymbol = it }, modifier = Modifier.fillMaxWidth()) }
                    LabeledField(tr(state, "类型", "Type")) { OutlinedTextField(instructionType, { instructionType = it }, modifier = Modifier.fillMaxWidth()) }
                    LabeledField(tr(state, "后缀 / 细节", "Suffix / Detail")) { OutlinedTextField(suffix, { suffix = it }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
                    ActionRow(
                        onPrimary = {
                            scope.launch {
                                state.saveInstruction(
                                    selectedInstruction?.id,
                                    CreateLlvmInstructionRequest(
                                        blockId = state.selectedBlockId ?: return@launch,
                                        opcode = opcode,
                                        resultSymbol = resultSymbol.ifBlank { null },
                                        typeText = instructionType.ifBlank { null },
                                        textSuffix = suffix.ifBlank { null },
                                        terminator = opcode in listOf(LlvmInstructionOpcode.RET, LlvmInstructionOpcode.BR, LlvmInstructionOpcode.SWITCH, LlvmInstructionOpcode.UNREACHABLE),
                                    ),
                                )
                            }
                        },
                        onSecondary = { scope.launch { state.removeSelectedInstruction() } },
                        primaryLabel = tr(state, "保存指令", "Save Instruction"),
                        secondaryLabel = tr(state, "删除指令", "Delete Instruction"),
                    )
                }
            },
        )
    }
}

@Composable
private fun MetadataTab(state: PlaygroundWorkbenchState, scope: CoroutineScope) {
    val selected = state.metadataNodes.firstOrNull { it.id == state.selectedMetadataNodeId }
    var name by remember(selected?.id) { mutableStateOf(selected?.name ?: "") }
    var kind by remember(selected?.id) { mutableStateOf(selected?.kind ?: LlvmMetadataKind.GENERIC) }
    var distinct by remember(selected?.id) { mutableStateOf(selected?.distinct ?: false) }

    SplitEditor(
        left = {
            ExplorerList(
                title = tr(state, "Metadata 节点", "Metadata Nodes"),
                items = state.metadataNodes,
                selectedId = state.selectedMetadataNodeId,
                itemId = { it.id },
                itemTitle = { it.name ?: "!anon" },
                itemSubtitle = { it.kind.name },
                onSelect = state::selectMetadataNode,
            )
        },
        right = {
            FormSection(title = tr(state, "Metadata 编辑", "Metadata Editor")) {
                LabeledField(tr(state, "名称", "Name")) { OutlinedTextField(name, { name = it }, modifier = Modifier.fillMaxWidth()) }
                LabeledField(tr(state, "种类", "Kind")) { EnumSelector(kind, LlvmMetadataKind.entries, onSelected = { kind = it }) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = distinct, onCheckedChange = { distinct = it })
                    Text("distinct")
                }
                ActionRow(
                    onPrimary = {
                        scope.launch {
                            state.saveMetadataNode(
                                selected?.id,
                                CreateLlvmMetadataNodeRequest(
                                    moduleId = state.selectedModuleId ?: return@launch,
                                    name = name.ifBlank { null },
                                    kind = kind,
                                    distinct = distinct,
                                ),
                            )
                        }
                    },
                    onSecondary = { scope.launch { state.removeSelectedMetadataNode() } },
                    primaryLabel = tr(state, "保存 Metadata", "Save Metadata"),
                    secondaryLabel = tr(state, "删除 Metadata", "Delete Metadata"),
                )
                HorizontalDivider()
                Text(tr(state, "已命名 Metadata", "Named Metadata"), fontWeight = FontWeight.SemiBold)
                state.namedMetadata.forEach {
                    Text("!${it.name}", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
    )
}

@Composable
private fun CompileTab(state: PlaygroundWorkbenchState, scope: CoroutineScope) {
    val selected = state.compileProfiles.firstOrNull { it.id == state.selectedProfileId }
    var name by remember(selected?.id) { mutableStateOf(selected?.name ?: "local") }
    var targetPlatform by remember(selected?.id) { mutableStateOf(selected?.targetPlatform ?: "host") }
    var outputDir by remember(selected?.id) { mutableStateOf(selected?.outputDirectory ?: "/tmp/llvm-ir-out") }
    var optPath by remember(selected?.id) { mutableStateOf(selected?.optPath.orEmpty()) }
    var llcPath by remember(selected?.id) { mutableStateOf(selected?.llcPath.orEmpty()) }
    var clangPath by remember(selected?.id) { mutableStateOf(selected?.clangPath.orEmpty()) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SplitEditor(
            left = {
                ExplorerList(
                    title = tr(state, "编译配置", "Compile Profiles"),
                    items = state.compileProfiles,
                    selectedId = state.selectedProfileId,
                    itemId = { it.id },
                    itemTitle = { it.name },
                    itemSubtitle = { it.targetPlatform },
                    onSelect = state::selectProfile,
                )
            },
            right = {
                FormSection(title = tr(state, "编译配置编辑", "Compile Profile Editor")) {
                    LabeledField(tr(state, "名称", "Name")) { OutlinedTextField(name, { name = it }, modifier = Modifier.fillMaxWidth()) }
                    LabeledField(tr(state, "目标平台", "Target Platform")) { OutlinedTextField(targetPlatform, { targetPlatform = it }, modifier = Modifier.fillMaxWidth()) }
                    LabeledField(tr(state, "输出目录", "Output Directory")) { OutlinedTextField(outputDir, { outputDir = it }, modifier = Modifier.fillMaxWidth()) }
                    LabeledField("opt") { OutlinedTextField(optPath, { optPath = it }, modifier = Modifier.fillMaxWidth()) }
                    LabeledField("llc") { OutlinedTextField(llcPath, { llcPath = it }, modifier = Modifier.fillMaxWidth()) }
                    LabeledField("clang") { OutlinedTextField(clangPath, { clangPath = it }, modifier = Modifier.fillMaxWidth()) }
                    ActionRow(
                        onPrimary = {
                            scope.launch {
                                state.saveCompileProfile(
                                    selected?.id,
                                    CreateLlvmCompileProfileRequest(
                                        moduleId = state.selectedModuleId ?: return@launch,
                                        name = name,
                                        targetPlatform = targetPlatform,
                                        outputDirectory = outputDir,
                                        optPath = optPath.ifBlank { null },
                                        llcPath = llcPath.ifBlank { null },
                                        clangPath = clangPath.ifBlank { null },
                                    ),
                                )
                            }
                        },
                        onSecondary = { scope.launch { state.removeSelectedProfile() } },
                        primaryLabel = tr(state, "保存配置", "Save Profile"),
                        secondaryLabel = tr(state, "删除配置", "Delete Profile"),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { scope.launch { state.exportSelectedModule() } }) { Text(tr(state, "导出 .ll", "Export .ll")) }
                        FilledTonalButton(onClick = { scope.launch { state.createAndRunCompileJob() } }) { Text(tr(state, "执行编译", "Run")) }
                    }
                }
            },
        )
        FormSection(title = tr(state, "编译任务", "Compile Jobs")) {
            state.compileJobs.forEach { job ->
                Text("${job.status.name}  ${job.id}", style = MaterialTheme.typography.bodySmall)
            }
            state.lastCompileResult?.let { result ->
                HorizontalDivider()
                Text(tr(state, "最近编译结果", "Latest Compile Result"), fontWeight = FontWeight.SemiBold)
                Text(result.job.status.name, fontWeight = FontWeight.Bold)
                Text(result.steps.joinToString("\n\n") { step ->
                    buildString {
                        append(step.command.joinToString(" "))
                        append("\nexit=${step.exitCode}")
                        if (step.stdoutText.isNotBlank()) append("\nstdout:\n${step.stdoutText}")
                        if (step.stderrText.isNotBlank()) append("\nstderr:\n${step.stderrText}")
                    }
                })
            }
        }
    }
}

@Composable
private fun SnapshotTab(state: PlaygroundWorkbenchState, scope: CoroutineScope) {
    EditorSurface {
        FormSection(title = tr(state, "快照", "Snapshot")) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scope.launch { state.exportSnapshot() } }) { Text(tr(state, "导出快照", "Export Snapshot")) }
                FilledTonalButton(onClick = { scope.launch { state.importSnapshot() } }) { Text(tr(state, "导入快照", "Import Snapshot")) }
            }
            OutlinedTextField(
                value = state.snapshotEditorText,
                onValueChange = { state.snapshotEditorText = it },
                modifier = Modifier.fillMaxWidth().height(480.dp),
                minLines = 20,
            )
        }
    }
}

@Composable
private fun PreviewTab(state: PlaygroundWorkbenchState, scope: CoroutineScope) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FormSection(title = tr(state, "校验结果", "Validation")) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { scope.launch { state.validateSelectedModule() } }) { Text(tr(state, "重新校验", "Revalidate")) }
                FilledTonalButton(onClick = { scope.launch { state.exportSelectedModule() } }) { Text(tr(state, "刷新预览", "Refresh Preview")) }
            }
            if (state.validationIssues.isEmpty()) {
                Text(tr(state, "暂无问题", "No issues"))
            } else {
                state.validationIssues.forEach {
                    Text("[${it.severity}] ${it.location} - ${it.message}")
                }
            }
        }
        FormSection(title = tr(state, ".ll 预览", ".ll Preview"), modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = state.exportPreviewText,
                onValueChange = {},
                modifier = Modifier.fillMaxSize(),
                minLines = 24,
            )
        }
    }
}

@Composable
private fun EditorSurface(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun SplitEditor(
    left: @Composable BoxScope.() -> Unit,
    right: @Composable BoxScope.() -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.width(280.dp).fillMaxHeight()) {
            Box(modifier = Modifier.fillMaxSize().padding(12.dp), content = left)
        }
        Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f).fillMaxHeight()) {
            Box(modifier = Modifier.fillMaxSize().padding(12.dp), content = right)
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        content()
    }
}

@Composable
private fun ActionRow(
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    primaryLabel: String,
    secondaryLabel: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onPrimary) { Text(primaryLabel) }
        OutlinedButton(onClick = onSecondary) { Text(secondaryLabel) }
    }
}

@Composable
private fun <T> ExplorerList(
    title: String,
    items: List<T>,
    selectedId: String?,
    itemId: (T) -> String,
    itemTitle: (T) -> String,
    itemSubtitle: (T) -> String,
    onSelect: (String?) -> Unit,
) where T : Any {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title.isNotBlank()) {
            Text(title, fontWeight = FontWeight.SemiBold)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(items) { item ->
                val id = itemId(item)
                Surface(
                    tonalElevation = if (id == selectedId) 2.dp else 0.dp,
                    color = if (id == selectedId) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(id) },
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(itemTitle(item), fontWeight = FontWeight.SemiBold)
                        val subtitle = itemSubtitle(item)
                        if (subtitle.isNotBlank()) {
                            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun <T : Enum<T>> EnumSelector(
    selected: T,
    entries: List<T>,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(entry.name) },
                    onClick = {
                        expanded = false
                        onSelected(entry)
                    },
                )
            }
        }
    }
}

private fun parseMapText(text: String): Map<String, String> {
    return text.lineSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .mapNotNull { line ->
            val index = line.indexOf('=')
            if (index <= 0) {
                null
            } else {
                line.substring(0, index).trim() to line.substring(index + 1).trim()
            }
        }
        .toMap()
}

private fun tr(state: PlaygroundWorkbenchState, zh: String, en: String): String {
    return if (state.uiLanguage == PlaygroundUiLanguage.ZH_CN) zh else en
}
