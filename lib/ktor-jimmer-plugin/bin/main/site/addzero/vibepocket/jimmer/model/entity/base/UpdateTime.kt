package site.addzero.vibepocket.jimmer.model.entity.base

import org.babyfish.jimmer.sql.MappedSuperclass
import java.time.Instant

@MappedSuperclass
interface UpdateTime {
    /**
     * 修改时间
     */
    val updateTime: Instant?
}
