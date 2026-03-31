package site.addzero.kcloud.plugins.system.pluginmarket.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.system.pluginmarket.PluginMarketWorkbenchState

@KoinViewModel
class PluginMarketPackagesViewModel(
    val state: PluginMarketWorkbenchState,
) : ViewModel() {
    var selectedBottomTab by mutableStateOf(0)
}
