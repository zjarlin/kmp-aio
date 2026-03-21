@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.SunoTaskResource::class)

package site.addzero.vibepocket.model

import kotlin.Boolean
import kotlin.Suppress
import kotlin.Unit
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.`impl`.FetcherImpl
import org.babyfish.jimmer.sql.kt.fetcher.FetcherCreator

@GeneratedBy(type = SunoTaskResource::class)
public fun FetcherCreator<SunoTaskResource>.`by`(block: SunoTaskResourceFetcherDsl.() -> Unit): Fetcher<SunoTaskResource> {
    val dsl = SunoTaskResourceFetcherDsl(emptySunoTaskResourceFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@GeneratedBy(type = SunoTaskResource::class)
public fun FetcherCreator<SunoTaskResource>.`by`(base: Fetcher<SunoTaskResource>?, block: SunoTaskResourceFetcherDsl.() -> Unit): Fetcher<SunoTaskResource> {
    val dsl = SunoTaskResourceFetcherDsl(base ?: emptySunoTaskResourceFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@DslScope
@GeneratedBy(type = SunoTaskResource::class)
public class SunoTaskResourceFetcherDsl(
    fetcher: Fetcher<SunoTaskResource> = emptySunoTaskResourceFetcher,
) {
    private var _fetcher: Fetcher<SunoTaskResource> = fetcher

    public fun internallyGetFetcher(): Fetcher<SunoTaskResource> = _fetcher

    public fun allScalarFields() {
        _fetcher = _fetcher.allScalarFields()
    }

    public fun allTableFields() {
        _fetcher = _fetcher.allTableFields()
    }

    public fun taskId(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("taskId")
        } else {
            _fetcher.remove("taskId")
        }
    }

    public fun type(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("type")
        } else {
            _fetcher.remove("type")
        }
    }

    public fun status(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("status")
        } else {
            _fetcher.remove("status")
        }
    }

    public fun requestJson(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("requestJson")
        } else {
            _fetcher.remove("requestJson")
        }
    }

    public fun tracksJson(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("tracksJson")
        } else {
            _fetcher.remove("tracksJson")
        }
    }

    public fun detailJson(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("detailJson")
        } else {
            _fetcher.remove("detailJson")
        }
    }

    public fun errorMessage(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("errorMessage")
        } else {
            _fetcher.remove("errorMessage")
        }
    }

    public fun createdAt(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("createdAt")
        } else {
            _fetcher.remove("createdAt")
        }
    }

    public fun updatedAt(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("updatedAt")
        } else {
            _fetcher.remove("updatedAt")
        }
    }
}

private val emptySunoTaskResourceFetcher: Fetcher<SunoTaskResource> =
        FetcherImpl(SunoTaskResource::class.java)
