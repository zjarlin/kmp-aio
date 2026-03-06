package site.addzero.notes.ai

import site.addzero.notes.model.Note
import site.addzero.notes.model.OrganizeResult

class LocalNoteOrganizer {
    fun organize(current: Note, allNotes: List<Note>): OrganizeResult {
        val references = ReferenceTokenParser
            .extractReferences(current.markdown)
            .filterNot { reference -> reference == "thisFile" }

        val usedReferences = mutableListOf("@thisFile")
        val missingReferences = mutableListOf<String>()
        val contextBlocks = mutableListOf<Pair<String, String>>()

        contextBlocks += "@thisFile" to current.markdown

        references.forEach { reference ->
            val targetNote = ReferenceTokenParser.resolveReference(reference, allNotes)
            if (targetNote == null) {
                missingReferences += "@$reference"
            } else {
                val token = "@${targetNote.path}"
                usedReferences += token
                contextBlocks += token to targetNote.markdown
            }
        }

        val summaryLines = buildSummary(contextBlocks)
        val todoLines = buildTodos(contextBlocks)
        val normalizedBody = normalizeBody(current.markdown)

        val organizedMarkdown = buildString {
            appendLine("# ${current.title}")
            appendLine()
            appendLine("## AI整理摘要")
            if (summaryLines.isEmpty()) {
                appendLine("- 暂无可提炼摘要，建议补充正文内容")
            } else {
                summaryLines.forEach { line ->
                    appendLine("- $line")
                }
            }
            appendLine()
            appendLine("## 待办拆解")
            if (todoLines.isEmpty()) {
                appendLine("- [ ] 暂未识别到待办，建议手动补充下一步动作")
            } else {
                todoLines.forEach { line ->
                    appendLine("- [ ] $line")
                }
            }
            appendLine()
            appendLine("## 整理后正文")
            appendLine(normalizedBody.ifBlank { "（空）" })
            appendLine()
            appendLine("## 上下文引用")
            contextBlocks.forEach { (token, content) ->
                appendLine("### $token")
                excerpt(content).forEach { line ->
                    appendLine("> $line")
                }
                appendLine()
            }
        }.trim()

        return OrganizeResult(
            organizedMarkdown = organizedMarkdown,
            usedReferences = usedReferences.distinct(),
            missingReferences = missingReferences
        )
    }

    private fun normalizeBody(markdown: String): String {
        return markdown
            .lines()
            .map { line ->
                line.replace(Regex("""@[a-zA-Z0-9_./-]+"""), "").trimEnd()
            }
            .joinToString("\n")
            .trim()
    }

    private fun buildSummary(contextBlocks: List<Pair<String, String>>): List<String> {
        return contextBlocks
            .flatMap { (_, content) -> content.lines() }
            .map { line -> line.trim() }
            .filter { line ->
                line.isNotBlank() &&
                    !line.startsWith("#") &&
                    !line.startsWith("- [") &&
                    !line.startsWith(">") &&
                    !line.startsWith("```")
            }
            .take(6)
    }

    private fun buildTodos(contextBlocks: List<Pair<String, String>>): List<String> {
        return contextBlocks
            .flatMap { (_, content) -> content.lines() }
            .map { line -> line.trim() }
            .mapNotNull { line ->
                when {
                    line.startsWith("- [ ]") -> line.removePrefix("- [ ]").trim()
                    line.startsWith("- [x]") -> line.removePrefix("- [x]").trim()
                    line.startsWith("- ") -> line.removePrefix("- ").trim()
                    else -> null
                }
            }
            .filter { line -> line.isNotBlank() }
            .take(8)
    }

    private fun excerpt(content: String): List<String> {
        val lines = content
            .lines()
            .map { line -> line.trim() }
            .filter { line -> line.isNotBlank() }
            .take(10)
        if (lines.isEmpty()) {
            return listOf("（空内容）")
        }
        return lines
    }
}
