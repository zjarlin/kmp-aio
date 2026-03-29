package site.addzero.coding.playground.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.addzero.coding.playground.NeteaseDemoState
import site.addzero.kcloud.api.netease.NeteaseSearchSong

@Composable
fun NeteaseDemoPanel(
    state: NeteaseDemoState,
) {
    val scope = rememberCoroutineScope()
    val selectedSong = state.songs.firstOrNull { it.id == state.selectedSongId }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NeteaseInfoCard()
        OutlinedTextField(
            value = state.token,
            onValueChange = state::updateToken,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("可选 Token") },
            placeholder = { Text("不填也可以直接试搜索") },
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = state::updateQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("歌曲关键词") },
            placeholder = { Text("例如：周杰伦 晴天") },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { scope.launch { state.searchSongs() } },
                modifier = Modifier.weight(1f),
                enabled = !state.loading,
            ) {
                Text(if (state.loading) "搜索中…" else "搜索歌曲")
            }
            TextButton(
                onClick = {
                    selectedSong?.let { song ->
                        scope.launch { state.loadLyric(song.id) }
                    }
                },
                enabled = selectedSong != null && !state.lyricLoading,
            ) {
                Text(if (state.lyricLoading) "加载中…" else "刷新歌词")
            }
        }
        Text(
            text = state.statusMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        if (state.songs.isEmpty()) {
            NeteaseHint("还没有结果。点击“搜索歌曲”后，这里会展示 `api-netease` 返回的真实数据。")
        } else {
            Text("搜索结果", fontWeight = FontWeight.SemiBold)
            state.songs.forEach { song ->
                NeteaseSongCard(
                    song = song,
                    selected = song.id == state.selectedSongId,
                    onSelect = {
                        scope.launch {
                            state.loadLyric(song.id)
                        }
                    },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("歌词预览", fontWeight = FontWeight.SemiBold)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF111827),
        ) {
            SelectionContainer {
                Text(
                    text = state.lyricPreview,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE5EEF9),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun NeteaseInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("api-netease 已接入 coding-playground", fontWeight = FontWeight.SemiBold)
            Text(
                "这里直接调用 `MusicSearchClient.musicApi.search/getLyric`，不是 mock，也没有再包一层应用内适配器。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun NeteaseSongCard(
    song: NeteaseSearchSong,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            } else {
                MaterialTheme.colorScheme.background
            },
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(song.name, fontWeight = FontWeight.SemiBold)
                if (selected) {
                    Text(
                        "当前查看",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = listOfNotNull(
                    song.artistNames.ifBlank { null },
                    song.album?.name?.takeIf { it.isNotBlank() },
                ).joinToString(" / "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = "songId=${song.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun NeteaseHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(10.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}
