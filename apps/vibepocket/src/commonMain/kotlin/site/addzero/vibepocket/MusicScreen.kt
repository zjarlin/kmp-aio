package site.addzero.vibepocket

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import site.addzero.vibepocket.network.ApiClient
import site.addzero.vibepocket.network.Song

@Composable
fun MusicScreen() {
    val apiClient = remember { ApiClient() }
//    val client = HttpClient()
    val coroutineScope = rememberCoroutineScope()
    val fileSaverLauncher = rememberFileSaverLauncher { file ->
        // 保存完成后的回调，file 为保存的文件（可能为 null 如果用户取消）
    }


    var musicSearchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Song>>(emptyList()) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    var sunoPrompt by remember { mutableStateOf("") }
    var generatedSongUrl by remember { mutableStateOf<String?>(null) }
    var sunoError by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Music Search Section
        Text("Music Search", style = MaterialTheme.typography.headlineSmall) // h5 -> headlineSmall
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = musicSearchQuery,
                onValueChange = { musicSearchQuery = it },
                label = { Text("Search Query") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    searchError = null
                    isSearching = true
                    coroutineScope.launch {
                        try {
                            val result = apiClient.searchMusic(musicSearchQuery)
                            searchResults = result.results
                        } catch (e: Exception) {
                            searchError = e.message
                        } finally {
                            isSearching = false
                        }
                    }
                },
                enabled = musicSearchQuery.isNotBlank() && !isSearching
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    ) // colors -> colorScheme
                } else {
                    Text("Search")
                }
            }
        }
        searchError?.let {
            Text("Error: $it", color = MaterialTheme.colorScheme.error) // colors -> colorScheme
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (searchResults.isNotEmpty()) {
            Text("Search Results", style = MaterialTheme.typography.headlineMedium) // h6 -> headlineMedium
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(searchResults) { song ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    song.title,
                                    style = MaterialTheme.typography.titleMedium
                                ) // subtitle1 -> titleMedium
                                Text(song.artist, style = MaterialTheme.typography.bodySmall) // body2 -> bodySmall
                            }
                            Row {
                                Button(onClick = { /* TODO: Play song */ }) { Text("Play") }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
//                                    coroutineScope.launch {
//                                        downloadFile(song.url, "${song.artist} - ${song.title}.mp3")
//                                    }
                                }) { Text("Download") }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // AI Song Generation Section
        Text("AI Song Generation", style = MaterialTheme.typography.headlineSmall) // h5 -> headlineSmall
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = sunoPrompt,
                onValueChange = { sunoPrompt = it },
                label = { Text("Song Prompt") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    sunoError = null
                    generatedSongUrl = null
                    isGenerating = true
                    coroutineScope.launch {
                        try {
                            val result = apiClient.generateSong(sunoPrompt)
                            generatedSongUrl = result.songUrl
                        } catch (e: Exception) {
                            sunoError = e.message
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = sunoPrompt.isNotBlank() && !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    ) // colors -> colorScheme
                } else {
                    Text("Generate")
                }
            }
        }
        sunoError?.let {
            Text("Error: $it", color = MaterialTheme.colorScheme.error) // colors -> colorScheme
        }
        generatedSongUrl?.let { url ->
            Spacer(modifier = Modifier.height(8.dp))
            Text("Generated Song URL: $url")
            Button(onClick = { /* TODO: Play generated song */ }) { Text("Play Generated") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {

                coroutineScope.launch {
                    val client = apiClient.client
                    val bytes = client.get(site.addzero.vibepocket.url).readRawBytes()
                    fileSaverLauncher.launch(
                        bytes = bytes,
                        baseName = "audio",
                        extension = "mp3"
                    )
                }


//                coroutineScope.launch {
//
//
//                    downloadFile(url, "generated_suno_song.mp3")
//                }
            }) { Text("Download Generated") }
        }
    }
}
