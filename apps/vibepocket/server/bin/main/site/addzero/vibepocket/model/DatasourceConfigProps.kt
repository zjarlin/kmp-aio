@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.DatasourceConfig::class)

package site.addzero.vibepocket.model

import kotlin.Boolean
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

public val KNonNullProps<DatasourceConfig>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<Long>(DatasourceConfigProps.ID.unwrap()) as KNonNullPropExpression<Long>

public val KNullableProps<DatasourceConfig>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<Long>(DatasourceConfigProps.ID.unwrap()) as KNullablePropExpression<Long>

public val KNonNullProps<DatasourceConfig>.owner: KNonNullPropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.OWNER.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<DatasourceConfig>.owner: KNullablePropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.OWNER.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<DatasourceConfig>.name: KNonNullPropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.NAME.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<DatasourceConfig>.name: KNullablePropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.NAME.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<DatasourceConfig>.dbType: KNonNullPropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.DB_TYPE.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<DatasourceConfig>.dbType: KNullablePropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.DB_TYPE.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<DatasourceConfig>.url: KNonNullPropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.URL.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<DatasourceConfig>.url: KNullablePropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.URL.unwrap()) as KNullablePropExpression<String>

public val KProps<DatasourceConfig>.username: KNullablePropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.USERNAME.unwrap()) as KNullablePropExpression<String>

public val KProps<DatasourceConfig>.password: KNullablePropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.PASSWORD.unwrap()) as KNullablePropExpression<String>

public val KProps<DatasourceConfig>.driverClass: KNullablePropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.DRIVER_CLASS.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<DatasourceConfig>.enabled: KNonNullPropExpression<Boolean>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<Boolean>(DatasourceConfigProps.ENABLED.unwrap()) as KNonNullPropExpression<Boolean>

public val KNullableProps<DatasourceConfig>.enabled: KNullablePropExpression<Boolean>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<Boolean>(DatasourceConfigProps.ENABLED.unwrap()) as KNullablePropExpression<Boolean>

public val KProps<DatasourceConfig>.description: KNullablePropExpression<String>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = get<String>(DatasourceConfigProps.DESCRIPTION.unwrap()) as KNullablePropExpression<String>

public val KRemoteRef.NonNull<DatasourceConfig>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNonNullPropExpression<Long>

public val KRemoteRef.Nullable<DatasourceConfig>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = DatasourceConfig::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNullablePropExpression<Long>

@GeneratedBy(type = DatasourceConfig::class)
public fun KNonNullTable<DatasourceConfig>.fetchBy(block: DatasourceConfigFetcherDsl.() -> Unit): Selection<DatasourceConfig> = fetch(newFetcher(DatasourceConfig::class).`by`(block))

@GeneratedBy(type = DatasourceConfig::class)
public fun KNullableTable<DatasourceConfig>.fetchBy(block: DatasourceConfigFetcherDsl.() -> Unit): Selection<DatasourceConfig?> = fetch(newFetcher(DatasourceConfig::class).`by`(block))

@GeneratedBy(type = DatasourceConfig::class)
public object DatasourceConfigProps {
    public val ID: TypedProp.Scalar<DatasourceConfig, Long> =
            TypedProp.scalar(DatasourceConfig::id.toImmutableProp())

    public val OWNER: TypedProp.Scalar<DatasourceConfig, String> =
            TypedProp.scalar(DatasourceConfig::owner.toImmutableProp())

    public val NAME: TypedProp.Scalar<DatasourceConfig, String> =
            TypedProp.scalar(DatasourceConfig::name.toImmutableProp())

    public val DB_TYPE: TypedProp.Scalar<DatasourceConfig, String> =
            TypedProp.scalar(DatasourceConfig::dbType.toImmutableProp())

    public val URL: TypedProp.Scalar<DatasourceConfig, String> =
            TypedProp.scalar(DatasourceConfig::url.toImmutableProp())

    public val USERNAME: TypedProp.Scalar<DatasourceConfig, String?> =
            TypedProp.scalar(DatasourceConfig::username.toImmutableProp())

    public val PASSWORD: TypedProp.Scalar<DatasourceConfig, String?> =
            TypedProp.scalar(DatasourceConfig::password.toImmutableProp())

    public val DRIVER_CLASS: TypedProp.Scalar<DatasourceConfig, String?> =
            TypedProp.scalar(DatasourceConfig::driverClass.toImmutableProp())

    public val ENABLED: TypedProp.Scalar<DatasourceConfig, Boolean> =
            TypedProp.scalar(DatasourceConfig::enabled.toImmutableProp())

    public val DESCRIPTION: TypedProp.Scalar<DatasourceConfig, String?> =
            TypedProp.scalar(DatasourceConfig::description.toImmutableProp())
}
