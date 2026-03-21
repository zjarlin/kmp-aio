@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.AppConfig::class)

package site.addzero.vibepocket.model

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

public val KNonNullProps<AppConfig>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = AppConfig::class)
    get() = get<Long>(AppConfigProps.ID.unwrap()) as KNonNullPropExpression<Long>

public val KNullableProps<AppConfig>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = AppConfig::class)
    get() = get<Long>(AppConfigProps.ID.unwrap()) as KNullablePropExpression<Long>

public val KNonNullProps<AppConfig>.key: KNonNullPropExpression<String>
    @GeneratedBy(type = AppConfig::class)
    get() = get<String>(AppConfigProps.KEY.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<AppConfig>.key: KNullablePropExpression<String>
    @GeneratedBy(type = AppConfig::class)
    get() = get<String>(AppConfigProps.KEY.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<AppConfig>.`value`: KNonNullPropExpression<String>
    @GeneratedBy(type = AppConfig::class)
    get() = get<String>(AppConfigProps.VALUE.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<AppConfig>.`value`: KNullablePropExpression<String>
    @GeneratedBy(type = AppConfig::class)
    get() = get<String>(AppConfigProps.VALUE.unwrap()) as KNullablePropExpression<String>

public val KProps<AppConfig>.description: KNullablePropExpression<String>
    @GeneratedBy(type = AppConfig::class)
    get() = get<String>(AppConfigProps.DESCRIPTION.unwrap()) as KNullablePropExpression<String>

public val KRemoteRef.NonNull<AppConfig>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = AppConfig::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNonNullPropExpression<Long>

public val KRemoteRef.Nullable<AppConfig>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = AppConfig::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNullablePropExpression<Long>

@GeneratedBy(type = AppConfig::class)
public fun KNonNullTable<AppConfig>.fetchBy(block: AppConfigFetcherDsl.() -> Unit): Selection<AppConfig> = fetch(newFetcher(AppConfig::class).`by`(block))

@GeneratedBy(type = AppConfig::class)
public fun KNullableTable<AppConfig>.fetchBy(block: AppConfigFetcherDsl.() -> Unit): Selection<AppConfig?> = fetch(newFetcher(AppConfig::class).`by`(block))

@GeneratedBy(type = AppConfig::class)
public object AppConfigProps {
    public val ID: TypedProp.Scalar<AppConfig, Long> =
            TypedProp.scalar(AppConfig::id.toImmutableProp())

    public val KEY: TypedProp.Scalar<AppConfig, String> =
            TypedProp.scalar(AppConfig::key.toImmutableProp())

    public val VALUE: TypedProp.Scalar<AppConfig, String> =
            TypedProp.scalar(AppConfig::`value`.toImmutableProp())

    public val DESCRIPTION: TypedProp.Scalar<AppConfig, String?> =
            TypedProp.scalar(AppConfig::description.toImmutableProp())
}
