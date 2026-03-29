package site.addzero.kcloud.plugins.rbac

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.system.api.UserProfileDto
import site.addzero.kcloud.system.api.UserProfileUpdateRequest

@Single
class UserCenterWorkbenchState(
    private val remoteService: UserCenterRemoteService,
) {
    var isBusy by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    var profile by mutableStateOf<UserProfileDto?>(null)
        private set

    var displayName by mutableStateOf("")
    var email by mutableStateOf("")
    var avatarLabel by mutableStateOf("")
    var locale by mutableStateOf("zh-CN")
    var timeZone by mutableStateOf("Asia/Shanghai")

    private var loaded = false

    suspend fun ensureLoaded() {
        if (loaded) {
            return
        }
        refresh()
        loaded = true
    }

    suspend fun refresh() {
        runBusy(successMessage = "已刷新用户资料") {
            applyProfile(remoteService.readCurrentProfile())
        }
    }

    suspend fun save() {
        require(displayName.isNotBlank()) {
            "显示名称不能为空"
        }
        runBusy(successMessage = "已保存用户资料") {
            val saved = remoteService.saveCurrentProfile(
                UserProfileUpdateRequest(
                    displayName = displayName.trim(),
                    email = email.trim().ifBlank { null },
                    avatarLabel = avatarLabel.trim(),
                    locale = locale.trim().ifBlank { "zh-CN" },
                    timeZone = timeZone.trim().ifBlank { "Asia/Shanghai" },
                ),
            )
            applyProfile(saved)
        }
    }

    private fun applyProfile(
        value: UserProfileDto,
    ) {
        profile = value
        displayName = value.displayName
        email = value.email.orEmpty()
        avatarLabel = value.avatarLabel
        locale = value.locale
        timeZone = value.timeZone
    }

    private suspend fun runBusy(
        successMessage: String,
        block: suspend () -> Unit,
    ) {
        isBusy = true
        runCatching {
            block()
        }.onSuccess {
            message = successMessage
        }.onFailure { throwable ->
            message = throwable.message ?: "操作失败"
        }
        isBusy = false
    }
}
