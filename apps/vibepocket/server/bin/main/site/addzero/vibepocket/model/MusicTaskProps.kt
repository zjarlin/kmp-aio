@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.MusicTask::class)

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

public val KNonNullProps<MusicTask>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = MusicTask::class)
    get() = get<Long>(MusicTaskProps.ID.unwrap()) as KNonNullPropExpression<Long>

public val KNullableProps<MusicTask>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = MusicTask::class)
    get() = get<Long>(MusicTaskProps.ID.unwrap()) as KNullablePropExpression<Long>

public val KNonNullProps<MusicTask>.taskId: KNonNullPropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.TASK_ID.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<MusicTask>.taskId: KNullablePropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.TASK_ID.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<MusicTask>.status: KNonNullPropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.STATUS.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<MusicTask>.status: KNullablePropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.STATUS.unwrap()) as KNullablePropExpression<String>

public val KProps<MusicTask>.title: KNullablePropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.TITLE.unwrap()) as KNullablePropExpression<String>

public val KProps<MusicTask>.tags: KNullablePropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.TAGS.unwrap()) as KNullablePropExpression<String>

public val KProps<MusicTask>.prompt: KNullablePropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.PROMPT.unwrap()) as KNullablePropExpression<String>

public val KProps<MusicTask>.mv: KNullablePropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.MV.unwrap()) as KNullablePropExpression<String>

public val KProps<MusicTask>.audioUrl: KNullablePropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.AUDIO_URL.unwrap()) as KNullablePropExpression<String>

public val KProps<MusicTask>.videoUrl: KNullablePropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.VIDEO_URL.unwrap()) as KNullablePropExpression<String>

public val KProps<MusicTask>.errorMessage: KNullablePropExpression<String>
    @GeneratedBy(type = MusicTask::class)
    get() = get<String>(MusicTaskProps.ERROR_MESSAGE.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<MusicTask>.createdAt: KNonNullPropExpression<LocalDateTime>
    @GeneratedBy(type = MusicTask::class)
    get() = get<LocalDateTime>(MusicTaskProps.CREATED_AT.unwrap()) as KNonNullPropExpression<LocalDateTime>

public val KNullableProps<MusicTask>.createdAt: KNullablePropExpression<LocalDateTime>
    @GeneratedBy(type = MusicTask::class)
    get() = get<LocalDateTime>(MusicTaskProps.CREATED_AT.unwrap()) as KNullablePropExpression<LocalDateTime>

public val KNonNullProps<MusicTask>.updatedAt: KNonNullPropExpression<LocalDateTime>
    @GeneratedBy(type = MusicTask::class)
    get() = get<LocalDateTime>(MusicTaskProps.UPDATED_AT.unwrap()) as KNonNullPropExpression<LocalDateTime>

public val KNullableProps<MusicTask>.updatedAt: KNullablePropExpression<LocalDateTime>
    @GeneratedBy(type = MusicTask::class)
    get() = get<LocalDateTime>(MusicTaskProps.UPDATED_AT.unwrap()) as KNullablePropExpression<LocalDateTime>

public val KRemoteRef.NonNull<MusicTask>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = MusicTask::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNonNullPropExpression<Long>

public val KRemoteRef.Nullable<MusicTask>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = MusicTask::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNullablePropExpression<Long>

@GeneratedBy(type = MusicTask::class)
public fun KNonNullTable<MusicTask>.fetchBy(block: MusicTaskFetcherDsl.() -> Unit): Selection<MusicTask> = fetch(newFetcher(MusicTask::class).`by`(block))

@GeneratedBy(type = MusicTask::class)
public fun KNullableTable<MusicTask>.fetchBy(block: MusicTaskFetcherDsl.() -> Unit): Selection<MusicTask?> = fetch(newFetcher(MusicTask::class).`by`(block))

@GeneratedBy(type = MusicTask::class)
public object MusicTaskProps {
    public val ID: TypedProp.Scalar<MusicTask, Long> =
            TypedProp.scalar(MusicTask::id.toImmutableProp())

    public val TASK_ID: TypedProp.Scalar<MusicTask, String> =
            TypedProp.scalar(MusicTask::taskId.toImmutableProp())

    public val STATUS: TypedProp.Scalar<MusicTask, String> =
            TypedProp.scalar(MusicTask::status.toImmutableProp())

    public val TITLE: TypedProp.Scalar<MusicTask, String?> =
            TypedProp.scalar(MusicTask::title.toImmutableProp())

    public val TAGS: TypedProp.Scalar<MusicTask, String?> =
            TypedProp.scalar(MusicTask::tags.toImmutableProp())

    public val PROMPT: TypedProp.Scalar<MusicTask, String?> =
            TypedProp.scalar(MusicTask::prompt.toImmutableProp())

    public val MV: TypedProp.Scalar<MusicTask, String?> =
            TypedProp.scalar(MusicTask::mv.toImmutableProp())

    public val AUDIO_URL: TypedProp.Scalar<MusicTask, String?> =
            TypedProp.scalar(MusicTask::audioUrl.toImmutableProp())

    public val VIDEO_URL: TypedProp.Scalar<MusicTask, String?> =
            TypedProp.scalar(MusicTask::videoUrl.toImmutableProp())

    public val ERROR_MESSAGE: TypedProp.Scalar<MusicTask, String?> =
            TypedProp.scalar(MusicTask::errorMessage.toImmutableProp())

    public val CREATED_AT: TypedProp.Scalar<MusicTask, LocalDateTime> =
            TypedProp.scalar(MusicTask::createdAt.toImmutableProp())

    public val UPDATED_AT: TypedProp.Scalar<MusicTask, LocalDateTime> =
            TypedProp.scalar(MusicTask::updatedAt.toImmutableProp())
}
