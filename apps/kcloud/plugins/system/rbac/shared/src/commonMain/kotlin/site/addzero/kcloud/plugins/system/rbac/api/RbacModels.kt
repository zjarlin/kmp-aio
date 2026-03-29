package site.addzero.kcloud.plugins.system.rbac.api

import kotlinx.serialization.Serializable

@Serializable
data class RbacRoleDto(
    val id: Long,
    val roleKey: String,
    val roleCode: String,
    val name: String,
    val description: String? = null,
    val builtIn: Boolean = false,
    val enabled: Boolean = true,
    val createTimeMillis: Long,
    val updateTimeMillis: Long? = null,
)

@Serializable
data class RbacRoleMutationRequest(
    val roleCode: String,
    val name: String,
    val description: String? = null,
    val enabled: Boolean = true,
)

@Serializable
data class RbacDeleteResult(
    val ok: Boolean = true,
)
