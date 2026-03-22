@file:Suppress("warnings")

package site.addzero.vibepocket.jimmer.model.entity.base

import kotlin.Suppress
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.meta.ImmutableType

@DslScope
@GeneratedBy(type = BaseEntity::class)
public interface BaseEntityDraft : BaseEntity, LongIdDraft, CreatedTimeDraft, UpdateTimeDraft {
    @GeneratedBy(type = BaseEntity::class)
    public object `$` {
        public val type: ImmutableType = ImmutableType
                    .newBuilder(
                        "0.9.120",
                        BaseEntity::class,
                        listOf(
                            LongIdDraft.`$`.type,
                            CreatedTimeDraft.`$`.type,
                            UpdateTimeDraft.`$`.type
                        ),
                        null
                    )
                    .build()
    }
}
