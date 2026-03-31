package site.addzero.kcloud.plugins.system.configcenter.screen

import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.system.configcenter.ConfigCenterWorkbenchState

@KoinViewModel
class ConfigCenterProjectsViewModel(
    val state: ConfigCenterWorkbenchState,
) : ViewModel()
