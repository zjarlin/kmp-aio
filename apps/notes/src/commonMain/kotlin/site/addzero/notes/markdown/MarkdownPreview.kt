package site.addzero.notes.markdown

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m3.Markdown

@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier,
    emptyText: String = "Markdown 预览区",
    emptyContentAlignment: Alignment = Alignment.Center
) {
    if (markdown.isBlank()) {
        Box(
            modifier = modifier,
            contentAlignment = emptyContentAlignment
        ) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Markdown(
        content = markdown,
        modifier = modifier
    )
}
