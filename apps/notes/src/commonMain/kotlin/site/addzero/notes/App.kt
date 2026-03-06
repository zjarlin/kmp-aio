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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import site.addzero.notes.markdown.MarkdownPreview
import site.addzero.notes.model.DataSourceHealth
import site.addzero.notes.model.KnowledgeGraph
import site.addzero.notes.model.Note
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class WorkspacePage {
    GRAPH_HOME,
    NOTE_EDITOR
}

private enum class FolderFilter(val label: String) {
    ALL("全部笔记"),
    PINNED("置顶笔记")
}

private enum class EditorViewMode(val label: String) {
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
    var editorViewMode by remember { mutableStateOf(EditorViewMode.SPLIT) }
    var noteSearchQuery by remember { mutableStateOf("") }
    var graphSearchQuery by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var pathInput by remember { mutableStateOf("") }
    var markdownInput by remember { mutableStateOf("") }
    var sourceHealth by remember { mutableStateOf<List<DataSourceHealth>>(emptyList()) }
    var notice by remember { mutableStateOf("准备就绪") }
    var busy by remember { mutableStateOf(false) }

    suspend fun refresh(preferredNoteId: String?) {
        val latest = repository.listNotes()
        val health = repository.dataSourceHealth()

        notes = latest
        sourceHealth = health

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
        runCatching { refresh(null) }
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavigationPanel(
                workspacePage = workspacePage,
                folderFilter = folderFilter,
                notice = notice,
                onSelectHome = { workspacePage = WorkspacePage.GRAPH_HOME },
                onSelectEditor = { workspacePage = WorkspacePage.NOTE_EDITOR },
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
            } else {
                EditorPanel(
                    note = selectedNote,
                    titleInput = titleInput,
                    pathInput = pathInput,
                    markdownInput = markdownInput,
                    editorViewMode = editorViewMode,
                    sourceHealth = sourceHealth,
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
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun NavigationPanel(
    workspacePage: WorkspacePage,
    folderFilter: FolderFilter,
    notice: String,
    onSelectHome: () -> Unit,
    onSelectEditor: () -> Unit,
    onSelectFilter: (FolderFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "VibeNotes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "AI + RAG + 图谱",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onSelectHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (workspacePage == WorkspacePage.GRAPH_HOME) "● 首页图谱" else "首页图谱")
        }
        Button(
            onClick = onSelectEditor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (workspacePage == WorkspacePage.NOTE_EDITOR) "● 笔记编辑" else "笔记编辑")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("笔记分组", style = MaterialTheme.typography.titleSmall)
        FolderFilter.entries.forEach { filter ->
            val selected = folderFilter == filter
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectFilter(filter) }
            ) {
                Text(
                    text = if (selected) "✓ ${filter.label}" else filter.label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = notice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    Column(
        modifier = modifier
            .fillMaxHeight()
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
                fontWeight = FontWeight.SemiBold
            )
            Button(onClick = onCreateNote, enabled = !busy) {
                Text("新建")
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("过滤标题/路径/正文") },
            singleLine = true
        )

        if (notes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无笔记，点击“新建”开始",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notes, key = { note -> note.id }) { note ->
                val selected = note.id == selectedNoteId
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectNote(note) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = (if (note.pinned) "📌 " else "") + note.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = note.path,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = noteSummary(note.markdown),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
    sourceHealth: List<DataSourceHealth>,
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
    Column(
        modifier = modifier
            .fillMaxHeight()
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
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSync, enabled = !busy) { Text("同步") }
                Button(onClick = onSave, enabled = note != null && !busy) { Text("保存") }
                Button(onClick = onOrganize, enabled = note != null && !busy) { Text("一键整理") }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sourceHealth.forEach { health ->
                val color = if (health.available) Color(0xFFD9F5DE) else Color(0xFFFFE1E1)
                Surface(
                    color = color,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "${health.type}: ${if (health.available) "OK" else "OFF"}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        if (note == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "请选择笔记进行编辑",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        OutlinedTextField(
            value = titleInput,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("标题") },
            singleLine = true
        )
        OutlinedTextField(
            value = pathInput,
            onValueChange = onPathChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("路径（可用于 @路径 引用）") },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EditorViewMode.entries.forEach { mode ->
                val active = mode == editorViewMode
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (active) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.clickable { onViewModeChange(mode) }
                ) {
                    Text(
                        text = mode.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onTogglePinned, enabled = !busy) {
                Text(if (note.pinned) "取消置顶" else "置顶")
            }
            Button(onClick = onDelete, enabled = !busy) {
                Text("删除")
            }
        }

        when (editorViewMode) {
            EditorViewMode.EDIT -> {
                OutlinedTextField(
                    value = markdownInput,
                    onValueChange = onMarkdownChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = { Text("Markdown（支持 @thisFile / @路径）") }
                )
            }

            EditorViewMode.PREVIEW -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp
                ) {
                    MarkdownPreview(markdown = markdownInput)
                }
            }

            EditorViewMode.SPLIT -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = markdownInput,
                        onValueChange = onMarkdownChange,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        label = { Text("Markdown") }
                    )
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 1.dp
                    ) {
                        MarkdownPreview(markdown = markdownInput)
                    }
                }
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
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "知识图谱首页",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "RAG 检索入口：大屏搜笔记 + 图谱关系浏览",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("图谱检索（关键词、路径、正文）") }
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
            style = MaterialTheme.typography.titleSmall
        )

        if (hits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无结果，试试换个关键词",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(hits, key = { hit -> hit.note.id }) { hit ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNote(hit.note) }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = hit.note.title,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "${hit.note.path} · score=${hit.score}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = hit.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无节点，创建笔记并使用 @路径 建立关系",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val nodes = graph.nodes.take(36)
    val indexById = nodes.mapIndexed { index, node -> node.noteId to index }.toMap()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
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
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Text("处理中...")
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
