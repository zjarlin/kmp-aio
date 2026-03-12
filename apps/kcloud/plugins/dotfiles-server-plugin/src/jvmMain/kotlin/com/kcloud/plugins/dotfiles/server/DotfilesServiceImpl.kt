package com.kcloud.plugins.dotfiles.server

import com.kcloud.plugin.KCloudLocalPaths
import com.kcloud.plugin.readKCloudJson
import com.kcloud.plugin.writeKCloudJson
import com.kcloud.plugins.dotfiles.DotfilesCommandResult
import com.kcloud.plugins.dotfiles.DotfilesService
import com.kcloud.plugins.dotfiles.DotfilesSettings
import com.kcloud.plugins.dotfiles.DotfilesStatus
import java.io.File

private const val DOTFILES_PLUGIN_ID = "dotfiles-plugin"

class DotfilesServiceImpl : DotfilesService {
    private val pluginDirectory = KCloudLocalPaths.pluginDir(DOTFILES_PLUGIN_ID)
    private val settingsFile = File(pluginDirectory, "settings.json")

    override fun loadSettings(): DotfilesSettings {
        return readKCloudJson(settingsFile) {
            DotfilesSettings(
                workingDirectory = pluginDirectory.absolutePath
            )
        }
    }

    override fun saveSettings(settings: DotfilesSettings): DotfilesSettings {
        val normalized = settings.copy(
            workingDirectory = settings.workingDirectory.ifBlank { pluginDirectory.absolutePath }
        )
        writeKCloudJson(settingsFile, normalized)
        return normalized
    }

    override fun readStatus(): DotfilesStatus {
        val settings = loadSettings()
        val version = runChezmoi(settings, "--version")
        val status = runChezmoi(settings, "status")
        return DotfilesStatus(
            cliAvailable = version.success,
            versionOutput = version.output,
            statusOutput = status.output,
            workingDirectory = settings.workingDirectory
        )
    }

    override fun initializeRepository(): DotfilesCommandResult {
        val settings = loadSettings()
        if (settings.repoUrl.isBlank()) {
            return DotfilesCommandResult(false, "仓库地址不能为空")
        }
        return runChezmoi(settings, "init", settings.repoUrl)
    }

    override fun diff(): DotfilesCommandResult {
        return runChezmoi(loadSettings(), "diff")
    }

    override fun applyChanges(): DotfilesCommandResult {
        return runChezmoi(loadSettings(), "apply", "-v")
    }

    private fun runChezmoi(
        settings: DotfilesSettings,
        vararg arguments: String
    ): DotfilesCommandResult {
        return runCatching {
            val workingDirectory = File(settings.workingDirectory.ifBlank { pluginDirectory.absolutePath }).also { directory ->
                if (!directory.exists()) {
                    directory.mkdirs()
                }
            }
            val command = listOf("chezmoi") + arguments
            val process = ProcessBuilder(command)
                .directory(workingDirectory)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            DotfilesCommandResult(
                success = exitCode == 0,
                output = buildString {
                    appendLine("$ ${command.joinToString(" ")}")
                    append(output.ifBlank { "(no output)" })
                }
            )
        }.getOrElse { throwable ->
            DotfilesCommandResult(false, throwable.message ?: "执行 chezmoi 失败")
        }
    }
}
