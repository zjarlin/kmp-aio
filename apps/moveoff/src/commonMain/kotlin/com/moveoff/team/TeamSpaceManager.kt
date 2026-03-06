package com.moveoff.team

import com.moveoff.db.Database
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * 团队共享空间管理器
 *
 * 管理团队共享空间的创建、成员、权限等
 */
class TeamSpaceManager(
    private val database: Database,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val _spaces = MutableStateFlow<List<SharedSpace>>(emptyList())
    val spaces: StateFlow<List<SharedSpace>> = _spaces.asStateFlow()

    private val _currentSpace = MutableStateFlow<SharedSpace?>(null)
    val currentSpace: StateFlow<SharedSpace?> = _currentSpace.asStateFlow()

    private val _invitations = MutableStateFlow<List<SpaceInvitation>>(emptyList())
    val invitations: StateFlow<List<SpaceInvitation>> = _invitations.asStateFlow()

    init {
        // 加载用户的空间列表
        scope.launch {
            loadSpaces()
        }
    }

    /**
     * 加载用户的空间列表
     */
    private suspend fun loadSpaces() {
        // TODO: 从数据库或API加载
        _spaces.value = emptyList()
    }

    /**
     * 创建新的共享空间
     */
    suspend fun createSpace(
        name: String,
        description: String = "",
        ownerId: String
    ): Result<SharedSpace> = withContext(Dispatchers.IO) {
        try {
            val space = SharedSpace(
                id = generateId(),
                name = name,
                description = description,
                ownerId = ownerId,
                members = listOf(
                    TeamMember(
                        userId = ownerId,
                        email = "",
                        name = "Owner",
                        role = TeamRole.OWNER
                    )
                )
            )

            // TODO: 保存到数据库
            _spaces.value = _spaces.value + space

            logger.info { "创建共享空间: ${space.name}" }
            Result.success(space)
        } catch (e: Exception) {
            logger.error(e) { "创建共享空间失败" }
            Result.failure(e)
        }
    }

    /**
     * 删除共享空间
     */
    suspend fun deleteSpace(spaceId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val space = _spaces.value.find { it.id == spaceId }
                ?: return@withContext Result.failure(IllegalArgumentException("空间不存在"))

            if (!space.isOwner(userId)) {
                return@withContext Result.failure(SecurityException("只有所有者可以删除空间"))
            }

            // TODO: 从数据库删除
            _spaces.value = _spaces.value.filter { it.id != spaceId }

            if (_currentSpace.value?.id == spaceId) {
                _currentSpace.value = null
            }

            logger.info { "删除共享空间: $spaceId" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "删除共享空间失败" }
            Result.failure(e)
        }
    }

    /**
     * 邀请成员加入空间
     */
    suspend fun inviteMember(
        spaceId: String,
        email: String,
        role: TeamRole,
        invitedBy: String
    ): Result<SpaceInvitation> = withContext(Dispatchers.IO) {
        try {
            val space = _spaces.value.find { it.id == spaceId }
                ?: return@withContext Result.failure(IllegalArgumentException("空间不存在"))

            if (!space.canUserManage(invitedBy)) {
                return@withContext Result.failure(SecurityException("无权限邀请成员"))
            }

            val invitation = SpaceInvitation(
                id = generateId(),
                spaceId = spaceId,
                spaceName = space.name,
                invitedBy = invitedBy,
                invitedByName = "Admin", // TODO: 获取用户名
                invitedEmail = email,
                role = role
            )

            // TODO: 发送邀请邮件
            _invitations.value = _invitations.value + invitation

            logger.info { "邀请成员: $email 到空间 ${space.name}" }
            Result.success(invitation)
        } catch (e: Exception) {
            logger.error(e) { "邀请成员失败" }
            Result.failure(e)
        }
    }

    /**
     * 接受邀请
     */
    suspend fun acceptInvitation(
        invitationId: String,
        userId: String,
        userName: String,
        userEmail: String
    ): Result<SharedSpace> = withContext(Dispatchers.IO) {
        try {
            val invitation = _invitations.value.find { it.id == invitationId }
                ?: return@withContext Result.failure(IllegalArgumentException("邀请不存在"))

            if (invitation.status != InvitationStatus.PENDING) {
                return@withContext Result.failure(IllegalStateException("邀请已处理"))
            }

            if (invitation.invitedEmail != userEmail) {
                return@withContext Result.failure(SecurityException("邀请邮箱不匹配"))
            }

            val space = _spaces.value.find { it.id == invitation.spaceId }
                ?: return@withContext Result.failure(IllegalArgumentException("空间不存在"))

            val newMember = TeamMember(
                userId = userId,
                email = userEmail,
                name = userName,
                role = invitation.role
            )

            val updatedSpace = space.copy(
                members = space.members + newMember,
                updatedAt = System.currentTimeMillis()
            )

            // 更新空间列表
            _spaces.value = _spaces.value.map {
                if (it.id == space.id) updatedSpace else it
            }

            // 更新邀请状态
            _invitations.value = _invitations.value.map {
                if (it.id == invitationId) it.copy(status = InvitationStatus.ACCEPTED) else it
            }

            logger.info { "用户 $userName 接受邀请加入 ${space.name}" }
            Result.success(updatedSpace)
        } catch (e: Exception) {
            logger.error(e) { "接受邀请失败" }
            Result.failure(e)
        }
    }

    /**
     * 移除成员
     */
    suspend fun removeMember(
        spaceId: String,
        memberId: String,
        removedBy: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val space = _spaces.value.find { it.id == spaceId }
                ?: return@withContext Result.failure(IllegalArgumentException("空间不存在"))

            if (!space.canUserManage(removedBy)) {
                return@withContext Result.failure(SecurityException("无权限移除成员"))
            }

            if (space.isOwner(memberId)) {
                return@withContext Result.failure(SecurityException("不能移除所有者"))
            }

            val updatedSpace = space.copy(
                members = space.members.filter { it.userId != memberId },
                updatedAt = System.currentTimeMillis()
            )

            _spaces.value = _spaces.value.map {
                if (it.id == spaceId) updatedSpace else it
            }

            logger.info { "从空间 ${space.name} 移除成员 $memberId" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "移除成员失败" }
            Result.failure(e)
        }
    }

    /**
     * 更改成员角色
     */
    suspend fun changeMemberRole(
        spaceId: String,
        memberId: String,
        newRole: TeamRole,
        changedBy: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val space = _spaces.value.find { it.id == spaceId }
                ?: return@withContext Result.failure(IllegalArgumentException("空间不存在"))

            if (!space.canUserManage(changedBy)) {
                return@withContext Result.failure(SecurityException("无权限更改角色"))
            }

            // 不能更改自己的角色
            if (memberId == changedBy) {
                return@withContext Result.failure(IllegalArgumentException("不能更改自己的角色"))
            }

            val updatedSpace = space.copy(
                members = space.members.map { member ->
                    if (member.userId == memberId) member.copy(role = newRole) else member
                },
                updatedAt = System.currentTimeMillis()
            )

            _spaces.value = _spaces.value.map {
                if (it.id == spaceId) updatedSpace else it
            }

            logger.info { "更改成员 $memberId 角色为 $newRole" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "更改成员角色失败" }
            Result.failure(e)
        }
    }

    /**
     * 切换当前空间
     */
    fun switchSpace(spaceId: String) {
        _currentSpace.value = _spaces.value.find { it.id == spaceId }
    }

    /**
     * 更新空间设置
     */
    suspend fun updateSettings(
        spaceId: String,
        settings: SpaceSettings,
        updatedBy: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val space = _spaces.value.find { it.id == spaceId }
                ?: return@withContext Result.failure(IllegalArgumentException("空间不存在"))

            if (!space.canUserManage(updatedBy)) {
                return@withContext Result.failure(SecurityException("无权限更新设置"))
            }

            val updatedSpace = space.copy(
                settings = settings,
                updatedAt = System.currentTimeMillis()
            )

            _spaces.value = _spaces.value.map {
                if (it.id == spaceId) updatedSpace else it
            }

            if (_currentSpace.value?.id == spaceId) {
                _currentSpace.value = updatedSpace
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateId(): String {
        return UUID.randomUUID().toString()
    }
}