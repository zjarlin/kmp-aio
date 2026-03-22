@file:Suppress("warnings")

package site.addzero.vibepocket.jimmer.model.entity.base

import java.time.Instant
import kotlin.Suppress
import org.babyfish.jimmer.Draft
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.meta.ImmutablePropCategory
import org.babyfish.jimmer.meta.ImmutableType

@DslScope
@GeneratedBy(type = UpdateTime::class)
public interface UpdateTimeDraft : UpdateTime, Draft {
    override var updateTime: Instant?

    @GeneratedBy(type = UpdateTime::class)
    public object `$` {
        public val type: ImmutableType = ImmutableType
                    .newBuilder(
                        "0.9.120",
                        UpdateTime::class,
                        listOf(

                        ),
                        null
                    )
                    .add(-1, "updateTime", ImmutablePropCategory.SCALAR, Instant::class.java, true)
                    .build()
    }
}
