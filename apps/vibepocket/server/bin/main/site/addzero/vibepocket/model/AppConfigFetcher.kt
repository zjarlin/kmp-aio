@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.AppConfig::class)

package site.addzero.vibepocket.model

import kotlin.Boolean
import kotlin.Suppress
import kotlin.Unit
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.`impl`.FetcherImpl
import org.babyfish.jimmer.sql.kt.fetcher.FetcherCreator

@GeneratedBy(type = AppConfig::class)
public fun FetcherCreator<AppConfig>.`by`(block: AppConfigFetcherDsl.() -> Unit): Fetcher<AppConfig> {
    val dsl = AppConfigFetcherDsl(emptyAppConfigFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@GeneratedBy(type = AppConfig::class)
public fun FetcherCreator<AppConfig>.`by`(base: Fetcher<AppConfig>?, block: AppConfigFetcherDsl.() -> Unit): Fetcher<AppConfig> {
    val dsl = AppConfigFetcherDsl(base ?: emptyAppConfigFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@DslScope
@GeneratedBy(type = AppConfig::class)
public class AppConfigFetcherDsl(
    fetcher: Fetcher<AppConfig> = emptyAppConfigFetcher,
) {
    private var _fetcher: Fetcher<AppConfig> = fetcher

    public fun internallyGetFetcher(): Fetcher<AppConfig> = _fetcher

    public fun allScalarFields() {
        _fetcher = _fetcher.allScalarFields()
    }

    public fun allTableFields() {
        _fetcher = _fetcher.allTableFields()
    }

    public fun key(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("key")
        } else {
            _fetcher.remove("key")
        }
    }

    public fun `value`(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("value")
        } else {
            _fetcher.remove("value")
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

private val emptyAppConfigFetcher: Fetcher<AppConfig> = FetcherImpl(AppConfig::class.java)
