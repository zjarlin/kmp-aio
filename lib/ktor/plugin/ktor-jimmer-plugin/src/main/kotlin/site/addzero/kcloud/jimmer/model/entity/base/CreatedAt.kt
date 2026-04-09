package site.addzero.kcloud.jimmer.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface CreatedAt {
    /**
     * 创建时间戳，单位为毫秒。
     */
    val createdAt: Long
}
