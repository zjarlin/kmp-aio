package site.addzero.notes.markdown

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import site.addzero.notes.ui.components.LiquidGlassCard

private data class MarkdownBlock(
    val start: Int,
    val end: Int,
    val text: String,
)

@Composable
fun LiveMarkdownEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Markdown（实时渲染）",
    enabled: Boolean = true,
) {
    val blocks = remember(value) { splitMarkdownBlocks(value) }
    val scrollState = rememberScrollState()

    var caretOffset by remember { mutableIntStateOf(0) }
    var focusRequestNonce by remember { mutableIntStateOf(0) }
    val activeBlockIndex = remember(blocks, caretOffset) {
        findBlockIndexByOffset(blocks, caretOffset)
    }

    LiquidGlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            blocks.forEachIndexed { index, block ->
                if (index == activeBlockIndex && enabled) {
                    val focusRequester = remember { FocusRequester() }
                    val localCaret = (caretOffset - block.start).coerceIn(0, block.text.length)
                    val fieldValue = TextFieldValue(
                        text = block.text,
                        selection = TextRange(localCaret),
                    )
                    val minLines = block.text
                        .lineSequence()
                        .count()
                        .coerceIn(4, 18)

                    OutlinedTextField(
                        value = fieldValue,
                        onValueChange = { next ->
                            val updatedText = value.replaceRange(block.start, block.end, next.text)
                            val nextCaret = (block.start + next.selection.start)
                                .coerceIn(0, updatedText.length)
                            caretOffset = nextCaret
                            onValueChange(updatedText)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        enabled = enabled,
                        singleLine = false,
                        minLines = minLines,
                        placeholder = {
                            Text(text = placeholder)
                        },
                    )

                    LaunchedEffect(focusRequestNonce, enabled) {
                        if (enabled) {
                            focusRequester.requestFocus()
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = enabled) {
                                caretOffset = block.start.coerceIn(0, value.length)
                                focusRequestNonce += 1
                            }
                    ) {
                        MarkdownPreview(
                            markdown = block.text,
                            modifier = Modifier.fillMaxWidth(),
                            emptyText = "",
                            emptyContentAlignment = Alignment.TopStart,
                        )
                    }
                }
            }
        }
    }
}

private fun splitMarkdownBlocks(source: String): List<MarkdownBlock> {
    if (source.isEmpty()) {
        return listOf(
            MarkdownBlock(
                start = 0,
                end = 0,
                text = "",
            )
        )
    }

    val blocks = mutableListOf<MarkdownBlock>()
    var start = 0

    while (start < source.length) {
        val separatorIndex = source.indexOf("\n\n", start)
        val end = if (separatorIndex < 0) {
            source.length
        } else {
            separatorIndex + 2
        }
        blocks += MarkdownBlock(
            start = start,
            end = end,
            text = source.substring(start, end),
        )
        start = end
    }

    if (blocks.isEmpty()) {
        blocks += MarkdownBlock(
            start = 0,
            end = 0,
            text = "",
        )
    }
    return blocks
}

private fun findBlockIndexByOffset(blocks: List<MarkdownBlock>, offset: Int): Int {
    if (blocks.isEmpty()) {
        return 0
    }

    val clamped = offset.coerceIn(0, blocks.last().end)
    val index = blocks.indexOfFirst { block ->
        clamped >= block.start && clamped <= block.end
    }

    return if (index >= 0) {
        index
    } else {
        blocks.lastIndex
    }
}
