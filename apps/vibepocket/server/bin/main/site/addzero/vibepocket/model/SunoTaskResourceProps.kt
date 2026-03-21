@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.SunoTaskResource::class)

package site.addzero.vibepocket.model

import java.time.LocalDateTime
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.toImmutableProp
import org.babyfish.jimmer.meta.TypedProp
import org.babyfish.jimmer.sql.ast.Selection
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullPropExpression
import org.babyfish.jimmer.sql.kt.ast.expression.KNullablePropExpression
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullProps
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.babyfish.jimmer.sql.kt.ast.table.KNullableProps
import org.babyfish.jimmer.sql.kt.ast.table.KNullableTable
import org.babyfish.jimmer.sql.kt.ast.table.KProps
import org.babyfish.jimmer.sql.kt.ast.table.KRemoteRef
import org.babyfish.jimmer.sql.kt.ast.table.`impl`.KRemoteRefImplementor
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher

public val KNonNullProps<SunoTaskResource>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<Long>(SunoTaskResourceProps.ID.unwrap()) as KNonNullPropExpression<Long>

public val KNullableProps<SunoTaskResource>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<Long>(SunoTaskResourceProps.ID.unwrap()) as KNullablePropExpression<Long>

public val KNonNullProps<SunoTaskResource>.taskId: KNonNullPropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.TASK_ID.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<SunoTaskResource>.taskId: KNullablePropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.TASK_ID.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<SunoTaskResource>.type: KNonNullPropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.TYPE.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<SunoTaskResource>.type: KNullablePropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.TYPE.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<SunoTaskResource>.status: KNonNullPropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.STATUS.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<SunoTaskResource>.status: KNullablePropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.STATUS.unwrap()) as KNullablePropExpression<String>

public val KProps<SunoTaskResource>.requestJson: KNullablePropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.REQUEST_JSON.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<SunoTaskResource>.tracksJson: KNonNullPropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.TRACKS_JSON.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<SunoTaskResource>.tracksJson: KNullablePropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.TRACKS_JSON.unwrap()) as KNullablePropExpression<String>

public val KProps<SunoTaskResource>.detailJson: KNullablePropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.DETAIL_JSON.unwrap()) as KNullablePropExpression<String>

public val KProps<SunoTaskResource>.errorMessage: KNullablePropExpression<String>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<String>(SunoTaskResourceProps.ERROR_MESSAGE.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<SunoTaskResource>.createdAt: KNonNullPropExpression<LocalDateTime>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<LocalDateTime>(SunoTaskResourceProps.CREATED_AT.unwrap()) as KNonNullPropExpression<LocalDateTime>

public val KNullableProps<SunoTaskResource>.createdAt: KNullablePropExpression<LocalDateTime>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<LocalDateTime>(SunoTaskResourceProps.CREATED_AT.unwrap()) as KNullablePropExpression<LocalDateTime>

public val KNonNullProps<SunoTaskResource>.updatedAt: KNonNullPropExpression<LocalDateTime>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<LocalDateTime>(SunoTaskResourceProps.UPDATED_AT.unwrap()) as KNonNullPropExpression<LocalDateTime>

public val KNullableProps<SunoTaskResource>.updatedAt: KNullablePropExpression<LocalDateTime>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = get<LocalDateTime>(SunoTaskResourceProps.UPDATED_AT.unwrap()) as KNullablePropExpression<LocalDateTime>

public val KRemoteRef.NonNull<SunoTaskResource>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNonNullPropExpression<Long>

public val KRemoteRef.Nullable<SunoTaskResource>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = SunoTaskResource::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNullablePropExpression<Long>

@GeneratedBy(type = SunoTaskResource::class)
public fun KNonNullTable<SunoTaskResource>.fetchBy(block: SunoTaskResourceFetcherDsl.() -> Unit): Selection<SunoTaskResource> = fetch(newFetcher(SunoTaskResource::class).`by`(block))

@GeneratedBy(type = SunoTaskResource::class)
public fun KNullableTable<SunoTaskResource>.fetchBy(block: SunoTaskResourceFetcherDsl.() -> Unit): Selection<SunoTaskResource?> = fetch(newFetcher(SunoTaskResource::class).`by`(block))

@GeneratedBy(type = SunoTaskResource::class)
public object SunoTaskResourceProps {
    public val ID: TypedProp.Scalar<SunoTaskResource, Long> =
            TypedProp.scalar(SunoTaskResource::id.toImmutableProp())

    public val TASK_ID: TypedProp.Scalar<SunoTaskResource, String> =
            TypedProp.scalar(SunoTaskResource::taskId.toImmutableProp())

    public val TYPE: TypedProp.Scalar<SunoTaskResource, String> =
            TypedProp.scalar(SunoTaskResource::type.toImmutableProp())

    public val STATUS: TypedProp.Scalar<SunoTaskResource, String> =
            TypedProp.scalar(SunoTaskResource::status.toImmutableProp())

    public val REQUEST_JSON: TypedProp.Scalar<SunoTaskResource, String?> =
            TypedProp.scalar(SunoTaskResource::requestJson.toImmutableProp())

    public val TRACKS_JSON: TypedProp.Scalar<SunoTaskResource, String> =
            TypedProp.scalar(SunoTaskResource::tracksJson.toImmutableProp())

    public val DETAIL_JSON: TypedProp.Scalar<SunoTaskResource, String?> =
            TypedProp.scalar(SunoTaskResource::detailJson.toImmutableProp())

    public val ERROR_MESSAGE: TypedProp.Scalar<SunoTaskResource, String?> =
            TypedProp.scalar(SunoTaskResource::errorMessage.toImmutableProp())

    public val CREATED_AT: TypedProp.Scalar<SunoTaskResource, LocalDateTime> =
            TypedProp.scalar(SunoTaskResource::createdAt.toImmutableProp())

    public val UPDATED_AT: TypedProp.Scalar<SunoTaskResource, LocalDateTime> =
            TypedProp.scalar(SunoTaskResource::updatedAt.toImmutableProp())
}
