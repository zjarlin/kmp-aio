@file:Suppress("warnings")

package site.addzero.vibepocket.model

import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonPropertyOrder
import java.io.Serializable
import java.lang.IllegalStateException
import java.time.LocalDateTime
import kotlin.Any
import kotlin.Boolean
import kotlin.Cloneable
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.MutableList
import org.babyfish.jimmer.CircularReferenceException
import org.babyfish.jimmer.Draft
import org.babyfish.jimmer.DraftConsumer
import org.babyfish.jimmer.ImmutableObjects
import org.babyfish.jimmer.UnloadedException
import org.babyfish.jimmer.`internal`.GeneratedBy
import org.babyfish.jimmer.client.Description
import org.babyfish.jimmer.jackson.ImmutableModuleRequiredException
import org.babyfish.jimmer.kt.DslScope
import org.babyfish.jimmer.kt.ImmutableCreator
import org.babyfish.jimmer.meta.ImmutablePropCategory
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.meta.PropId
import org.babyfish.jimmer.runtime.DraftContext
import org.babyfish.jimmer.runtime.DraftSpi
import org.babyfish.jimmer.runtime.ImmutableSpi
import org.babyfish.jimmer.runtime.Internal
import org.babyfish.jimmer.runtime.Visibility

@DslScope
@GeneratedBy(type = FavoriteTrack::class)
public interface FavoriteTrackDraft : FavoriteTrack, Draft {
    override var id: Long

    override var trackId: String

    override var taskId: String

    override var audioUrl: String?

    override var title: String?

    override var tags: String?

    override var imageUrl: String?

    override var duration: Double?

    override var createdAt: LocalDateTime

    @GeneratedBy(type = FavoriteTrack::class)
    public object `$` {
        public const val SLOT_ID: Int = 0

        public const val SLOT_TRACK_ID: Int = 1

        public const val SLOT_TASK_ID: Int = 2

        public const val SLOT_AUDIO_URL: Int = 3

        public const val SLOT_TITLE: Int = 4

        public const val SLOT_TAGS: Int = 5

        public const val SLOT_IMAGE_URL: Int = 6

        public const val SLOT_DURATION: Int = 7

        public const val SLOT_CREATED_AT: Int = 8

        public val type: ImmutableType = ImmutableType
            .newBuilder(
                "0.9.120",
                FavoriteTrack::class,
                listOf(

                ),
            ) { ctx, base ->
                DraftImpl(ctx, base as FavoriteTrack?)
            }
            .id(SLOT_ID, "id", Long::class.java)
            .key(SLOT_TRACK_ID, "trackId", String::class.java, false)
            .add(SLOT_TASK_ID, "taskId", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_AUDIO_URL, "audioUrl", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_TITLE, "title", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_TAGS, "tags", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_IMAGE_URL, "imageUrl", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_DURATION, "duration", ImmutablePropCategory.SCALAR, Double::class.java, true)
            .add(SLOT_CREATED_AT, "createdAt", ImmutablePropCategory.SCALAR, LocalDateTime::class.java, false)
            .build()

        public fun produce(base: FavoriteTrack? = null, resolveImmediately: Boolean = false): FavoriteTrack {
            val consumer = DraftConsumer<FavoriteTrackDraft> {}
            return Internal.produce(type, base, resolveImmediately, consumer) as FavoriteTrack
        }

        public fun produce(
            base: FavoriteTrack? = null,
            resolveImmediately: Boolean = false,
            block: FavoriteTrackDraft.() -> Unit,
        ): FavoriteTrack {
            val consumer = DraftConsumer<FavoriteTrackDraft> { block(it) }
            return Internal.produce(type, base, resolveImmediately, consumer) as FavoriteTrack
        }

        @GeneratedBy(type = FavoriteTrack::class)
        @JsonPropertyOrder("dummyPropForJacksonError__", "id", "trackId", "taskId", "audioUrl", "title", "tags", "imageUrl", "duration", "createdAt")
        private abstract interface Implementor : FavoriteTrack, ImmutableSpi {
            public val dummyPropForJacksonError__: Int
                get() = throw ImmutableModuleRequiredException()

            override fun __get(prop: PropId): Any? = when (prop.asIndex()) {
                -1 ->
                	__get(prop.asName())
                SLOT_ID ->
                	id
                SLOT_TRACK_ID ->
                	trackId
                SLOT_TASK_ID ->
                	taskId
                SLOT_AUDIO_URL ->
                	audioUrl
                SLOT_TITLE ->
                	title
                SLOT_TAGS ->
                	tags
                SLOT_IMAGE_URL ->
                	imageUrl
                SLOT_DURATION ->
                	duration
                SLOT_CREATED_AT ->
                	createdAt
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.FavoriteTrack\": " + 
                    prop
                )

            }

            override fun __get(prop: String): Any? = when (prop) {
                "id" ->
                	id
                "trackId" ->
                	trackId
                "taskId" ->
                	taskId
                "audioUrl" ->
                	audioUrl
                "title" ->
                	title
                "tags" ->
                	tags
                "imageUrl" ->
                	imageUrl
                "duration" ->
                	duration
                "createdAt" ->
                	createdAt
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.FavoriteTrack\": " + 
                    prop
                )

            }

            override fun __type(): ImmutableType = `$`.type
        }

        @GeneratedBy(type = FavoriteTrack::class)
        private class Impl : Implementor, Cloneable, Serializable {
            @get:JsonIgnore
            internal var __visibility: Visibility? = null

            @get:JsonIgnore
            internal var __idValue: Long = 0

            @get:JsonIgnore
            internal var __idLoaded: Boolean = false

            @get:JsonIgnore
            internal var __trackIdValue: String? = null

            @get:JsonIgnore
            internal var __taskIdValue: String? = null

            @get:JsonIgnore
            internal var __audioUrlValue: String? = null

            @get:JsonIgnore
            internal var __audioUrlLoaded: Boolean = false

            @get:JsonIgnore
            internal var __titleValue: String? = null

            @get:JsonIgnore
            internal var __titleLoaded: Boolean = false

            @get:JsonIgnore
            internal var __tagsValue: String? = null

            @get:JsonIgnore
            internal var __tagsLoaded: Boolean = false

            @get:JsonIgnore
            internal var __imageUrlValue: String? = null

            @get:JsonIgnore
            internal var __imageUrlLoaded: Boolean = false

            @get:JsonIgnore
            internal var __durationValue: Double? = null

            @get:JsonIgnore
            internal var __durationLoaded: Boolean = false

            @get:JsonIgnore
            internal var __createdAtValue: LocalDateTime? = null

            override val id: Long
                get() {
                    if (!__idLoaded) {
                        throw UnloadedException(FavoriteTrack::class.java, "id")
                    }
                    return __idValue
                }

            @Description(value = "音轨 ID（唯一标识） ")
            override val trackId: String
                get() {
                    val __trackIdValue = this.__trackIdValue
                    if (__trackIdValue === null) {
                        throw UnloadedException(FavoriteTrack::class.java, "trackId")
                    }
                    return __trackIdValue
                }

            @Description(value = "所属任务 ID ")
            override val taskId: String
                get() {
                    val __taskIdValue = this.__taskIdValue
                    if (__taskIdValue === null) {
                        throw UnloadedException(FavoriteTrack::class.java, "taskId")
                    }
                    return __taskIdValue
                }

            @Description(value = "音频 URL ")
            override val audioUrl: String?
                get() {
                    if (!__audioUrlLoaded) {
                        throw UnloadedException(FavoriteTrack::class.java, "audioUrl")
                    }
                    return __audioUrlValue
                }

            @Description(value = "标题 ")
            override val title: String?
                get() {
                    if (!__titleLoaded) {
                        throw UnloadedException(FavoriteTrack::class.java, "title")
                    }
                    return __titleValue
                }

            @Description(value = "风格标签 ")
            override val tags: String?
                get() {
                    if (!__tagsLoaded) {
                        throw UnloadedException(FavoriteTrack::class.java, "tags")
                    }
                    return __tagsValue
                }

            @Description(value = "封面图 URL ")
            override val imageUrl: String?
                get() {
                    if (!__imageUrlLoaded) {
                        throw UnloadedException(FavoriteTrack::class.java, "imageUrl")
                    }
                    return __imageUrlValue
                }

            @Description(value = "时长（秒） ")
            override val duration: Double?
                get() {
                    if (!__durationLoaded) {
                        throw UnloadedException(FavoriteTrack::class.java, "duration")
                    }
                    return __durationValue
                }

            @Description(value = "创建时间 ")
            override val createdAt: LocalDateTime
                get() {
                    val __createdAtValue = this.__createdAtValue
                    if (__createdAtValue === null) {
                        throw UnloadedException(FavoriteTrack::class.java, "createdAt")
                    }
                    return __createdAtValue
                }

            public override fun clone(): Impl {
                val copy = super.clone() as Impl
                val originalVisibility = this.__visibility
                if (originalVisibility != null) {
                    val newVisibility = Visibility.of(9)
                    for (propId in 0 until 9) {
                        newVisibility.show(propId, originalVisibility.visible(propId))
                    }
                    copy.__visibility = newVisibility
                } else {
                    copy.__visibility = null
                }
                return copy
            }

            override fun __isLoaded(prop: PropId): Boolean = when (prop.asIndex()) {
                -1 ->
                	__isLoaded(prop.asName())
                SLOT_ID ->
                	__idLoaded
                SLOT_TRACK_ID ->
                	__trackIdValue !== null
                SLOT_TASK_ID ->
                	__taskIdValue !== null
                SLOT_AUDIO_URL ->
                	__audioUrlLoaded
                SLOT_TITLE ->
                	__titleLoaded
                SLOT_TAGS ->
                	__tagsLoaded
                SLOT_IMAGE_URL ->
                	__imageUrlLoaded
                SLOT_DURATION ->
                	__durationLoaded
                SLOT_CREATED_AT ->
                	__createdAtValue !== null
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.FavoriteTrack\": " + 
                    prop
                )

            }

            override fun __isLoaded(prop: String): Boolean = when (prop) {
                "id" ->
                	__idLoaded
                "trackId" ->
                	__trackIdValue !== null
                "taskId" ->
                	__taskIdValue !== null
                "audioUrl" ->
                	__audioUrlLoaded
                "title" ->
                	__titleLoaded
                "tags" ->
                	__tagsLoaded
                "imageUrl" ->
                	__imageUrlLoaded
                "duration" ->
                	__durationLoaded
                "createdAt" ->
                	__createdAtValue !== null
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.FavoriteTrack\": " + 
                    prop
                )

            }

            override fun __isVisible(prop: PropId): Boolean {
                val __visibility = this.__visibility ?: return true
                return when (prop.asIndex()) {
                    -1 ->
                    	__isVisible(prop.asName())
                    SLOT_ID ->
                    	__visibility.visible(SLOT_ID)
                    SLOT_TRACK_ID ->
                    	__visibility.visible(SLOT_TRACK_ID)
                    SLOT_TASK_ID ->
                    	__visibility.visible(SLOT_TASK_ID)
                    SLOT_AUDIO_URL ->
                    	__visibility.visible(SLOT_AUDIO_URL)
                    SLOT_TITLE ->
                    	__visibility.visible(SLOT_TITLE)
                    SLOT_TAGS ->
                    	__visibility.visible(SLOT_TAGS)
                    SLOT_IMAGE_URL ->
                    	__visibility.visible(SLOT_IMAGE_URL)
                    SLOT_DURATION ->
                    	__visibility.visible(SLOT_DURATION)
                    SLOT_CREATED_AT ->
                    	__visibility.visible(SLOT_CREATED_AT)
                    else -> true
                }
            }

            override fun __isVisible(prop: String): Boolean {
                val __visibility = this.__visibility ?: return true
                return when (prop) {
                    "id" ->
                    	__visibility.visible(SLOT_ID)
                    "trackId" ->
                    	__visibility.visible(SLOT_TRACK_ID)
                    "taskId" ->
                    	__visibility.visible(SLOT_TASK_ID)
                    "audioUrl" ->
                    	__visibility.visible(SLOT_AUDIO_URL)
                    "title" ->
                    	__visibility.visible(SLOT_TITLE)
                    "tags" ->
                    	__visibility.visible(SLOT_TAGS)
                    "imageUrl" ->
                    	__visibility.visible(SLOT_IMAGE_URL)
                    "duration" ->
                    	__visibility.visible(SLOT_DURATION)
                    "createdAt" ->
                    	__visibility.visible(SLOT_CREATED_AT)
                    else -> true
                }
            }

            public fun __shallowHashCode(): Int {
                var hash = __visibility?.hashCode() ?: 0
                if (__idLoaded) {
                    hash = 31 * hash + __idValue.hashCode()
                }
                if (__trackIdValue !== null) {
                    hash = 31 * hash + __trackIdValue.hashCode()
                }
                if (__taskIdValue !== null) {
                    hash = 31 * hash + __taskIdValue.hashCode()
                }
                if (__audioUrlLoaded) {
                    hash = 31 * hash + (__audioUrlValue?.hashCode() ?: 0)
                }
                if (__titleLoaded) {
                    hash = 31 * hash + (__titleValue?.hashCode() ?: 0)
                }
                if (__tagsLoaded) {
                    hash = 31 * hash + (__tagsValue?.hashCode() ?: 0)
                }
                if (__imageUrlLoaded) {
                    hash = 31 * hash + (__imageUrlValue?.hashCode() ?: 0)
                }
                if (__durationLoaded) {
                    hash = 31 * hash + (__durationValue?.hashCode() ?: 0)
                }
                if (__createdAtValue !== null) {
                    hash = 31 * hash + __createdAtValue.hashCode()
                }
                return hash
            }

            override fun hashCode(): Int {
                var hash = __visibility?.hashCode() ?: 0
                if (__idLoaded) {
                    hash = 31 * hash + __idValue.hashCode()
                    return hash
                }
                if (__trackIdValue !== null) {
                    hash = 31 * hash + __trackIdValue.hashCode()
                }
                if (__taskIdValue !== null) {
                    hash = 31 * hash + __taskIdValue.hashCode()
                }
                if (__audioUrlLoaded) {
                    hash = 31 * hash + (__audioUrlValue?.hashCode() ?: 0)
                }
                if (__titleLoaded) {
                    hash = 31 * hash + (__titleValue?.hashCode() ?: 0)
                }
                if (__tagsLoaded) {
                    hash = 31 * hash + (__tagsValue?.hashCode() ?: 0)
                }
                if (__imageUrlLoaded) {
                    hash = 31 * hash + (__imageUrlValue?.hashCode() ?: 0)
                }
                if (__durationLoaded) {
                    hash = 31 * hash + (__durationValue?.hashCode() ?: 0)
                }
                if (__createdAtValue !== null) {
                    hash = 31 * hash + __createdAtValue.hashCode()
                }
                return hash
            }

            override fun __hashCode(shallow: Boolean): Int = if (shallow) __shallowHashCode() else hashCode()

            public fun __shallowEquals(other: Any?): Boolean {
                val __other = other as? Implementor
                if (__other === null) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_ID)) != __other.__isVisible(PropId.byIndex(SLOT_ID))) {
                    return false
                }
                val __idLoaded = 
                    this.__idLoaded
                if (__idLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_ID)))) {
                    return false
                }
                if (__idLoaded && this.__idValue != __other.id) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_TRACK_ID)) != __other.__isVisible(PropId.byIndex(SLOT_TRACK_ID))) {
                    return false
                }
                val __trackIdLoaded = 
                    this.__trackIdValue !== null
                if (__trackIdLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TRACK_ID)))) {
                    return false
                }
                if (__trackIdLoaded && this.__trackIdValue != __other.trackId) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_TASK_ID)) != __other.__isVisible(PropId.byIndex(SLOT_TASK_ID))) {
                    return false
                }
                val __taskIdLoaded = 
                    this.__taskIdValue !== null
                if (__taskIdLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TASK_ID)))) {
                    return false
                }
                if (__taskIdLoaded && this.__taskIdValue != __other.taskId) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_AUDIO_URL)) != __other.__isVisible(PropId.byIndex(SLOT_AUDIO_URL))) {
                    return false
                }
                val __audioUrlLoaded = 
                    this.__audioUrlLoaded
                if (__audioUrlLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_AUDIO_URL)))) {
                    return false
                }
                if (__audioUrlLoaded && this.__audioUrlValue != __other.audioUrl) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_TITLE)) != __other.__isVisible(PropId.byIndex(SLOT_TITLE))) {
                    return false
                }
                val __titleLoaded = 
                    this.__titleLoaded
                if (__titleLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TITLE)))) {
                    return false
                }
                if (__titleLoaded && this.__titleValue != __other.title) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_TAGS)) != __other.__isVisible(PropId.byIndex(SLOT_TAGS))) {
                    return false
                }
                val __tagsLoaded = 
                    this.__tagsLoaded
                if (__tagsLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TAGS)))) {
                    return false
                }
                if (__tagsLoaded && this.__tagsValue != __other.tags) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_IMAGE_URL)) != __other.__isVisible(PropId.byIndex(SLOT_IMAGE_URL))) {
                    return false
                }
                val __imageUrlLoaded = 
                    this.__imageUrlLoaded
                if (__imageUrlLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_IMAGE_URL)))) {
                    return false
                }
                if (__imageUrlLoaded && this.__imageUrlValue != __other.imageUrl) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_DURATION)) != __other.__isVisible(PropId.byIndex(SLOT_DURATION))) {
                    return false
                }
                val __durationLoaded = 
                    this.__durationLoaded
                if (__durationLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DURATION)))) {
                    return false
                }
                if (__durationLoaded && this.__durationValue != __other.duration) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_CREATED_AT)) != __other.__isVisible(PropId.byIndex(SLOT_CREATED_AT))) {
                    return false
                }
                val __createdAtLoaded = 
                    this.__createdAtValue !== null
                if (__createdAtLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_CREATED_AT)))) {
                    return false
                }
                if (__createdAtLoaded && this.__createdAtValue != __other.createdAt) {
                    return false
                }
                return true
            }

            override fun equals(other: Any?): Boolean {
                val __other = other as? Implementor
                if (__other === null) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_ID)) != __other.__isVisible(PropId.byIndex(SLOT_ID))) {
                    return false
                }
                val __idLoaded = 
                    this.__idLoaded
                if (__idLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_ID)))) {
                    return false
                }
                if (__idLoaded) {
                    return this.__idValue == __other.id
                }
                if (__isVisible(PropId.byIndex(SLOT_TRACK_ID)) != __other.__isVisible(PropId.byIndex(SLOT_TRACK_ID))) {
                    return false
                }
                val __trackIdLoaded = 
                    this.__trackIdValue !== null
                if (__trackIdLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TRACK_ID)))) {
                    return false
                }
                if (__trackIdLoaded && this.__trackIdValue != __other.trackId) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_TASK_ID)) != __other.__isVisible(PropId.byIndex(SLOT_TASK_ID))) {
                    return false
                }
                val __taskIdLoaded = 
                    this.__taskIdValue !== null
                if (__taskIdLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TASK_ID)))) {
                    return false
                }
                if (__taskIdLoaded && this.__taskIdValue != __other.taskId) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_AUDIO_URL)) != __other.__isVisible(PropId.byIndex(SLOT_AUDIO_URL))) {
                    return false
                }
                val __audioUrlLoaded = 
                    this.__audioUrlLoaded
                if (__audioUrlLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_AUDIO_URL)))) {
                    return false
                }
                if (__audioUrlLoaded && this.__audioUrlValue != __other.audioUrl) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_TITLE)) != __other.__isVisible(PropId.byIndex(SLOT_TITLE))) {
                    return false
                }
                val __titleLoaded = 
                    this.__titleLoaded
                if (__titleLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TITLE)))) {
                    return false
                }
                if (__titleLoaded && this.__titleValue != __other.title) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_TAGS)) != __other.__isVisible(PropId.byIndex(SLOT_TAGS))) {
                    return false
                }
                val __tagsLoaded = 
                    this.__tagsLoaded
                if (__tagsLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TAGS)))) {
                    return false
                }
                if (__tagsLoaded && this.__tagsValue != __other.tags) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_IMAGE_URL)) != __other.__isVisible(PropId.byIndex(SLOT_IMAGE_URL))) {
                    return false
                }
                val __imageUrlLoaded = 
                    this.__imageUrlLoaded
                if (__imageUrlLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_IMAGE_URL)))) {
                    return false
                }
                if (__imageUrlLoaded && this.__imageUrlValue != __other.imageUrl) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_DURATION)) != __other.__isVisible(PropId.byIndex(SLOT_DURATION))) {
                    return false
                }
                val __durationLoaded = 
                    this.__durationLoaded
                if (__durationLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DURATION)))) {
                    return false
                }
                if (__durationLoaded && this.__durationValue != __other.duration) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_CREATED_AT)) != __other.__isVisible(PropId.byIndex(SLOT_CREATED_AT))) {
                    return false
                }
                val __createdAtLoaded = 
                    this.__createdAtValue !== null
                if (__createdAtLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_CREATED_AT)))) {
                    return false
                }
                if (__createdAtLoaded && this.__createdAtValue != __other.createdAt) {
                    return false
                }
                return true
            }

            override fun __equals(obj: Any?, shallow: Boolean): Boolean = if (shallow) __shallowEquals(obj) else equals(obj)

            override fun toString(): String = ImmutableObjects.toString(this)
        }

        @GeneratedBy(type = FavoriteTrack::class)
        internal class DraftImpl(
            ctx: DraftContext?,
            base: FavoriteTrack?,
        ) : Implementor,
            FavoriteTrackDraft,
            DraftSpi {
            private val __ctx: DraftContext? = ctx

            private val __base: Impl? = base as Impl?

            private var __modified: Impl? = if (base === null) Impl() else null

            private var __resolving: Boolean = false

            private var __resolved: FavoriteTrack? = null

            override var id: Long
                get() = (__modified ?: __base!!).id
                set(id) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__idValue = id
                    __tmpModified.__idLoaded = true
                }

            override var trackId: String
                get() = (__modified ?: __base!!).trackId
                set(trackId) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__trackIdValue = trackId
                }

            override var taskId: String
                get() = (__modified ?: __base!!).taskId
                set(taskId) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__taskIdValue = taskId
                }

            override var audioUrl: String?
                get() = (__modified ?: __base!!).audioUrl
                set(audioUrl) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__audioUrlValue = audioUrl
                    __tmpModified.__audioUrlLoaded = true
                }

            override var title: String?
                get() = (__modified ?: __base!!).title
                set(title) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__titleValue = title
                    __tmpModified.__titleLoaded = true
                }

            override var tags: String?
                get() = (__modified ?: __base!!).tags
                set(tags) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__tagsValue = tags
                    __tmpModified.__tagsLoaded = true
                }

            override var imageUrl: String?
                get() = (__modified ?: __base!!).imageUrl
                set(imageUrl) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__imageUrlValue = imageUrl
                    __tmpModified.__imageUrlLoaded = true
                }

            override var duration: Double?
                get() = (__modified ?: __base!!).duration
                set(duration) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__durationValue = duration
                    __tmpModified.__durationLoaded = true
                }

            override var createdAt: LocalDateTime
                get() = (__modified ?: __base!!).createdAt
                set(createdAt) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__createdAtValue = createdAt
                }

            override fun __isLoaded(prop: PropId): Boolean = (__modified ?: __base!!).__isLoaded(prop)

            override fun __isLoaded(prop: String): Boolean = (__modified ?: __base!!).__isLoaded(prop)

            override fun __isVisible(prop: PropId): Boolean = (__modified ?: __base!!).__isVisible(prop)

            override fun __isVisible(prop: String): Boolean = (__modified ?: __base!!).__isVisible(prop)

            override fun hashCode(): Int = (__modified ?: __base!!).hashCode()

            override fun __hashCode(shallow: Boolean): Int = (__modified ?: __base!!).__hashCode(shallow)

            override fun equals(other: Any?): Boolean = (__modified ?: __base!!).equals(other)

            override fun __equals(other: Any?, shallow: Boolean): Boolean = (__modified ?: __base!!).__equals(other, shallow)

            override fun toString(): String = ImmutableObjects.toString(this)

            override fun __unload(prop: PropId) {
                if (__resolved != null) {
                    throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                }
                when (prop.asIndex()) {
                    -1 ->
                    	__unload(prop.asName())
                    SLOT_ID ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__idValue = 0
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__idLoaded = false
                        }
                    SLOT_TRACK_ID ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__trackIdValue = null
                    SLOT_TASK_ID ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__taskIdValue = null
                    SLOT_AUDIO_URL ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__audioUrlValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__audioUrlLoaded = false
                        }
                    SLOT_TITLE ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__titleValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__titleLoaded = false
                        }
                    SLOT_TAGS ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__tagsValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__tagsLoaded = false
                        }
                    SLOT_IMAGE_URL ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__imageUrlValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__imageUrlLoaded = false
                        }
                    SLOT_DURATION ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__durationValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__durationLoaded = false
                        }
                    SLOT_CREATED_AT ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__createdAtValue = null
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.FavoriteTrack\": " + 
                        prop
                    )

                }
            }

            override fun __unload(prop: String) {
                if (__resolved != null) {
                    throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                }
                when (prop) {
                    "id" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__idValue = 0
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__idLoaded = false
                        }
                    "trackId" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__trackIdValue = null
                    "taskId" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__taskIdValue = null
                    "audioUrl" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__audioUrlValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__audioUrlLoaded = false
                        }
                    "title" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__titleValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__titleLoaded = false
                        }
                    "tags" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__tagsValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__tagsLoaded = false
                        }
                    "imageUrl" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__imageUrlValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__imageUrlLoaded = false
                        }
                    "duration" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__durationValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__durationLoaded = false
                        }
                    "createdAt" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__createdAtValue = null
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.FavoriteTrack\": " + 
                        prop
                    )

                }
            }

            override fun __set(prop: PropId, `value`: Any?) {
                when (prop.asIndex()) {
                    -1 ->
                    	__set(prop.asName(), value)
                    SLOT_ID ->
                    	this.id = value as Long?
                    	?: throw IllegalArgumentException("'id cannot be null")
                    SLOT_TRACK_ID ->
                    	this.trackId = value as String?
                    	?: throw IllegalArgumentException("'trackId cannot be null")
                    SLOT_TASK_ID ->
                    	this.taskId = value as String?
                    	?: throw IllegalArgumentException("'taskId cannot be null")
                    SLOT_AUDIO_URL ->
                    	this.audioUrl = value as String?
                    SLOT_TITLE ->
                    	this.title = value as String?
                    SLOT_TAGS ->
                    	this.tags = value as String?
                    SLOT_IMAGE_URL ->
                    	this.imageUrl = value as String?
                    SLOT_DURATION ->
                    	this.duration = value as Double?
                    SLOT_CREATED_AT ->
                    	this.createdAt = value as LocalDateTime?
                    	?: throw IllegalArgumentException("'createdAt cannot be null")
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.FavoriteTrack\": " + 
                        prop
                    )

                }
            }

            override fun __set(prop: String, `value`: Any?) {
                when (prop) {
                    "id" ->
                    	this.id = value as Long?
                    	?: throw IllegalArgumentException("'id cannot be null")
                    "trackId" ->
                    	this.trackId = value as String?
                    	?: throw IllegalArgumentException("'trackId cannot be null")
                    "taskId" ->
                    	this.taskId = value as String?
                    	?: throw IllegalArgumentException("'taskId cannot be null")
                    "audioUrl" ->
                    	this.audioUrl = value as String?
                    "title" ->
                    	this.title = value as String?
                    "tags" ->
                    	this.tags = value as String?
                    "imageUrl" ->
                    	this.imageUrl = value as String?
                    "duration" ->
                    	this.duration = value as Double?
                    "createdAt" ->
                    	this.createdAt = value as LocalDateTime?
                    	?: throw IllegalArgumentException("'createdAt cannot be null")
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.FavoriteTrack\": " + 
                        prop
                    )

                }
            }

            override fun __show(prop: PropId, visible: Boolean) {
                if (__resolved != null) {
                    throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                }
                val __visibility = (__modified ?: __base!!).__visibility
                    ?: if (visible) {
                        null
                    } else {
                        Visibility.of(9).also{
                            (__modified ?: __base!!.clone())
                            .also { __modified = it }.__visibility = it}
                    }
                    ?: return
                when (prop.asIndex()) {
                    -1 ->
                    	__show(prop.asName(), visible)
                    SLOT_ID ->
                    	__visibility.show(SLOT_ID, visible)
                    SLOT_TRACK_ID ->
                    	__visibility.show(SLOT_TRACK_ID, visible)
                    SLOT_TASK_ID ->
                    	__visibility.show(SLOT_TASK_ID, visible)
                    SLOT_AUDIO_URL ->
                    	__visibility.show(SLOT_AUDIO_URL, visible)
                    SLOT_TITLE ->
                    	__visibility.show(SLOT_TITLE, visible)
                    SLOT_TAGS ->
                    	__visibility.show(SLOT_TAGS, visible)
                    SLOT_IMAGE_URL ->
                    	__visibility.show(SLOT_IMAGE_URL, visible)
                    SLOT_DURATION ->
                    	__visibility.show(SLOT_DURATION, visible)
                    SLOT_CREATED_AT ->
                    	__visibility.show(SLOT_CREATED_AT, visible)
                    else -> throw IllegalArgumentException(
                        "Illegal property id: \"" + 
                        prop + 
                        "\",it does not exists"
                    )
                }
            }

            override fun __show(prop: String, visible: Boolean) {
                if (__resolved != null) {
                    throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                }
                val __visibility = (__modified ?: __base!!).__visibility
                    ?: if (visible) {
                        null
                    } else {
                        Visibility.of(9).also{
                            (__modified ?: __base!!.clone())
                            .also { __modified = it }.__visibility = it}
                    }
                    ?: return
                when (prop) {
                    "id" ->
                    	__visibility.show(SLOT_ID, visible)
                    "trackId" ->
                    	__visibility.show(SLOT_TRACK_ID, visible)
                    "taskId" ->
                    	__visibility.show(SLOT_TASK_ID, visible)
                    "audioUrl" ->
                    	__visibility.show(SLOT_AUDIO_URL, visible)
                    "title" ->
                    	__visibility.show(SLOT_TITLE, visible)
                    "tags" ->
                    	__visibility.show(SLOT_TAGS, visible)
                    "imageUrl" ->
                    	__visibility.show(SLOT_IMAGE_URL, visible)
                    "duration" ->
                    	__visibility.show(SLOT_DURATION, visible)
                    "createdAt" ->
                    	__visibility.show(SLOT_CREATED_AT, visible)
                    else -> throw IllegalArgumentException(
                        "Illegal property name: \"" + 
                        prop + 
                        "\",it does not exists"
                    )
                }
            }

            override fun __draftContext(): DraftContext = __ctx()

            override fun __resolve(): Any {
                val __resolved = this.__resolved
                if (__resolved != null) {
                    return __resolved
                }
                if (__resolving) {
                    throw CircularReferenceException()
                }
                __resolving = true
                val __ctx = __ctx()
                try {
                    val base = __base
                    var __tmpModified = __modified
                    if (base !== null && __tmpModified === null) {
                        this.__resolved = base
                        return base
                    }
                    this.__resolved = __tmpModified
                    return __tmpModified!!
                } finally {
                    __resolving = false
                }
            }

            override fun __isResolved(): Boolean = __resolved != null

            private fun __ctx(): DraftContext = __ctx ?: error("The current draft object is simple draft which does not support converting nested object to nested draft")

            internal fun __unwrap(): Any = __modified ?: error("Internal bug, draft for builder must have `__modified`")
        }
    }

    @GeneratedBy(type = FavoriteTrack::class)
    public class Builder {
        private val __draft: `$`.DraftImpl

        public constructor(base: FavoriteTrack?) {
            __draft = `$`.DraftImpl(null, base)
        }

        public constructor() : this(null)

        public fun id(id: Long?): Builder {
            if (id !== null) {
                __draft.id = id
                __draft.__show(PropId.byIndex(`$`.SLOT_ID), true)
            }
            return this
        }

        public fun trackId(trackId: String?): Builder {
            if (trackId !== null) {
                __draft.trackId = trackId
                __draft.__show(PropId.byIndex(`$`.SLOT_TRACK_ID), true)
            }
            return this
        }

        public fun taskId(taskId: String?): Builder {
            if (taskId !== null) {
                __draft.taskId = taskId
                __draft.__show(PropId.byIndex(`$`.SLOT_TASK_ID), true)
            }
            return this
        }

        public fun audioUrl(audioUrl: String?): Builder {
            __draft.audioUrl = audioUrl
            __draft.__show(PropId.byIndex(`$`.SLOT_AUDIO_URL), true)
            return this
        }

        public fun title(title: String?): Builder {
            __draft.title = title
            __draft.__show(PropId.byIndex(`$`.SLOT_TITLE), true)
            return this
        }

        public fun tags(tags: String?): Builder {
            __draft.tags = tags
            __draft.__show(PropId.byIndex(`$`.SLOT_TAGS), true)
            return this
        }

        public fun imageUrl(imageUrl: String?): Builder {
            __draft.imageUrl = imageUrl
            __draft.__show(PropId.byIndex(`$`.SLOT_IMAGE_URL), true)
            return this
        }

        public fun duration(duration: Double?): Builder {
            __draft.duration = duration
            __draft.__show(PropId.byIndex(`$`.SLOT_DURATION), true)
            return this
        }

        public fun createdAt(createdAt: LocalDateTime?): Builder {
            if (createdAt !== null) {
                __draft.createdAt = createdAt
                __draft.__show(PropId.byIndex(`$`.SLOT_CREATED_AT), true)
            }
            return this
        }

        public fun build(): FavoriteTrack = __draft.__unwrap() as FavoriteTrack
    }
}

@GeneratedBy(type = FavoriteTrack::class)
public fun ImmutableCreator<FavoriteTrack>.`by`(resolveImmediately: Boolean = false, block: FavoriteTrackDraft.() -> Unit): FavoriteTrack = FavoriteTrackDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = FavoriteTrack::class)
public fun ImmutableCreator<FavoriteTrack>.`by`(base: FavoriteTrack?, resolveImmediately: Boolean = false): FavoriteTrack = FavoriteTrackDraft.`$`.produce(base, resolveImmediately)

@GeneratedBy(type = FavoriteTrack::class)
public fun ImmutableCreator<FavoriteTrack>.`by`(
    base: FavoriteTrack?,
    resolveImmediately: Boolean = false,
    block: FavoriteTrackDraft.() -> Unit,
): FavoriteTrack = FavoriteTrackDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = FavoriteTrack::class)
public fun FavoriteTrack(resolveImmediately: Boolean = false, block: FavoriteTrackDraft.() -> Unit): FavoriteTrack = FavoriteTrackDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = FavoriteTrack::class)
public fun FavoriteTrack(
    base: FavoriteTrack?,
    resolveImmediately: Boolean = false,
    block: FavoriteTrackDraft.() -> Unit,
): FavoriteTrack = FavoriteTrackDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = FavoriteTrack::class)
public fun MutableList<FavoriteTrackDraft>.addBy(resolveImmediately: Boolean = false, block: FavoriteTrackDraft.() -> Unit): MutableList<FavoriteTrackDraft> {
    add(FavoriteTrackDraft.`$`.produce(null, resolveImmediately, block) as FavoriteTrackDraft)
    return this
}

@GeneratedBy(type = FavoriteTrack::class)
public fun MutableList<FavoriteTrackDraft>.addBy(base: FavoriteTrack?, resolveImmediately: Boolean = false): MutableList<FavoriteTrackDraft> {
    add(FavoriteTrackDraft.`$`.produce(base, resolveImmediately) as FavoriteTrackDraft)
    return this
}

@GeneratedBy(type = FavoriteTrack::class)
public fun MutableList<FavoriteTrackDraft>.addBy(
    base: FavoriteTrack?,
    resolveImmediately: Boolean = false,
    block: FavoriteTrackDraft.() -> Unit,
): MutableList<FavoriteTrackDraft> {
    add(FavoriteTrackDraft.`$`.produce(base, resolveImmediately, block) as FavoriteTrackDraft)
    return this
}

@GeneratedBy(type = FavoriteTrack::class)
public fun FavoriteTrack.copy(resolveImmediately: Boolean = false, block: FavoriteTrackDraft.() -> Unit): FavoriteTrack = FavoriteTrackDraft.`$`.produce(this, resolveImmediately, block)
