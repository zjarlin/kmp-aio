package site.addzero.vibepocket.jimmer.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface BaseEntity : LongId, CreatedTime, UpdateTime {
}
