@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.jimmer.model.entity.base.BaseEntity::class)

package site.addzero.vibepocket.jimmer.model.entity.base

import java.time.Instant
import kotlin.Long
import kotlin.Suppress
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.toImmutableProp
import org.babyfish.jimmer.meta.TypedProp
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNullablePropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullProps
import org.babyfish.jimmer.sql.kt.ast.table.KNullableProps
import org.babyfish.jimmer.sql.kt.ast.table.KProps

public val KNonNullProps<BaseEntity>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = BaseEntity::class)
    get() = get<Long>(BaseEntityProps.ID.unwrap()) as KNonNullPropExpression<Long>

public val KNullableProps<BaseEntity>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = BaseEntity::class)
    get() = get<Long>(BaseEntityProps.ID.unwrap()) as KNullablePropExpression<Long>

public val KNonNullProps<BaseEntity>.createTime: KNonNullPropExpression<Instant>
    @GeneratedBy(type = BaseEntity::class)
    get() = get<Instant>(BaseEntityProps.CREATE_TIME.unwrap()) as KNonNullPropExpression<Instant>

public val KNullableProps<BaseEntity>.createTime: KNullablePropExpression<Instant>
    @GeneratedBy(type = BaseEntity::class)
    get() = get<Instant>(BaseEntityProps.CREATE_TIME.unwrap()) as KNullablePropExpression<Instant>

public val KProps<BaseEntity>.updateTime: KNullablePropExpression<Instant>
    @GeneratedBy(type = BaseEntity::class)
    get() = get<Instant>(BaseEntityProps.UPDATE_TIME.unwrap()) as KNullablePropExpression<Instant>

@GeneratedBy(type = BaseEntity::class)
public object BaseEntityProps {
    public val ID: TypedProp.Scalar<BaseEntity, Long> =
            TypedProp.scalar(BaseEntity::id.toImmutableProp())

    public val CREATE_TIME: TypedProp.Scalar<BaseEntity, Instant> =
            TypedProp.scalar(BaseEntity::createTime.toImmutableProp())

    public val UPDATE_TIME: TypedProp.Scalar<BaseEntity, Instant?> =
            TypedProp.scalar(BaseEntity::updateTime.toImmutableProp())
}
