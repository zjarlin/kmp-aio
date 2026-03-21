@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.FavoriteTrack::class)

package site.addzero.vibepocket.model

import kotlin.Boolean
import kotlin.Suppress
import kotlin.Unit
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.`impl`.FetcherImpl
import org.babyfish.jimmer.sql.kt.fetcher.FetcherCreator

@GeneratedBy(type = FavoriteTrack::class)
public fun FetcherCreator<FavoriteTrack>.`by`(block: FavoriteTrackFetcherDsl.() -> Unit): Fetcher<FavoriteTrack> {
    val dsl = FavoriteTrackFetcherDsl(emptyFavoriteTrackFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@GeneratedBy(type = FavoriteTrack::class)
public fun FetcherCreator<FavoriteTrack>.`by`(base: Fetcher<FavoriteTrack>?, block: FavoriteTrackFetcherDsl.() -> Unit): Fetcher<FavoriteTrack> {
    val dsl = FavoriteTrackFetcherDsl(base ?: emptyFavoriteTrackFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@DslScope
@GeneratedBy(type = FavoriteTrack::class)
public class FavoriteTrackFetcherDsl(
    fetcher: Fetcher<FavoriteTrack> = emptyFavoriteTrackFetcher,
) {
    private var _fetcher: Fetcher<FavoriteTrack> = fetcher

    public fun internallyGetFetcher(): Fetcher<FavoriteTrack> = _fetcher

    public fun allScalarFields() {
        _fetcher = _fetcher.allScalarFields()
    }

    public fun allTableFields() {
        _fetcher = _fetcher.allTableFields()
    }

    public fun trackId(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("trackId")
        } else {
            _fetcher.remove("trackId")
        }
    }

    public fun taskId(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("taskId")
        } else {
            _fetcher.remove("taskId")
        }
    }

    public fun audioUrl(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("audioUrl")
        } else {
            _fetcher.remove("audioUrl")
        }
    }

    public fun title(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("title")
        } else {
            _fetcher.remove("title")
        }
    }

    public fun tags(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("tags")
        } else {
            _fetcher.remove("tags")
        }
    }

    public fun imageUrl(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("imageUrl")
        } else {
            _fetcher.remove("imageUrl")
        }
    }

    public fun duration(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("duration")
        } else {
            _fetcher.remove("duration")
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

private val emptyFavoriteTrackFetcher: Fetcher<FavoriteTrack> =
        FetcherImpl(FavoriteTrack::class.java)
