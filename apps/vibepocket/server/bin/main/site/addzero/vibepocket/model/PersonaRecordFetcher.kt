@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.PersonaRecord::class)

package site.addzero.vibepocket.model

import kotlin.Boolean
import kotlin.Suppress
import kotlin.Unit
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.`impl`.FetcherImpl
import org.babyfish.jimmer.sql.kt.fetcher.FetcherCreator

@GeneratedBy(type = PersonaRecord::class)
public fun FetcherCreator<PersonaRecord>.`by`(block: PersonaRecordFetcherDsl.() -> Unit): Fetcher<PersonaRecord> {
    val dsl = PersonaRecordFetcherDsl(emptyPersonaRecordFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@GeneratedBy(type = PersonaRecord::class)
public fun FetcherCreator<PersonaRecord>.`by`(base: Fetcher<PersonaRecord>?, block: PersonaRecordFetcherDsl.() -> Unit): Fetcher<PersonaRecord> {
    val dsl = PersonaRecordFetcherDsl(base ?: emptyPersonaRecordFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@DslScope
@GeneratedBy(type = PersonaRecord::class)
public class PersonaRecordFetcherDsl(
    fetcher: Fetcher<PersonaRecord> = emptyPersonaRecordFetcher,
) {
    private var _fetcher: Fetcher<PersonaRecord> = fetcher

    public fun internallyGetFetcher(): Fetcher<PersonaRecord> = _fetcher

    public fun allScalarFields() {
        _fetcher = _fetcher.allScalarFields()
    }

    public fun allTableFields() {
        _fetcher = _fetcher.allTableFields()
    }

    public fun personaId(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("personaId")
        } else {
            _fetcher.remove("personaId")
        }
    }

    public fun name(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("name")
        } else {
            _fetcher.remove("name")
        }
    }

    public fun description(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("description")
        } else {
            _fetcher.remove("description")
        }
    }

    public fun createdAt(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("createdAt")
        } else {
            _fetcher.remove("createdAt")
        }
    }
}

private val emptyPersonaRecordFetcher: Fetcher<PersonaRecord> =
        FetcherImpl(PersonaRecord::class.java)
