package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.mcuconsole.client.McuConsoleWorkbenchState

@KoinViewModel
class McuControlViewModel(
    val state: McuConsoleWorkbenchState,
) : ViewModel() {
    var followLatestLogs by mutableStateOf(true)
}

@KoinViewModel
class McuDebugViewModel(
    val state: McuConsoleWorkbenchState,
) : ViewModel()

@KoinViewModel
class McuFlashViewModel(
    val state: McuConsoleWorkbenchState,
) : ViewModel()

@KoinViewModel
class McuModbusViewModel(
    val state: McuConsoleWorkbenchState,
) : ViewModel()

@KoinViewModel
class McuOnlineDevViewModel(
    val state: McuConsoleWorkbenchState,
) : ViewModel()
