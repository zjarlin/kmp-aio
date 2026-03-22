@file:Suppress("warnings")

package site.addzero.vibepocket.jimmer.model.entity.base

import kotlin.Long
import kotlin.Suppress
import org.babyfish.jimmer.Draft
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.meta.ImmutableType

@DslScope
@GeneratedBy(type = LongId::class)
public interface LongIdDraft : LongId, Draft {
    override var id: Long

    @GeneratedBy(type = LongId::class)
    public object `$` {
        public val type: ImmutableType = ImmutableType
                    .newBuilder(
                        "0.9.120",
                        LongId::class,
                        listOf(

                        ),
                        null
                    )
                    .id(-1, "id", Long::class.java)
                    .build()
    }
}
