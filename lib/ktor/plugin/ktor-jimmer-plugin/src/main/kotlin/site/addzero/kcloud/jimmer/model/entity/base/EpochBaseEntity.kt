package site.addzero.kcloud.jimmer.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
/**
 * 统一使用毫秒时间戳的实体基类。
 */
interface EpochBaseEntity : LongId, CreatedAt, UpdatedAt {
}
