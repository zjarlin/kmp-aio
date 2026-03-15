package com.kcloud.plugins.settings

import androidx.compose.runtime.Composable
import com.kcloud.model.AppSettings
import org.koin.core.annotation.Single

interface SettingsSectionContributor {
    val sectionId: String
    val order: Int get() = 100

    @Composable
    fun Section(
        persisted: AppSettings,
        draft: AppSettings,
        onDraftChange: (AppSettings) -> Unit
    )
}

@Single
class SettingsSectionRegistry(
    contributors: List<SettingsSectionContributor>
) {
    val contributors: List<SettingsSectionContributor> = contributors.sortedBy { it.order }
}
