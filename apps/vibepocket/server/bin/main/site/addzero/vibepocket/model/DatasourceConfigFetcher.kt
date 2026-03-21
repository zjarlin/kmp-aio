@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.DatasourceConfig::class)

package site.addzero.vibepocket.model

import kotlin.Boolean
import kotlin.Suppress
import kotlin.Unit
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.`impl`.FetcherImpl
import org.babyfish.jimmer.sql.kt.fetcher.FetcherCreator

@GeneratedBy(type = DatasourceConfig::class)
public fun FetcherCreator<DatasourceConfig>.`by`(block: DatasourceConfigFetcherDsl.() -> Unit): Fetcher<DatasourceConfig> {
    val dsl = DatasourceConfigFetcherDsl(emptyDatasourceConfigFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@GeneratedBy(type = DatasourceConfig::class)
public fun FetcherCreator<DatasourceConfig>.`by`(base: Fetcher<DatasourceConfig>?, block: DatasourceConfigFetcherDsl.() -> Unit): Fetcher<DatasourceConfig> {
    val dsl = DatasourceConfigFetcherDsl(base ?: emptyDatasourceConfigFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@DslScope
@GeneratedBy(type = DatasourceConfig::class)
public class DatasourceConfigFetcherDsl(
    fetcher: Fetcher<DatasourceConfig> = emptyDatasourceConfigFetcher,
) {
    private var _fetcher: Fetcher<DatasourceConfig> = fetcher

    public fun internallyGetFetcher(): Fetcher<DatasourceConfig> = _fetcher

    public fun allScalarFields() {
        _fetcher = _fetcher.allScalarFields()
    }

    public fun allTableFields() {
        _fetcher = _fetcher.allTableFields()
    }

    public fun owner(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("owner")
        } else {
            _fetcher.remove("owner")
        }
    }

    public fun name(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("name")
        } else {
            _fetcher.remove("name")
        }
    }

    public fun dbType(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("dbType")
        } else {
            _fetcher.remove("dbType")
        }
    }

    public fun url(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("url")
        } else {
            _fetcher.remove("url")
        }
    }

    public fun username(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("username")
        } else {
            _fetcher.remove("username")
        }
    }

    public fun password(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("password")
        } else {
            _fetcher.remove("password")
        }
    }

    public fun driverClass(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("driverClass")
        } else {
            _fetcher.remove("driverClass")
        }
    }

    public fun enabled(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("enabled")
        } else {
            _fetcher.remove("enabled")
        }
    }

    public fun description(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("description")
        } else {
            _fetcher.remove("description")
        }
    }
}

private val emptyDatasourceConfigFetcher: Fetcher<DatasourceConfig> =
        FetcherImpl(DatasourceConfig::class.java)
