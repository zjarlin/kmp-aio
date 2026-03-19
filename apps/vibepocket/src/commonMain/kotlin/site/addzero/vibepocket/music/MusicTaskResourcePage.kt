package site.addzero.vibepocket.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import site.addzero.media.playlist.player.DefaultPlaylistPlayer
import site.addzero.vibepocket.api.ServerApiClient
import site.addzero.vibepocket.api.suno.SunoTaskDetail
import site.addzero.vibepocket.model.SunoTaskResourceItem
import site.addzero.vibepocket.ui.StudioEmptyState
import site.addzero.vibepocket.ui.StudioMetricCard
import site.addzero.vibepocket.ui.StudioPill
import site.addzero.vibepocket.ui.StudioSectionCard

private val taskResourcePrettyJson = Json {
    prettyPrint = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

@Composable
fun MusicTaskResourcePage() {
    val scope = rememberCoroutineScope()

    var keyword by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<SunoTaskResourceItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTaskId by remember { mutableStateOf<String?>(null) }
    var liveDetail by remember(selectedTaskId) { mutableStateOf<SunoTaskDetail?>(null) }
    var liveSyncMessage by remember(selectedTaskId) { mutableStateOf<String?>(null) }
    var isRefreshingLiveDetail by remember(selectedTaskId) { mutableStateOf(false) }

    suspend fun refreshTaskFromSuno(
        currentItem: SunoTaskResourceItem,
        manual: Boolean,
    ) {
        isRefreshingLiveDetail = true
        liveSyncMessage = if (manual) {
            "正在按 taskId 查询 Suno..."
        } else {
            "正在自动按 taskId 回查 Suno..."
        }
        try {
            val refreshedSnapshot = refreshSunoTaskSnapshotById(
                taskId = currentItem.taskId,
                fallbackType = currentItem.type,
                requestJson = currentItem.requestJson,
            )
            liveDetail = refreshedSnapshot.detail
            liveSyncMessage = buildString {
                append(if (manual) "已按 taskId 从 Suno 刷新：" else "已自动按 taskId 从 Suno 刷新：")
                append(refreshedSnapshot.detail.displayStatus)
                append("。")
                append(refreshedSnapshot.archiveStatus)
            }
            refreshedSnapshot.archivedItem?.let { archivedItem ->
                items = items.replaceTaskResource(archivedItem)
            }
        } catch (error: Exception) {
            liveSyncMessage = buildString {
                append(if (manual) "按 taskId 查询失败：" else "自动按 taskId 回查失败：")
                append(SunoWorkflowService.errorMessage(error))
            }
        } finally {
            isRefreshingLiveDetail = false
        }
    }

    fun refreshTaskResources() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val loaded = ServerApiClient.sunoTaskResourceApi.list()
                val runtimeConfig = runCatching { SunoWorkflowService.loadConfig() }
                    .getOrDefault(SunoRuntimeConfig())
                val reconciled = if (runtimeConfig.hasToken) {
                    loaded.reconcileTaskResourcesWithSuno { partial ->
                        items = partial
                    }
                } else {
                    loaded
                }
                items = reconciled
                if (reconciled.none { it.taskId == selectedTaskId }) {
                    selectedTaskId = reconciled.firstOrNull()?.taskId
                }
            } catch (error: Exception) {
                errorMessage = error.message ?: "加载生成日志失败"
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshSelectedTaskFromSuno() {
        val currentItem = items.firstOrNull { it.taskId == selectedTaskId } ?: return
        scope.launch { refreshTaskFromSuno(currentItem, manual = true) }
    }

    LaunchedEffect(Unit) {
        refreshTaskResources()
    }

    val filteredItems = remember(items, keyword) {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.isBlank()) {
            items
        } else {
            items.filter { item -> item.matchesKeyword(normalizedKeyword) }
        }
    }
    val selectedItem = filteredItems.firstOrNull { it.taskId == selectedTaskId }
        ?: filteredItems.firstOrNull()

    LaunchedEffect(selectedItem?.taskId) {
        if (selectedItem != null && selectedTaskId != selectedItem.taskId) {
            selectedTaskId = selectedItem.taskId
        }
    }

    LaunchedEffect(selectedItem?.taskId) {
        selectedItem?.let { item ->
            refreshTaskFromSuno(
                currentItem = item,
                manual = false,
            )
        }
    }

    val successCount = remember(items) { items.count { it.isSuccessStatus() } }
    val failedCount = remember(items) { items.count { it.isFailedStatus() } }
    val runningCount = remember(items) { items.count { it.isRunningStatus() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StudioPill(
            text = "Task Resources",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = "生成管理",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "这里是结构化生成日志：每条记录都会绑定 taskId、提交请求、状态、结果音轨和错误信息。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StudioMetricCard(
                label = "总任务",
                value = items.size.toString(),
                modifier = Modifier.width(112.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
            StudioMetricCard(
                label = "成功",
                value = successCount.toString(),
                modifier = Modifier.width(112.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
            StudioMetricCard(
                label = "失败",
                value = failedCount.toString(),
                modifier = Modifier.width(112.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer,
            )
            StudioMetricCard(
                label = "进行中",
                value = runningCount.toString(),
                modifier = Modifier.width(112.dp),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            )
        }

        StudioSectionCard(
            title = "任务筛选",
            subtitle = "支持按 taskId、类型、状态、标题和错误消息筛选。",
            action = {
                OutlinedButton(
                    onClick = ::refreshTaskResources,
                    enabled = !isLoading,
                ) {
                    Text(if (isLoading) "刷新中..." else "刷新")
                }
            },
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜索任务") },
                placeholder = { Text("输入 taskId / 类型 / 标题 / 状态 / 错误关键词") },
                singleLine = true,
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StudioSectionCard(
                modifier = Modifier
                    .weight(0.34f)
                    .fillMaxHeight(),
                title = "任务列表",
                subtitle = "按最近更新时间倒序显示。",
            ) {
                when {
                    isLoading && items.isEmpty() -> ResourceLoadingState()
                    errorMessage != null && items.isEmpty() -> ResourceErrorState(
                        message = errorMessage.orEmpty(),
                        onRetry = ::refreshTaskResources,
                    )

                    filteredItems.isEmpty() -> StudioEmptyState(
                        icon = "🧾",
                        title = "暂无生成日志",
                        description = if (keyword.isBlank()) {
                            "先去音乐工作台提交一次任务，这里就会出现记录。"
                        } else {
                            "没有匹配当前筛选条件的任务。"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                items = filteredItems,
                                key = { item -> item.taskId },
                            ) { item ->
                                TaskResourceListItem(
                                    item = item,
                                    selected = item.taskId == selectedItem?.taskId,
                                    onClick = { selectedTaskId = item.taskId },
                                )
                            }
                        }
                    }
                }
            }

            StudioSectionCard(
                modifier = Modifier
                    .weight(0.66f)
                    .fillMaxHeight(),
                title = "任务详情",
                subtitle = "选中一条任务后，可以查看请求体、Suno 返回详情和结果音轨。taskId 可随时回查远端。",
                action = {
                    if (selectedItem != null) {
                        OutlinedButton(
                            onClick = ::refreshSelectedTaskFromSuno,
                            enabled = !isRefreshingLiveDetail,
                        ) {
                            Text(if (isRefreshingLiveDetail) "查询中..." else "按 taskId 查询")
                        }
                    }
                },
            ) {
                if (selectedItem == null) {
                    StudioEmptyState(
                        icon = "🎛",
                        title = "未选择任务",
                        description = "左侧选中一条生成记录后，这里会展示绑定的 taskId 和结果集。",
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    TaskResourceDetail(
                        item = selectedItem,
                        liveDetail = liveDetail,
                        liveSyncMessage = liveSyncMessage,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskResourceListItem(
    item: SunoTaskResourceItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val statusColor = when {
        item.isSuccessStatus() -> MaterialTheme.colorScheme.secondaryContainer
        item.isFailedStatus() -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.displayTitle(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.taskId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                StudioPill(
                    text = item.displayStatus(),
                    containerColor = statusColor,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text = buildString {
                    append(item.type.ifBlank { "unknown" })
                    append(" · ")
                    append(item.tracks.size)
                    append(" 首")
                    item.updatedAt?.takeIf { it.isNotBlank() }?.let { updatedAt ->
                        append(" · ")
                        append(updatedAt)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            item.errorMessage?.takeIf { it.isNotBlank() }?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TaskResourceDetail(
    item: SunoTaskResourceItem,
    liveDetail: SunoTaskDetail? = null,
    liveSyncMessage: String? = null,
    modifier: Modifier = Modifier,
) {
    val playlistItems = remember(item) { item.toPlaylistEntries() }
    val effectiveTaskId = liveDetail?.taskId ?: item.taskId
    val effectiveType = liveDetail?.type?.takeIf { it.isNotBlank() } ?: item.type
    val effectiveStatus = liveDetail?.displayStatus ?: item.displayStatus()
    val effectiveTrackCount = liveDetail?.response?.sunoData?.size ?: item.tracks.size
    val liveTracks = liveDetail?.response?.sunoData.orEmpty()
    val effectiveError = liveDetail?.errorMessage ?: liveDetail?.errorCode ?: item.errorMessage
    val effectiveDetailJson = liveDetail?.let { taskResourcePrettyJson.encodeToString(it) } ?: item.detailJson
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StudioSectionCard(
            title = "任务摘要",
            subtitle = "当前记录和 taskId 绑定在同一条日志里，远端回查结果优先生效。",
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StudioMetricCard(
                    label = "类型",
                    value = effectiveType.ifBlank { "-" },
                    modifier = Modifier.width(112.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
                StudioMetricCard(
                    label = "状态",
                    value = effectiveStatus,
                    modifier = Modifier.width(112.dp),
                    containerColor = when {
                        liveDetail != null && liveDetail.isSuccess -> MaterialTheme.colorScheme.secondaryContainer
                        liveDetail != null && liveDetail.isFailed -> MaterialTheme.colorScheme.errorContainer
                        item.isSuccessStatus() -> MaterialTheme.colorScheme.secondaryContainer
                        item.isFailedStatus() -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                )
                StudioMetricCard(
                    label = "结果数",
                    value = effectiveTrackCount.toString(),
                    modifier = Modifier.width(112.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            TaskResourceValueBlock(
                label = "Task ID",
                value = effectiveTaskId,
            )
            item.createdAt?.takeIf { it.isNotBlank() }?.let { createdAt ->
                TaskResourceValueBlock(
                    label = "创建时间",
                    value = createdAt,
                )
            }
            item.updatedAt?.takeIf { it.isNotBlank() }?.let { updatedAt ->
                TaskResourceValueBlock(
                    label = "更新时间",
                    value = updatedAt,
                )
            }
            liveSyncMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (
                        message.startsWith("按 taskId 查询失败") ||
                        message.startsWith("自动按 taskId 回查失败")
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        }

        StudioSectionCard(
            title = "结果音轨",
            subtitle = if (liveTracks.isEmpty() && playlistItems.isEmpty()) {
                "当前任务还没有产出可播放结果。"
            } else {
                "优先展示按 taskId 从 Suno 回查到的结果；没有时再回退到本地归档。"
            },
        ) {
            if (liveTracks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                ) {
                    DefaultPlaylistPlayer(
                        items = liveTracks,
                        modifier = Modifier.fillMaxSize(),
                        itemKey = { track ->
                            track.playbackId(effectiveTaskId)
                        },
                        titleOf = { track ->
                            track.displayTitle()
                        },
                        subtitleOf = { track ->
                            track.displaySubtitle(
                                effectiveTaskId.take(8),
                                item.updatedAt,
                                effectiveStatus,
                            )
                        },
                        durationMsOf = { track ->
                            track.durationMsOrNull()
                        },
                        coverUrlOf = { track ->
                            track.imageUrl
                        },
                        hasResolvableAudioOf = { track ->
                            !track.audioUrl.isNullOrBlank() || !track.streamAudioUrl.isNullOrBlank()
                        },
                        resolveAudioSource = { track ->
                            track.resolvedAudioSource()
                        },
                    )
                }
            } else if (playlistItems.isEmpty()) {
                StudioEmptyState(
                    icon = "🎧",
                    title = "暂无结果音轨",
                    description = effectiveError?.takeIf { it.isNotBlank() }
                        ?: "任务可能还在处理中，或者 Suno 没有返回可播放结果。",
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                ) {
                    DefaultPlaylistPlayer(
                        items = playlistItems,
                        modifier = Modifier.fillMaxSize(),
                        itemKey = { entry ->
                            entry.track.playbackId(entry.item.taskId)
                        },
                        titleOf = { entry ->
                            entry.track.displayTitle()
                        },
                        subtitleOf = { entry ->
                            entry.track.displaySubtitle(
                                entry.item.taskId.take(8),
                                entry.item.updatedAt,
                                entry.item.displayStatus(),
                            )
                        },
                        durationMsOf = { entry ->
                            entry.track.durationMsOrNull()
                        },
                        coverUrlOf = { entry ->
                            entry.track.imageUrl
                        },
                        hasResolvableAudioOf = { entry ->
                            !entry.track.audioUrl.isNullOrBlank() || !entry.track.streamAudioUrl.isNullOrBlank()
                        },
                        resolveAudioSource = { entry ->
                            entry.track.resolvedAudioSource()
                        },
                    )
                }
            }
        }

        effectiveError?.takeIf { it.isNotBlank() }?.let { errorMessage ->
            StudioSectionCard(
                title = "错误信息",
                subtitle = "任务失败或部分失败时，错误文本会保存在这里。",
            ) {
                TaskResourceMonospaceText(errorMessage)
            }
        }

        StudioSectionCard(
            title = "请求 JSON",
            subtitle = "提交时发给 Suno 的请求体快照。",
        ) {
            TaskResourceMonospaceText(
                item.requestJson?.prettyJsonOrRaw() ?: "暂无请求体快照",
            )
        }

        StudioSectionCard(
            title = "详情 JSON",
            subtitle = "轮询完成后保存的 Suno 原始详情，用来对照状态和结果集。",
        ) {
            TaskResourceMonospaceText(
                effectiveDetailJson?.prettyJsonOrRaw() ?: "暂无详情快照",
            )
        }
    }
}

@Composable
private fun TaskResourceValueBlock(
    label: String,
    value: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ) {
            SelectionContainer {
                Text(
                    text = value,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun TaskResourceMonospaceText(
    value: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        SelectionContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(10.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun ResourceLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = "正在加载生成日志...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResourceErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        StudioSectionCard(
            title = "日志加载失败",
            subtitle = message,
        ) {
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

private fun SunoTaskResourceItem.matchesKeyword(keyword: String): Boolean {
    val normalizedKeyword = keyword.trim()
    if (normalizedKeyword.isBlank()) {
        return true
    }
    return sequenceOf(
        taskId,
        type,
        status,
        errorMessage,
        requestJson,
        detailJson,
        tracks.firstOrNull()?.title,
        tracks.firstOrNull()?.tags,
    ).filterNotNull().any { candidate ->
        candidate.contains(normalizedKeyword, ignoreCase = true)
    }
}

private fun SunoTaskResourceItem.displayTitle(): String {
    return tracks.firstOrNull()?.title?.takeIf { it.isNotBlank() }
        ?: when (type.lowercase()) {
            "upload_cover" -> "翻唱任务"
            "generate" -> "生成音乐任务"
            else -> "未命名任务"
        }
}

private fun SunoTaskResourceItem.displayStatus(): String {
    return when (status.uppercase()) {
        "SUCCESS" -> "成功"
        "FAILED" -> "失败"
        "PENDING" -> "排队中"
        "PROCESSING" -> "处理中"
        else -> status.ifBlank { "-" }
    }
}

private fun SunoTaskResourceItem.isSuccessStatus(): Boolean {
    return status.equals("SUCCESS", ignoreCase = true)
}

private fun SunoTaskResourceItem.isFailedStatus(): Boolean {
    return status.equals("FAILED", ignoreCase = true)
}

private fun SunoTaskResourceItem.isRunningStatus(): Boolean {
    return !isSuccessStatus() && !isFailedStatus()
}

private fun String.prettyJsonOrRaw(): String {
    val raw = trim()
    if (raw.isBlank()) {
        return raw
    }
    return runCatching {
        val element = taskResourcePrettyJson.parseToJsonElement(raw)
        taskResourcePrettyJson.encodeToString(JsonElement.serializer(), element)
    }.getOrDefault(raw)
}

private fun List<SunoTaskResourceItem>.replaceTaskResource(
    refreshedItem: SunoTaskResourceItem,
): List<SunoTaskResourceItem> {
    return map { current ->
        if (current.taskId == refreshedItem.taskId) refreshedItem else current
    }
}

private suspend fun List<SunoTaskResourceItem>.reconcileTaskResourcesWithSuno(
    onPartialUpdate: (List<SunoTaskResourceItem>) -> Unit,
): List<SunoTaskResourceItem> {
    var current = this
    for (item in this) {
        if (!item.shouldRefreshFromSuno()) {
            continue
        }
        val refreshedItem = runCatching {
            refreshSunoTaskSnapshotById(
                taskId = item.taskId,
                fallbackType = item.type,
                requestJson = item.requestJson,
            ).archivedItem
        }.getOrNull() ?: continue
        current = current.replaceTaskResource(refreshedItem)
        onPartialUpdate(current)
    }
    return current
}

private fun SunoTaskResourceItem.shouldRefreshFromSuno(): Boolean {
    return status.equals("PENDING", ignoreCase = true) ||
        status.equals("PROCESSING", ignoreCase = true) ||
        tracks.isEmpty() ||
        detailJson.isNullOrBlank()
}
