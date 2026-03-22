package com.kcloud.features.ai

import androidx.compose.runtime.Composable
import com.kcloud.model.AppSettings
import com.kcloud.features.ai.spi.AiDiagnosticsService
import com.kcloud.features.ai.ui.AiSettingsSection
import com.kcloud.features.settings.SettingsSectionContributor
import org.koin.core.annotation.Single

@Single
class AiSettingsSectionContributor(
    private val diagnosticsService: AiDiagnosticsService,
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
