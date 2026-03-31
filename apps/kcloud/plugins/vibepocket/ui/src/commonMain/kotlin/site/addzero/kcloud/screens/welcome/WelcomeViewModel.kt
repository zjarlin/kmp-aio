package site.addzero.kcloud.screens.welcome

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.music.SunoRuntimeConfig
import site.addzero.kcloud.music.persistSunoRuntimeConfig

enum class WelcomeStep {
    INTRO,
    CONFIG,
}

data class WelcomeScreenState(
    val step: WelcomeStep = WelcomeStep.INTRO,
    val runtimeConfig: SunoRuntimeConfig = SunoRuntimeConfig(),
)

@KoinViewModel
class WelcomeViewModel : ViewModel() {
    private val screenScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var state by mutableStateOf(WelcomeScreenState())
        private set

    fun advance() {
        state = state.copy(step = WelcomeStep.CONFIG)
    }

    fun updateSunoToken(value: String) {
        state = state.copy(
            runtimeConfig = state.runtimeConfig.copy(apiToken = value),
        )
    }

    fun updateSunoBaseUrl(value: String) {
        state = state.copy(
            runtimeConfig = state.runtimeConfig.copy(baseUrl = value),
        )
    }

    fun completeSetup(
        onSetupComplete: (token: String, baseUrl: String) -> Unit,
    ) {
        val runtimeConfig = state.runtimeConfig
        screenScope.launch {
            runCatching {
                persistSunoRuntimeConfig(
                    apiToken = runtimeConfig.apiToken,
                    baseUrl = runtimeConfig.baseUrl,
                    callbackUrl = runtimeConfig.callbackUrl,
                )
            }
            onSetupComplete(runtimeConfig.apiToken, runtimeConfig.baseUrl)
        }
    }

    override fun onCleared() {
        super.onCleared()
        screenScope.cancel()
    }
}
