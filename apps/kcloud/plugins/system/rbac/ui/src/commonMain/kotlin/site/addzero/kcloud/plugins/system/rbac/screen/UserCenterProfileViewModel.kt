package site.addzero.kcloud.plugins.system.rbac.screen

import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel
import site.addzero.kcloud.plugins.system.rbac.UserCenterWorkbenchState

@KoinViewModel
class UserCenterProfileViewModel(
    val state: UserCenterWorkbenchState,
) : ViewModel()
