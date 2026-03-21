@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.MusicHistory::class)

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
import org.babyfish.jimmer.sql.kt.ast.table.KRemoteRef
import org.babyfish.jimmer.sql.kt.ast.table.`impl`.KRemoteRefImplementor
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher

public val KNonNullProps<MusicHistory>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<Long>(MusicHistoryProps.ID.unwrap()) as KNonNullPropExpression<Long>

public val KNullableProps<MusicHistory>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<Long>(MusicHistoryProps.ID.unwrap()) as KNullablePropExpression<Long>

public val KNonNullProps<MusicHistory>.taskId: KNonNullPropExpression<String>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<String>(MusicHistoryProps.TASK_ID.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<MusicHistory>.taskId: KNullablePropExpression<String>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<String>(MusicHistoryProps.TASK_ID.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<MusicHistory>.type: KNonNullPropExpression<String>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<String>(MusicHistoryProps.TYPE.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<MusicHistory>.type: KNullablePropExpression<String>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<String>(MusicHistoryProps.TYPE.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<MusicHistory>.status: KNonNullPropExpression<String>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<String>(MusicHistoryProps.STATUS.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<MusicHistory>.status: KNullablePropExpression<String>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<String>(MusicHistoryProps.STATUS.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<MusicHistory>.tracksJson: KNonNullPropExpression<String>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<String>(MusicHistoryProps.TRACKS_JSON.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<MusicHistory>.tracksJson: KNullablePropExpression<String>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<String>(MusicHistoryProps.TRACKS_JSON.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<MusicHistory>.createdAt: KNonNullPropExpression<LocalDateTime>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<LocalDateTime>(MusicHistoryProps.CREATED_AT.unwrap()) as KNonNullPropExpression<LocalDateTime>

public val KNullableProps<MusicHistory>.createdAt: KNullablePropExpression<LocalDateTime>
    @GeneratedBy(type = MusicHistory::class)
    get() = get<LocalDateTime>(MusicHistoryProps.CREATED_AT.unwrap()) as KNullablePropExpression<LocalDateTime>

public val KRemoteRef.NonNull<MusicHistory>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = MusicHistory::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNonNullPropExpression<Long>

public val KRemoteRef.Nullable<MusicHistory>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = MusicHistory::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNullablePropExpression<Long>

@GeneratedBy(type = MusicHistory::class)
public fun KNonNullTable<MusicHistory>.fetchBy(block: MusicHistoryFetcherDsl.() -> Unit): Selection<MusicHistory> = fetch(newFetcher(MusicHistory::class).`by`(block))

@GeneratedBy(type = MusicHistory::class)
public fun KNullableTable<MusicHistory>.fetchBy(block: MusicHistoryFetcherDsl.() -> Unit): Selection<MusicHistory?> = fetch(newFetcher(MusicHistory::class).`by`(block))

@GeneratedBy(type = MusicHistory::class)
public object MusicHistoryProps {
    public val ID: TypedProp.Scalar<MusicHistory, Long> =
            TypedProp.scalar(MusicHistory::id.toImmutableProp())

    public val TASK_ID: TypedProp.Scalar<MusicHistory, String> =
            TypedProp.scalar(MusicHistory::taskId.toImmutableProp())

    public val TYPE: TypedProp.Scalar<MusicHistory, String> =
            TypedProp.scalar(MusicHistory::type.toImmutableProp())

    public val STATUS: TypedProp.Scalar<MusicHistory, String> =
            TypedProp.scalar(MusicHistory::status.toImmutableProp())

    public val TRACKS_JSON: TypedProp.Scalar<MusicHistory, String> =
            TypedProp.scalar(MusicHistory::tracksJson.toImmutableProp())

    public val CREATED_AT: TypedProp.Scalar<MusicHistory, LocalDateTime> =
            TypedProp.scalar(MusicHistory::createdAt.toImmutableProp())
}
