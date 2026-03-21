@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.PersonaRecord::class)

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

public val KNonNullProps<PersonaRecord>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = PersonaRecord::class)
    get() = get<Long>(PersonaRecordProps.ID.unwrap()) as KNonNullPropExpression<Long>

public val KNullableProps<PersonaRecord>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = PersonaRecord::class)
    get() = get<Long>(PersonaRecordProps.ID.unwrap()) as KNullablePropExpression<Long>

public val KNonNullProps<PersonaRecord>.personaId: KNonNullPropExpression<String>
    @GeneratedBy(type = PersonaRecord::class)
    get() = get<String>(PersonaRecordProps.PERSONA_ID.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<PersonaRecord>.personaId: KNullablePropExpression<String>
    @GeneratedBy(type = PersonaRecord::class)
    get() = get<String>(PersonaRecordProps.PERSONA_ID.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<PersonaRecord>.name: KNonNullPropExpression<String>
    @GeneratedBy(type = PersonaRecord::class)
    get() = get<String>(PersonaRecordProps.NAME.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<PersonaRecord>.name: KNullablePropExpression<String>
    @GeneratedBy(type = PersonaRecord::class)
    get() = get<String>(PersonaRecordProps.NAME.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<PersonaRecord>.description: KNonNullPropExpression<String>
    @GeneratedBy(type = PersonaRecord::class)
    get() = get<String>(PersonaRecordProps.DESCRIPTION.unwrap()) as KNonNullPropExpression<String>

public val KNullableProps<PersonaRecord>.description: KNullablePropExpression<String>
    @GeneratedBy(type = PersonaRecord::class)
    get() = get<String>(PersonaRecordProps.DESCRIPTION.unwrap()) as KNullablePropExpression<String>

public val KNonNullProps<PersonaRecord>.createdAt: KNonNullPropExpression<LocalDateTime>
    @GeneratedBy(type = PersonaRecord::class)
    get() = get<LocalDateTime>(PersonaRecordProps.CREATED_AT.unwrap()) as KNonNullPropExpression<LocalDateTime>

public val KNullableProps<PersonaRecord>.createdAt: KNullablePropExpression<LocalDateTime>
    @GeneratedBy(type = PersonaRecord::class)
    get() = get<LocalDateTime>(PersonaRecordProps.CREATED_AT.unwrap()) as KNullablePropExpression<LocalDateTime>

public val KRemoteRef.NonNull<PersonaRecord>.id: KNonNullPropExpression<Long>
    @GeneratedBy(type = PersonaRecord::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNonNullPropExpression<Long>

public val KRemoteRef.Nullable<PersonaRecord>.id: KNullablePropExpression<Long>
    @GeneratedBy(type = PersonaRecord::class)
    get() = (this as KRemoteRefImplementor<*>).id<Long>() as KNullablePropExpression<Long>

@GeneratedBy(type = PersonaRecord::class)
public fun KNonNullTable<PersonaRecord>.fetchBy(block: PersonaRecordFetcherDsl.() -> Unit): Selection<PersonaRecord> = fetch(newFetcher(PersonaRecord::class).`by`(block))

@GeneratedBy(type = PersonaRecord::class)
public fun KNullableTable<PersonaRecord>.fetchBy(block: PersonaRecordFetcherDsl.() -> Unit): Selection<PersonaRecord?> = fetch(newFetcher(PersonaRecord::class).`by`(block))

@GeneratedBy(type = PersonaRecord::class)
public object PersonaRecordProps {
    public val ID: TypedProp.Scalar<PersonaRecord, Long> =
            TypedProp.scalar(PersonaRecord::id.toImmutableProp())

    public val PERSONA_ID: TypedProp.Scalar<PersonaRecord, String> =
            TypedProp.scalar(PersonaRecord::personaId.toImmutableProp())

    public val NAME: TypedProp.Scalar<PersonaRecord, String> =
            TypedProp.scalar(PersonaRecord::name.toImmutableProp())

    public val DESCRIPTION: TypedProp.Scalar<PersonaRecord, String> =
            TypedProp.scalar(PersonaRecord::description.toImmutableProp())

    public val CREATED_AT: TypedProp.Scalar<PersonaRecord, LocalDateTime> =
            TypedProp.scalar(PersonaRecord::createdAt.toImmutableProp())
}
