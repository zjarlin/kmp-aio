@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.MusicHistory::class)

package site.addzero.vibepocket.model

import kotlin.Boolean
import kotlin.Suppress
import kotlin.Unit
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.`impl`.FetcherImpl
import org.babyfish.jimmer.sql.kt.fetcher.FetcherCreator

@GeneratedBy(type = MusicHistory::class)
public fun FetcherCreator<MusicHistory>.`by`(block: MusicHistoryFetcherDsl.() -> Unit): Fetcher<MusicHistory> {
    val dsl = MusicHistoryFetcherDsl(emptyMusicHistoryFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@GeneratedBy(type = MusicHistory::class)
public fun FetcherCreator<MusicHistory>.`by`(base: Fetcher<MusicHistory>?, block: MusicHistoryFetcherDsl.() -> Unit): Fetcher<MusicHistory> {
    val dsl = MusicHistoryFetcherDsl(base ?: emptyMusicHistoryFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@DslScope
@GeneratedBy(type = MusicHistory::class)
public class MusicHistoryFetcherDsl(
    fetcher: Fetcher<MusicHistory> = emptyMusicHistoryFetcher,
) {
    private var _fetcher: Fetcher<MusicHistory> = fetcher

    public fun internallyGetFetcher(): Fetcher<MusicHistory> = _fetcher

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

    public fun tracksJson(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("tracksJson")
        } else {
            _fetcher.remove("tracksJson")
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

private val emptyMusicHistoryFetcher: Fetcher<MusicHistory> = FetcherImpl(MusicHistory::class.java)
