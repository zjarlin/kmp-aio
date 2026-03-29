package site.addzero.kcloud.plugins.rbac

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.annotation.Single
import site.addzero.kcloud.system.api.RbacRoleDto
import site.addzero.kcloud.system.api.RbacRoleMutationRequest

@Single
class RbacWorkbenchState(
    private val remoteService: RbacRemoteService,
) {
    val roles = mutableStateListOf<RbacRoleDto>()

    var selectedRoleId by mutableStateOf<Long?>(null)
        private set

    var roleCode by mutableStateOf("")
    var roleName by mutableStateOf("")
    var roleDescription by mutableStateOf("")
    var roleEnabled by mutableStateOf(true)
    var statusMessage by mutableStateOf("")
        private set
    var isBusy by mutableStateOf(false)
        private set

    private var loaded = false

    val selectedRole: RbacRoleDto?
        get() = roles.firstOrNull { role -> role.id == selectedRoleId }

    suspend fun ensureLoaded() {
        if (loaded) {
            return
        }
        refreshRoles()
        loaded = true
    }

    suspend fun refreshRoles() {
        runBusy("已刷新角色列表") {
            loadRoles(selectedRoleId)
        }
    }

    fun beginCreateRole() {
        selectedRoleId = null
        roleCode = ""
        roleName = ""
        roleDescription = ""
        roleEnabled = true
    }

    fun selectRole(
        roleId: Long,
    ) {
        val role = roles.firstOrNull { value -> value.id == roleId } ?: return
        selectedRoleId = role.id
        roleCode = role.roleCode
        roleName = role.name
        roleDescription = role.description.orEmpty()
        roleEnabled = role.enabled
    }

    suspend fun saveRole() {
        require(roleCode.isNotBlank()) { "角色编码不能为空" }
        require(roleName.isNotBlank()) { "角色名称不能为空" }
        val currentRoleId = selectedRoleId
        if (currentRoleId == null) {
            runBusy("已创建角色") {
                val created = remoteService.createRole(currentRequest())
                upsertRole(created)
                selectRole(created.id)
            }
            return
        }
        runBusy("已保存角色") {
            val updated = remoteService.updateRole(currentRoleId, currentRequest())
            upsertRole(updated)
            selectRole(updated.id)
        }
    }

    suspend fun deleteSelectedRole() {
        val role = selectedRole ?: return
        runBusy("已删除角色") {
            remoteService.deleteRole(role.id)
            loadRoles(preferredRoleId = null)
        }
    }

    private suspend fun loadRoles(
        preferredRoleId: Long?,
    ) {
        val loadedRoles = remoteService.listRoles()
        roles.replaceAll(loadedRoles)
        val nextRoleId = preferredRoleId?.takeIf { currentRoleId ->
            loadedRoles.any { role -> role.id == currentRoleId }
        } ?: loadedRoles.firstOrNull()?.id
        if (nextRoleId == null) {
            beginCreateRole()
        } else {
            selectRole(nextRoleId)
        }
    }

    private fun currentRequest(): RbacRoleMutationRequest {
        return RbacRoleMutationRequest(
            roleCode = roleCode.trim(),
            name = roleName.trim(),
            description = roleDescription.trim().ifBlank { null },
            enabled = roleEnabled,
        )
    }

    private fun upsertRole(
        role: RbacRoleDto,
    ) {
        val index = roles.indexOfFirst { value -> value.id == role.id }
        if (index >= 0) {
            roles[index] = role
        } else {
            roles.add(0, role)
        }
    }

    private suspend fun runBusy(
        successMessage: String,
        block: suspend () -> Unit,
    ) {
        isBusy = true
        runCatching {
            block()
        }.onSuccess {
            statusMessage = successMessage
        }.onFailure { throwable ->
            statusMessage = throwable.message ?: "操作失败"
        }
        isBusy = false
    }
}

private fun <T> MutableList<T>.replaceAll(
    newItems: List<T>,
) {
    clear()
    addAll(newItems)
}
