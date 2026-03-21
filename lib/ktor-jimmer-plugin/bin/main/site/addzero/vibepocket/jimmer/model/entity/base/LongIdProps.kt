@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.jimmer.model.entity.base.LongId::class)

package site.addzero.vibepocket.jimmer.model.entity.base

import kotlin.Long
import kotlin.Suppress
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.toImmutableProp
import org.babyfish.jimmer.meta.TypedProp
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNullablePropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullProps
import org.babyfish.jimmer.sql.kt.ast.table.KNullableProps

public val KNonNullProps<LongId>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = LongId::class)
    get() = get<Long>(LongIdProps.ID.unwrap()) as KNonNullPropExpression<Long>

public val KNullableProps<LongId>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = LongId::class)
    get() = get<Long>(LongIdProps.ID.unwrap()) as KNullablePropExpression<Long>

@GeneratedBy(type = LongId::class)
public object LongIdProps {
    public val ID: TypedProp.Scalar<LongId, Long> = TypedProp.scalar(LongId::id.toImmutableProp())
}
