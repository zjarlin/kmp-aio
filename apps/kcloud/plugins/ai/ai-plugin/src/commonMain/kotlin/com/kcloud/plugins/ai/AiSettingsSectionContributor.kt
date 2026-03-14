package com.kcloud.plugins.ai

import androidx.compose.runtime.Composable
import com.kcloud.model.AppSettings
import com.kcloud.plugins.ai.spi.AiDiagnosticsService
import com.kcloud.plugins.ai.ui.AiSettingsSection
import com.kcloud.plugins.settings.SettingsSectionContributor
import org.koin.core.annotation.Single

@Single
class AiSettingsSectionContributor(
    private val diagnosticsService: AiDiagnosticsService
) : SettingsSectionContributor {
    override val sectionId = "settings.ai"
    override val order = 80

    @Composable
    override fun Section(
        persisted: AppSettings,
        draft: AppSettings,
        onDraftChange: (AppSettings) -> Unit
    ) {
        AiSettingsSection(
            persisted = persisted,
            draft = draft,
            onDraftChange = onDraftChange,
            diagnosticsService = diagnosticsService
        )
    }
}
