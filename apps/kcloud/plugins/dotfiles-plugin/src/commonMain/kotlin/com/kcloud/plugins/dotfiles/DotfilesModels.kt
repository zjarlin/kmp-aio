package com.kcloud.plugins.dotfiles

import kotlinx.serialization.Serializable

@Serializable
data class DotfilesSettings(
    val repoUrl: String = "",
    val workingDirectory: String = "",
    val sourceDirectory: String = ""
)

@Serializable
data class DotfilesStatus(
    val cliAvailable: Boolean,
    val versionOutput: String,
    val statusOutput: String,
    val workingDirectory: String
)

@Serializable
data class DotfilesCommandResult(
    val success: Boolean,
    val output: String
)

interface DotfilesService {
    fun loadSettings(): DotfilesSettings
    fun saveSettings(settings: DotfilesSettings): DotfilesSettings
    fun readStatus(): DotfilesStatus
    fun initializeRepository(): DotfilesCommandResult
    fun diff(): DotfilesCommandResult
    fun applyChanges(): DotfilesCommandResult
}
