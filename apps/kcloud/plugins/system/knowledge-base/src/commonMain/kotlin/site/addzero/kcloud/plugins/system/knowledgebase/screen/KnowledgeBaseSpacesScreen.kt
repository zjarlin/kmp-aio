package site.addzero.kcloud.plugins.system.knowledgebase.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import site.addzero.annotation.Route
import site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseWorkbenchState

@Route(
    value = "知识库",
    title = "知识空间",
    routePath = "system/knowledge-base/spaces",
    icon = "MenuBook",
    order = 40.0,
)
@Composable
fun KnowledgeBaseSpacesScreen() {
    KnowledgeBaseSpacesContent(state = koinInject())
}

@Composable
private fun KnowledgeBaseSpacesContent(
    state: KnowledgeBaseWorkbenchState,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        state.ensureLoaded()
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.weight(0.8f).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "知识空间",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.spaces, key = { space -> space.id }) { space ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.selectedSpaceId == space.id) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                                },
                            ),
                            onClick = {
                                scope.launch {
                                    state.selectSpace(space.id)
                                }
                            },
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = space.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = space.description.orEmpty().ifBlank { "未填写描述" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "空间编辑",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (state.statusMessage.isNotBlank()) {
                    Text(
                        text = state.statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                OutlinedTextField(
                    value = state.spaceName,
                    onValueChange = { state.spaceName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("空间名称") },
                )
                OutlinedTextField(
                    value = state.spaceDescription,
                    onValueChange = { state.spaceDescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("空间描述") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !state.isBusy,
                        onClick = { state.beginCreateSpace() },
                    ) {
                        Text("新建空间")
                    }
                    Button(
                        enabled = !state.isBusy,
                        onClick = { scope.launch { state.saveSpace() } },
                    ) {
                        Text("保存空间")
                    }
                    Button(
                        enabled = !state.isBusy && state.selectedSpaceId != null,
                        onClick = { scope.launch { state.deleteSelectedSpace() } },
                    ) {
                        Text("删除空间")
                    }
                }
                Text(
                    text = "文档列表",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.documents, key = { document -> document.id }) { document ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (state.selectedDocumentId == document.id) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f)
                                },
                            ),
                            onClick = {
                                state.selectDocument(document.id)
                            },
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = document.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "更新时间：${document.updateTimeMillis ?: document.createTimeMillis}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1.2f).fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "文档编辑",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OutlinedTextField(
                    value = state.documentTitle,
                    onValueChange = { state.documentTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("文档标题") },
                )
                OutlinedTextField(
                    value = state.documentContent,
                    onValueChange = { state.documentContent = it },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    minLines = 12,
                    label = { Text("正文") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !state.isBusy,
                        onClick = { state.beginCreateDocument() },
                    ) {
                        Text("新建文档")
                    }
                    Button(
                        enabled = !state.isBusy,
                        onClick = { scope.launch { state.saveDocument() } },
                    ) {
                        Text("保存文档")
                    }
                    Button(
                        enabled = !state.isBusy && state.selectedDocumentId != null,
                        onClick = { scope.launch { state.deleteSelectedDocument() } },
                    ) {
                        Text("删除文档")
                    }
                }
            }
        }
    }
}
