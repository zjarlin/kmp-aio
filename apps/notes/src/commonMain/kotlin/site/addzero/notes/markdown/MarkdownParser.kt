package site.addzero.notes.markdown

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class Bullet(val text: String) : MarkdownBlock
    data class Checklist(val checked: Boolean, val text: String) : MarkdownBlock
    data class Quote(val text: String) : MarkdownBlock
    data class Code(val language: String, val content: String) : MarkdownBlock
    data object Empty : MarkdownBlock
}

fun parseMarkdown(markdown: String): List<MarkdownBlock> {
    if (markdown.isBlank()) {
        return emptyList()
    }

    val blocks = mutableListOf<MarkdownBlock>()
    var inCodeBlock = false
    var codeLanguage = ""
    val codeLines = mutableListOf<String>()

    markdown.lines().forEach { rawLine ->
        val line = rawLine.trimEnd()
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                blocks += MarkdownBlock.Code(
                    language = codeLanguage,
                    content = codeLines.joinToString("\n")
                )
                codeLines.clear()
                codeLanguage = ""
                inCodeBlock = false
            } else {
                inCodeBlock = true
                codeLanguage = trimmed.removePrefix("```").trim()
            }
            return@forEach
        }

        if (inCodeBlock) {
            codeLines += rawLine
            return@forEach
        }

        when {
            trimmed.isBlank() -> blocks += MarkdownBlock.Empty
            trimmed.startsWith("- [ ] ") -> blocks += MarkdownBlock.Checklist(
                checked = false,
                text = trimmed.removePrefix("- [ ] ").trim()
            )

            trimmed.startsWith("- [x] ", ignoreCase = true) -> blocks += MarkdownBlock.Checklist(
                checked = true,
                text = trimmed.removePrefix("- [x] ").trim()
            )

            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> blocks += MarkdownBlock.Bullet(
                text = trimmed.drop(2).trim()
            )

            trimmed.startsWith("> ") -> blocks += MarkdownBlock.Quote(
                text = trimmed.removePrefix("> ").trim()
            )

            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { char -> char == '#' }.length.coerceIn(1, 6)
                val text = trimmed.drop(level).trim()
                blocks += MarkdownBlock.Heading(level = level, text = text)
            }

            else -> blocks += MarkdownBlock.Paragraph(trimmed)
        }
    }

    if (inCodeBlock) {
        blocks += MarkdownBlock.Code(
            language = codeLanguage,
            content = codeLines.joinToString("\n")
        )
    }

    return collapseEmptyBlocks(blocks)
}

private fun collapseEmptyBlocks(blocks: List<MarkdownBlock>): List<MarkdownBlock> {
    val result = mutableListOf<MarkdownBlock>()
    var previousEmpty = false
    blocks.forEach { block ->
        if (block is MarkdownBlock.Empty) {
            if (!previousEmpty) {
                result += block
            }
            previousEmpty = true
            return@forEach
        }
        previousEmpty = false
        result += block
    }
    return result
}
