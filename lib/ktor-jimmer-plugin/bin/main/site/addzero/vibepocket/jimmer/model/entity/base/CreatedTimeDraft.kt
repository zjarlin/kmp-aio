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
@GeneratedBy(type = CreatedTime::class)
public interface CreatedTimeDraft : CreatedTime, Draft {
    override var createTime: Instant

    @GeneratedBy(type = CreatedTime::class)
    public object `$` {
        public val type: ImmutableType = ImmutableType
                    .newBuilder(
                        "0.10.6",
                        CreatedTime::class,
                        listOf(

                        ),
                        null
                    )
                    .add(-1, "createTime", ImmutablePropCategory.SCALAR, Instant::class.java, false)
                    .build()
    }
}
