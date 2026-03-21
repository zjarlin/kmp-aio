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
@GeneratedBy(type = PersonaRecord::class)
public interface PersonaRecordDraft : PersonaRecord, Draft {
    override var id: Long

    override var personaId: String

    override var name: String

    override var description: String

    override var createdAt: LocalDateTime

    @GeneratedBy(type = PersonaRecord::class)
    public object `$` {
        public const val SLOT_ID: Int = 0

        public const val SLOT_PERSONA_ID: Int = 1

        public const val SLOT_NAME: Int = 2

        public const val SLOT_DESCRIPTION: Int = 3

        public const val SLOT_CREATED_AT: Int = 4

        public val type: ImmutableType = ImmutableType
            .newBuilder(
                "0.10.6",
                PersonaRecord::class,
                listOf(

                ),
            ) { ctx, base ->
                DraftImpl(ctx, base as PersonaRecord?)
            }
            .id(SLOT_ID, "id", Long::class.java)
            .key(SLOT_PERSONA_ID, "personaId", String::class.java, false)
            .add(SLOT_NAME, "name", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_DESCRIPTION, "description", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_CREATED_AT, "createdAt", ImmutablePropCategory.SCALAR, LocalDateTime::class.java, false)
            .build()

        public fun produce(base: PersonaRecord? = null, resolveImmediately: Boolean = false): PersonaRecord {
            val consumer = DraftConsumer<PersonaRecordDraft> {}
            return Internal.produce(type, base, resolveImmediately, consumer) as PersonaRecord
        }

        public fun produce(
            base: PersonaRecord? = null,
            resolveImmediately: Boolean = false,
            block: PersonaRecordDraft.() -> Unit,
        ): PersonaRecord {
            val consumer = DraftConsumer<PersonaRecordDraft> { block(it) }
            return Internal.produce(type, base, resolveImmediately, consumer) as PersonaRecord
        }

        @GeneratedBy(type = PersonaRecord::class)
        @JsonPropertyOrder("dummyPropForJacksonError__", "id", "personaId", "name", "description", "createdAt")
        private abstract interface Implementor : PersonaRecord, ImmutableSpi {
            public val dummyPropForJacksonError__: Int
                get() = throw ImmutableModuleRequiredException()

            override fun __get(prop: PropId): Any? = when (prop.asIndex()) {
                -1 ->
                	__get(prop.asName())
                SLOT_ID ->
                	id
                SLOT_PERSONA_ID ->
                	personaId
                SLOT_NAME ->
                	name
                SLOT_DESCRIPTION ->
                	description
                SLOT_CREATED_AT ->
                	createdAt
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.PersonaRecord\": " + 
                    prop
                )

            }

            override fun __get(prop: String): Any? = when (prop) {
                "id" ->
                	id
                "personaId" ->
                	personaId
                "name" ->
                	name
                "description" ->
                	description
                "createdAt" ->
                	createdAt
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.PersonaRecord\": " + 
                    prop
                )

            }

            override fun __type(): ImmutableType = `$`.type
        }

        @GeneratedBy(type = PersonaRecord::class)
        private class Impl : Implementor, Cloneable, Serializable {
            @get:JsonIgnore
            internal var __visibility: Visibility? = null

            @get:JsonIgnore
            internal var __idValue: Long = 0

            @get:JsonIgnore
            internal var __idLoaded: Boolean = false

            @get:JsonIgnore
            internal var __personaIdValue: String? = null

            @get:JsonIgnore
            internal var __nameValue: String? = null

            @get:JsonIgnore
            internal var __descriptionValue: String? = null

            @get:JsonIgnore
            internal var __createdAtValue: LocalDateTime? = null

            override val id: Long
                get() {
                    if (!__idLoaded) {
                        throw UnloadedException(PersonaRecord::class.java, "id")
                    }
                    return __idValue
                }

            override val personaId: String
                get() {
                    val __personaIdValue = this.__personaIdValue
                    if (__personaIdValue === null) {
                        throw UnloadedException(PersonaRecord::class.java, "personaId")
                    }
                    return __personaIdValue
                }

            override val name: String
                get() {
                    val __nameValue = this.__nameValue
                    if (__nameValue === null) {
                        throw UnloadedException(PersonaRecord::class.java, "name")
                    }
                    return __nameValue
                }

            override val description: String
                get() {
                    val __descriptionValue = this.__descriptionValue
                    if (__descriptionValue === null) {
                        throw UnloadedException(PersonaRecord::class.java, "description")
                    }
                    return __descriptionValue
                }

            override val createdAt: LocalDateTime
                get() {
                    val __createdAtValue = this.__createdAtValue
                    if (__createdAtValue === null) {
                        throw UnloadedException(PersonaRecord::class.java, "createdAt")
                    }
                    return __createdAtValue
                }

            public override fun clone(): Impl {
                val copy = super.clone() as Impl
                val originalVisibility = this.__visibility
                if (originalVisibility != null) {
                    val newVisibility = Visibility.of(5)
                    for (propId in 0 until 5) {
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
                SLOT_PERSONA_ID ->
                	__personaIdValue !== null
                SLOT_NAME ->
                	__nameValue !== null
                SLOT_DESCRIPTION ->
                	__descriptionValue !== null
                SLOT_CREATED_AT ->
                	__createdAtValue !== null
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.PersonaRecord\": " + 
                    prop
                )

            }

            override fun __isLoaded(prop: String): Boolean = when (prop) {
                "id" ->
                	__idLoaded
                "personaId" ->
                	__personaIdValue !== null
                "name" ->
                	__nameValue !== null
                "description" ->
                	__descriptionValue !== null
                "createdAt" ->
                	__createdAtValue !== null
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.PersonaRecord\": " + 
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
                    SLOT_PERSONA_ID ->
                    	__visibility.visible(SLOT_PERSONA_ID)
                    SLOT_NAME ->
                    	__visibility.visible(SLOT_NAME)
                    SLOT_DESCRIPTION ->
                    	__visibility.visible(SLOT_DESCRIPTION)
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
                    "personaId" ->
                    	__visibility.visible(SLOT_PERSONA_ID)
                    "name" ->
                    	__visibility.visible(SLOT_NAME)
                    "description" ->
                    	__visibility.visible(SLOT_DESCRIPTION)
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
                if (__personaIdValue !== null) {
                    hash = 31 * hash + __personaIdValue.hashCode()
                }
                if (__nameValue !== null) {
                    hash = 31 * hash + __nameValue.hashCode()
                }
                if (__descriptionValue !== null) {
                    hash = 31 * hash + __descriptionValue.hashCode()
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
                if (__personaIdValue !== null) {
                    hash = 31 * hash + __personaIdValue.hashCode()
                }
                if (__nameValue !== null) {
                    hash = 31 * hash + __nameValue.hashCode()
                }
                if (__descriptionValue !== null) {
                    hash = 31 * hash + __descriptionValue.hashCode()
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
                if (__isVisible(PropId.byIndex(SLOT_PERSONA_ID)) != __other.__isVisible(PropId.byIndex(SLOT_PERSONA_ID))) {
                    return false
                }
                val __personaIdLoaded = 
                    this.__personaIdValue !== null
                if (__personaIdLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_PERSONA_ID)))) {
                    return false
                }
                if (__personaIdLoaded && this.__personaIdValue != __other.personaId) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_NAME)) != __other.__isVisible(PropId.byIndex(SLOT_NAME))) {
                    return false
                }
                val __nameLoaded = 
                    this.__nameValue !== null
                if (__nameLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_NAME)))) {
                    return false
                }
                if (__nameLoaded && this.__nameValue != __other.name) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_DESCRIPTION)) != __other.__isVisible(PropId.byIndex(SLOT_DESCRIPTION))) {
                    return false
                }
                val __descriptionLoaded = 
                    this.__descriptionValue !== null
                if (__descriptionLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DESCRIPTION)))) {
                    return false
                }
                if (__descriptionLoaded && this.__descriptionValue != __other.description) {
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
                if (__isVisible(PropId.byIndex(SLOT_PERSONA_ID)) != __other.__isVisible(PropId.byIndex(SLOT_PERSONA_ID))) {
                    return false
                }
                val __personaIdLoaded = 
                    this.__personaIdValue !== null
                if (__personaIdLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_PERSONA_ID)))) {
                    return false
                }
                if (__personaIdLoaded && this.__personaIdValue != __other.personaId) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_NAME)) != __other.__isVisible(PropId.byIndex(SLOT_NAME))) {
                    return false
                }
                val __nameLoaded = 
                    this.__nameValue !== null
                if (__nameLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_NAME)))) {
                    return false
                }
                if (__nameLoaded && this.__nameValue != __other.name) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_DESCRIPTION)) != __other.__isVisible(PropId.byIndex(SLOT_DESCRIPTION))) {
                    return false
                }
                val __descriptionLoaded = 
                    this.__descriptionValue !== null
                if (__descriptionLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DESCRIPTION)))) {
                    return false
                }
                if (__descriptionLoaded && this.__descriptionValue != __other.description) {
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

        @GeneratedBy(type = PersonaRecord::class)
        internal class DraftImpl(
            ctx: DraftContext?,
            base: PersonaRecord?,
        ) : Implementor,
            PersonaRecordDraft,
            DraftSpi {
            private val __ctx: DraftContext? = ctx

            private val __base: Impl? = base as Impl?

            private var __modified: Impl? = if (base === null) Impl() else null

            private var __resolving: Boolean = false

            private var __resolved: PersonaRecord? = null

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

            override var personaId: String
                get() = (__modified ?: __base!!).personaId
                set(personaId) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__personaIdValue = personaId
                }

            override var name: String
                get() = (__modified ?: __base!!).name
                set(name) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__nameValue = name
                }

            override var description: String
                get() = (__modified ?: __base!!).description
                set(description) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__descriptionValue = description
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
                    SLOT_PERSONA_ID ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__personaIdValue = null
                    SLOT_NAME ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__nameValue = null
                    SLOT_DESCRIPTION ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__descriptionValue = null
                    SLOT_CREATED_AT ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__createdAtValue = null
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.PersonaRecord\": " + 
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
                    "personaId" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__personaIdValue = null
                    "name" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__nameValue = null
                    "description" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__descriptionValue = null
                    "createdAt" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__createdAtValue = null
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.PersonaRecord\": " + 
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
                    SLOT_PERSONA_ID ->
                    	this.personaId = value as String?
                    	?: throw IllegalArgumentException("'personaId cannot be null")
                    SLOT_NAME ->
                    	this.name = value as String?
                    	?: throw IllegalArgumentException("'name cannot be null")
                    SLOT_DESCRIPTION ->
                    	this.description = value as String?
                    	?: throw IllegalArgumentException("'description cannot be null")
                    SLOT_CREATED_AT ->
                    	this.createdAt = value as LocalDateTime?
                    	?: throw IllegalArgumentException("'createdAt cannot be null")
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.PersonaRecord\": " + 
                        prop
                    )

                }
            }

            override fun __set(prop: String, `value`: Any?) {
                when (prop) {
                    "id" ->
                    	this.id = value as Long?
                    	?: throw IllegalArgumentException("'id cannot be null")
                    "personaId" ->
                    	this.personaId = value as String?
                    	?: throw IllegalArgumentException("'personaId cannot be null")
                    "name" ->
                    	this.name = value as String?
                    	?: throw IllegalArgumentException("'name cannot be null")
                    "description" ->
                    	this.description = value as String?
                    	?: throw IllegalArgumentException("'description cannot be null")
                    "createdAt" ->
                    	this.createdAt = value as LocalDateTime?
                    	?: throw IllegalArgumentException("'createdAt cannot be null")
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.PersonaRecord\": " + 
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
                        Visibility.of(5).also{
                            (__modified ?: __base!!.clone())
                            .also { __modified = it }.__visibility = it}
                    }
                    ?: return
                when (prop.asIndex()) {
                    -1 ->
                    	__show(prop.asName(), visible)
                    SLOT_ID ->
                    	__visibility.show(SLOT_ID, visible)
                    SLOT_PERSONA_ID ->
                    	__visibility.show(SLOT_PERSONA_ID, visible)
                    SLOT_NAME ->
                    	__visibility.show(SLOT_NAME, visible)
                    SLOT_DESCRIPTION ->
                    	__visibility.show(SLOT_DESCRIPTION, visible)
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
                        Visibility.of(5).also{
                            (__modified ?: __base!!.clone())
                            .also { __modified = it }.__visibility = it}
                    }
                    ?: return
                when (prop) {
                    "id" ->
                    	__visibility.show(SLOT_ID, visible)
                    "personaId" ->
                    	__visibility.show(SLOT_PERSONA_ID, visible)
                    "name" ->
                    	__visibility.show(SLOT_NAME, visible)
                    "description" ->
                    	__visibility.show(SLOT_DESCRIPTION, visible)
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

    @GeneratedBy(type = PersonaRecord::class)
    public class Builder {
        private val __draft: `$`.DraftImpl

        public constructor(base: PersonaRecord?) {
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

        public fun personaId(personaId: String?): Builder {
            if (personaId !== null) {
                __draft.personaId = personaId
                __draft.__show(PropId.byIndex(`$`.SLOT_PERSONA_ID), true)
            }
            return this
        }

        public fun name(name: String?): Builder {
            if (name !== null) {
                __draft.name = name
                __draft.__show(PropId.byIndex(`$`.SLOT_NAME), true)
            }
            return this
        }

        public fun description(description: String?): Builder {
            if (description !== null) {
                __draft.description = description
                __draft.__show(PropId.byIndex(`$`.SLOT_DESCRIPTION), true)
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

        public fun build(): PersonaRecord = __draft.__unwrap() as PersonaRecord
    }
}

@GeneratedBy(type = PersonaRecord::class)
public fun ImmutableCreator<PersonaRecord>.`by`(resolveImmediately: Boolean = false, block: PersonaRecordDraft.() -> Unit): PersonaRecord = PersonaRecordDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = PersonaRecord::class)
public fun ImmutableCreator<PersonaRecord>.`by`(base: PersonaRecord?, resolveImmediately: Boolean = false): PersonaRecord = PersonaRecordDraft.`$`.produce(base, resolveImmediately)

@GeneratedBy(type = PersonaRecord::class)
public fun ImmutableCreator<PersonaRecord>.`by`(
    base: PersonaRecord?,
    resolveImmediately: Boolean = false,
    block: PersonaRecordDraft.() -> Unit,
): PersonaRecord = PersonaRecordDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = PersonaRecord::class)
public fun PersonaRecord(resolveImmediately: Boolean = false, block: PersonaRecordDraft.() -> Unit): PersonaRecord = PersonaRecordDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = PersonaRecord::class)
public fun PersonaRecord(
    base: PersonaRecord?,
    resolveImmediately: Boolean = false,
    block: PersonaRecordDraft.() -> Unit,
): PersonaRecord = PersonaRecordDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = PersonaRecord::class)
public fun MutableList<PersonaRecordDraft>.addBy(resolveImmediately: Boolean = false, block: PersonaRecordDraft.() -> Unit): MutableList<PersonaRecordDraft> {
    add(PersonaRecordDraft.`$`.produce(null, resolveImmediately, block) as PersonaRecordDraft)
    return this
}

@GeneratedBy(type = PersonaRecord::class)
public fun MutableList<PersonaRecordDraft>.addBy(base: PersonaRecord?, resolveImmediately: Boolean = false): MutableList<PersonaRecordDraft> {
    add(PersonaRecordDraft.`$`.produce(base, resolveImmediately) as PersonaRecordDraft)
    return this
}

@GeneratedBy(type = PersonaRecord::class)
public fun MutableList<PersonaRecordDraft>.addBy(
    base: PersonaRecord?,
    resolveImmediately: Boolean = false,
    block: PersonaRecordDraft.() -> Unit,
): MutableList<PersonaRecordDraft> {
    add(PersonaRecordDraft.`$`.produce(base, resolveImmediately, block) as PersonaRecordDraft)
    return this
}

@GeneratedBy(type = PersonaRecord::class)
public fun PersonaRecord.copy(resolveImmediately: Boolean = false, block: PersonaRecordDraft.() -> Unit): PersonaRecord = PersonaRecordDraft.`$`.produce(this, resolveImmediately, block)
