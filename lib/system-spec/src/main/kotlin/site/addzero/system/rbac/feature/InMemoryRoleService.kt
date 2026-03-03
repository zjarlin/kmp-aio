package site.addzero.system.rbac.feature

import site.addzero.system.common.dto.PageResult
import site.addzero.system.common.exception.DuplicateResourceException
import site.addzero.system.common.exception.ResourceNotFoundException
import site.addzero.system.rbac.dto.*
import site.addzero.system.rbac.spi.RoleSpi
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于内存的角色服务默认实现
 * 适用于开发测试环境，生产环境应替换为持久化实现
 */
open class InMemoryRoleService : RoleSpi {

    protected val roleStore = ConcurrentHashMap<String, RoleDTO>()
    protected val rolePermissions = ConcurrentHashMap<String, MutableSet<String>>()
    protected val idGenerator = java.util.concurrent.atomic.AtomicLong(1)

    override fun create(request: RoleCreateRequest): RoleDTO {
        if (roleStore.values.any { it.code == request.code }) {
            throw DuplicateResourceException("Role", "code")
        }

        val role = RoleDTO(
            id = idGenerator.getAndIncrement().toString(),
            code = request.code,
            name = request.name,
            description = request.description,
            status = RoleStatus.ENABLED,
            sortOrder = request.sortOrder,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        roleStore[role.id] = role
        rolePermissions[role.id] = mutableSetOf()
        return role
    }

    override fun update(roleId: String, request: RoleUpdateRequest): RoleDTO {
        val existing = roleStore[roleId]
            ?: throw ResourceNotFoundException("Role", roleId)

        val updated = existing.copy(
            name = request.name ?: existing.name,
            description = request.description ?: existing.description,
            status = request.status ?: existing.status,
            sortOrder = request.sortOrder ?: existing.sortOrder,
            updatedAt = Instant.now()
        )

        roleStore[roleId] = updated
        return updated
    }

    override fun delete(roleId: String) {
        roleStore.remove(roleId) ?: throw ResourceNotFoundException("Role", roleId)
        rolePermissions.remove(roleId)
    }

    override fun getById(roleId: String): RoleDTO? = roleStore[roleId]

    override fun getByCode(code: String): RoleDTO? =
        roleStore.values.find { it.code == code }

    override fun page(query: RoleQuery): PageResult<RoleDTO> {
        val filtered = roleStore.values.filter { role ->
            (query.keyword == null ||
                    role.name.contains(query.keyword, ignoreCase = true) ||
                    role.code.contains(query.keyword, ignoreCase = true)) &&
                    (query.status == null || role.status == query.status)
        }.sortedBy { it.sortOrder }

        val total = filtered.size.toLong()
        val offset = query.offset().toInt()
        val limit = query.limit()
        val list = filtered.drop(offset).take(limit)

        return PageResult(list, total, query.pageNum, query.pageSize)
    }

    override fun listAll(): List<RoleDTO> =
        roleStore.values.sortedBy { it.sortOrder }

    override fun assignPermissions(roleId: String, permissionIds: Set<String>) {
        if (!roleStore.containsKey(roleId)) {
            throw ResourceNotFoundException("Role", roleId)
        }
        rolePermissions[roleId] = permissionIds.toMutableSet()
    }

    override fun getPermissionIds(roleId: String): Set<String> {
        if (!roleStore.containsKey(roleId)) {
            throw ResourceNotFoundException("Role", roleId)
        }
        return rolePermissions[roleId]?.toSet() ?: emptySet()
    }
}
