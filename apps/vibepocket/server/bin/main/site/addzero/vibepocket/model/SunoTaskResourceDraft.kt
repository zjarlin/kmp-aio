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
@GeneratedBy(type = SunoTaskResource::class)
public interface SunoTaskResourceDraft : SunoTaskResource, Draft {
    override var id: Long

    override var taskId: String

    override var type: String

    override var status: String

    override var requestJson: String?

    override var tracksJson: String

    override var detailJson: String?

    override var errorMessage: String?

    override var createdAt: LocalDateTime

    override var updatedAt: LocalDateTime

    @GeneratedBy(type = SunoTaskResource::class)
    public object `$` {
        public const val SLOT_ID: Int = 0

        public const val SLOT_TASK_ID: Int = 1

        public const val SLOT_TYPE: Int = 2

        public const val SLOT_STATUS: Int = 3

        public const val SLOT_REQUEST_JSON: Int = 4

        public const val SLOT_TRACKS_JSON: Int = 5

        public const val SLOT_DETAIL_JSON: Int = 6

        public const val SLOT_ERROR_MESSAGE: Int = 7

        public const val SLOT_CREATED_AT: Int = 8

        public const val SLOT_UPDATED_AT: Int = 9

        public val type: ImmutableType = ImmutableType
            .newBuilder(
                "0.10.6",
                SunoTaskResource::class,
                listOf(

                ),
            ) { ctx, base ->
                DraftImpl(ctx, base as SunoTaskResource?)
            }
            .id(SLOT_ID, "id", Long::class.java)
            .key(SLOT_TASK_ID, "taskId", String::class.java, false)
            .add(SLOT_TYPE, "type", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_STATUS, "status", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_REQUEST_JSON, "requestJson", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_TRACKS_JSON, "tracksJson", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_DETAIL_JSON, "detailJson", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_ERROR_MESSAGE, "errorMessage", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_CREATED_AT, "createdAt", ImmutablePropCategory.SCALAR, LocalDateTime::class.java, false)
            .add(SLOT_UPDATED_AT, "updatedAt", ImmutablePropCategory.SCALAR, LocalDateTime::class.java, false)
            .build()

        public fun produce(base: SunoTaskResource? = null, resolveImmediately: Boolean = false): SunoTaskResource {
            val consumer = DraftConsumer<SunoTaskResourceDraft> {}
            return Internal.produce(type, base, resolveImmediately, consumer) as SunoTaskResource
        }

        public fun produce(
            base: SunoTaskResource? = null,
            resolveImmediately: Boolean = false,
            block: SunoTaskResourceDraft.() -> Unit,
        ): SunoTaskResource {
            val consumer = DraftConsumer<SunoTaskResourceDraft> { block(it) }
            return Internal.produce(type, base, resolveImmediately, consumer) as SunoTaskResource
        }

        @GeneratedBy(type = SunoTaskResource::class)
        @JsonPropertyOrder("dummyPropForJacksonError__", "id", "taskId", "type", "status", "requestJson", "tracksJson", "detailJson", "errorMessage", "createdAt", "updatedAt")
        private abstract interface Implementor : SunoTaskResource, ImmutableSpi {
            public val dummyPropForJacksonError__: Int
                get() = throw ImmutableModuleRequiredException()

            override fun __get(prop: PropId): Any? = when (prop.asIndex()) {
                -1 ->
                	__get(prop.asName())
                SLOT_ID ->
                	id
                SLOT_TASK_ID ->
                	taskId
                SLOT_TYPE ->
                	type
                SLOT_STATUS ->
                	status
                SLOT_REQUEST_JSON ->
                	requestJson
                SLOT_TRACKS_JSON ->
                	tracksJson
                SLOT_DETAIL_JSON ->
                	detailJson
                SLOT_ERROR_MESSAGE ->
                	errorMessage
                SLOT_CREATED_AT ->
                	createdAt
                SLOT_UPDATED_AT ->
                	updatedAt
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.SunoTaskResource\": " + 
                    prop
                )

            }

            override fun __get(prop: String): Any? = when (prop) {
                "id" ->
                	id
                "taskId" ->
                	taskId
                "type" ->
                	type
                "status" ->
                	status
                "requestJson" ->
                	requestJson
                "tracksJson" ->
                	tracksJson
                "detailJson" ->
                	detailJson
                "errorMessage" ->
                	errorMessage
                "createdAt" ->
                	createdAt
                "updatedAt" ->
                	updatedAt
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.SunoTaskResource\": " + 
                    prop
                )

            }

            override fun __type(): ImmutableType = `$`.type
        }

        @GeneratedBy(type = SunoTaskResource::class)
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
            internal var __typeValue: String? = null

            @get:JsonIgnore
            internal var __statusValue: String? = null

            @get:JsonIgnore
            internal var __requestJsonValue: String? = null

            @get:JsonIgnore
            internal var __requestJsonLoaded: Boolean = false

            @get:JsonIgnore
            internal var __tracksJsonValue: String? = null

            @get:JsonIgnore
            internal var __detailJsonValue: String? = null

            @get:JsonIgnore
            internal var __detailJsonLoaded: Boolean = false

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
                        throw UnloadedException(SunoTaskResource::class.java, "id")
                    }
                    return __idValue
                }

            override val taskId: String
                get() {
                    val __taskIdValue = this.__taskIdValue
                    if (__taskIdValue === null) {
                        throw UnloadedException(SunoTaskResource::class.java, "taskId")
                    }
                    return __taskIdValue
                }

            override val type: String
                get() {
                    val __typeValue = this.__typeValue
                    if (__typeValue === null) {
                        throw UnloadedException(SunoTaskResource::class.java, "type")
                    }
                    return __typeValue
                }

            override val status: String
                get() {
                    val __statusValue = this.__statusValue
                    if (__statusValue === null) {
                        throw UnloadedException(SunoTaskResource::class.java, "status")
                    }
                    return __statusValue
                }

            override val requestJson: String?
                get() {
                    if (!__requestJsonLoaded) {
                        throw UnloadedException(SunoTaskResource::class.java, "requestJson")
                    }
                    return __requestJsonValue
                }

            override val tracksJson: String
                get() {
                    val __tracksJsonValue = this.__tracksJsonValue
                    if (__tracksJsonValue === null) {
                        throw UnloadedException(SunoTaskResource::class.java, "tracksJson")
                    }
                    return __tracksJsonValue
                }

            override val detailJson: String?
                get() {
                    if (!__detailJsonLoaded) {
                        throw UnloadedException(SunoTaskResource::class.java, "detailJson")
                    }
                    return __detailJsonValue
                }

            override val errorMessage: String?
                get() {
                    if (!__errorMessageLoaded) {
                        throw UnloadedException(SunoTaskResource::class.java, "errorMessage")
                    }
                    return __errorMessageValue
                }

            override val createdAt: LocalDateTime
                get() {
                    val __createdAtValue = this.__createdAtValue
                    if (__createdAtValue === null) {
                        throw UnloadedException(SunoTaskResource::class.java, "createdAt")
                    }
                    return __createdAtValue
                }

            override val updatedAt: LocalDateTime
                get() {
                    val __updatedAtValue = this.__updatedAtValue
                    if (__updatedAtValue === null) {
                        throw UnloadedException(SunoTaskResource::class.java, "updatedAt")
                    }
                    return __updatedAtValue
                }

            public override fun clone(): Impl {
                val copy = super.clone() as Impl
                val originalVisibility = this.__visibility
                if (originalVisibility != null) {
                    val newVisibility = Visibility.of(10)
                    for (propId in 0 until 10) {
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
                SLOT_TYPE ->
                	__typeValue !== null
                SLOT_STATUS ->
                	__statusValue !== null
                SLOT_REQUEST_JSON ->
                	__requestJsonLoaded
                SLOT_TRACKS_JSON ->
                	__tracksJsonValue !== null
                SLOT_DETAIL_JSON ->
                	__detailJsonLoaded
                SLOT_ERROR_MESSAGE ->
                	__errorMessageLoaded
                SLOT_CREATED_AT ->
                	__createdAtValue !== null
                SLOT_UPDATED_AT ->
                	__updatedAtValue !== null
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.SunoTaskResource\": " + 
                    prop
                )

            }

            override fun __isLoaded(prop: String): Boolean = when (prop) {
                "id" ->
                	__idLoaded
                "taskId" ->
                	__taskIdValue !== null
                "type" ->
                	__typeValue !== null
                "status" ->
                	__statusValue !== null
                "requestJson" ->
                	__requestJsonLoaded
                "tracksJson" ->
                	__tracksJsonValue !== null
                "detailJson" ->
                	__detailJsonLoaded
                "errorMessage" ->
                	__errorMessageLoaded
                "createdAt" ->
                	__createdAtValue !== null
                "updatedAt" ->
                	__updatedAtValue !== null
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.SunoTaskResource\": " + 
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
                    SLOT_TYPE ->
                    	__visibility.visible(SLOT_TYPE)
                    SLOT_STATUS ->
                    	__visibility.visible(SLOT_STATUS)
                    SLOT_REQUEST_JSON ->
                    	__visibility.visible(SLOT_REQUEST_JSON)
                    SLOT_TRACKS_JSON ->
                    	__visibility.visible(SLOT_TRACKS_JSON)
                    SLOT_DETAIL_JSON ->
                    	__visibility.visible(SLOT_DETAIL_JSON)
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
                    "type" ->
                    	__visibility.visible(SLOT_TYPE)
                    "status" ->
                    	__visibility.visible(SLOT_STATUS)
                    "requestJson" ->
                    	__visibility.visible(SLOT_REQUEST_JSON)
                    "tracksJson" ->
                    	__visibility.visible(SLOT_TRACKS_JSON)
                    "detailJson" ->
                    	__visibility.visible(SLOT_DETAIL_JSON)
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
                if (__typeValue !== null) {
                    hash = 31 * hash + __typeValue.hashCode()
                }
                if (__statusValue !== null) {
                    hash = 31 * hash + __statusValue.hashCode()
                }
                if (__requestJsonLoaded) {
                    hash = 31 * hash + (__requestJsonValue?.hashCode() ?: 0)
                }
                if (__tracksJsonValue !== null) {
                    hash = 31 * hash + __tracksJsonValue.hashCode()
                }
                if (__detailJsonLoaded) {
                    hash = 31 * hash + (__detailJsonValue?.hashCode() ?: 0)
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
                if (__typeValue !== null) {
                    hash = 31 * hash + __typeValue.hashCode()
                }
                if (__statusValue !== null) {
                    hash = 31 * hash + __statusValue.hashCode()
                }
                if (__requestJsonLoaded) {
                    hash = 31 * hash + (__requestJsonValue?.hashCode() ?: 0)
                }
                if (__tracksJsonValue !== null) {
                    hash = 31 * hash + __tracksJsonValue.hashCode()
                }
                if (__detailJsonLoaded) {
                    hash = 31 * hash + (__detailJsonValue?.hashCode() ?: 0)
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
                if (__isVisible(PropId.byIndex(SLOT_TYPE)) != __other.__isVisible(PropId.byIndex(SLOT_TYPE))) {
                    return false
                }
                val __typeLoaded = 
                    this.__typeValue !== null
                if (__typeLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TYPE)))) {
                    return false
                }
                if (__typeLoaded && this.__typeValue != __other.type) {
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
                if (__isVisible(PropId.byIndex(SLOT_REQUEST_JSON)) != __other.__isVisible(PropId.byIndex(SLOT_REQUEST_JSON))) {
                    return false
                }
                val __requestJsonLoaded = 
                    this.__requestJsonLoaded
                if (__requestJsonLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_REQUEST_JSON)))) {
                    return false
                }
                if (__requestJsonLoaded && this.__requestJsonValue != __other.requestJson) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_TRACKS_JSON)) != __other.__isVisible(PropId.byIndex(SLOT_TRACKS_JSON))) {
                    return false
                }
                val __tracksJsonLoaded = 
                    this.__tracksJsonValue !== null
                if (__tracksJsonLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TRACKS_JSON)))) {
                    return false
                }
                if (__tracksJsonLoaded && this.__tracksJsonValue != __other.tracksJson) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_DETAIL_JSON)) != __other.__isVisible(PropId.byIndex(SLOT_DETAIL_JSON))) {
                    return false
                }
                val __detailJsonLoaded = 
                    this.__detailJsonLoaded
                if (__detailJsonLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DETAIL_JSON)))) {
                    return false
                }
                if (__detailJsonLoaded && this.__detailJsonValue != __other.detailJson) {
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
                if (__isVisible(PropId.byIndex(SLOT_TYPE)) != __other.__isVisible(PropId.byIndex(SLOT_TYPE))) {
                    return false
                }
                val __typeLoaded = 
                    this.__typeValue !== null
                if (__typeLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TYPE)))) {
                    return false
                }
                if (__typeLoaded && this.__typeValue != __other.type) {
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
                if (__isVisible(PropId.byIndex(SLOT_REQUEST_JSON)) != __other.__isVisible(PropId.byIndex(SLOT_REQUEST_JSON))) {
                    return false
                }
                val __requestJsonLoaded = 
                    this.__requestJsonLoaded
                if (__requestJsonLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_REQUEST_JSON)))) {
                    return false
                }
                if (__requestJsonLoaded && this.__requestJsonValue != __other.requestJson) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_TRACKS_JSON)) != __other.__isVisible(PropId.byIndex(SLOT_TRACKS_JSON))) {
                    return false
                }
                val __tracksJsonLoaded = 
                    this.__tracksJsonValue !== null
                if (__tracksJsonLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_TRACKS_JSON)))) {
                    return false
                }
                if (__tracksJsonLoaded && this.__tracksJsonValue != __other.tracksJson) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_DETAIL_JSON)) != __other.__isVisible(PropId.byIndex(SLOT_DETAIL_JSON))) {
                    return false
                }
                val __detailJsonLoaded = 
                    this.__detailJsonLoaded
                if (__detailJsonLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DETAIL_JSON)))) {
                    return false
                }
                if (__detailJsonLoaded && this.__detailJsonValue != __other.detailJson) {
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

        @GeneratedBy(type = SunoTaskResource::class)
        internal class DraftImpl(
            ctx: DraftContext?,
            base: SunoTaskResource?,
        ) : Implementor,
            SunoTaskResourceDraft,
            DraftSpi {
            private val __ctx: DraftContext? = ctx

            private val __base: Impl? = base as Impl?

            private var __modified: Impl? = if (base === null) Impl() else null

            private var __resolving: Boolean = false

            private var __resolved: SunoTaskResource? = null

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

            override var type: String
                get() = (__modified ?: __base!!).type
                set(type) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__typeValue = type
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

            override var requestJson: String?
                get() = (__modified ?: __base!!).requestJson
                set(requestJson) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__requestJsonValue = requestJson
                    __tmpModified.__requestJsonLoaded = true
                }

            override var tracksJson: String
                get() = (__modified ?: __base!!).tracksJson
                set(tracksJson) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__tracksJsonValue = tracksJson
                }

            override var detailJson: String?
                get() = (__modified ?: __base!!).detailJson
                set(detailJson) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__detailJsonValue = detailJson
                    __tmpModified.__detailJsonLoaded = true
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
                    SLOT_TYPE ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__typeValue = null
                    SLOT_STATUS ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__statusValue = null
                    SLOT_REQUEST_JSON ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__requestJsonValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__requestJsonLoaded = false
                        }
                    SLOT_TRACKS_JSON ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__tracksJsonValue = null
                    SLOT_DETAIL_JSON ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__detailJsonValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__detailJsonLoaded = false
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
                        " for \"site.addzero.vibepocket.model.SunoTaskResource\": " + 
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
                    "type" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__typeValue = null
                    "status" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__statusValue = null
                    "requestJson" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__requestJsonValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__requestJsonLoaded = false
                        }
                    "tracksJson" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__tracksJsonValue = null
                    "detailJson" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__detailJsonValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__detailJsonLoaded = false
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
                        " for \"site.addzero.vibepocket.model.SunoTaskResource\": " + 
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
                    SLOT_TYPE ->
                    	this.type = value as String?
                    	?: throw IllegalArgumentException("'type cannot be null")
                    SLOT_STATUS ->
                    	this.status = value as String?
                    	?: throw IllegalArgumentException("'status cannot be null")
                    SLOT_REQUEST_JSON ->
                    	this.requestJson = value as String?
                    SLOT_TRACKS_JSON ->
                    	this.tracksJson = value as String?
                    	?: throw IllegalArgumentException("'tracksJson cannot be null")
                    SLOT_DETAIL_JSON ->
                    	this.detailJson = value as String?
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
                        " for \"site.addzero.vibepocket.model.SunoTaskResource\": " + 
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
                    "type" ->
                    	this.type = value as String?
                    	?: throw IllegalArgumentException("'type cannot be null")
                    "status" ->
                    	this.status = value as String?
                    	?: throw IllegalArgumentException("'status cannot be null")
                    "requestJson" ->
                    	this.requestJson = value as String?
                    "tracksJson" ->
                    	this.tracksJson = value as String?
                    	?: throw IllegalArgumentException("'tracksJson cannot be null")
                    "detailJson" ->
                    	this.detailJson = value as String?
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
                        " for \"site.addzero.vibepocket.model.SunoTaskResource\": " + 
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
                        Visibility.of(10).also{
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
                    SLOT_TYPE ->
                    	__visibility.show(SLOT_TYPE, visible)
                    SLOT_STATUS ->
                    	__visibility.show(SLOT_STATUS, visible)
                    SLOT_REQUEST_JSON ->
                    	__visibility.show(SLOT_REQUEST_JSON, visible)
                    SLOT_TRACKS_JSON ->
                    	__visibility.show(SLOT_TRACKS_JSON, visible)
                    SLOT_DETAIL_JSON ->
                    	__visibility.show(SLOT_DETAIL_JSON, visible)
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
                        Visibility.of(10).also{
                            (__modified ?: __base!!.clone())
                            .also { __modified = it }.__visibility = it}
                    }
                    ?: return
                when (prop) {
                    "id" ->
                    	__visibility.show(SLOT_ID, visible)
                    "taskId" ->
                    	__visibility.show(SLOT_TASK_ID, visible)
                    "type" ->
                    	__visibility.show(SLOT_TYPE, visible)
                    "status" ->
                    	__visibility.show(SLOT_STATUS, visible)
                    "requestJson" ->
                    	__visibility.show(SLOT_REQUEST_JSON, visible)
                    "tracksJson" ->
                    	__visibility.show(SLOT_TRACKS_JSON, visible)
                    "detailJson" ->
                    	__visibility.show(SLOT_DETAIL_JSON, visible)
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

    @GeneratedBy(type = SunoTaskResource::class)
    public class Builder {
        private val __draft: `$`.DraftImpl

        public constructor(base: SunoTaskResource?) {
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

        public fun type(type: String?): Builder {
            if (type !== null) {
                __draft.type = type
                __draft.__show(PropId.byIndex(`$`.SLOT_TYPE), true)
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

        public fun requestJson(requestJson: String?): Builder {
            __draft.requestJson = requestJson
            __draft.__show(PropId.byIndex(`$`.SLOT_REQUEST_JSON), true)
            return this
        }

        public fun tracksJson(tracksJson: String?): Builder {
            if (tracksJson !== null) {
                __draft.tracksJson = tracksJson
                __draft.__show(PropId.byIndex(`$`.SLOT_TRACKS_JSON), true)
            }
            return this
        }

        public fun detailJson(detailJson: String?): Builder {
            __draft.detailJson = detailJson
            __draft.__show(PropId.byIndex(`$`.SLOT_DETAIL_JSON), true)
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

        public fun build(): SunoTaskResource = __draft.__unwrap() as SunoTaskResource
    }
}

@GeneratedBy(type = SunoTaskResource::class)
public fun ImmutableCreator<SunoTaskResource>.`by`(resolveImmediately: Boolean = false, block: SunoTaskResourceDraft.() -> Unit): SunoTaskResource = SunoTaskResourceDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = SunoTaskResource::class)
public fun ImmutableCreator<SunoTaskResource>.`by`(base: SunoTaskResource?, resolveImmediately: Boolean = false): SunoTaskResource = SunoTaskResourceDraft.`$`.produce(base, resolveImmediately)

@GeneratedBy(type = SunoTaskResource::class)
public fun ImmutableCreator<SunoTaskResource>.`by`(
    base: SunoTaskResource?,
    resolveImmediately: Boolean = false,
    block: SunoTaskResourceDraft.() -> Unit,
): SunoTaskResource = SunoTaskResourceDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = SunoTaskResource::class)
public fun SunoTaskResource(resolveImmediately: Boolean = false, block: SunoTaskResourceDraft.() -> Unit): SunoTaskResource = SunoTaskResourceDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = SunoTaskResource::class)
public fun SunoTaskResource(
    base: SunoTaskResource?,
    resolveImmediately: Boolean = false,
    block: SunoTaskResourceDraft.() -> Unit,
): SunoTaskResource = SunoTaskResourceDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = SunoTaskResource::class)
public fun MutableList<SunoTaskResourceDraft>.addBy(resolveImmediately: Boolean = false, block: SunoTaskResourceDraft.() -> Unit): MutableList<SunoTaskResourceDraft> {
    add(SunoTaskResourceDraft.`$`.produce(null, resolveImmediately, block) as SunoTaskResourceDraft)
    return this
}

@GeneratedBy(type = SunoTaskResource::class)
public fun MutableList<SunoTaskResourceDraft>.addBy(base: SunoTaskResource?, resolveImmediately: Boolean = false): MutableList<SunoTaskResourceDraft> {
    add(SunoTaskResourceDraft.`$`.produce(base, resolveImmediately) as SunoTaskResourceDraft)
    return this
}

@GeneratedBy(type = SunoTaskResource::class)
public fun MutableList<SunoTaskResourceDraft>.addBy(
    base: SunoTaskResource?,
    resolveImmediately: Boolean = false,
    block: SunoTaskResourceDraft.() -> Unit,
): MutableList<SunoTaskResourceDraft> {
    add(SunoTaskResourceDraft.`$`.produce(base, resolveImmediately, block) as SunoTaskResourceDraft)
    return this
}

@GeneratedBy(type = SunoTaskResource::class)
public fun SunoTaskResource.copy(resolveImmediately: Boolean = false, block: SunoTaskResourceDraft.() -> Unit): SunoTaskResource = SunoTaskResourceDraft.`$`.produce(this, resolveImmediately, block)
