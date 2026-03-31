package site.addzero.kcloud.plugins.system.rbac.screen

import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.system.rbac.RbacWorkbenchState

@KoinViewModel
class RbacUserViewModel(
    val state: RbacWorkbenchState,
) : ViewModel()
