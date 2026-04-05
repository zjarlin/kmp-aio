package site.addzero.kcloud.music

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import site.addzero.cupertino.workbench.material3.FilterChip
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.cupertino.workbench.button.WorkbenchTextButton as TextButton
import site.addzero.kcloud.api.ApiProvider
import site.addzero.kcloud.vibepocket.model.PersonaItem

internal suspend fun loadSavedPersonas(): List<PersonaItem> {
    return ApiProvider.personaApi.getPersonas()
        .distinctBy { it.personaId }
        .sortedByDescending { it.createdAt.orEmpty() }
}

internal fun List<PersonaItem>.upsertPersona(persona: PersonaItem): List<PersonaItem> {
    val withoutCurrent = filterNot { it.personaId == persona.personaId }
    return listOf(persona) + withoutCurrent
}

internal fun List<PersonaItem>.containsPersona(personaId: String?): Boolean {
    if (personaId == null) {
        return true
    }
    return any { it.personaId == personaId }
}

@Composable
internal fun PersonaSelectionPanel(
    personas: List<PersonaItem>,
    selectedPersonaId: String?,
    onPersonaChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "还没有 Persona。先在生成结果的更多操作里创建一个，再回来复用声线。",
    onRefresh: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
) {
    val selectedPersona = personas.firstOrNull { it.personaId == selectedPersonaId }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "已保存 ${personas.size} 个 Persona",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (onRefresh != null) {
                TextButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                ) {
                    Text(if (isRefreshing) "刷新中..." else "刷新列表")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedPersonaId == null,
                onClick = { onPersonaChange(null) },
                label = { Text("无") },
            )
            personas.forEach { persona ->
                FilterChip(
                    selected = persona.personaId == selectedPersonaId,
                    onClick = { onPersonaChange(persona.personaId) },
                    label = { Text(persona.name) },
                )
            }
        }

        when {
            personas.isEmpty() -> {
                DialogInfoCard(
                    title = "暂无 Persona",
                    body = emptyMessage,
                    accent = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            selectedPersona != null -> {
                val detail = buildString {
                    append(selectedPersona.description)
                    append("\n\nPersona ID: ")
                    append(selectedPersona.personaId)
                    selectedPersona.createdAt
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            append("\n创建时间: ")
                            append(it)
                        }
                }
                DialogInfoCard(
                    title = "当前已选: ${selectedPersona.name}",
                    body = detail,
                    accent = MaterialTheme.colorScheme.secondaryContainer,
                )
            }

            else -> {
                DialogHint("当前未绑定 Persona，将按照普通声线参数生成。")
            }
        }
    }
}
