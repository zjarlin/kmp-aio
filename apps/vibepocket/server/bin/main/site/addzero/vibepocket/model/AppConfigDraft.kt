@file:Suppress("warnings")

package site.addzero.vibepocket.model

import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonPropertyOrder
import java.io.Serializable
import java.lang.IllegalStateException
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
@GeneratedBy(type = AppConfig::class)
public interface AppConfigDraft : AppConfig, Draft {
    override var id: Long

    override var key: String

    override var `value`: String

    override var description: String?

    @GeneratedBy(type = AppConfig::class)
    public object `$` {
        public const val SLOT_ID: Int = 0

        public const val SLOT_KEY: Int = 1

        public const val SLOT_VALUE: Int = 2

        public const val SLOT_DESCRIPTION: Int = 3

        public val type: ImmutableType = ImmutableType
            .newBuilder(
                "0.10.6",
                AppConfig::class,
                listOf(

                ),
            ) { ctx, base ->
                DraftImpl(ctx, base as AppConfig?)
            }
            .id(SLOT_ID, "id", Long::class.java)
            .key(SLOT_KEY, "key", String::class.java, false)
            .add(SLOT_VALUE, "value", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_DESCRIPTION, "description", ImmutablePropCategory.SCALAR, String::class.java, true)
            .build()

        public fun produce(base: AppConfig? = null, resolveImmediately: Boolean = false): AppConfig {
            val consumer = DraftConsumer<AppConfigDraft> {}
            return Internal.produce(type, base, resolveImmediately, consumer) as AppConfig
        }

        public fun produce(
            base: AppConfig? = null,
            resolveImmediately: Boolean = false,
            block: AppConfigDraft.() -> Unit,
        ): AppConfig {
            val consumer = DraftConsumer<AppConfigDraft> { block(it) }
            return Internal.produce(type, base, resolveImmediately, consumer) as AppConfig
        }

        @GeneratedBy(type = AppConfig::class)
        @JsonPropertyOrder("dummyPropForJacksonError__", "id", "key", "value", "description")
        private abstract interface Implementor : AppConfig, ImmutableSpi {
            public val dummyPropForJacksonError__: Int
                get() = throw ImmutableModuleRequiredException()

            override fun __get(prop: PropId): Any? = when (prop.asIndex()) {
                -1 ->
                	__get(prop.asName())
                SLOT_ID ->
                	id
                SLOT_KEY ->
                	key
                SLOT_VALUE ->
                	value
                SLOT_DESCRIPTION ->
                	description
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.AppConfig\": " + 
                    prop
                )

            }

            override fun __get(prop: String): Any? = when (prop) {
                "id" ->
                	id
                "key" ->
                	key
                "value" ->
                	value
                "description" ->
                	description
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.AppConfig\": " + 
                    prop
                )

            }

            override fun __type(): ImmutableType = `$`.type
        }

        @GeneratedBy(type = AppConfig::class)
        private class Impl : Implementor, Cloneable, Serializable {
            @get:JsonIgnore
            internal var __visibility: Visibility? = null

            @get:JsonIgnore
            internal var __idValue: Long = 0

            @get:JsonIgnore
            internal var __idLoaded: Boolean = false

            @get:JsonIgnore
            internal var __keyValue: String? = null

            @get:JsonIgnore
            internal var __valueValue: String? = null

            @get:JsonIgnore
            internal var __descriptionValue: String? = null

            @get:JsonIgnore
            internal var __descriptionLoaded: Boolean = false

            override val id: Long
                get() {
                    if (!__idLoaded) {
                        throw UnloadedException(AppConfig::class.java, "id")
                    }
                    return __idValue
                }

            override val key: String
                get() {
                    val __keyValue = this.__keyValue
                    if (__keyValue === null) {
                        throw UnloadedException(AppConfig::class.java, "key")
                    }
                    return __keyValue
                }

            override val `value`: String
                get() {
                    val __valueValue = this.__valueValue
                    if (__valueValue === null) {
                        throw UnloadedException(AppConfig::class.java, "value")
                    }
                    return __valueValue
                }

            override val description: String?
                get() {
                    if (!__descriptionLoaded) {
                        throw UnloadedException(AppConfig::class.java, "description")
                    }
                    return __descriptionValue
                }

            public override fun clone(): Impl {
                val copy = super.clone() as Impl
                val originalVisibility = this.__visibility
                if (originalVisibility != null) {
                    val newVisibility = Visibility.of(4)
                    for (propId in 0 until 4) {
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
                SLOT_KEY ->
                	__keyValue !== null
                SLOT_VALUE ->
                	__valueValue !== null
                SLOT_DESCRIPTION ->
                	__descriptionLoaded
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.AppConfig\": " + 
                    prop
                )

            }

            override fun __isLoaded(prop: String): Boolean = when (prop) {
                "id" ->
                	__idLoaded
                "key" ->
                	__keyValue !== null
                "value" ->
                	__valueValue !== null
                "description" ->
                	__descriptionLoaded
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.AppConfig\": " + 
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
                    SLOT_KEY ->
                    	__visibility.visible(SLOT_KEY)
                    SLOT_VALUE ->
                    	__visibility.visible(SLOT_VALUE)
                    SLOT_DESCRIPTION ->
                    	__visibility.visible(SLOT_DESCRIPTION)
                    else -> true
                }
            }

            override fun __isVisible(prop: String): Boolean {
                val __visibility = this.__visibility ?: return true
                return when (prop) {
                    "id" ->
                    	__visibility.visible(SLOT_ID)
                    "key" ->
                    	__visibility.visible(SLOT_KEY)
                    "value" ->
                    	__visibility.visible(SLOT_VALUE)
                    "description" ->
                    	__visibility.visible(SLOT_DESCRIPTION)
                    else -> true
                }
            }

            public fun __shallowHashCode(): Int {
                var hash = __visibility?.hashCode() ?: 0
                if (__idLoaded) {
                    hash = 31 * hash + __idValue.hashCode()
                }
                if (__keyValue !== null) {
                    hash = 31 * hash + __keyValue.hashCode()
                }
                if (__valueValue !== null) {
                    hash = 31 * hash + __valueValue.hashCode()
                }
                if (__descriptionLoaded) {
                    hash = 31 * hash + (__descriptionValue?.hashCode() ?: 0)
                }
                return hash
            }

            override fun hashCode(): Int {
                var hash = __visibility?.hashCode() ?: 0
                if (__idLoaded) {
                    hash = 31 * hash + __idValue.hashCode()
                    return hash
                }
                if (__keyValue !== null) {
                    hash = 31 * hash + __keyValue.hashCode()
                }
                if (__valueValue !== null) {
                    hash = 31 * hash + __valueValue.hashCode()
                }
                if (__descriptionLoaded) {
                    hash = 31 * hash + (__descriptionValue?.hashCode() ?: 0)
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
                if (__isVisible(PropId.byIndex(SLOT_KEY)) != __other.__isVisible(PropId.byIndex(SLOT_KEY))) {
                    return false
                }
                val __keyLoaded = 
                    this.__keyValue !== null
                if (__keyLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_KEY)))) {
                    return false
                }
                if (__keyLoaded && this.__keyValue != __other.key) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_VALUE)) != __other.__isVisible(PropId.byIndex(SLOT_VALUE))) {
                    return false
                }
                val __valueLoaded = 
                    this.__valueValue !== null
                if (__valueLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_VALUE)))) {
                    return false
                }
                if (__valueLoaded && this.__valueValue != __other.value) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_DESCRIPTION)) != __other.__isVisible(PropId.byIndex(SLOT_DESCRIPTION))) {
                    return false
                }
                val __descriptionLoaded = 
                    this.__descriptionLoaded
                if (__descriptionLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DESCRIPTION)))) {
                    return false
                }
                if (__descriptionLoaded && this.__descriptionValue != __other.description) {
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
                if (__isVisible(PropId.byIndex(SLOT_KEY)) != __other.__isVisible(PropId.byIndex(SLOT_KEY))) {
                    return false
                }
                val __keyLoaded = 
                    this.__keyValue !== null
                if (__keyLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_KEY)))) {
                    return false
                }
                if (__keyLoaded && this.__keyValue != __other.key) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_VALUE)) != __other.__isVisible(PropId.byIndex(SLOT_VALUE))) {
                    return false
                }
                val __valueLoaded = 
                    this.__valueValue !== null
                if (__valueLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_VALUE)))) {
                    return false
                }
                if (__valueLoaded && this.__valueValue != __other.value) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_DESCRIPTION)) != __other.__isVisible(PropId.byIndex(SLOT_DESCRIPTION))) {
                    return false
                }
                val __descriptionLoaded = 
                    this.__descriptionLoaded
                if (__descriptionLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DESCRIPTION)))) {
                    return false
                }
                if (__descriptionLoaded && this.__descriptionValue != __other.description) {
                    return false
                }
                return true
            }

            override fun __equals(obj: Any?, shallow: Boolean): Boolean = if (shallow) __shallowEquals(obj) else equals(obj)

            override fun toString(): String = ImmutableObjects.toString(this)
        }

        @GeneratedBy(type = AppConfig::class)
        internal class DraftImpl(
            ctx: DraftContext?,
            base: AppConfig?,
        ) : Implementor,
            AppConfigDraft,
            DraftSpi {
            private val __ctx: DraftContext? = ctx

            private val __base: Impl? = base as Impl?

            private var __modified: Impl? = if (base === null) Impl() else null

            private var __resolving: Boolean = false

            private var __resolved: AppConfig? = null

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

            override var key: String
                get() = (__modified ?: __base!!).key
                set(key) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__keyValue = key
                }

            override var `value`: String
                get() = (__modified ?: __base!!).value
                set(`value`) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__valueValue = value
                }

            override var description: String?
                get() = (__modified ?: __base!!).description
                set(description) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__descriptionValue = description
                    __tmpModified.__descriptionLoaded = true
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
                    SLOT_KEY ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__keyValue = null
                    SLOT_VALUE ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__valueValue = null
                    SLOT_DESCRIPTION ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__descriptionValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__descriptionLoaded = false
                        }
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.AppConfig\": " + 
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
                    "key" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__keyValue = null
                    "value" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__valueValue = null
                    "description" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__descriptionValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__descriptionLoaded = false
                        }
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.AppConfig\": " + 
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
                    SLOT_KEY ->
                    	this.key = value as String?
                    	?: throw IllegalArgumentException("'key cannot be null")
                    SLOT_VALUE ->
                    	this.value = value as String?
                    	?: throw IllegalArgumentException("'value cannot be null")
                    SLOT_DESCRIPTION ->
                    	this.description = value as String?
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.AppConfig\": " + 
                        prop
                    )

                }
            }

            override fun __set(prop: String, `value`: Any?) {
                when (prop) {
                    "id" ->
                    	this.id = value as Long?
                    	?: throw IllegalArgumentException("'id cannot be null")
                    "key" ->
                    	this.key = value as String?
                    	?: throw IllegalArgumentException("'key cannot be null")
                    "value" ->
                    	this.value = value as String?
                    	?: throw IllegalArgumentException("'value cannot be null")
                    "description" ->
                    	this.description = value as String?
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.AppConfig\": " + 
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
                        Visibility.of(4).also{
                            (__modified ?: __base!!.clone())
                            .also { __modified = it }.__visibility = it}
                    }
                    ?: return
                when (prop.asIndex()) {
                    -1 ->
                    	__show(prop.asName(), visible)
                    SLOT_ID ->
                    	__visibility.show(SLOT_ID, visible)
                    SLOT_KEY ->
                    	__visibility.show(SLOT_KEY, visible)
                    SLOT_VALUE ->
                    	__visibility.show(SLOT_VALUE, visible)
                    SLOT_DESCRIPTION ->
                    	__visibility.show(SLOT_DESCRIPTION, visible)
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
                        Visibility.of(4).also{
                            (__modified ?: __base!!.clone())
                            .also { __modified = it }.__visibility = it}
                    }
                    ?: return
                when (prop) {
                    "id" ->
                    	__visibility.show(SLOT_ID, visible)
                    "key" ->
                    	__visibility.show(SLOT_KEY, visible)
                    "value" ->
                    	__visibility.show(SLOT_VALUE, visible)
                    "description" ->
                    	__visibility.show(SLOT_DESCRIPTION, visible)
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

    @GeneratedBy(type = AppConfig::class)
    public class Builder {
        private val __draft: `$`.DraftImpl

        public constructor(base: AppConfig?) {
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

        public fun key(key: String?): Builder {
            if (key !== null) {
                __draft.key = key
                __draft.__show(PropId.byIndex(`$`.SLOT_KEY), true)
            }
            return this
        }

        public fun `value`(`value`: String?): Builder {
            if (value !== null) {
                __draft.value = value
                __draft.__show(PropId.byIndex(`$`.SLOT_VALUE), true)
            }
            return this
        }

        public fun description(description: String?): Builder {
            __draft.description = description
            __draft.__show(PropId.byIndex(`$`.SLOT_DESCRIPTION), true)
            return this
        }

        public fun build(): AppConfig = __draft.__unwrap() as AppConfig
    }
}

@GeneratedBy(type = AppConfig::class)
public fun ImmutableCreator<AppConfig>.`by`(resolveImmediately: Boolean = false, block: AppConfigDraft.() -> Unit): AppConfig = AppConfigDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = AppConfig::class)
public fun ImmutableCreator<AppConfig>.`by`(base: AppConfig?, resolveImmediately: Boolean = false): AppConfig = AppConfigDraft.`$`.produce(base, resolveImmediately)

@GeneratedBy(type = AppConfig::class)
public fun ImmutableCreator<AppConfig>.`by`(
    base: AppConfig?,
    resolveImmediately: Boolean = false,
    block: AppConfigDraft.() -> Unit,
): AppConfig = AppConfigDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = AppConfig::class)
public fun AppConfig(resolveImmediately: Boolean = false, block: AppConfigDraft.() -> Unit): AppConfig = AppConfigDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = AppConfig::class)
public fun AppConfig(
    base: AppConfig?,
    resolveImmediately: Boolean = false,
    block: AppConfigDraft.() -> Unit,
): AppConfig = AppConfigDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = AppConfig::class)
public fun MutableList<AppConfigDraft>.addBy(resolveImmediately: Boolean = false, block: AppConfigDraft.() -> Unit): MutableList<AppConfigDraft> {
    add(AppConfigDraft.`$`.produce(null, resolveImmediately, block) as AppConfigDraft)
    return this
}

@GeneratedBy(type = AppConfig::class)
public fun MutableList<AppConfigDraft>.addBy(base: AppConfig?, resolveImmediately: Boolean = false): MutableList<AppConfigDraft> {
    add(AppConfigDraft.`$`.produce(base, resolveImmediately) as AppConfigDraft)
    return this
}

@GeneratedBy(type = AppConfig::class)
public fun MutableList<AppConfigDraft>.addBy(
    base: AppConfig?,
    resolveImmediately: Boolean = false,
    block: AppConfigDraft.() -> Unit,
): MutableList<AppConfigDraft> {
    add(AppConfigDraft.`$`.produce(base, resolveImmediately, block) as AppConfigDraft)
    return this
}

@GeneratedBy(type = AppConfig::class)
public fun AppConfig.copy(resolveImmediately: Boolean = false, block: AppConfigDraft.() -> Unit): AppConfig = AppConfigDraft.`$`.produce(this, resolveImmediately, block)
