package site.addzero.vibepocket.music

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.vibepocket.api.suno.SUNO_MODELS
import site.addzero.vibepocket.api.suno.VOCAL_GENDERS
import site.addzero.vibepocket.model.PersonaItem
import site.addzero.vibepocket.ui.StudioSectionCard

@Composable
fun ParamsStep(
    title: String,
    onTitleChange: (String) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    mv: String,
    onMvChange: (String) -> Unit,
    makeInstrumental: Boolean,
    onMakeInstrumentalChange: (Boolean) -> Unit,
    vocalGender: String,
    onVocalGenderChange: (String) -> Unit,
    negativeTags: String,
    onNegativeTagsChange: (String) -> Unit,
    gptDescriptionPrompt: String,
    onGptDescriptionPromptChange: (String) -> Unit,
    personas: List<PersonaItem> = emptyList(),
    selectedPersonaId: String? = null,
    onPersonaChange: (String?) -> Unit = {},
) {
    @Composable
    fun BasicInfoPanel() {
        StudioSectionCard(
            title = "基本信息",
            subtitle = "先给这首歌一个标题，再补上你想要和不想要的风格。",
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("歌曲标题") },
                placeholder = { Text("给你的歌起个名字") },
                singleLine = true,
            )
            OutlinedTextField(
                value = tags,
                onValueChange = onTagsChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("风格标签") },
                placeholder = { Text("例如：pop, rock, chinese gospel") },
                singleLine = true,
            )
            OutlinedTextField(
                value = negativeTags,
                onValueChange = onNegativeTagsChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("负面标签") },
                placeholder = { Text("例如：heavy metal, screaming") },
                singleLine = true,
            )
        }
    }

    @Composable
    fun VoicePanel() {
        StudioSectionCard(
            title = "模型与声音",
            subtitle = "挑一个模型版本、声音方向，以及是否制作纯音乐。",
        ) {
            FieldLabel("模型版本")
            ChipSelector(
                options = SUNO_MODELS,
                selected = mv,
                onSelect = onMvChange,
            )

            FieldLabel("声音性别")
            ChipSelector(
                options = VOCAL_GENDERS.map { it.first },
                labels = VOCAL_GENDERS.map { it.second },
                selected = vocalGender,
                onSelect = onVocalGenderChange,
            )

            FieldLabel("Persona 声音角色")
            PersonaSelector(
                personas = personas,
                selectedPersonaId = selectedPersonaId,
                onPersonaChange = onPersonaChange,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "纯音乐（无人声）",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "打开后会优先生成器乐版本，不再强调 vocal。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = makeInstrumental,
                    onCheckedChange = onMakeInstrumentalChange,
                )
            }
        }
    }

    @Composable
    fun PromptPanel() {
        StudioSectionCard(
            title = "AI 灵感描述",
            subtitle = "可选。用自然语言描述你希望得到的音乐气质。",
        ) {
            OutlinedTextField(
                value = gptDescriptionPrompt,
                onValueChange = onGptDescriptionPromptChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("描述词") },
                placeholder = { Text("例如：Powerful black male gospel lead vocal, deep and soulful...") },
                singleLine = false,
                minLines = 5,
            )
        }
    }

    BoxWithConstraints {
        val useWideLayout = maxWidth >= 980.dp
        if (useWideLayout) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        BasicInfoPanel()
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        VoicePanel()
                    }
                }
                PromptPanel()
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BasicInfoPanel()
                VoicePanel()
                PromptPanel()
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun ChipSelector(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    labels: List<String>? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        options.forEachIndexed { index, option ->
            val displayLabel = labels?.getOrNull(index) ?: option
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(displayLabel) },
            )
        }
    }
}

@Composable
private fun PersonaSelector(
    personas: List<PersonaItem>,
    selectedPersonaId: String?,
    onPersonaChange: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                label = {
                    Text(
                        text = persona.name,
                        modifier = Modifier.widthIn(max = 180.dp),
                    )
                },
            )
        }
    }
}
