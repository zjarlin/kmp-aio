package site.addzero.system.rbac.feature

import site.addzero.system.common.dto.PageResult
import site.addzero.system.common.exception.DuplicateResourceException
import site.addzero.system.common.exception.ResourceNotFoundException
import site.addzero.system.rbac.dto.*
import site.addzero.system.rbac.spi.PermissionSpi
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于内存的权限服务默认实现
 */
open class InMemoryPermissionService : PermissionSpi {

    protected val permissionStore = ConcurrentHashMap<String, PermissionDTO>()
    protected val idGenerator = java.util.concurrent.atomic.AtomicLong(1)

    override fun create(request: PermissionCreateRequest): PermissionDTO {
        if (permissionStore.values.any { it.code == request.code }) {
            throw DuplicateResourceException("Permission", "code")
        }

        val permission = PermissionDTO(
            id = idGenerator.getAndIncrement().toString(),
            code = request.code,
            name = request.name,
            resourceType = request.resourceType,
            resourceId = request.resourceId,
            action = request.action,
            description = request.description,
            status = PermissionStatus.ENABLED,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        permissionStore[permission.id] = permission
        return permission
    }

    override fun update(permissionId: String, request: PermissionUpdateRequest): PermissionDTO {
        val existing = permissionStore[permissionId]
            ?: throw ResourceNotFoundException("Permission", permissionId)

        val updated = existing.copy(
            name = request.name ?: existing.name,
            action = request.action ?: existing.action,
            description = request.description ?: existing.description,
            status = request.status ?: existing.status,
            updatedAt = Instant.now()
        )

        permissionStore[permissionId] = updated
        return updated
    }

    override fun delete(permissionId: String) {
        permissionStore.remove(permissionId)
            ?: throw ResourceNotFoundException("Permission", permissionId)
    }

    override fun getById(permissionId: String): PermissionDTO? = permissionStore[permissionId]

    override fun getByCode(code: String): PermissionDTO? =
        permissionStore.values.find { it.code == code }

    override fun page(query: PermissionQuery): PageResult<PermissionDTO> {
        val filtered = permissionStore.values.filter { perm ->
            (query.keyword == null ||
                    perm.name.contains(query.keyword, ignoreCase = true) ||
                    perm.code.contains(query.keyword, ignoreCase = true)) &&
                    (query.resourceType == null || perm.resourceType == query.resourceType) &&
                    (query.status == null || perm.status == query.status)
        }

        val total = filtered.size.toLong()
        val offset = query.offset().toInt()
        val limit = query.limit()
        val list = filtered.drop(offset).take(limit)

        return PageResult(list, total, query.pageNum, query.pageSize)
    }

    override fun listAll(): List<PermissionDTO> =
        permissionStore.values.toList()

    override fun listByResourceType(resourceType: String): List<PermissionDTO> =
        permissionStore.values.filter { it.resourceType.name == resourceType }

    override fun existsByCode(code: String): Boolean =
        permissionStore.values.any { it.code == code }
}
