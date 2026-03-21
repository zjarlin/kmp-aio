package site.addzero.system.rbac.dto

import java.time.Instant

/**
 * 角色数据传输对象
 */
data class RoleDTO(
    val id: String,
    val code: String,
    val name: String,
    val description: String?,
    val status: RoleStatus,
    val sortOrder: Int,
    val createdAt: Instant?,
    val updatedAt: Instant?
)

enum class RoleStatus {
    ENABLED,
    DISABLED
}

data class RoleCreateRequest(
    val code: String,
    val name: String,
    val description: String? = null,
    val sortOrder: Int = 0
)

data class RoleUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val status: RoleStatus? = null,
    val sortOrder: Int? = null
)

data class RoleQuery(
    override val pageNum: Int = 1,
    override val pageSize: Int = 10,
    val keyword: String? = null,
    val status: RoleStatus? = null
) : site.addzero.system.common.dto.PageQuery(pageNum, pageSize)
