package site.addzero.notes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.notes.ai.LocalNoteOrganizer
import site.addzero.notes.ai.RagSearchEngine
import site.addzero.notes.ai.RagSearchHit
import site.addzero.notes.data.NoteRepository
import site.addzero.notes.graph.KnowledgeGraphBuilder
import site.addzero.notes.markdown.LiveMarkdownEditor
import site.addzero.notes.markdown.MarkdownPreview
import site.addzero.notes.model.DataSourceType
import site.addzero.notes.model.KnowledgeGraph
import site.addzero.notes.model.Note
import site.addzero.notes.model.StorageSettings
import site.addzero.notes.model.StorageSettingsUpdate
import site.addzero.notes.ui.components.GlassButton
import site.addzero.notes.ui.components.GlassListItem
import site.addzero.notes.ui.components.GlassSearchField
import site.addzero.notes.ui.components.GlassTextArea
import site.addzero.notes.ui.components.GlassTextField
import site.addzero.notes.ui.components.GlassTheme
import site.addzero.notes.ui.components.LiquidGlassButton
import site.addzero.notes.ui.components.LiquidGlassCard
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class WorkspacePage {
    GRAPH_HOME,
    NOTE_EDITOR,
    GLOBAL_SETTINGS
}

private enum class FolderFilter(val label: String) {
    ALL("全部笔记"),
    PINNED("置顶笔记")
}

private enum class EditorViewMode(val label: String) {
    LIVE("实时"),
    EDIT("编辑"),
    PREVIEW("预览"),
    SPLIT("分栏")
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        NotesRoot(
            repository = remember { PlatformRepositoryFactory.createNoteRepository() }
        )
    }
}

@Composable
private fun NotesRoot(repository: NoteRepository) {
    val scope = rememberCoroutineScope()
    val organizer = remember { LocalNoteOrganizer() }
    val graphBuilder = remember { KnowledgeGraphBuilder() }
    val ragSearchEngine = remember { RagSearchEngine() }

    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    var folderFilter by remember { mutableStateOf(FolderFilter.ALL) }
    var workspacePage by remember { mutableStateOf(WorkspacePage.GRAPH_HOME) }
    var editorViewMode by remember { mutableStateOf(EditorViewMode.LIVE) }
    var noteSearchQuery by remember { mutableStateOf("") }
    var graphSearchQuery by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var pathInput by remember { mutableStateOf("") }
    var markdownInput by remember { mutableStateOf("") }
    var storageSettings by remember { mutableStateOf(StorageSettings()) }
    var postgresPasswordInput by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("准备就绪") }
    var busy by remember { mutableStateOf(false) }

    suspend fun refreshStorageSettings() {
        storageSettings = repository.storageSettings()
    }

    suspend fun refresh(preferredNoteId: String?) {
        val latest = repository.listNotes()

        notes = latest

        val fallbackId = latest.firstOrNull()?.id
        val nextSelected = preferredNoteId
            ?.takeIf { targetId -> latest.any { note -> note.id == targetId } }
            ?: fallbackId

        selectedNoteId = nextSelected
        val selected = latest.firstOrNull { note -> note.id == nextSelected }
        titleInput = selected?.title.orEmpty()
        pathInput = selected?.path.orEmpty()
        markdownInput = selected?.markdown.orEmpty()
    }

    fun runAction(action: suspend () -> Unit) {
        scope.launch {
            busy = true
            try {
                action()
            } catch (throwable: Throwable) {
                notice = "操作失败：${throwable.message.orEmpty()}"
            } finally {
                busy = false
            }
        }
    }

    LaunchedEffect(Unit) {
        busy = true
        runCatching {
            refreshStorageSettings()
            refresh(null)
        }
            .onSuccess { notice = "已载入 ${notes.size} 条笔记" }
            .onFailure { throwable -> notice = "初始化失败：${throwable.message.orEmpty()}" }
        busy = false
    }

    val filteredNotes = remember(notes, folderFilter, noteSearchQuery) {
        val byFolder = when (folderFilter) {
            FolderFilter.ALL -> notes
            FolderFilter.PINNED -> notes.filter { note -> note.pinned }
        }
        val query = noteSearchQuery.trim().lowercase()
        if (query.isBlank()) {
            byFolder
        } else {
            byFolder.filter { note ->
                note.title.lowercase().contains(query) ||
                    note.path.lowercase().contains(query) ||
                    note.markdown.lowercase().contains(query)
            }
        }
    }

    LaunchedEffect(filteredNotes, selectedNoteId) {
        if (filteredNotes.isEmpty()) {
            selectedNoteId = null
            titleInput = ""
            pathInput = ""
            markdownInput = ""
            return@LaunchedEffect
        }
        val stillExists = filteredNotes.any { note -> note.id == selectedNoteId }
        if (!stillExists) {
            val target = filteredNotes.first()
            selectedNoteId = target.id
            titleInput = target.title
            pathInput = target.path
            markdownInput = target.markdown
        }
    }

    val selectedNote = remember(notes, selectedNoteId) {
        notes.firstOrNull { note -> note.id == selectedNoteId }
    }

    val graph = remember(notes) { graphBuilder.build(notes) }
    val graphHits = remember(graphSearchQuery, notes, graph) {
        ragSearchEngine.search(graphSearchQuery, notes, graph)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(GlassTheme.DarkBackground)
        ) {
            NavigationPanel(
                workspacePage = workspacePage,
                folderFilter = folderFilter,
                notice = notice,
                onSelectHome = { workspacePage = WorkspacePage.GRAPH_HOME },
                onSelectEditor = { workspacePage = WorkspacePage.NOTE_EDITOR },
                onSelectSettings = { workspacePage = WorkspacePage.GLOBAL_SETTINGS },
                onSelectFilter = { nextFilter -> folderFilter = nextFilter },
                modifier = Modifier.width(180.dp)
            )
            DividerColumn()
            NotesListPanel(
                notes = filteredNotes,
                selectedNoteId = selectedNoteId,
                query = noteSearchQuery,
                busy = busy,
                onQueryChange = { noteSearchQuery = it },
                onCreateNote = {
                    runAction {
                        val created = repository.createNote()
                        refresh(created.id)
                        workspacePage = WorkspacePage.NOTE_EDITOR
                        notice = "已创建 ${created.path}"
                    }
                },
                onSelectNote = { note ->
                    selectedNoteId = note.id
                    titleInput = note.title
                    pathInput = note.path
                    markdownInput = note.markdown
                    workspacePage = WorkspacePage.NOTE_EDITOR
                },
                modifier = Modifier.width(320.dp)
            )
            DividerColumn()

            if (workspacePage == WorkspacePage.GRAPH_HOME) {
                KnowledgeGraphHome(
                    graph = graph,
                    searchQuery = graphSearchQuery,
                    hits = graphHits,
                    selectedNoteId = selectedNoteId,
                    onSearchChange = { graphSearchQuery = it },
                    onOpenNote = { note ->
                        selectedNoteId = note.id
                        titleInput = note.title
                        pathInput = note.path
                        markdownInput = note.markdown
                        workspacePage = WorkspacePage.NOTE_EDITOR
                        notice = "已打开 ${note.path}"
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (workspacePage == WorkspacePage.NOTE_EDITOR) {
                EditorPanel(
                    note = selectedNote,
                    titleInput = titleInput,
                    pathInput = pathInput,
                    markdownInput = markdownInput,
                    editorViewMode = editorViewMode,
                    busy = busy,
                    onTitleChange = { titleInput = it },
                    onPathChange = { pathInput = it },
                    onMarkdownChange = { markdownInput = it },
                    onViewModeChange = { editorViewMode = it },
                    onSave = {
                        val current = selectedNote ?: return@EditorPanel
                        runAction {
                            val saved = repository.saveNote(
                                current.copy(
                                    title = titleInput,
                                    path = pathInput,
                                    markdown = markdownInput
                                )
                            )
                            refresh(saved.id)
                            notice = "已保存 ${saved.path}"
                        }
                    },
                    onTogglePinned = {
                        val current = selectedNote ?: return@EditorPanel
                        runAction {
                            val updated = repository.togglePinned(current.id)
                            refresh(updated?.id ?: current.id)
                            notice = if (updated?.pinned == true) {
                                "已置顶 ${updated.path}"
                            } else {
                                "已取消置顶 ${current.path}"
                            }
                        }
                    },
                    onDelete = {
                        val current = selectedNote ?: return@EditorPanel
                        runAction {
                            repository.deleteNote(current.id)
                            refresh(null)
                            notice = "已删除 ${current.path}"
                        }
                    },
                    onSync = {
                        runAction {
                            val result = repository.sync()
                            refresh(selectedNoteId)
                            notice = if (result.isSuccess) {
                                "同步成功：推送${result.pushedCount}，拉取${result.pulledCount}"
                            } else {
                                "同步完成，异常${result.errors.size}条"
                            }
                        }
                    },
                    onOrganize = {
                        val current = selectedNote ?: return@EditorPanel
                        runAction {
                            val draft = current.copy(
                                title = titleInput,
                                path = pathInput,
                                markdown = markdownInput
                            )
                            val noteSet = notes.map { note ->
                                if (note.id == draft.id) {
                                    draft
                                } else {
                                    note
                                }
                            }
                            val organizeResult = organizer.organize(draft, noteSet)
                            val saved = repository.saveNote(
                                draft.copy(markdown = organizeResult.organizedMarkdown)
                            )
                            refresh(saved.id)
                            notice = "整理完成：引用${organizeResult.usedReferences.size}，缺失${organizeResult.missingReferences.size}"
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                StorageSettingsPanel(
                    settings = storageSettings,
                    postgresPassword = postgresPasswordInput,
                    busy = busy,
                    onActiveSourceChange = { source ->
                        storageSettings = storageSettings.copy(activeSource = source)
                    },
                    onSqlitePathChange = { path ->
                        storageSettings = storageSettings.copy(sqlitePath = path)
                    },
                    onPostgresUrlChange = { url ->
                        storageSettings = storageSettings.copy(postgresUrl = url)
                    },
                    onPostgresUserChange = { user ->
                        storageSettings = storageSettings.copy(postgresUser = user)
                    },
                    onPostgresPasswordChange = { password ->
                        postgresPasswordInput = password
                    },
                    onApply = {
                        runAction {
                            val updated = repository.updateStorageSettings(
                                StorageSettingsUpdate(
                                    activeSource = storageSettings.activeSource,
                                    sqlitePath = storageSettings.sqlitePath,
                                    postgresUrl = storageSettings.postgresUrl,
                                    postgresUser = storageSettings.postgresUser,
                                    postgresPassword = postgresPasswordInput
                                )
                            )
                            storageSettings = updated
                            postgresPasswordInput = ""
                            refresh(selectedNoteId)
                            notice = if (updated.activeSource == DataSourceType.SQLITE) {
                                "存储方式已切换为 SQLite"
                            } else {
                                "存储方式已切换为 PostgreSQL"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (busy) {
            BusyOverlay()
        }
    }
}

@Composable
private fun DividerColumn() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(GlassTheme.GlassBorder.copy(alpha = 0.25f))
    )
}

@Composable
private fun NavigationPanel(
    workspacePage: WorkspacePage,
    folderFilter: FolderFilter,
    notice: String,
    onSelectHome: () -> Unit,
    onSelectEditor: () -> Unit,
    onSelectSettings: () -> Unit,
    onSelectFilter: (FolderFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "VibeNotes",
                style = MaterialTheme.typography.titleLarge,
                color = GlassTheme.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "AI + RAG + 图谱",
                style = MaterialTheme.typography.labelMedium,
                color = GlassTheme.TextSecondary
            )
            LiquidGlassButton(
                text = if (workspacePage == WorkspacePage.GRAPH_HOME) "● 首页图谱" else "首页图谱",
                onClick = onSelectHome,
                modifier = Modifier.fillMaxWidth()
            )
            GlassButton(
                text = if (workspacePage == WorkspacePage.NOTE_EDITOR) "● 笔记编辑" else "笔记编辑",
                onClick = onSelectEditor,
                modifier = Modifier.fillMaxWidth()
            )
            GlassButton(
                text = if (workspacePage == WorkspacePage.GLOBAL_SETTINGS) "● 全局设置" else "全局设置",
                onClick = onSelectSettings,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "笔记分组",
                style = MaterialTheme.typography.titleSmall,
                color = GlassTheme.TextPrimary
            )
            FolderFilter.entries.forEach { filter ->
                val selected = folderFilter == filter
                GlassButton(
                    text = if (selected) "✓ ${filter.label}" else filter.label,
                    onClick = { onSelectFilter(filter) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = notice,
                style = MaterialTheme.typography.bodySmall,
                color = GlassTheme.TextTertiary
            )
        }
    }
}

@Composable
private fun NotesListPanel(
    notes: List<Note>,
    selectedNoteId: String?,
    query: String,
    busy: Boolean,
    onQueryChange: (String) -> Unit,
    onCreateNote: () -> Unit,
    onSelectNote: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "笔记列表",
                    style = MaterialTheme.typography.titleMedium,
                    color = GlassTheme.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                LiquidGlassButton(
                    text = "新建",
                    onClick = onCreateNote,
                    enabled = !busy
                )
            }

            GlassSearchField(
                value = query,
                onValueChange = onQueryChange,
                onSearch = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = "过滤标题/路径/正文"
            )

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无笔记，点击“新建”开始",
                        color = GlassTheme.TextTertiary
                    )
                }
                return@LiquidGlassCard
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(notes, key = { note -> note.id }) { note ->
                    val selected = note.id == selectedNoteId
                    GlassListItem(
                        title = (if (note.pinned) "📌 " else "") + note.title,
                        subtitle = note.path,
                        isSelected = selected,
                        onClick = { onSelectNote(note) },
                        trailing = {
                            Text(
                                text = noteSummary(note.markdown),
                                style = MaterialTheme.typography.labelSmall,
                                color = GlassTheme.TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorPanel(
    note: Note?,
    titleInput: String,
    pathInput: String,
    markdownInput: String,
    editorViewMode: EditorViewMode,
    busy: Boolean,
    onTitleChange: (String) -> Unit,
    onPathChange: (String) -> Unit,
    onMarkdownChange: (String) -> Unit,
    onViewModeChange: (EditorViewMode) -> Unit,
    onSave: () -> Unit,
    onTogglePinned: () -> Unit,
    onDelete: () -> Unit,
    onSync: () -> Unit,
    onOrganize: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "编辑器",
                    style = MaterialTheme.typography.titleLarge,
                    color = GlassTheme.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassButton(text = "同步", onClick = onSync, enabled = !busy)
                    GlassButton(text = "保存", onClick = onSave, enabled = note != null && !busy)
                    LiquidGlassButton(text = "一键整理", onClick = onOrganize, enabled = note != null && !busy)
                }
            }

            if (note == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "请选择笔记进行编辑",
                        color = GlassTheme.TextTertiary
                    )
                }
                return@LiquidGlassCard
            }

            GlassTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "标题"
            )
            GlassTextField(
                value = pathInput,
                onValueChange = onPathChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "路径（可用于 @路径 引用）"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EditorViewMode.entries.forEach { mode ->
                    val active = mode == editorViewMode
                    GlassButton(
                        text = if (active) "● ${mode.label}" else mode.label,
                        onClick = { onViewModeChange(mode) }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                GlassButton(
                    text = if (note.pinned) "取消置顶" else "置顶",
                    onClick = onTogglePinned,
                    enabled = !busy
                )
                GlassButton(text = "删除", onClick = onDelete, enabled = !busy)
            }

            when (editorViewMode) {
                EditorViewMode.LIVE -> {
                    LiveMarkdownEditor(
                        value = markdownInput,
                        onValueChange = onMarkdownChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        placeholder = "Markdown（实时渲染，支持 @thisFile / @路径）",
                        enabled = !busy
                    )
                }

                EditorViewMode.EDIT -> {
                    GlassTextArea(
                        value = markdownInput,
                        onValueChange = onMarkdownChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        placeholder = "Markdown（支持 @thisFile / @路径）"
                    )
                }

                EditorViewMode.PREVIEW -> {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        MarkdownPreview(
                            markdown = markdownInput,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                EditorViewMode.SPLIT -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassTextArea(
                            value = markdownInput,
                            onValueChange = onMarkdownChange,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            placeholder = "Markdown"
                        )
                        LiquidGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            MarkdownPreview(
                                markdown = markdownInput,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageSettingsPanel(
    settings: StorageSettings,
    postgresPassword: String,
    busy: Boolean,
    onActiveSourceChange: (DataSourceType) -> Unit,
    onSqlitePathChange: (String) -> Unit,
    onPostgresUrlChange: (String) -> Unit,
    onPostgresUserChange: (String) -> Unit,
    onPostgresPasswordChange: (String) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "全局设置",
                style = MaterialTheme.typography.titleLarge,
                color = GlassTheme.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "存储方式默认使用 SQLite，可切换到 PostgreSQL",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassTheme.TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassButton(
                    text = if (settings.activeSource == DataSourceType.SQLITE) "● SQLite" else "SQLite",
                    onClick = { onActiveSourceChange(DataSourceType.SQLITE) },
                    enabled = !busy
                )
                GlassButton(
                    text = if (settings.activeSource == DataSourceType.POSTGRES) "● PostgreSQL" else "PostgreSQL",
                    onClick = { onActiveSourceChange(DataSourceType.POSTGRES) },
                    enabled = !busy
                )
            }

            Text(
                text = "SQLite 存储路径",
                style = MaterialTheme.typography.titleSmall,
                color = GlassTheme.TextPrimary
            )
            GlassTextField(
                value = settings.sqlitePath,
                onValueChange = onSqlitePathChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = if (settings.sqliteDefaultPath.isBlank()) {
                    "~/.vibepocket/notes/vibenotes-server.db"
                } else {
                    settings.sqliteDefaultPath
                },
                enabled = !busy
            )
            Text(
                text = "默认路径：${settings.sqliteDefaultPath.ifBlank { "~/.vibepocket/notes/vibenotes-server.db" }}",
                style = MaterialTheme.typography.bodySmall,
                color = GlassTheme.TextTertiary
            )

            Text(
                text = "PostgreSQL 连接",
                style = MaterialTheme.typography.titleSmall,
                color = GlassTheme.TextPrimary
            )
            GlassTextField(
                value = settings.postgresUrl,
                onValueChange = onPostgresUrlChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "jdbc:postgresql://127.0.0.1:5432/vibenotes",
                enabled = !busy
            )
            GlassTextField(
                value = settings.postgresUser,
                onValueChange = onPostgresUserChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "postgres",
                enabled = !busy
            )
            GlassTextField(
                value = postgresPassword,
                onValueChange = onPostgresPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "postgres 密码（不填则保持原值）",
                enabled = !busy
            )
            Text(
                text = if (settings.postgresAvailable) {
                    "PostgreSQL 当前可连接"
                } else {
                    "PostgreSQL 当前不可连接或未配置"
                },
                style = MaterialTheme.typography.bodySmall,
                color = GlassTheme.TextTertiary
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                LiquidGlassButton(
                    text = "应用设置",
                    onClick = onApply,
                    enabled = !busy
                )
            }
        }
    }
}

@Composable
private fun KnowledgeGraphHome(
    graph: KnowledgeGraph,
    searchQuery: String,
    hits: List<RagSearchHit>,
    selectedNoteId: String?,
    onSearchChange: (String) -> Unit,
    onOpenNote: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "知识图谱首页",
                style = MaterialTheme.typography.titleLarge,
                color = GlassTheme.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "RAG 检索入口：大屏搜笔记 + 图谱关系浏览",
                style = MaterialTheme.typography.bodyMedium,
                color = GlassTheme.TextSecondary
            )

            GlassSearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                onSearch = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = "图谱检索（关键词、路径、正文）"
            )

            KnowledgeGraphCanvas(
                graph = graph,
                selectedNoteId = selectedNoteId,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            )

            Text(
                text = "检索结果（${hits.size}）",
                style = MaterialTheme.typography.titleSmall,
                color = GlassTheme.TextPrimary
            )

            if (hits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无结果，试试换个关键词",
                        color = GlassTheme.TextTertiary
                    )
                }
                return@LiquidGlassCard
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hits, key = { hit -> hit.note.id }) { hit ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenNote(hit.note) }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = hit.note.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = GlassTheme.TextPrimary
                            )
                            Text(
                                text = "${hit.note.path} · score=${hit.score}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GlassTheme.TextSecondary
                            )
                            Text(
                                text = hit.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassTheme.TextTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KnowledgeGraphCanvas(
    graph: KnowledgeGraph,
    selectedNoteId: String?,
    modifier: Modifier = Modifier
) {
    if (graph.nodes.isEmpty()) {
        Box(
            modifier = modifier
                .background(GlassTheme.GlassSurface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无节点，创建笔记并使用 @路径 建立关系",
                color = GlassTheme.TextTertiary
            )
        }
        return
    }

    val nodes = graph.nodes.take(36)
    val indexById = nodes.mapIndexed { index, node -> node.noteId to index }.toMap()

    Box(
        modifier = modifier
            .background(GlassTheme.GlassSurface.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) * 0.36f

            val positions = nodes.mapIndexed { index, _ ->
                val angle = (index.toFloat() / nodes.size.toFloat()) * (2f * PI.toFloat())
                val x = center.x + radius * cos(angle)
                val y = center.y + radius * sin(angle)
                Offset(x, y)
            }

            graph.edges.forEach { edge ->
                val fromIndex = indexById[edge.fromNoteId] ?: return@forEach
                val toIndex = indexById[edge.toNoteId] ?: return@forEach
                drawLine(
                    color = Color(0xFF9FA8DA),
                    start = positions[fromIndex],
                    end = positions[toIndex],
                    strokeWidth = 2.2f
                )
            }

            nodes.forEachIndexed { index, node ->
                val selected = node.noteId == selectedNoteId
                drawCircle(
                    color = if (selected) Color(0xFF3F51B5) else Color(0xFF5C6BC0),
                    center = positions[index],
                    radius = if (selected) 13f else 9f
                )
            }
        }
        Text(
            text = "节点 ${graph.nodes.size} · 边 ${graph.edges.size}",
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color(0xAA000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
private fun BusyOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x2B000000)),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Text("处理中...", color = GlassTheme.TextPrimary)
            }
        }
    }
}

private fun noteSummary(markdown: String): String {
    val firstLine = markdown
        .lines()
        .map { line -> line.trim() }
        .firstOrNull { line -> line.isNotBlank() }
    return firstLine ?: "空内容"
}
