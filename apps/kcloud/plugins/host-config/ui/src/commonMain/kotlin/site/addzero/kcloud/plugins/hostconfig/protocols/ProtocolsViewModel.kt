package site.addzero.kcloud.plugins.hostconfig.protocols

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.hostconfig.api.external.generated.TemplateApi

@KoinViewModel
/**
 * 管理协议界面的状态与交互逻辑。
 *
 * @property templateApi 模板API。
 */
class ProtocolsViewModel(
    private val templateApi: TemplateApi,
) : ViewModel() {
    var screenState by mutableStateOf(ProtocolsScreenState())
        private set

    init {
        refresh()
    }

    /**
     * 刷新当前界面数据。
     */
    fun refresh() {
        viewModelScope.launch {
            screenState = screenState.copy(
                loading = true,
                errorMessage = null,
            )
            runCatching {
                val protocols = templateApi.listProtocolTemplates()
                val selectedId = protocols.firstOrNull()?.id
                val modules = selectedId?.let { templateApi.listModuleTemplates(it) }.orEmpty()
                screenState = ProtocolsScreenState(
                    loading = false,
                    protocolTemplates = protocols,
                    selectedProtocolTemplateId = selectedId,
                    moduleTemplates = modules,
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    loading = false,
                    errorMessage = throwable.message ?: "加载协议模板失败",
                )
            }
        }
    }

    /**
     * 选择协议模板。
     *
     * @param protocolTemplateId 协议模板 ID。
     */
    fun selectProtocolTemplate(
        protocolTemplateId: Long,
    ) {
        viewModelScope.launch {
            runCatching {
                val modules = templateApi.listModuleTemplates(protocolTemplateId)
                screenState = screenState.copy(
                    selectedProtocolTemplateId = protocolTemplateId,
                    moduleTemplates = modules,
                )
            }.onFailure { throwable ->
                screenState = screenState.copy(
                    errorMessage = throwable.message ?: "读取模块模板失败",
                )
            }
        }
    }
}
