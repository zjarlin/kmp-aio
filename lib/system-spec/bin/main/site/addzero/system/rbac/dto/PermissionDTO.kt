package site.addzero.system.rbac.dto

import java.time.Instant

/**
 * 权限数据传输对象
 */
data class PermissionDTO(
    val id: String,
    val code: String,
    val name: String,
    val resourceType: ResourceType,
    val resourceId: String?,
    val action: ActionType,
    val description: String?,
    val status: PermissionStatus,
    val createdAt: Instant?,
    val updatedAt: Instant?
)

enum class ResourceType {
    MENU,      // 菜单
    BUTTON,    // 按钮
    API,       // 接口
    DATA,      // 数据
    FILE       // 文件
}

enum class ActionType {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    EXECUTE,
    ALL
}

enum class PermissionStatus {
    ENABLED,
    DISABLED
}

data class PermissionCreateRequest(
    val code: String,
    val name: String,
    val resourceType: ResourceType,
    val resourceId: String? = null,
    val action: ActionType = ActionType.ALL,
    val description: String? = null
)

data class PermissionUpdateRequest(
    val name: String? = null,
    val action: ActionType? = null,
    val description: String? = null,
    val status: PermissionStatus? = null
)

data class PermissionQuery(
    override val pageNum: Int = 1,
    override val pageSize: Int = 10,
    val keyword: String? = null,
    val resourceType: ResourceType? = null,
    val status: PermissionStatus? = null
) : site.addzero.system.common.dto.PageQuery(pageNum, pageSize)
