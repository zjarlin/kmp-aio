package site.addzero.kcloud.music

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import site.addzero.cupertino.workbench.material3.FilterChip
import site.addzero.cupertino.workbench.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import site.addzero.core.network.json.json
import site.addzero.kcloud.api.ApiProvider
import site.addzero.kcloud.api.getConfigValueOrNull
import site.addzero.kcloud.vibepocket.model.ConfigEntry

private const val MAX_FORM_HISTORY_SIZE = 6
private const val VIBE_FORM_HISTORY_KEY = "music_vibe_form_history"
private const val UPLOAD_COVER_FORM_HISTORY_KEY = "music_upload_cover_form_history"

private val formHistoryJson = json

@Serializable
private data class VibeFormHistoryPayload(
    val items: List<SavedVibeFormDraft> = emptyList(),
)

@Serializable
private data class UploadCoverFormHistoryPayload(
    val items: List<SavedUploadCoverFormDraft> = emptyList(),
)

@Serializable
internal data class SavedVibeFormDraft(
    val lyrics: String = "",
    val songName: String = "",
    val artistName: String = "",
    val title: String = "",
    val tags: String = "",
    val mv: String = "V4_5",
    val makeInstrumental: Boolean = false,
    val vocalGender: String = "m",
    val negativeTags: String = "",
    val gptDescriptionPrompt: String = "",
    val selectedPersonaId: String? = null,
    val updatedAt: Long = 0L,
) {
    fun chipLabel(): String {
        val primary = title.trim()
            .ifBlank { songName.trim() }
            .ifBlank { tags.trim() }
            .ifBlank { lyrics.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty().trim() }
            .ifBlank { "未命名 Vibe" }
            .truncateForChip()
        val suffix = listOfNotNull(
            mv.takeIf { it.isNotBlank() },
            "歌词".takeIf { lyrics.isNotBlank() },
        ).joinToString(" · ")
        return if (suffix.isBlank()) {
            primary
        } else {
            "$primary · $suffix"
        }
    }
}

@Serializable
internal data class SavedUploadCoverFormDraft(
    val uploadUrl: String = "",
    val sourceTitle: String = "",
    val sourceSubtitle: String = "",
    val prompt: String = "",
    val style: String = "",
    val title: String = "",
    val selectedModel: String = "V4_5ALL",
    val selectedGender: String = "m",
    val selectedPersonaId: String? = null,
    val updatedAt: Long = 0L,
) {
    fun chipLabel(): String {
        val primary = sourceTitle.trim()
            .ifBlank { title.trim() }
            .ifBlank { uploadUrl.sourceFileName() }
            .ifBlank { style.trim() }
            .ifBlank { "未命名翻唱" }
            .truncateForChip()
        return "$primary · $selectedModel"
    }

    fun restoredSourceTitle(): String {
        return sourceTitle.trim()
            .ifBlank { title.trim() }
            .ifBlank { uploadUrl.sourceFileName() }
            .ifBlank { "已回填音源" }
    }

    fun restoredSourceSubtitle(): String {
        val restoredSubtitle = sourceSubtitle.trim()
        if (restoredSubtitle.isNotBlank()) {
            return restoredSubtitle
        }
        if (uploadUrl.isBlank()) {
            return "最近填写里没有可用音源，请重新搜索一首歌。"
        }
        return buildString {
            append("音源已从最近填写回填，可直接提交翻唱。")
            val urlSummary = uploadUrl.sourceSummary()
            if (urlSummary.isNotBlank()) {
                append(" ")
                append(urlSummary)
            }
        }
    }
}

internal suspend fun loadVibeFormHistory(): List<SavedVibeFormDraft> {
    val rawValue = getConfigValueOrNull(VIBE_FORM_HISTORY_KEY)
        ?.trim()
        .orEmpty()
    if (rawValue.isBlank()) {
        return emptyList()
    }
    return runCatching {
        formHistoryJson.decodeFromString<VibeFormHistoryPayload>(rawValue).items
            .map { it.normalized() }
            .sortedByDescending { it.updatedAt }
    }.getOrDefault(emptyList())
}

internal suspend fun saveVibeFormDraft(draft: SavedVibeFormDraft) {
    val normalizedDraft = draft.normalized()
    if (!normalizedDraft.hasContent()) {
        return
    }
    val mergedHistory = listOf(normalizedDraft) + loadVibeFormHistory().filterNot {
        it.sameContentAs(normalizedDraft)
    }
    persistFormHistory(
        key = VIBE_FORM_HISTORY_KEY,
        description = "Music Vibe recent form history",
        value = formHistoryJson.encodeToString(
            VibeFormHistoryPayload(mergedHistory.take(MAX_FORM_HISTORY_SIZE))
        ),
    )
}

internal suspend fun loadUploadCoverFormHistory(): List<SavedUploadCoverFormDraft> {
    val rawValue = getConfigValueOrNull(UPLOAD_COVER_FORM_HISTORY_KEY)
        ?.trim()
        .orEmpty()
    if (rawValue.isBlank()) {
        return emptyList()
    }
    return runCatching {
        formHistoryJson.decodeFromString<UploadCoverFormHistoryPayload>(rawValue).items
            .map { it.normalized() }
            .deduplicateUploadCoverDrafts()
    }.getOrDefault(emptyList())
}

internal suspend fun saveUploadCoverFormDraft(draft: SavedUploadCoverFormDraft) {
    val normalizedDraft = draft.normalized()
    if (!normalizedDraft.hasContent()) {
        return
    }
    val mergedHistory = listOf(normalizedDraft) + loadUploadCoverFormHistory().filterNot {
        it.sameMeaningAs(normalizedDraft)
    }
    persistFormHistory(
        key = UPLOAD_COVER_FORM_HISTORY_KEY,
        description = "Upload cover recent form history",
        value = formHistoryJson.encodeToString(
            UploadCoverFormHistoryPayload(mergedHistory.take(MAX_FORM_HISTORY_SIZE))
        ),
    )
}

@Composable
internal fun RecentFormHistoryChips(
    labels: List<String>,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, label ->
            FilterChip(
                selected = false,
                onClick = { onSelectIndex(index) },
                label = { Text(label) },
            )
        }
    }
}

private suspend fun persistFormHistory(
    key: String,
    description: String,
    value: String,
) {
    ApiProvider.configApi.updateConfig(
        ConfigEntry(
            key = key,
            value = value,
            description = description,
        )
    )
}

private fun SavedVibeFormDraft.normalized(): SavedVibeFormDraft {
    return copy(
        lyrics = lyrics.trim(),
        songName = songName.trim(),
        artistName = artistName.trim(),
        title = title.trim(),
        tags = tags.trim(),
        mv = mv.trim().ifBlank { "V4_5" },
        vocalGender = vocalGender.trim().ifBlank { "m" },
        negativeTags = negativeTags.trim(),
        gptDescriptionPrompt = gptDescriptionPrompt.trim(),
        selectedPersonaId = selectedPersonaId?.trim()?.ifBlank { null },
        updatedAt = updatedAt.takeIf { it > 0L } ?: getTimeMillis(),
    )
}

private fun SavedVibeFormDraft.hasContent(): Boolean {
    return lyrics.isNotBlank() ||
        songName.isNotBlank() ||
        artistName.isNotBlank() ||
        title.isNotBlank() ||
        tags.isNotBlank() ||
        negativeTags.isNotBlank() ||
        gptDescriptionPrompt.isNotBlank() ||
        makeInstrumental ||
        selectedPersonaId != null
}

private fun SavedVibeFormDraft.sameContentAs(other: SavedVibeFormDraft): Boolean {
    return normalized().copy(updatedAt = 0L) == other.normalized().copy(updatedAt = 0L)
}

private fun SavedUploadCoverFormDraft.normalized(): SavedUploadCoverFormDraft {
    return copy(
        uploadUrl = uploadUrl.trim(),
        sourceTitle = sourceTitle.trim(),
        sourceSubtitle = sourceSubtitle.trim(),
        prompt = prompt.trim(),
        style = style.trim(),
        title = title.trim(),
        selectedModel = selectedModel.trim().ifBlank { "V4_5ALL" },
        selectedGender = selectedGender.trim().ifBlank { "m" },
        selectedPersonaId = selectedPersonaId?.trim()?.ifBlank { null },
        updatedAt = updatedAt.takeIf { it > 0L } ?: getTimeMillis(),
    )
}

private fun SavedUploadCoverFormDraft.hasContent(): Boolean {
    return uploadUrl.isNotBlank() ||
        prompt.isNotBlank() ||
        style.isNotBlank() ||
        title.isNotBlank() ||
        selectedPersonaId != null
}

private fun SavedUploadCoverFormDraft.sameContentAs(other: SavedUploadCoverFormDraft): Boolean {
    return normalized().copy(updatedAt = 0L) == other.normalized().copy(updatedAt = 0L)
}

private fun SavedUploadCoverFormDraft.sameMeaningAs(other: SavedUploadCoverFormDraft): Boolean {
    return normalized().semanticDedupKey() == other.normalized().semanticDedupKey()
}

private fun List<SavedUploadCoverFormDraft>.deduplicateUploadCoverDrafts(): List<SavedUploadCoverFormDraft> {
    val uniqueDrafts = mutableListOf<SavedUploadCoverFormDraft>()
    sortedByDescending { it.updatedAt }.forEach { draft ->
        if (uniqueDrafts.none { it.sameMeaningAs(draft) }) {
            uniqueDrafts += draft
        }
    }
    return uniqueDrafts
}

private fun SavedUploadCoverFormDraft.semanticDedupKey(): String {
    return listOf(
        preferredSourceIdentity(),
        prompt.trim().lowercase(),
        style.trim().lowercase(),
        title.trim().lowercase(),
        selectedModel.trim().ifBlank { "V4_5ALL" }.lowercase(),
        selectedGender.trim().ifBlank { "m" }.lowercase(),
        selectedPersonaId?.trim().orEmpty().lowercase(),
    ).joinToString("\u0001")
}

private fun SavedUploadCoverFormDraft.preferredSourceIdentity(): String {
    val normalizedSourceTitle = sourceTitle.trim().lowercase()
    if (normalizedSourceTitle.isNotBlank()) {
        return "source:$normalizedSourceTitle"
    }
    val normalizedDraftTitle = title.trim().lowercase()
    if (normalizedDraftTitle.isNotBlank()) {
        return "title:$normalizedDraftTitle"
    }
    val normalizedSourceUrl = uploadUrl.canonicalSourceIdentity()
    if (normalizedSourceUrl.isNotBlank()) {
        return "url:$normalizedSourceUrl"
    }
    return ""
}

private fun String.canonicalSourceIdentity(): String {
    val sanitizedUrl = trim()
        .substringBefore('?')
        .substringBefore('#')
        .lowercase()
    if (sanitizedUrl.isBlank()) {
        return ""
    }
    val host = sanitizedUrl.substringAfter("://", sanitizedUrl)
        .substringBefore('/')
        .trim()
    val fileName = sanitizedUrl.substringAfterLast('/')
        .trim()
    if (fileName.isBlank()) {
        return sanitizedUrl
    }
    return when {
        host.contains("tempfile.redpandaai.co") -> fileName
        host.contains("sunoapiorg.redpandaai.co") -> fileName
        host.contains("music.126.net") -> fileName
        host.contains("qqmusic.qq.com") -> fileName
        else -> sanitizedUrl
    }
}

internal fun String.sourceFileName(): String {
    return substringBefore('?')
        .substringAfterLast('/')
        .trim()
}

internal fun String.sourceSummary(): String {
    val host = substringAfter("://", "")
        .substringBefore('/')
        .trim()
    val fileName = sourceFileName()
    return listOfNotNull(
        host.takeIf { it.isNotBlank() },
        fileName.takeIf { it.isNotBlank() },
    ).joinToString(" · ")
}

private fun String.truncateForChip(maxLength: Int = 22): String {
    val normalizedText = trim()
    if (normalizedText.length <= maxLength) {
        return normalizedText
    }
    return normalizedText.take(maxLength - 3) + "..."
}
