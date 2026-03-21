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
@GeneratedBy(type = MusicHistory::class)
public interface MusicHistoryDraft : MusicHistory, Draft {
    override var id: Long

    override var taskId: String

    override var type: String

    override var status: String

    override var tracksJson: String

    override var createdAt: LocalDateTime

    @GeneratedBy(type = MusicHistory::class)
    public object `$` {
        public const val SLOT_ID: Int = 0

        public const val SLOT_TASK_ID: Int = 1

        public const val SLOT_TYPE: Int = 2

        public const val SLOT_STATUS: Int = 3

        public const val SLOT_TRACKS_JSON: Int = 4

        public const val SLOT_CREATED_AT: Int = 5

        public val type: ImmutableType = ImmutableType
            .newBuilder(
                "0.10.6",
                MusicHistory::class,
                listOf(

                ),
            ) { ctx, base ->
                DraftImpl(ctx, base as MusicHistory?)
            }
            .id(SLOT_ID, "id", Long::class.java)
            .key(SLOT_TASK_ID, "taskId", String::class.java, false)
            .add(SLOT_TYPE, "type", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_STATUS, "status", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_TRACKS_JSON, "tracksJson", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_CREATED_AT, "createdAt", ImmutablePropCategory.SCALAR, LocalDateTime::class.java, false)
            .build()

        public fun produce(base: MusicHistory? = null, resolveImmediately: Boolean = false): MusicHistory {
            val consumer = DraftConsumer<MusicHistoryDraft> {}
            return Internal.produce(type, base, resolveImmediately, consumer) as MusicHistory
        }

        public fun produce(
            base: MusicHistory? = null,
            resolveImmediately: Boolean = false,
            block: MusicHistoryDraft.() -> Unit,
        ): MusicHistory {
            val consumer = DraftConsumer<MusicHistoryDraft> { block(it) }
            return Internal.produce(type, base, resolveImmediately, consumer) as MusicHistory
        }

        @GeneratedBy(type = MusicHistory::class)
        @JsonPropertyOrder("dummyPropForJacksonError__", "id", "taskId", "type", "status", "tracksJson", "createdAt")
        private abstract interface Implementor : MusicHistory, ImmutableSpi {
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
                SLOT_TRACKS_JSON ->
                	tracksJson
                SLOT_CREATED_AT ->
                	createdAt
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.MusicHistory\": " + 
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
                "tracksJson" ->
                	tracksJson
                "createdAt" ->
                	createdAt
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.MusicHistory\": " + 
                    prop
                )

            }

            override fun __type(): ImmutableType = `$`.type
        }

        @GeneratedBy(type = MusicHistory::class)
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
            internal var __tracksJsonValue: String? = null

            @get:JsonIgnore
            internal var __createdAtValue: LocalDateTime? = null

            override val id: Long
                get() {
                    if (!__idLoaded) {
                        throw UnloadedException(MusicHistory::class.java, "id")
                    }
                    return __idValue
                }

            override val taskId: String
                get() {
                    val __taskIdValue = this.__taskIdValue
                    if (__taskIdValue === null) {
                        throw UnloadedException(MusicHistory::class.java, "taskId")
                    }
                    return __taskIdValue
                }

            override val type: String
                get() {
                    val __typeValue = this.__typeValue
                    if (__typeValue === null) {
                        throw UnloadedException(MusicHistory::class.java, "type")
                    }
                    return __typeValue
                }

            override val status: String
                get() {
                    val __statusValue = this.__statusValue
                    if (__statusValue === null) {
                        throw UnloadedException(MusicHistory::class.java, "status")
                    }
                    return __statusValue
                }

            override val tracksJson: String
                get() {
                    val __tracksJsonValue = this.__tracksJsonValue
                    if (__tracksJsonValue === null) {
                        throw UnloadedException(MusicHistory::class.java, "tracksJson")
                    }
                    return __tracksJsonValue
                }

            override val createdAt: LocalDateTime
                get() {
                    val __createdAtValue = this.__createdAtValue
                    if (__createdAtValue === null) {
                        throw UnloadedException(MusicHistory::class.java, "createdAt")
                    }
                    return __createdAtValue
                }

            public override fun clone(): Impl {
                val copy = super.clone() as Impl
                val originalVisibility = this.__visibility
                if (originalVisibility != null) {
                    val newVisibility = Visibility.of(6)
                    for (propId in 0 until 6) {
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
                SLOT_TRACKS_JSON ->
                	__tracksJsonValue !== null
                SLOT_CREATED_AT ->
                	__createdAtValue !== null
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.MusicHistory\": " + 
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
                "tracksJson" ->
                	__tracksJsonValue !== null
                "createdAt" ->
                	__createdAtValue !== null
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.MusicHistory\": " + 
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
                    SLOT_TRACKS_JSON ->
                    	__visibility.visible(SLOT_TRACKS_JSON)
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
                    "taskId" ->
                    	__visibility.visible(SLOT_TASK_ID)
                    "type" ->
                    	__visibility.visible(SLOT_TYPE)
                    "status" ->
                    	__visibility.visible(SLOT_STATUS)
                    "tracksJson" ->
                    	__visibility.visible(SLOT_TRACKS_JSON)
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
                if (__taskIdValue !== null) {
                    hash = 31 * hash + __taskIdValue.hashCode()
                }
                if (__typeValue !== null) {
                    hash = 31 * hash + __typeValue.hashCode()
                }
                if (__statusValue !== null) {
                    hash = 31 * hash + __statusValue.hashCode()
                }
                if (__tracksJsonValue !== null) {
                    hash = 31 * hash + __tracksJsonValue.hashCode()
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
                if (__taskIdValue !== null) {
                    hash = 31 * hash + __taskIdValue.hashCode()
                }
                if (__typeValue !== null) {
                    hash = 31 * hash + __typeValue.hashCode()
                }
                if (__statusValue !== null) {
                    hash = 31 * hash + __statusValue.hashCode()
                }
                if (__tracksJsonValue !== null) {
                    hash = 31 * hash + __tracksJsonValue.hashCode()
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

        @GeneratedBy(type = MusicHistory::class)
        internal class DraftImpl(
            ctx: DraftContext?,
            base: MusicHistory?,
        ) : Implementor,
            MusicHistoryDraft,
            DraftSpi {
            private val __ctx: DraftContext? = ctx

            private val __base: Impl? = base as Impl?

            private var __modified: Impl? = if (base === null) Impl() else null

            private var __resolving: Boolean = false

            private var __resolved: MusicHistory? = null

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
                    SLOT_TRACKS_JSON ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__tracksJsonValue = null
                    SLOT_CREATED_AT ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__createdAtValue = null
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.MusicHistory\": " + 
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
                    "tracksJson" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__tracksJsonValue = null
                    "createdAt" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__createdAtValue = null
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.MusicHistory\": " + 
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
                    SLOT_TRACKS_JSON ->
                    	this.tracksJson = value as String?
                    	?: throw IllegalArgumentException("'tracksJson cannot be null")
                    SLOT_CREATED_AT ->
                    	this.createdAt = value as LocalDateTime?
                    	?: throw IllegalArgumentException("'createdAt cannot be null")
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.MusicHistory\": " + 
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
                    "tracksJson" ->
                    	this.tracksJson = value as String?
                    	?: throw IllegalArgumentException("'tracksJson cannot be null")
                    "createdAt" ->
                    	this.createdAt = value as LocalDateTime?
                    	?: throw IllegalArgumentException("'createdAt cannot be null")
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.MusicHistory\": " + 
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
                        Visibility.of(6).also{
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
                    SLOT_TRACKS_JSON ->
                    	__visibility.show(SLOT_TRACKS_JSON, visible)
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
                        Visibility.of(6).also{
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
                    "tracksJson" ->
                    	__visibility.show(SLOT_TRACKS_JSON, visible)
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

    @GeneratedBy(type = MusicHistory::class)
    public class Builder {
        private val __draft: `$`.DraftImpl

        public constructor(base: MusicHistory?) {
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

        public fun tracksJson(tracksJson: String?): Builder {
            if (tracksJson !== null) {
                __draft.tracksJson = tracksJson
                __draft.__show(PropId.byIndex(`$`.SLOT_TRACKS_JSON), true)
            }
            return this
        }

        public fun createdAt(createdAt: LocalDateTime?): Builder {
            if (createdAt !== null) {
                __draft.createdAt = createdAt
                __draft.__show(PropId.byIndex(`$`.SLOT_CREATED_AT), true)
            }
            return this
        }

        public fun build(): MusicHistory = __draft.__unwrap() as MusicHistory
    }
}

@GeneratedBy(type = MusicHistory::class)
public fun ImmutableCreator<MusicHistory>.`by`(resolveImmediately: Boolean = false, block: MusicHistoryDraft.() -> Unit): MusicHistory = MusicHistoryDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = MusicHistory::class)
public fun ImmutableCreator<MusicHistory>.`by`(base: MusicHistory?, resolveImmediately: Boolean = false): MusicHistory = MusicHistoryDraft.`$`.produce(base, resolveImmediately)

@GeneratedBy(type = MusicHistory::class)
public fun ImmutableCreator<MusicHistory>.`by`(
    base: MusicHistory?,
    resolveImmediately: Boolean = false,
    block: MusicHistoryDraft.() -> Unit,
): MusicHistory = MusicHistoryDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = MusicHistory::class)
public fun MusicHistory(resolveImmediately: Boolean = false, block: MusicHistoryDraft.() -> Unit): MusicHistory = MusicHistoryDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = MusicHistory::class)
public fun MusicHistory(
    base: MusicHistory?,
    resolveImmediately: Boolean = false,
    block: MusicHistoryDraft.() -> Unit,
): MusicHistory = MusicHistoryDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = MusicHistory::class)
public fun MutableList<MusicHistoryDraft>.addBy(resolveImmediately: Boolean = false, block: MusicHistoryDraft.() -> Unit): MutableList<MusicHistoryDraft> {
    add(MusicHistoryDraft.`$`.produce(null, resolveImmediately, block) as MusicHistoryDraft)
    return this
}

@GeneratedBy(type = MusicHistory::class)
public fun MutableList<MusicHistoryDraft>.addBy(base: MusicHistory?, resolveImmediately: Boolean = false): MutableList<MusicHistoryDraft> {
    add(MusicHistoryDraft.`$`.produce(base, resolveImmediately) as MusicHistoryDraft)
    return this
}

@GeneratedBy(type = MusicHistory::class)
public fun MutableList<MusicHistoryDraft>.addBy(
    base: MusicHistory?,
    resolveImmediately: Boolean = false,
    block: MusicHistoryDraft.() -> Unit,
): MutableList<MusicHistoryDraft> {
    add(MusicHistoryDraft.`$`.produce(base, resolveImmediately, block) as MusicHistoryDraft)
    return this
}

@GeneratedBy(type = MusicHistory::class)
public fun MusicHistory.copy(resolveImmediately: Boolean = false, block: MusicHistoryDraft.() -> Unit): MusicHistory = MusicHistoryDraft.`$`.produce(this, resolveImmediately, block)
