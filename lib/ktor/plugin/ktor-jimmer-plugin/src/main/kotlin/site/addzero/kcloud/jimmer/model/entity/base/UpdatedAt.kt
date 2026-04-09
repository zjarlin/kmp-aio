package site.addzero.kcloud.jimmer.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface UpdatedAt {
    /**
     * 更新时间戳，单位为毫秒。
     */
    val updatedAt: Long
}
