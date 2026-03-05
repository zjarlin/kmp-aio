package site.addzero.vibepocket.model

import org.babyfish.jimmer.sql.*

/**
 * 应用配置（键值对存储）
 */
@Entity
@Table(name = "app_config")
interface AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    /** 配置键，唯一 */
    @Key
    val key: String

    /** 配置值 */
    val value: String

    /** 描述 */
    val description: String?
}
