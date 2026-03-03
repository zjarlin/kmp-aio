package site.addzero.vibepocket.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.addzero.component.glass.*
import site.addzero.vibepocket.model.PersonaItem

// Constants for model versions
private val SUNO_MODELS = listOf("v3.5", "v3", "v2")

// Constants for vocal genders, with display labels
private val VOCAL_GENDERS = listOf(
    Pair("male", "男声"),
    Pair("female", "女声"),
    Pair("any", "任意"),
)

/**
 * 第二步：Vibe 参数配置
 */
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 基本信息
        NeonGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = GlassColors.NeonPurple
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🎤 基本信息", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                FieldLabel("歌曲标题")
                GlassTextField(
                    value = title, onValueChange = onTitleChange,
                    placeholder = "给你的歌起个名字", modifier = Modifier.fillMaxWidth()
                )

                FieldLabel("风格标签")
                GlassTextField(
                    value = tags, onValueChange = onTagsChange,
                    placeholder = "例如: pop, rock, 黑人福音, chinese", modifier = Modifier.fillMaxWidth()
                )

                FieldLabel("负面标签（不想要的风格）")
                GlassTextField(
                    value = negativeTags, onValueChange = onNegativeTagsChange,
                    placeholder = "例如: heavy metal, screaming", modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 模型与声音
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("⚙️ 模型与声音", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                FieldLabel("模型版本")
                ChipSelector(options = SUNO_MODELS, selected = mv, onSelect = onMvChange)

                FieldLabel("声音性别")
                ChipSelector(
                    options = VOCAL_GENDERS.map { it.first },
                    labels = VOCAL_GENDERS.map { it.second },
                    selected = vocalGender, onSelect = onVocalGenderChange
                )

                // Persona 声音角色选择
                FieldLabel("Persona 声音角色")
                PersonaSelector(
                    personas = personas,
                    selectedPersonaId = selectedPersonaId,
                    onPersonaChange = onPersonaChange,
                )

                // 纯音乐开关
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { onMakeInstrumentalChange(!makeInstrumental) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("纯音乐（无人声）", color = Color.White, fontSize = 14.sp)
                    Box(
                        modifier = Modifier.size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (makeInstrumental) GlassColors.NeonCyan
                                else Color.White.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (makeInstrumental) {
                            Text("✓", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // GPT 描述
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("💡 AI 灵感描述（可选）", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "用自然语言描述你想要的音乐风格，AI 会据此生成",
                    color = GlassTheme.TextTertiary, fontSize = 12.sp
                )
                GlassTextArea(
                    value = gptDescriptionPrompt,
                    onValueChange = onGptDescriptionPromptChange,
                    placeholder = "例如: Powerful Black male gospel lead vocal, deep, soulful...",
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 200.dp)
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, color = GlassTheme.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
}

@Composable
fun ChipSelector(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    labels: List<String>? = null
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            val displayLabel = labels?.getOrNull(index) ?: option
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) GlassColors.NeonCyan.copy(alpha = 0.3f)
                        else Color.White.copy(alpha = 0.08f)
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = displayLabel,
                    color = if (isSelected) GlassColors.NeonCyan else GlassTheme.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Persona 声音角色选择器
 * 使用横向可滚动 Chip 列表，第一项为"无 Persona"默认选项。
 */
@Composable
private fun PersonaSelector(
    personas: List<PersonaItem>,
    selectedPersonaId: String?,
    onPersonaChange: (String?) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
    ) {
        // 默认"无"选项
        val noneSelected = selectedPersonaId == null
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (noneSelected) GlassColors.NeonCyan.copy(alpha = 0.3f)
                    else Color.White.copy(alpha = 0.08f)
                )
                .clickable { onPersonaChange(null) }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "无",
                color = if (noneSelected) GlassColors.NeonCyan else GlassTheme.TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (noneSelected) FontWeight.Bold else FontWeight.Normal,
            )
        }

        // Persona 列表
        personas.forEach { persona ->
            val isSelected = persona.personaId == selectedPersonaId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) GlassColors.NeonCyan.copy(alpha = 0.3f)
                        else Color.White.copy(alpha = 0.08f)
                    )
                    .clickable { onPersonaChange(persona.personaId) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = persona.name,
                    color = if (isSelected) GlassColors.NeonCyan else GlassTheme.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}
