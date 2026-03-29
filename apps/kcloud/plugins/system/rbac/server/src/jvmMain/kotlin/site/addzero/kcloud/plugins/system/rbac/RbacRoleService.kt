package site.addzero.kcloud.plugins.system.rbac

import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.core.annotation.Single
import site.addzero.kcloud.plugins.system.rbac.model.RbacRole
import site.addzero.kcloud.plugins.system.rbac.model.by
import site.addzero.kcloud.plugins.system.rbac.api.RbacDeleteResult
import site.addzero.kcloud.plugins.system.rbac.api.RbacRoleDto
import site.addzero.kcloud.plugins.system.rbac.api.RbacRoleMutationRequest
import java.util.UUID

@Single
class RbacRoleService(
    private val sqlClient: KSqlClient,
) {
    fun listRoles(): List<RbacRoleDto> {
        ensureBootstrapRoles()
        return allRoleEntities()
            .sortedWith(
                compareByDescending<RbacRole> { it.builtIn }
                    .thenByDescending { it.enabled }
                    .thenBy { it.roleCode },
            )
            .map { it.toDto() }
    }

    fun createRole(
        request: RbacRoleMutationRequest,
    ): RbacRoleDto {
        ensureBootstrapRoles()
        val normalizedCode = normalizeRoleCode(request.roleCode)
        require(request.name.isNotBlank()) { "角色名称不能为空" }
        require(allRoleEntities().none { it.roleCode == normalizedCode }) { "角色编码已存在: $normalizedCode" }
        val saved = sqlClient.save(
            new(RbacRole::class).by {
                roleKey = UUID.randomUUID().toString()
                roleCode = normalizedCode
                name = request.name.trim()
                description = request.description?.trim()?.ifBlank { null }
                builtIn = false
                enabled = request.enabled
            },
        ).modifiedEntity
        return saved.toDto()
    }

    fun updateRole(
        roleId: Long,
        request: RbacRoleMutationRequest,
    ): RbacRoleDto {
        ensureBootstrapRoles()
        require(request.name.isNotBlank()) { "角色名称不能为空" }
        val existing = roleOrThrow(roleId)
        val normalizedCode = normalizeRoleCode(request.roleCode)
        if (existing.builtIn) {
            require(normalizedCode == existing.roleCode) { "内建角色编码不可修改" }
        }
        require(
            allRoleEntities().none { role ->
                role.id != roleId && role.roleCode == normalizedCode
            },
        ) { "角色编码已存在: $normalizedCode" }
        val saved = sqlClient.save(
            new(RbacRole::class).by {
                id = existing.id
                roleKey = existing.roleKey
                roleCode = if (existing.builtIn) {
                    existing.roleCode
                } else {
                    normalizedCode
                }
                name = request.name.trim()
                description = request.description?.trim()?.ifBlank { null }
                builtIn = existing.builtIn
                enabled = request.enabled
                createTime = existing.createTime
            },
        ).modifiedEntity
        return saved.toDto()
    }

    fun deleteRole(
        roleId: Long,
    ): RbacDeleteResult {
        ensureBootstrapRoles()
        val existing = roleOrThrow(roleId)
        require(!existing.builtIn) { "内建角色不允许删除" }
        sqlClient.deleteById(RbacRole::class, roleId)
        return RbacDeleteResult(ok = true)
    }

    private fun ensureBootstrapRoles() {
        val existingCodes = allRoleEntities()
            .map { it.roleCode }
            .toSet()
        defaultRoles
            .filter { definition -> definition.roleCode !in existingCodes }
            .forEach { definition ->
                sqlClient.save(
                    new(RbacRole::class).by {
                        roleKey = definition.roleKey
                        roleCode = definition.roleCode
                        name = definition.name
                        description = definition.description
                        builtIn = true
                        enabled = true
                    },
                )
            }
    }

    private fun allRoleEntities(): List<RbacRole> {
        return sqlClient.createQuery(RbacRole::class) {
            select(table)
        }.execute()
    }

    private fun roleOrThrow(
        roleId: Long,
    ): RbacRole {
        return sqlClient.findById(RbacRole::class, roleId)
            ?: throw NoSuchElementException("角色不存在: $roleId")
    }
}

private fun RbacRole.toDto(): RbacRoleDto {
    return RbacRoleDto(
        id = id,
        roleKey = roleKey,
        roleCode = roleCode,
        name = name,
        description = description,
        builtIn = builtIn,
        enabled = enabled,
        createTimeMillis = createTime.toEpochMilli(),
        updateTimeMillis = updateTime?.toEpochMilli(),
    )
}

private fun normalizeRoleCode(
    roleCode: String,
): String {
    val normalized = roleCode.trim()
        .uppercase()
        .replace(Regex("[\\s-]+"), "_")
        .replace(Regex("[^A-Z0-9_]"), "")
        .replace(Regex("_+"), "_")
        .trim('_')
    require(normalized.isNotBlank()) { "角色编码不能为空" }
    return normalized
}

private data class BootstrapRoleDefinition(
    val roleKey: String,
    val roleCode: String,
    val name: String,
    val description: String,
)

private val defaultRoles = listOf(
    BootstrapRoleDefinition(
        roleKey = "system.super-admin",
        roleCode = "SUPER_ADMIN",
        name = "超级管理员",
        description = "保留全部系统能力，作为系统初始化与兜底管理角色。",
    ),
    BootstrapRoleDefinition(
        roleKey = "system.ops",
        roleCode = "OPS",
        name = "运维",
        description = "偏向部署、巡检、配置与运行态处理的默认角色。",
    ),
    BootstrapRoleDefinition(
        roleKey = "system.read-only",
        roleCode = "READ_ONLY",
        name = "只读访客",
        description = "只读查看系统内容，不承担修改与发布动作。",
    ),
)
