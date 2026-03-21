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
@GeneratedBy(type = MusicTask::class)
public interface MusicTaskDraft : MusicTask, Draft {
    override var id: Long

    override var taskId: String

    override var status: String

    override var title: String?

    override var tags: String?

    override var prompt: String?

    override var mv: String?

    override var audioUrl: String?

    override var videoUrl: String?

    override var errorMessage: String?

    override var createdAt: LocalDateTime

    override var updatedAt: LocalDateTime

    @GeneratedBy(type = MusicTask::class)
    public object `$` {
        public const val SLOT_ID: Int = 0

        public const val SLOT_TASK_ID: Int = 1

        public const val SLOT_STATUS: Int = 2

        public const val SLOT_TITLE: Int = 3

        public const val SLOT_TAGS: Int = 4

        public const val SLOT_PROMPT: Int = 5

        public const val SLOT_MV: Int = 6

        public const val SLOT_AUDIO_URL: Int = 7

        public const val SLOT_VIDEO_URL: Int = 8

        public const val SLOT_ERROR_MESSAGE: Int = 9

        public const val SLOT_CREATED_AT: Int = 10

        public const val SLOT_UPDATED_AT: Int = 11

        public val type: ImmutableType = ImmutableType
            .newBuilder(
                "0.9.120",
                MusicTask::class,
                listOf(

                ),
            ) { ctx, base ->
                DraftImpl(ctx, base as MusicTask?)
            }
            .id(SLOT_ID, "id", Long::class.java)
            .add(SLOT_TASK_ID, "taskId", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_STATUS, "status", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_TITLE, "title", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_TAGS, "tags", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_PROMPT, "prompt", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_MV, "mv", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_AUDIO_URL, "audioUrl", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_VIDEO_URL, "videoUrl", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_ERROR_MESSAGE, "errorMessage", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_CREATED_AT, "createdAt", ImmutablePropCategory.SCALAR, LocalDateTime::class.java, false)
            .add(SLOT_UPDATED_AT, "updatedAt", ImmutablePropCategory.SCALAR, LocalDateTime::class.java, false)
            .build()

        public fun produce(base: MusicTask? = null, resolveImmediately: Boolean = false): MusicTask {
            val consumer = DraftConsumer<MusicTaskDraft> {}
            return Internal.produce(type, base, resolveImmediately, consumer) as MusicTask
        }

        public fun produce(
            base: MusicTask? = null,
            resolveImmediately: Boolean = false,
            block: MusicTaskDraft.() -> Unit,
        ): MusicTask {
            val consumer = DraftConsumer<MusicTaskDraft> { block(it) }
            return Internal.produce(type, base, resolveImmediately, consumer) as MusicTask
        }

        @GeneratedBy(type = MusicTask::class)
        @JsonPropertyOrder("dummyPropForJacksonError__", "id", "taskId", "status", "title", "tags", "prompt", "mv", "audioUrl", "videoUrl", "errorMessage", "createdAt", "updatedAt")
        private abstract interface Implementor : MusicTask, ImmutableSpi {
            public val dummyPropForJacksonError__: Int
                get() = throw ImmutableModuleRequiredException()

            override fun __get(prop: PropId): Any? = when (prop.asIndex()) {
                -1 ->
                	__get(prop.asName())
                SLOT_ID ->
                	id
                SLOT_TASK_ID ->
                	taskId
                SLOT_STATUS ->
                	status
                SLOT_TITLE ->
                	title
                SLOT_TAGS ->
                	tags
                SLOT_PROMPT ->
                	prompt
                SLOT_MV ->
                	mv
                SLOT_AUDIO_URL ->
                	audioUrl
                SLOT_VIDEO_URL ->
                	videoUrl
                SLOT_ERROR_MESSAGE ->
                	errorMessage
                SLOT_CREATED_AT ->
                	createdAt
                SLOT_UPDATED_AT ->
                	updatedAt
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.MusicTask\": " + 
                    prop
                )

            }

            override fun __get(prop: String): Any? = when (prop) {
                "id" ->
                	id
                "taskId" ->
                	taskId
                "status" ->
                	status
                "title" ->
                	title
                "tags" ->
                	tags
                "prompt" ->
                	prompt
                "mv" ->
                	mv
                "audioUrl" ->
                	audioUrl
                "videoUrl" ->
                	videoUrl
                "errorMessage" ->
                	errorMessage
                "createdAt" ->
                	createdAt
                "updatedAt" ->
                	updatedAt
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.MusicTask\": " + 
                    prop
                )

            }

            override fun __type(): ImmutableType = `$`.type
        }

        @GeneratedBy(type = MusicTask::class)
        private class Impl : Implementor, Cloneable, Serializable {
            @get:JsonIgnore
            internal var __visibility: Visibility? = null

            @get:JsonIgnore
            internal var __idValue: Long = 0

            @get:JsonIgnore
            internal var __idLoaded: Boolean = false

            @get:JsonIgnore
            internal var __taskIdValue: String? = null

            @get:JsonIgnore
            internal var __statusValue: String? = null

            @get:JsonIgnore
            internal var __titleValue: String? = null

            @get:JsonIgnore
            internal var __titleLoaded: Boolean = false

            @get:JsonIgnore
            internal var __tagsValue: String? = null

            @get:JsonIgnore
            internal var __tagsLoaded: Boolean = false

            @get:JsonIgnore
            internal var __promptValue: String? = null

            @get:JsonIgnore
            internal var __promptLoaded: Boolean = false

            @get:JsonIgnore
            internal var __mvValue: String? = null

            @get:JsonIgnore
            internal var __mvLoaded: Boolean = false

            @get:JsonIgnore
            internal var __audioUrlValue: String? = null

            @get:JsonIgnore
            internal var __audioUrlLoaded: Boolean = false

            @get:JsonIgnore
            internal var __videoUrlValue: String? = null

            @get:JsonIgnore
            internal var __videoUrlLoaded: Boolean = false

            @get:JsonIgnore
            internal var __errorMessageValue: String? = null

            @get:JsonIgnore
            internal var __errorMessageLoaded: Boolean = false

            @get:JsonIgnore
            internal var __createdAtValue: LocalDateTime? = null

            @get:JsonIgnore
            internal var __updatedAtValue: LocalDateTime? = null

            override val id: Long
                get() {
                    if (!__idLoaded) {
                        throw UnloadedException(MusicTask::class.java, "id")
                    }
                    return __idValue
                }

            @Description(value = "Suno 任务 ID ")
            override val taskId: String
                get() {
                    val __taskIdValue = this.__taskIdValue
                    if (__taskIdValue === null) {
                        throw UnloadedException(MusicTask::class.java, "taskId")
                    }
                    return __taskIdValue
                }

            @Description(value = "任务状态: queued / processing / complete / error ")
            override val status: String
                get() {
                    val __statusValue = this.__statusValue
                    if (__statusValue === null) {
                        throw UnloadedException(MusicTask::class.java, "status")
                    }
                    return __statusValue
                }

            @Description(value = "歌曲标题 ")
            override val title: String?
                get() {
                    if (!__titleLoaded) {
                        throw UnloadedException(MusicTask::class.java, "title")
                    }
                    return __titleValue
                }

            @Description(value = "风格标签 ")
            override val tags: String?
                get() {
                    if (!__tagsLoaded) {
                        throw UnloadedException(MusicTask::class.java, "tags")
                    }
                    return __tagsValue
                }

            @Description(value = "歌词 ")
            override val prompt: String?
                get() {
                    if (!__promptLoaded) {
                        throw UnloadedException(MusicTask::class.java, "prompt")
                    }
                    return __promptValue
                }

            @Description(value = "模型版本 ")
            override val mv: String?
                get() {
                    if (!__mvLoaded) {
                        throw UnloadedException(MusicTask::class.java, "mv")
                    }
                    return __mvValue
                }

            @Description(value = "音频 URL ")
            override val audioUrl: String?
                get() {
                    if (!__audioUrlLoaded) {
                        throw UnloadedException(MusicTask::class.java, "audioUrl")
                    }
                    return __audioUrlValue
                }

            @Description(value = "视频 URL ")
            override val videoUrl: String?
                get() {
                    if (!__videoUrlLoaded) {
                        throw UnloadedException(MusicTask::class.java, "videoUrl")
                    }
                    return __videoUrlValue
                }

            @Description(value = "错误信息 ")
            override val errorMessage: String?
                get() {
                    if (!__errorMessageLoaded) {
                        throw UnloadedException(MusicTask::class.java, "errorMessage")
                    }
                    return __errorMessageValue
                }

            @Description(value = "创建时间 ")
            override val createdAt: LocalDateTime
                get() {
                    val __createdAtValue = this.__createdAtValue
                    if (__createdAtValue === null) {
                        throw UnloadedException(MusicTask::class.java, "createdAt")
                    }
                    return __createdAtValue
                }

            @Description(value = "更新时间 ")
            override val updatedAt: LocalDateTime
                get() {
                    val __updatedAtValue = this.__updatedAtValue
                    if (__updatedAtValue === null) {
                        throw UnloadedException(MusicTask::class.java, "updatedAt")
                    }
                    return __updatedAtValue
                }

            public override fun clone(): Impl {
                val copy = super.clone() as Impl
                val originalVisibility = this.__visibility
                if (originalVisibility != null) {
                    val newVisibility = Visibility.of(12)
                    for (propId in 0 until 12) {
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
                SLOT_TASK_ID ->
                	__taskIdValue !== null
                SLOT_STATUS ->
                	__statusValue !== null
                SLOT_TITLE ->
                	__titleLoaded
                SLOT_TAGS ->
                	__tagsLoaded
                SLOT_PROMPT ->
                	__promptLoaded
                SLOT_MV ->
                	__mvLoaded
                SLOT_AUDIO_URL ->
                	__audioUrlLoaded
                SLOT_VIDEO_URL ->
                	__videoUrlLoaded
                SLOT_ERROR_MESSAGE ->
                	__errorMessageLoaded
                SLOT_CREATED_AT ->
                	__createdAtValue !== null
                SLOT_UPDATED_AT ->
                	__updatedAtValue !== null
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.MusicTask\": " + 
                    prop
                )

            }

            override fun __isLoaded(prop: String): Boolean = when (prop) {
                "id" ->
                	__idLoaded
                "taskId" ->
                	__taskIdValue !== null
                "status" ->
                	__statusValue !== null
                "title" ->
                	__titleLoaded
                "tags" ->
                	__tagsLoaded
                "prompt" ->
                	__promptLoaded
                "mv" ->
                	__mvLoaded
                "audioUrl" ->
                	__audioUrlLoaded
                "videoUrl" ->
                	__videoUrlLoaded
                "errorMessage" ->
                	__errorMessageLoaded
                "createdAt" ->
                	__createdAtValue !== null
                "updatedAt" ->
                	__updatedAtValue !== null
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.MusicTask\": " + 
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
                    SLOT_TASK_ID ->
                    	__visibility.visible(SLOT_TASK_ID)
                    SLOT_STATUS ->
                    	__visibility.visible(SLOT_STATUS)
                    SLOT_TITLE ->
                    	__visibility.visible(SLOT_TITLE)
                    SLOT_TAGS ->
                    	__visibility.visible(SLOT_TAGS)
                    SLOT_PROMPT ->
                    	__visibility.visible(SLOT_PROMPT)
                    SLOT_MV ->
                    	__visibility.visible(SLOT_MV)
                    SLOT_AUDIO_URL ->
                    	__visibility.visible(SLOT_AUDIO_URL)
                    SLOT_VIDEO_URL ->
                    	__visibility.visible(SLOT_VIDEO_URL)
                    SLOT_ERROR_MESSAGE ->
                    	__visibility.visible(SLOT_ERROR_MESSAGE)
                    SLOT_CREATED_AT ->
                    	__visibility.visible(SLOT_CREATED_AT)
                    SLOT_UPDATED_AT ->
                    	__visibility.visible(SLOT_UPDATED_AT)
                    else -> true
                }
            }

            override fun __isVisible(prop: String): Boolean {
                val __visibility = this.__visibility ?: return true
                return when (prop) {
                    "id" ->
                    	__visibility.visible(SLOT_ID)
                    "taskId" ->
                    	__visibility.visible(SLOT_TASK_ID)
                    "status" ->
                    	__visibility.visible(SLOT_STATUS)
                    "title" ->
                    	__visibility.visible(SLOT_TITLE)
                    "tags" ->
                    	__visibility.visible(SLOT_TAGS)
                    "prompt" ->
                    	__visibility.visible(SLOT_PROMPT)
                    "mv" ->
                    	__visibility.visible(SLOT_MV)
                    "audioUrl" ->
                    	__visibility.visible(SLOT_AUDIO_URL)
                    "videoUrl" ->
                    	__visibility.visible(SLOT_VIDEO_URL)
                    "errorMessage" ->
                    	__visibility.visible(SLOT_ERROR_MESSAGE)
                    "createdAt" ->
                    	__visibility.visible(SLOT_CREATED_AT)
                    "updatedAt" ->
                    	__visibility.visible(SLOT_UPDATED_AT)
                    else -> true
                }
            }

            public fun __shallowHashCode(): Int {
                var hash = __visibility?.hashCode() ?: 0
                if (__idLoaded) {
                    hash = 31 * hash + __idValue.hashCode()
                }
                if (__taskIdValue !== null) {
                    hash = 31 * hash + __taskIdValue.hashCode()
                }
                if (__statusValue !== null) {
                    hash = 31 * hash + __statusValue.hashCode()
                }
                if (__titleLoaded) {
                    hash = 31 * hash + (__titleValue?.hashCode() ?: 0)
                }
                if (__tagsLoaded) {
                    hash = 31 * hash + (__tagsValue?.hashCode() ?: 0)
                }
                if (__promptLoaded) {
                    hash = 31 * hash + (__promptValue?.hashCode() ?: 0)
                }
                if (__mvLoaded) {
                    hash = 31 * hash + (__mvValue?.hashCode() ?: 0)
                }
                if (__audioUrlLoaded) {
                    hash = 31 * hash + (__audioUrlValue?.hashCode() ?: 0)
                }
                if (__videoUrlLoaded) {
                    hash = 31 * hash + (__videoUrlValue?.hashCode() ?: 0)
                }
                if (__errorMessageLoaded) {
                    hash = 31 * hash + (__errorMessageValue?.hashCode() ?: 0)
                }
                if (__createdAtValue !== null) {
                    hash = 31 * hash + __createdAtValue.hashCode()
                }
                if (__updatedAtValue !== null) {
                    hash = 31 * hash + __updatedAtValue.hashCode()
                }
                return hash
            }

            override fun hashCode(): Int {
                var hash = __visibility?.hashCode() ?: 0
                if (__idLoaded) {
                    hash = 31 * hash + __idValue.hashCode()
                    return hash
                }
                if (__taskIdValue !== null) {
                    hash = 31 * hash + __taskIdValue.hashCode()
                }
                if (__statusValue !== null) {
                    hash = 31 * hash + __statusValue.hashCode()
                }
                if (__titleLoaded) {
                    hash = 31 * hash + (__titleValue?.hashCode() ?: 0)
                }
                if (__tagsLoaded) {
                    hash = 31 * hash + (__tagsValue?.hashCode() ?: 0)
                }
                if (__promptLoaded) {
                    hash = 31 * hash + (__promptValue?.hashCode() ?: 0)
                }
                if (__mvLoaded) {
                    hash = 31 * hash + (__mvValue?.hashCode() ?: 0)
                }
                if (__audioUrlLoaded) {
                    hash = 31 * hash + (__audioUrlValue?.hashCode() ?: 0)
                }
                if (__videoUrlLoaded) {
                    hash = 31 * hash + (__videoUrlValue?.hashCode() ?: 0)
                }
                if (__errorMessageLoaded) {
                    hash = 31 * hash + (__errorMessageValue?.hashCode() ?: 0)
                }
                if (__createdAtValue !== null) {
                    hash = 31 * hash + __createdAtValue.hashCode()
                }
                if (__updatedAtValue !== null) {
                    hash = 31 * hash + __updatedAtValue.hashCode()
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
                if (__isVisible(PropId.byIndex(SLOT_STATUS)) != __other.__isVisible(PropId.byIndex(SLOT_STATUS))) {
                    return false
                }
                val __statusLoaded = 
                    this.__statusValue !== null
                if (__statusLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_STATUS)))) {
                    return false
                }
                if (__statusLoaded && this.__statusValue != __other.status) {
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
                if (__isVisible(PropId.byIndex(SLOT_PROMPT)) != __other.__isVisible(PropId.byIndex(SLOT_PROMPT))) {
                    return false
                }
                val __promptLoaded = 
                    this.__promptLoaded
                if (__promptLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_PROMPT)))) {
                    return false
                }
                if (__promptLoaded && this.__promptValue != __other.prompt) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_MV)) != __other.__isVisible(PropId.byIndex(SLOT_MV))) {
                    return false
                }
                val __mvLoaded = 
                    this.__mvLoaded
                if (__mvLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_MV)))) {
                    return false
                }
                if (__mvLoaded && this.__mvValue != __other.mv) {
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
                if (__isVisible(PropId.byIndex(SLOT_VIDEO_URL)) != __other.__isVisible(PropId.byIndex(SLOT_VIDEO_URL))) {
                    return false
                }
                val __videoUrlLoaded = 
                    this.__videoUrlLoaded
                if (__videoUrlLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_VIDEO_URL)))) {
                    return false
                }
                if (__videoUrlLoaded && this.__videoUrlValue != __other.videoUrl) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_ERROR_MESSAGE)) != __other.__isVisible(PropId.byIndex(SLOT_ERROR_MESSAGE))) {
                    return false
                }
                val __errorMessageLoaded = 
                    this.__errorMessageLoaded
                if (__errorMessageLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_ERROR_MESSAGE)))) {
                    return false
                }
                if (__errorMessageLoaded && this.__errorMessageValue != __other.errorMessage) {
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
                if (__isVisible(PropId.byIndex(SLOT_UPDATED_AT)) != __other.__isVisible(PropId.byIndex(SLOT_UPDATED_AT))) {
                    return false
                }
                val __updatedAtLoaded = 
                    this.__updatedAtValue !== null
                if (__updatedAtLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_UPDATED_AT)))) {
                    return false
                }
                if (__updatedAtLoaded && this.__updatedAtValue != __other.updatedAt) {
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
                if (__isVisible(PropId.byIndex(SLOT_STATUS)) != __other.__isVisible(PropId.byIndex(SLOT_STATUS))) {
                    return false
                }
                val __statusLoaded = 
                    this.__statusValue !== null
                if (__statusLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_STATUS)))) {
                    return false
                }
                if (__statusLoaded && this.__statusValue != __other.status) {
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
                if (__isVisible(PropId.byIndex(SLOT_PROMPT)) != __other.__isVisible(PropId.byIndex(SLOT_PROMPT))) {
                    return false
                }
                val __promptLoaded = 
                    this.__promptLoaded
                if (__promptLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_PROMPT)))) {
                    return false
                }
                if (__promptLoaded && this.__promptValue != __other.prompt) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_MV)) != __other.__isVisible(PropId.byIndex(SLOT_MV))) {
                    return false
                }
                val __mvLoaded = 
                    this.__mvLoaded
                if (__mvLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_MV)))) {
                    return false
                }
                if (__mvLoaded && this.__mvValue != __other.mv) {
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
                if (__isVisible(PropId.byIndex(SLOT_VIDEO_URL)) != __other.__isVisible(PropId.byIndex(SLOT_VIDEO_URL))) {
                    return false
                }
                val __videoUrlLoaded = 
                    this.__videoUrlLoaded
                if (__videoUrlLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_VIDEO_URL)))) {
                    return false
                }
                if (__videoUrlLoaded && this.__videoUrlValue != __other.videoUrl) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_ERROR_MESSAGE)) != __other.__isVisible(PropId.byIndex(SLOT_ERROR_MESSAGE))) {
                    return false
                }
                val __errorMessageLoaded = 
                    this.__errorMessageLoaded
                if (__errorMessageLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_ERROR_MESSAGE)))) {
                    return false
                }
                if (__errorMessageLoaded && this.__errorMessageValue != __other.errorMessage) {
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
                if (__isVisible(PropId.byIndex(SLOT_UPDATED_AT)) != __other.__isVisible(PropId.byIndex(SLOT_UPDATED_AT))) {
                    return false
                }
                val __updatedAtLoaded = 
                    this.__updatedAtValue !== null
                if (__updatedAtLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_UPDATED_AT)))) {
                    return false
                }
                if (__updatedAtLoaded && this.__updatedAtValue != __other.updatedAt) {
                    return false
                }
                return true
            }

            override fun __equals(obj: Any?, shallow: Boolean): Boolean = if (shallow) __shallowEquals(obj) else equals(obj)

            override fun toString(): String = ImmutableObjects.toString(this)
        }

        @GeneratedBy(type = MusicTask::class)
        internal class DraftImpl(
            ctx: DraftContext?,
            base: MusicTask?,
        ) : Implementor,
            MusicTaskDraft,
            DraftSpi {
            private val __ctx: DraftContext? = ctx

            private val __base: Impl? = base as Impl?

            private var __modified: Impl? = if (base === null) Impl() else null

            private var __resolving: Boolean = false

            private var __resolved: MusicTask? = null

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

            override var status: String
                get() = (__modified ?: __base!!).status
                set(status) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__statusValue = status
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

            override var prompt: String?
                get() = (__modified ?: __base!!).prompt
                set(prompt) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__promptValue = prompt
                    __tmpModified.__promptLoaded = true
                }

            override var mv: String?
                get() = (__modified ?: __base!!).mv
                set(mv) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__mvValue = mv
                    __tmpModified.__mvLoaded = true
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

            override var videoUrl: String?
                get() = (__modified ?: __base!!).videoUrl
                set(videoUrl) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__videoUrlValue = videoUrl
                    __tmpModified.__videoUrlLoaded = true
                }

            override var errorMessage: String?
                get() = (__modified ?: __base!!).errorMessage
                set(errorMessage) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__errorMessageValue = errorMessage
                    __tmpModified.__errorMessageLoaded = true
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

            override var updatedAt: LocalDateTime
                get() = (__modified ?: __base!!).updatedAt
                set(updatedAt) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__updatedAtValue = updatedAt
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
                    SLOT_TASK_ID ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__taskIdValue = null
                    SLOT_STATUS ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__statusValue = null
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
                    SLOT_PROMPT ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__promptValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__promptLoaded = false
                        }
                    SLOT_MV ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__mvValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__mvLoaded = false
                        }
                    SLOT_AUDIO_URL ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__audioUrlValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__audioUrlLoaded = false
                        }
                    SLOT_VIDEO_URL ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__videoUrlValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__videoUrlLoaded = false
                        }
                    SLOT_ERROR_MESSAGE ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__errorMessageValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__errorMessageLoaded = false
                        }
                    SLOT_CREATED_AT ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__createdAtValue = null
                    SLOT_UPDATED_AT ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__updatedAtValue = null
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.MusicTask\": " + 
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
                    "taskId" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__taskIdValue = null
                    "status" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__statusValue = null
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
                    "prompt" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__promptValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__promptLoaded = false
                        }
                    "mv" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__mvValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__mvLoaded = false
                        }
                    "audioUrl" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__audioUrlValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__audioUrlLoaded = false
                        }
                    "videoUrl" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__videoUrlValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__videoUrlLoaded = false
                        }
                    "errorMessage" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__errorMessageValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__errorMessageLoaded = false
                        }
                    "createdAt" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__createdAtValue = null
                    "updatedAt" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__updatedAtValue = null
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.MusicTask\": " + 
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
                    SLOT_TASK_ID ->
                    	this.taskId = value as String?
                    	?: throw IllegalArgumentException("'taskId cannot be null")
                    SLOT_STATUS ->
                    	this.status = value as String?
                    	?: throw IllegalArgumentException("'status cannot be null")
                    SLOT_TITLE ->
                    	this.title = value as String?
                    SLOT_TAGS ->
                    	this.tags = value as String?
                    SLOT_PROMPT ->
                    	this.prompt = value as String?
                    SLOT_MV ->
                    	this.mv = value as String?
                    SLOT_AUDIO_URL ->
                    	this.audioUrl = value as String?
                    SLOT_VIDEO_URL ->
                    	this.videoUrl = value as String?
                    SLOT_ERROR_MESSAGE ->
                    	this.errorMessage = value as String?
                    SLOT_CREATED_AT ->
                    	this.createdAt = value as LocalDateTime?
                    	?: throw IllegalArgumentException("'createdAt cannot be null")
                    SLOT_UPDATED_AT ->
                    	this.updatedAt = value as LocalDateTime?
                    	?: throw IllegalArgumentException("'updatedAt cannot be null")
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.MusicTask\": " + 
                        prop
                    )

                }
            }

            override fun __set(prop: String, `value`: Any?) {
                when (prop) {
                    "id" ->
                    	this.id = value as Long?
                    	?: throw IllegalArgumentException("'id cannot be null")
                    "taskId" ->
                    	this.taskId = value as String?
                    	?: throw IllegalArgumentException("'taskId cannot be null")
                    "status" ->
                    	this.status = value as String?
                    	?: throw IllegalArgumentException("'status cannot be null")
                    "title" ->
                    	this.title = value as String?
                    "tags" ->
                    	this.tags = value as String?
                    "prompt" ->
                    	this.prompt = value as String?
                    "mv" ->
                    	this.mv = value as String?
                    "audioUrl" ->
                    	this.audioUrl = value as String?
                    "videoUrl" ->
                    	this.videoUrl = value as String?
                    "errorMessage" ->
                    	this.errorMessage = value as String?
                    "createdAt" ->
                    	this.createdAt = value as LocalDateTime?
                    	?: throw IllegalArgumentException("'createdAt cannot be null")
                    "updatedAt" ->
                    	this.updatedAt = value as LocalDateTime?
                    	?: throw IllegalArgumentException("'updatedAt cannot be null")
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.MusicTask\": " + 
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
                        Visibility.of(12).also{
                            (__modified ?: __base!!.clone())
                            .also { __modified = it }.__visibility = it}
                    }
                    ?: return
                when (prop.asIndex()) {
                    -1 ->
                    	__show(prop.asName(), visible)
                    SLOT_ID ->
                    	__visibility.show(SLOT_ID, visible)
                    SLOT_TASK_ID ->
                    	__visibility.show(SLOT_TASK_ID, visible)
                    SLOT_STATUS ->
                    	__visibility.show(SLOT_STATUS, visible)
                    SLOT_TITLE ->
                    	__visibility.show(SLOT_TITLE, visible)
                    SLOT_TAGS ->
                    	__visibility.show(SLOT_TAGS, visible)
                    SLOT_PROMPT ->
                    	__visibility.show(SLOT_PROMPT, visible)
                    SLOT_MV ->
                    	__visibility.show(SLOT_MV, visible)
                    SLOT_AUDIO_URL ->
                    	__visibility.show(SLOT_AUDIO_URL, visible)
                    SLOT_VIDEO_URL ->
                    	__visibility.show(SLOT_VIDEO_URL, visible)
                    SLOT_ERROR_MESSAGE ->
                    	__visibility.show(SLOT_ERROR_MESSAGE, visible)
                    SLOT_CREATED_AT ->
                    	__visibility.show(SLOT_CREATED_AT, visible)
                    SLOT_UPDATED_AT ->
                    	__visibility.show(SLOT_UPDATED_AT, visible)
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
                        Visibility.of(12).also{
                            (__modified ?: __base!!.clone())
                            .also { __modified = it }.__visibility = it}
                    }
                    ?: return
                when (prop) {
                    "id" ->
                    	__visibility.show(SLOT_ID, visible)
                    "taskId" ->
                    	__visibility.show(SLOT_TASK_ID, visible)
                    "status" ->
                    	__visibility.show(SLOT_STATUS, visible)
                    "title" ->
                    	__visibility.show(SLOT_TITLE, visible)
                    "tags" ->
                    	__visibility.show(SLOT_TAGS, visible)
                    "prompt" ->
                    	__visibility.show(SLOT_PROMPT, visible)
                    "mv" ->
                    	__visibility.show(SLOT_MV, visible)
                    "audioUrl" ->
                    	__visibility.show(SLOT_AUDIO_URL, visible)
                    "videoUrl" ->
                    	__visibility.show(SLOT_VIDEO_URL, visible)
                    "errorMessage" ->
                    	__visibility.show(SLOT_ERROR_MESSAGE, visible)
                    "createdAt" ->
                    	__visibility.show(SLOT_CREATED_AT, visible)
                    "updatedAt" ->
                    	__visibility.show(SLOT_UPDATED_AT, visible)
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

    @GeneratedBy(type = MusicTask::class)
    public class Builder {
        private val __draft: `$`.DraftImpl

        public constructor(base: MusicTask?) {
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

        public fun taskId(taskId: String?): Builder {
            if (taskId !== null) {
                __draft.taskId = taskId
                __draft.__show(PropId.byIndex(`$`.SLOT_TASK_ID), true)
            }
            return this
        }

        public fun status(status: String?): Builder {
            if (status !== null) {
                __draft.status = status
                __draft.__show(PropId.byIndex(`$`.SLOT_STATUS), true)
            }
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

        public fun prompt(prompt: String?): Builder {
            __draft.prompt = prompt
            __draft.__show(PropId.byIndex(`$`.SLOT_PROMPT), true)
            return this
        }

        public fun mv(mv: String?): Builder {
            __draft.mv = mv
            __draft.__show(PropId.byIndex(`$`.SLOT_MV), true)
            return this
        }

        public fun audioUrl(audioUrl: String?): Builder {
            __draft.audioUrl = audioUrl
            __draft.__show(PropId.byIndex(`$`.SLOT_AUDIO_URL), true)
            return this
        }

        public fun videoUrl(videoUrl: String?): Builder {
            __draft.videoUrl = videoUrl
            __draft.__show(PropId.byIndex(`$`.SLOT_VIDEO_URL), true)
            return this
        }

        public fun errorMessage(errorMessage: String?): Builder {
            __draft.errorMessage = errorMessage
            __draft.__show(PropId.byIndex(`$`.SLOT_ERROR_MESSAGE), true)
            return this
        }

        public fun createdAt(createdAt: LocalDateTime?): Builder {
            if (createdAt !== null) {
                __draft.createdAt = createdAt
                __draft.__show(PropId.byIndex(`$`.SLOT_CREATED_AT), true)
            }
            return this
        }

        public fun updatedAt(updatedAt: LocalDateTime?): Builder {
            if (updatedAt !== null) {
                __draft.updatedAt = updatedAt
                __draft.__show(PropId.byIndex(`$`.SLOT_UPDATED_AT), true)
            }
            return this
        }

        public fun build(): MusicTask = __draft.__unwrap() as MusicTask
    }
}

@GeneratedBy(type = MusicTask::class)
public fun ImmutableCreator<MusicTask>.`by`(resolveImmediately: Boolean = false, block: MusicTaskDraft.() -> Unit): MusicTask = MusicTaskDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = MusicTask::class)
public fun ImmutableCreator<MusicTask>.`by`(base: MusicTask?, resolveImmediately: Boolean = false): MusicTask = MusicTaskDraft.`$`.produce(base, resolveImmediately)

@GeneratedBy(type = MusicTask::class)
public fun ImmutableCreator<MusicTask>.`by`(
    base: MusicTask?,
    resolveImmediately: Boolean = false,
    block: MusicTaskDraft.() -> Unit,
): MusicTask = MusicTaskDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = MusicTask::class)
public fun MusicTask(resolveImmediately: Boolean = false, block: MusicTaskDraft.() -> Unit): MusicTask = MusicTaskDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = MusicTask::class)
public fun MusicTask(
    base: MusicTask?,
    resolveImmediately: Boolean = false,
    block: MusicTaskDraft.() -> Unit,
): MusicTask = MusicTaskDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = MusicTask::class)
public fun MutableList<MusicTaskDraft>.addBy(resolveImmediately: Boolean = false, block: MusicTaskDraft.() -> Unit): MutableList<MusicTaskDraft> {
    add(MusicTaskDraft.`$`.produce(null, resolveImmediately, block) as MusicTaskDraft)
    return this
}

@GeneratedBy(type = MusicTask::class)
public fun MutableList<MusicTaskDraft>.addBy(base: MusicTask?, resolveImmediately: Boolean = false): MutableList<MusicTaskDraft> {
    add(MusicTaskDraft.`$`.produce(base, resolveImmediately) as MusicTaskDraft)
    return this
}

@GeneratedBy(type = MusicTask::class)
public fun MutableList<MusicTaskDraft>.addBy(
    base: MusicTask?,
    resolveImmediately: Boolean = false,
    block: MusicTaskDraft.() -> Unit,
): MutableList<MusicTaskDraft> {
    add(MusicTaskDraft.`$`.produce(base, resolveImmediately, block) as MusicTaskDraft)
    return this
}

@GeneratedBy(type = MusicTask::class)
public fun MusicTask.copy(resolveImmediately: Boolean = false, block: MusicTaskDraft.() -> Unit): MusicTask = MusicTaskDraft.`$`.produce(this, resolveImmediately, block)
