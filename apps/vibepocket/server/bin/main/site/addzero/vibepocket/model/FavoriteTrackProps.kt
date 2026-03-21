@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.FavoriteTrack::class)

package site.addzero.vibepocket.model

import java.time.LocalDateTime
import kotlin.Double
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

public val KNonNullProps<FavoriteTrack>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<Long>(FavoriteTrackProps.ID.unwrap()) as KNonNullPropExpression<Long>

public val KNullableProps<FavoriteTrack>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<Long>(FavoriteTrackProps.ID.unwrap()) as KNullablePropExpression<Long>

public val KNonNullProps<FavoriteTrack>.trackId: KNonNullPropExpression<String>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<String>(FavoriteTrackProps.TRACK_ID.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<FavoriteTrack>.trackId: KNullablePropExpression<String>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<String>(FavoriteTrackProps.TRACK_ID.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<FavoriteTrack>.taskId: KNonNullPropExpression<String>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<String>(FavoriteTrackProps.TASK_ID.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<FavoriteTrack>.taskId: KNullablePropExpression<String>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<String>(FavoriteTrackProps.TASK_ID.unwrap()) as KNullablePropExpression<String>

public val KProps<FavoriteTrack>.audioUrl: KNullablePropExpression<String>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<String>(FavoriteTrackProps.AUDIO_URL.unwrap()) as KNullablePropExpression<String>

public val KProps<FavoriteTrack>.title: KNullablePropExpression<String>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<String>(FavoriteTrackProps.TITLE.unwrap()) as KNullablePropExpression<String>

public val KProps<FavoriteTrack>.tags: KNullablePropExpression<String>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<String>(FavoriteTrackProps.TAGS.unwrap()) as KNullablePropExpression<String>

public val KProps<FavoriteTrack>.imageUrl: KNullablePropExpression<String>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<String>(FavoriteTrackProps.IMAGE_URL.unwrap()) as KNullablePropExpression<String>

public val KProps<FavoriteTrack>.duration: KNullablePropExpression<Double>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<Double>(FavoriteTrackProps.DURATION.unwrap()) as KNullablePropExpression<Double>

public val KNonNullProps<FavoriteTrack>.createdAt: KNonNullPropExpression<LocalDateTime>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<LocalDateTime>(FavoriteTrackProps.CREATED_AT.unwrap()) as KNonNullPropExpression<LocalDateTime>

public val KNullableProps<FavoriteTrack>.createdAt: KNullablePropExpression<LocalDateTime>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = get<LocalDateTime>(FavoriteTrackProps.CREATED_AT.unwrap()) as KNullablePropExpression<LocalDateTime>

public val KRemoteRef.NonNull<FavoriteTrack>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNonNullPropExpression<Long>

public val KRemoteRef.Nullable<FavoriteTrack>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = FavoriteTrack::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNullablePropExpression<Long>

@GeneratedBy(type = FavoriteTrack::class)
public fun KNonNullTable<FavoriteTrack>.fetchBy(block: FavoriteTrackFetcherDsl.() -> Unit): Selection<FavoriteTrack> = fetch(newFetcher(FavoriteTrack::class).`by`(block))

@GeneratedBy(type = FavoriteTrack::class)
public fun KNullableTable<FavoriteTrack>.fetchBy(block: FavoriteTrackFetcherDsl.() -> Unit): Selection<FavoriteTrack?> = fetch(newFetcher(FavoriteTrack::class).`by`(block))

@GeneratedBy(type = FavoriteTrack::class)
public object FavoriteTrackProps {
    public val ID: TypedProp.Scalar<FavoriteTrack, Long> =
            TypedProp.scalar(FavoriteTrack::id.toImmutableProp())

    public val TRACK_ID: TypedProp.Scalar<FavoriteTrack, String> =
            TypedProp.scalar(FavoriteTrack::trackId.toImmutableProp())

    public val TASK_ID: TypedProp.Scalar<FavoriteTrack, String> =
            TypedProp.scalar(FavoriteTrack::taskId.toImmutableProp())

    public val AUDIO_URL: TypedProp.Scalar<FavoriteTrack, String?> =
            TypedProp.scalar(FavoriteTrack::audioUrl.toImmutableProp())

    public val TITLE: TypedProp.Scalar<FavoriteTrack, String?> =
            TypedProp.scalar(FavoriteTrack::title.toImmutableProp())

    public val TAGS: TypedProp.Scalar<FavoriteTrack, String?> =
            TypedProp.scalar(FavoriteTrack::tags.toImmutableProp())

    public val IMAGE_URL: TypedProp.Scalar<FavoriteTrack, String?> =
            TypedProp.scalar(FavoriteTrack::imageUrl.toImmutableProp())

    public val DURATION: TypedProp.Scalar<FavoriteTrack, Double?> =
            TypedProp.scalar(FavoriteTrack::duration.toImmutableProp())

    public val CREATED_AT: TypedProp.Scalar<FavoriteTrack, LocalDateTime> =
            TypedProp.scalar(FavoriteTrack::createdAt.toImmutableProp())
}
