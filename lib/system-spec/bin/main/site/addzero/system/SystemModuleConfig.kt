package site.addzero.system

import site.addzero.system.audit.feature.InMemoryAuditLogService
import site.addzero.system.audit.spi.AuditLogSpi
import site.addzero.system.config.feature.InMemoryConfigService
import site.addzero.system.config.spi.SystemConfigSpi
import site.addzero.system.fileupload.feature.InMemoryFileRecordService
import site.addzero.system.fileupload.feature.LocalFileStorageService
import site.addzero.system.fileupload.spi.FileRecordSpi
import site.addzero.system.fileupload.spi.FileStorageSpi
import site.addzero.system.notification.feature.InMemoryNotificationService
import site.addzero.system.notification.feature.InMemoryTemplateService
import site.addzero.system.notification.spi.MessageTemplateSpi
import site.addzero.system.notification.spi.NotificationSpi
import site.addzero.system.rbac.feature.*
import site.addzero.system.rbac.spi.PermissionCheckSpi
import site.addzero.system.rbac.spi.PermissionSpi
import site.addzero.system.rbac.spi.RoleSpi
import site.addzero.system.rbac.spi.UserRoleSpi

/**
 * 系统模块配置类
 * 提供默认实现的快速初始化
 *
 * 使用示例：
 * ```kotlin
 * val config = SystemModuleConfig.createDefault()
 * val roleSpi = config.roleSpi
 * val permissionSpi = config.permissionSpi
 * ```
 */
class SystemModuleConfig private constructor(
    val roleSpi: RoleSpi,
    val permissionSpi: PermissionSpi,
    val userRoleSpi: UserRoleSpi,
    val permissionCheckSpi: PermissionCheckSpi,
    val fileStorageSpi: FileStorageSpi,
    val fileRecordSpi: FileRecordSpi,
    val auditLogSpi: AuditLogSpi,
    val configSpi: SystemConfigSpi,
    val notificationSpi: NotificationSpi,
    val messageTemplateSpi: MessageTemplateSpi
) {
    companion object {

        /**
         * 创建默认的内存实现配置
         * 适用于开发测试环境
         */
        fun createDefault(storagePath: String = "/tmp/system-uploads"): SystemModuleConfig {
            // RBAC
            val roleService = InMemoryRoleService()
            val permissionService = InMemoryPermissionService()
            val userRoleService = InMemoryUserRoleService(roleService)
            val permissionCheckService = DefaultPermissionCheckService(
                userRoleService, roleService, permissionService
            )

            // File Upload
            val fileStorageService = LocalFileStorageService(storagePath)
            val fileRecordService = InMemoryFileRecordService()

            // Audit
            val auditLogService = InMemoryAuditLogService()

            // Config
            val configService = InMemoryConfigService()

            // Notification
            val notificationService = InMemoryNotificationService()
            val templateService = InMemoryTemplateService()

            return SystemModuleConfig(
                roleSpi = roleService,
                permissionSpi = permissionService,
                userRoleSpi = userRoleService,
                permissionCheckSpi = permissionCheckService,
                fileStorageSpi = fileStorageService,
                fileRecordSpi = fileRecordService,
                auditLogSpi = auditLogService,
                configSpi = configService,
                notificationSpi = notificationService,
                messageTemplateSpi = templateService
            )
        }
    }

    /**
     * 构建器模式，用于自定义SPI实现
     */
    class Builder {
        var roleSpi: RoleSpi? = null
        var permissionSpi: PermissionSpi? = null
        var userRoleSpi: UserRoleSpi? = null
        var permissionCheckSpi: PermissionCheckSpi? = null
        var fileStorageSpi: FileStorageSpi? = null
        var fileRecordSpi: FileRecordSpi? = null
        var auditLogSpi: AuditLogSpi? = null
        var configSpi: SystemConfigSpi? = null
        var notificationSpi: NotificationSpi? = null
        var messageTemplateSpi: MessageTemplateSpi? = null

        fun build(): SystemModuleConfig {
            val default = createDefault()

            return SystemModuleConfig(
                roleSpi = roleSpi ?: default.roleSpi,
                permissionSpi = permissionSpi ?: default.permissionSpi,
                userRoleSpi = userRoleSpi ?: default.userRoleSpi,
                permissionCheckSpi = permissionCheckSpi ?: default.permissionCheckSpi,
                fileStorageSpi = fileStorageSpi ?: default.fileStorageSpi,
                fileRecordSpi = fileRecordSpi ?: default.fileRecordSpi,
                auditLogSpi = auditLogSpi ?: default.auditLogSpi,
                configSpi = configSpi ?: default.configSpi,
                notificationSpi = notificationSpi ?: default.notificationSpi,
                messageTemplateSpi = messageTemplateSpi ?: default.messageTemplateSpi
            )
        }
    }
}

/**
 * DSL风格的构建器
 */
fun systemModule(block: SystemModuleConfig.Builder.() -> Unit): SystemModuleConfig {
    return SystemModuleConfig.Builder().apply(block).build()
}
