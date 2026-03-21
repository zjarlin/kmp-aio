@file:Suppress("warnings")
@file:GeneratedBy(type = site.addzero.vibepocket.model.MusicTask::class)

package site.addzero.vibepocket.model

import kotlin.Boolean
import kotlin.Suppress
import kotlin.Unit
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.`impl`.FetcherImpl
import org.babyfish.jimmer.sql.kt.fetcher.FetcherCreator

@GeneratedBy(type = MusicTask::class)
public fun FetcherCreator<MusicTask>.`by`(block: MusicTaskFetcherDsl.() -> Unit): Fetcher<MusicTask> {
    val dsl = MusicTaskFetcherDsl(emptyMusicTaskFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@GeneratedBy(type = MusicTask::class)
public fun FetcherCreator<MusicTask>.`by`(base: Fetcher<MusicTask>?, block: MusicTaskFetcherDsl.() -> Unit): Fetcher<MusicTask> {
    val dsl = MusicTaskFetcherDsl(base ?: emptyMusicTaskFetcher)
    dsl.block()
    return dsl.internallyGetFetcher()
}

@DslScope
@GeneratedBy(type = MusicTask::class)
public class MusicTaskFetcherDsl(
    fetcher: Fetcher<MusicTask> = emptyMusicTaskFetcher,
) {
    private var _fetcher: Fetcher<MusicTask> = fetcher

    public fun internallyGetFetcher(): Fetcher<MusicTask> = _fetcher

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

    public fun status(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("status")
        } else {
            _fetcher.remove("status")
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

    public fun prompt(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("prompt")
        } else {
            _fetcher.remove("prompt")
        }
    }

    public fun mv(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("mv")
        } else {
            _fetcher.remove("mv")
        }
    }

    public fun audioUrl(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("audioUrl")
        } else {
            _fetcher.remove("audioUrl")
        }
    }

    public fun videoUrl(enabled: Boolean = true) {
        _fetcher = if (enabled) {
            _fetcher.add("videoUrl")
        } else {
            _fetcher.remove("videoUrl")
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

private val emptyMusicTaskFetcher: Fetcher<MusicTask> = FetcherImpl(MusicTask::class.java)
