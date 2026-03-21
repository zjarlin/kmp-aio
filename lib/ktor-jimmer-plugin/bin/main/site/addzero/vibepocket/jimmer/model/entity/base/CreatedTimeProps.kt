@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.jimmer.model.entity.base.CreatedTime::class)

package site.addzero.vibepocket.jimmer.model.entity.base

import java.time.Instant
import kotlin.Suppress
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.toImmutableProp
import org.babyfish.jimmer.meta.TypedProp
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNullablePropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullProps
import org.babyfish.jimmer.sql.kt.ast.table.KNullableProps

public val KNonNullProps<CreatedTime>.createTime: KNonNullPropExpression<Instant>
    @GeneratedBy(type = CreatedTime::class)
    get() = get<Instant>(CreatedTimeProps.CREATE_TIME.unwrap()) as KNonNullPropExpression<Instant>

public val KNullableProps<CreatedTime>.createTime: KNullablePropExpression<Instant>
    @GeneratedBy(type = CreatedTime::class)
    get() = get<Instant>(CreatedTimeProps.CREATE_TIME.unwrap()) as KNullablePropExpression<Instant>

@GeneratedBy(type = CreatedTime::class)
public object CreatedTimeProps {
    public val CREATE_TIME: TypedProp.Scalar<CreatedTime, Instant> =
            TypedProp.scalar(CreatedTime::createTime.toImmutableProp())
}
