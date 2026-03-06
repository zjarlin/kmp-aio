package site.addzero.notes.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) { parseMarkdown(markdown) }
    if (blocks.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Markdown 预览区",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(blocks) { _, block ->
            when (block) {
                is MarkdownBlock.Heading -> Heading(block)
                is MarkdownBlock.Paragraph -> Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyLarge
                )

                is MarkdownBlock.Bullet -> Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        modifier = Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is MarkdownBlock.Checklist -> Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = if (block.checked) "☑" else "☐",
                        modifier = Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is MarkdownBlock.Quote -> Text(
                    text = "❝ ${block.text}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                is MarkdownBlock.Code -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.medium)
                        .padding(12.dp)
                ) {
                    if (block.language.isNotBlank()) {
                        Text(
                            text = block.language,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB0BEC5),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = block.content.ifBlank { " " },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFECEFF1)
                    )
                }

                MarkdownBlock.Empty -> Box(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun Heading(block: MarkdownBlock.Heading) {
    val style = when (block.level) {
        1 -> MaterialTheme.typography.headlineMedium
        2 -> MaterialTheme.typography.headlineSmall
        3 -> MaterialTheme.typography.titleLarge
        4 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleSmall
    }
    Text(
        text = block.text,
        style = style,
        fontWeight = FontWeight.SemiBold
    )
}
