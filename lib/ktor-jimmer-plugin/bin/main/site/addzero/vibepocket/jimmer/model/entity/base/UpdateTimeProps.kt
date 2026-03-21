@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.jimmer.model.entity.base.UpdateTime::class)

package site.addzero.vibepocket.jimmer.model.entity.base

import java.time.Instant
import kotlin.Suppress
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.toImmutableProp
import org.babyfish.jimmer.meta.TypedProp
import org.babyfish.jimmer.sql.kt.ast.expression.KNullablePropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KProps

public val KProps<UpdateTime>.updateTime: KNullablePropExpression<Instant>
    @GeneratedBy(type = UpdateTime::class)
    get() = get<Instant>(UpdateTimeProps.UPDATE_TIME.unwrap()) as KNullablePropExpression<Instant>

@GeneratedBy(type = UpdateTime::class)
public object UpdateTimeProps {
    public val UPDATE_TIME: TypedProp.Scalar<UpdateTime, Instant?> =
            TypedProp.scalar(UpdateTime::updateTime.toImmutableProp())
}
