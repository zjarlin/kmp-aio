package site.addzero.kcloud.jimmer.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
/**
 * 兼容旧命名的统一实体基类。
 */
interface BaseEntity : EpochBaseEntity {
}
