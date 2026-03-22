package site.addzero.coding.playground.server.generation

import org.koin.core.annotation.Single
import site.addzero.coding.playground.shared.dto.CompositeIntegrationResultDto
import site.addzero.coding.playground.shared.service.CompositeBuildIntegrator
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Single
class CompositeBuildIntegratorImpl : CompositeBuildIntegrator {
    override suspend fun integrate(targetRoot: String, includeBuildPath: String, marker: String): CompositeIntegrationResultDto {
        val settingsPath = Paths.get(targetRoot).resolve("settings.gradle.kts")
        settingsPath.parent?.createDirectories()
        val begin = "// >>> $marker BEGIN"
        val end = "// <<< $marker END"
        val block = """
            $begin
            includeBuild("$includeBuildPath")
            $end
        """.trimIndent()
        val newContent = if (!settingsPath.exists()) {
            block + "\n"
        } else {
            val current = settingsPath.readText()
            if (current.contains(begin) && current.contains(end)) {
                current.replace(Regex("${Regex.escape(begin)}[\\s\\S]*?${Regex.escape(end)}"), block)
            } else {
                current.trimEnd() + "\n\n" + block + "\n"
            }
        }
        val changed = !settingsPath.exists() || settingsPath.readTextOrNull() != newContent
        settingsPath.writeText(newContent)
        return CompositeIntegrationResultDto(
            targetFile = settingsPath.toString(),
            marker = marker,
            changed = changed,
        )
    }
}

private fun Path.readTextOrNull(): String? = if (exists()) readText() else null
