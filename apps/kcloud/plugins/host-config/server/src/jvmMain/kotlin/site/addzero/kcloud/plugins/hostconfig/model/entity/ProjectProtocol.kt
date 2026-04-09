package site.addzero.kcloud.plugins.hostconfig.model.entity

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import site.addzero.kcloud.jimmer.model.entity.base.EpochBaseEntity

@Entity
@Table(name = "host_config_project_protocol")
/**
 * 定义项目协议实体。
 */
interface ProjectProtocol : EpochBaseEntity {

    /**
     * 排序序号。
     */
    val sortIndex: Int

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
