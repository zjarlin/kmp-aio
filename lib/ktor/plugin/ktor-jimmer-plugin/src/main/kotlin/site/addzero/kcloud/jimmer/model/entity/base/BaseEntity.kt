package site.addzero.kcloud.jimmer.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
/**
 * 统一实体基类。
 */
interface BaseEntity : LongId, CreatedAt, UpdatedAt {
}
