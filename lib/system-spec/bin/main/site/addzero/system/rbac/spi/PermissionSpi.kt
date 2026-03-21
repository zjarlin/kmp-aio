package site.addzero.system.rbac.spi

import site.addzero.system.common.dto.PageQuery
import site.addzero.system.common.dto.PageResult
import site.addzero.system.rbac.dto.PermissionDTO
import site.addzero.system.rbac.dto.PermissionCreateRequest
import site.addzero.system.rbac.dto.PermissionUpdateRequest
import site.addzero.system.rbac.dto.PermissionQuery

/**
 * 权限服务SPI
 * 提供权限的增删改查功能
 */
interface PermissionSpi {
    /**
     * 创建权限
     */
    fun create(request: PermissionCreateRequest): PermissionDTO

    /**
     * 更新权限
     */
    fun update(permissionId: String, request: PermissionUpdateRequest): PermissionDTO

    /**
     * 删除权限
     */
    fun delete(permissionId: String)

    /**
     * 根据ID查询权限
     */
    fun getById(permissionId: String): PermissionDTO?

    /**
     * 根据权限编码查询
     */
    fun getByCode(code: String): PermissionDTO?

    /**
     * 分页查询权限
     */
    fun page(query: PermissionQuery): PageResult<PermissionDTO>

    /**
     * 查询全部权限
     */
    fun listAll(): List<PermissionDTO>

    /**
     * 根据资源类型查询权限
     */
    fun listByResourceType(resourceType: String): List<PermissionDTO>

    /**
     * 检查权限编码是否存在
     */
    fun existsByCode(code: String): Boolean
}
