package site.addzero.vibepocket.model

import org.babyfish.jimmer.sql.*

/**
 * 数据源配置（存储在 SQLite 元数据库中）
 *
 * 每条记录代表一个可用的数据源连接配置。
 * owner + name 组合唯一，同一个 owner 可以有多个数据源。
 */
@Entity
@Table(name = "datasource_config")
interface DatasourceConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /** 所有者标识（用户ID、组织ID、或任意业务标识） */
    @Key
    val owner: String

    /** 数据源名称（如 "main", "analytics", "readonly"） */
    @Key
    val name: String

    /** 数据库类型: SQLITE / MYSQL / POSTGRES */
    @Column(name = "db_type")
    val dbType: String

    /** JDBC URL */
    val url: String

    /** 用户名（可选） */
    val username: String?

    /** 密码（可选） */
    val password: String?

    /** 驱动类名（可选，不填则自动推断） */
    @Column(name = "driver_class")
    val driverClass: String?

    /** 是否启用 */
    val enabled: Boolean

    /** 备注 */
    val description: String?
}
