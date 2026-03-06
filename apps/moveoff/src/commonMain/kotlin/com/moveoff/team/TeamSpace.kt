package com.moveoff.team

import kotlinx.serialization.Serializable

/**
 * 团队成员角色
 */
@Serializable
enum class TeamRole {
    OWNER,      // 所有者：完全控制
    ADMIN,      // 管理员：管理成员和设置
    EDITOR,     // 编辑者：读写文件
    VIEWER      // 查看者：只读访问
}

/**
 * 团队成员
 */
@Serializable
data class TeamMember(
    val userId: String,
    val email: String,
    val name: String,
    val role: TeamRole,
    val joinedAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long? = null,
    val avatarUrl: String? = null
)

/**
 * 共享空间
 */
@Serializable
data class SharedSpace(
    val id: String,
    val name: String,
    val description: String = "",
    val ownerId: String,
    val storageQuota: Long = 10L * 1024 * 1024 * 1024, // 默认10GB
    val storageUsed: Long = 0,
    val members: List<TeamMember> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val settings: SpaceSettings = SpaceSettings()
) {
    fun getUsedPercentage(): Float {
        return if (storageQuota > 0) storageUsed.toFloat() / storageQuota else 0f
    }

    fun canUserEdit(userId: String): Boolean {
        val member = members.find { it.userId == userId } ?: return false
        return member.role in listOf(TeamRole.OWNER, TeamRole.ADMIN, TeamRole.EDITOR)
    }

    fun canUserManage(userId: String): Boolean {
        val member = members.find { it.userId == userId } ?: return false
        return member.role in listOf(TeamRole.OWNER, TeamRole.ADMIN)
    }

    fun isOwner(userId: String): Boolean {
        return members.find { it.userId == userId }?.role == TeamRole.OWNER
    }
}

/**
 * 空间设置
 */
@Serializable
data class SpaceSettings(
    val allowPublicLinks: Boolean = true,
    val requireApprovalForJoin: Boolean = false,
    val autoSyncEnabled: Boolean = true,
    val conflictResolution: TeamConflictResolution = TeamConflictResolution.LAST_MODIFIED_WINS,
    val retentionDays: Int = 90, // 版本保留天数
    val notificationsEnabled: Boolean = true
)

/**
 * 团队冲突解决策略
 */
@Serializable
enum class TeamConflictResolution {
    LAST_MODIFIED_WINS,
    MANUAL_RESOLUTION,
    BRANCH_AND_MERGE
}

/**
 * 共享链接
 */
@Serializable
data class ShareLink(
    val id: String,
    val spaceId: String,
    val path: String,
    val token: String,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val password: String? = null,
    val permissions: SharePermissions = SharePermissions()
)

/**
 * 共享权限
 */
@Serializable
data class SharePermissions(
    val canRead: Boolean = true,
    val canWrite: Boolean = false,
    val canDelete: Boolean = false,
    val canShare: Boolean = false
)

/**
 * 空间邀请
 */
@Serializable
data class SpaceInvitation(
    val id: String,
    val spaceId: String,
    val spaceName: String,
    val invitedBy: String,
    val invitedByName: String,
    val invitedEmail: String,
    val role: TeamRole,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000 // 7天过期
)

@Serializable
enum class InvitationStatus {
    PENDING, ACCEPTED, DECLINED, EXPIRED
}

/**
 * 活动日志条目
 */
@Serializable
data class ActivityLogEntry(
    val id: String,
    val spaceId: String,
    val userId: String,
    val userName: String,
    val action: ActivityAction,
    val targetPath: String? = null,
    val targetUserId: String? = null,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val ipAddress: String? = null
)

@Serializable
enum class ActivityAction {
    FILE_CREATED,
    FILE_UPDATED,
    FILE_DELETED,
    FILE_MOVED,
    FILE_SHARED,
    MEMBER_JOINED,
    MEMBER_LEFT,
    MEMBER_ROLE_CHANGED,
    SPACE_CREATED,
    SPACE_UPDATED,
    SPACE_DELETED,
    LINK_CREATED,
    LINK_REVOKED,
    SYNC_STARTED,
    SYNC_COMPLETED,
    CONFLICT_RESOLVED
}

/**
 * 同步策略（按网络类型）
 */
@Serializable
data class NetworkSyncPolicy(
    val networkType: NetworkType,
    val autoSyncEnabled: Boolean,
    val syncDirection: SyncDirection,
    val bandwidthLimit: Long? = null, // bytes/s
    val largeFileThreshold: Long = 100 * 1024 * 1024, // 100MB
    val largeFileBehavior: LargeFileBehavior = LargeFileBehavior.ASK
)

@Serializable
enum class NetworkType {
    WIFI,
    ETHERNET,
    MOBILE,
    METERED,
    UNKNOWN
}

@Serializable
enum class SyncDirection {
    BOTH,
    UPLOAD_ONLY,
    DOWNLOAD_ONLY
}

@Serializable
enum class LargeFileBehavior {
    ASK,        // 询问用户
    SKIP,       // 跳过
    SYNC        // 同步
}

/**
 * 智能同步设置
 */
@Serializable
data class SmartSyncSettings(
    val policies: Map<NetworkType, NetworkSyncPolicy> = defaultPolicies(),
    val scheduleEnabled: Boolean = false,
    val scheduleStartHour: Int = 2, // 凌晨2点开始
    val scheduleEndHour: Int = 6,   // 早上6点结束
    val batteryThreshold: Int = 20, // 电量低于20%暂停同步
    val pauseOnBatterySaver: Boolean = true
) {
    companion object {
        fun defaultPolicies(): Map<NetworkType, NetworkSyncPolicy> {
            return mapOf(
                NetworkType.WIFI to NetworkSyncPolicy(
                    networkType = NetworkType.WIFI,
                    autoSyncEnabled = true,
                    syncDirection = SyncDirection.BOTH
                ),
                NetworkType.ETHERNET to NetworkSyncPolicy(
                    networkType = NetworkType.ETHERNET,
                    autoSyncEnabled = true,
                    syncDirection = SyncDirection.BOTH
                ),
                NetworkType.MOBILE to NetworkSyncPolicy(
                    networkType = NetworkType.MOBILE,
                    autoSyncEnabled = false,
                    syncDirection = SyncDirection.DOWNLOAD_ONLY,
                    bandwidthLimit = 1024 * 1024 // 1MB/s
                ),
                NetworkType.METERED to NetworkSyncPolicy(
                    networkType = NetworkType.METERED,
                    autoSyncEnabled = false,
                    syncDirection = SyncDirection.DOWNLOAD_ONLY
                )
            )
        }
    }
}

/**
 * 审计日志查询参数
 */
data class AuditLogQuery(
    val spaceId: String? = null,
    val userId: String? = null,
    val actions: List<ActivityAction>? = null,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val page: Int = 1,
    val pageSize: Int = 50
)

/**
 * 审计日志查询结果
 */
data class AuditLogResult(
    val entries: List<ActivityLogEntry>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)