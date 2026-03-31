package site.addzero.kcloud.plugins.system.knowledgebase.screen

import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.system.knowledgebase.KnowledgeBaseWorkbenchState

@KoinViewModel
class KnowledgeBaseSpacesViewModel(
    val state: KnowledgeBaseWorkbenchState,
) : ViewModel()
