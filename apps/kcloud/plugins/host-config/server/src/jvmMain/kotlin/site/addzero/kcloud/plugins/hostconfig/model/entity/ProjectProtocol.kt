package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "host_config_project_protocol")
/**
 * 定义项目协议实体。
 */
interface ProjectProtocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键 ID。
     */
    val id: Long

    /**
     * 排序序号。
     */
    val sortIndex: Int

    /**
     * 创建时间戳。
     */
    val createdAt: Long

    /**
     * 更新时间戳。
     */
    val updatedAt: Long

    @ManyToOne
    /**
     * 项目。
     */
    val project: Project

    @ManyToOne
    /**
     * 协议。
     */
    val protocol: ProtocolInstance
}
