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
@GeneratedBy(type = DatasourceConfig::class)
public interface DatasourceConfigDraft : DatasourceConfig, Draft {
    override var id: Long

    override var owner: String

    override var name: String

    override var dbType: String

    override var url: String

    override var username: String?

    override var password: String?

    override var driverClass: String?

    override var enabled: Boolean

    override var description: String?

    @GeneratedBy(type = DatasourceConfig::class)
    public object `$` {
        public const val SLOT_ID: Int = 0

        public const val SLOT_OWNER: Int = 1

        public const val SLOT_NAME: Int = 2

        public const val SLOT_DB_TYPE: Int = 3

        public const val SLOT_URL: Int = 4

        public const val SLOT_USERNAME: Int = 5

        public const val SLOT_PASSWORD: Int = 6

        public const val SLOT_DRIVER_CLASS: Int = 7

        public const val SLOT_ENABLED: Int = 8

        public const val SLOT_DESCRIPTION: Int = 9

        public val type: ImmutableType = ImmutableType
            .newBuilder(
                "0.10.6",
                DatasourceConfig::class,
                listOf(

                ),
            ) { ctx, base ->
                DraftImpl(ctx, base as DatasourceConfig?)
            }
            .id(SLOT_ID, "id", Long::class.java)
            .key(SLOT_OWNER, "owner", String::class.java, false)
            .key(SLOT_NAME, "name", String::class.java, false)
            .add(SLOT_DB_TYPE, "dbType", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_URL, "url", ImmutablePropCategory.SCALAR, String::class.java, false)
            .add(SLOT_USERNAME, "username", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_PASSWORD, "password", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_DRIVER_CLASS, "driverClass", ImmutablePropCategory.SCALAR, String::class.java, true)
            .add(SLOT_ENABLED, "enabled", ImmutablePropCategory.SCALAR, Boolean::class.java, false)
            .add(SLOT_DESCRIPTION, "description", ImmutablePropCategory.SCALAR, String::class.java, true)
            .build()

        public fun produce(base: DatasourceConfig? = null, resolveImmediately: Boolean = false): DatasourceConfig {
            val consumer = DraftConsumer<DatasourceConfigDraft> {}
            return Internal.produce(type, base, resolveImmediately, consumer) as DatasourceConfig
        }

        public fun produce(
            base: DatasourceConfig? = null,
            resolveImmediately: Boolean = false,
            block: DatasourceConfigDraft.() -> Unit,
        ): DatasourceConfig {
            val consumer = DraftConsumer<DatasourceConfigDraft> { block(it) }
            return Internal.produce(type, base, resolveImmediately, consumer) as DatasourceConfig
        }

        @GeneratedBy(type = DatasourceConfig::class)
        @JsonPropertyOrder("dummyPropForJacksonError__", "id", "owner", "name", "dbType", "url", "username", "password", "driverClass", "enabled", "description")
        private abstract interface Implementor : DatasourceConfig, ImmutableSpi {
            public val dummyPropForJacksonError__: Int
                get() = throw ImmutableModuleRequiredException()

            override fun __get(prop: PropId): Any? = when (prop.asIndex()) {
                -1 ->
                	__get(prop.asName())
                SLOT_ID ->
                	id
                SLOT_OWNER ->
                	owner
                SLOT_NAME ->
                	name
                SLOT_DB_TYPE ->
                	dbType
                SLOT_URL ->
                	url
                SLOT_USERNAME ->
                	username
                SLOT_PASSWORD ->
                	password
                SLOT_DRIVER_CLASS ->
                	driverClass
                SLOT_ENABLED ->
                	enabled
                SLOT_DESCRIPTION ->
                	description
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.DatasourceConfig\": " + 
                    prop
                )

            }

            override fun __get(prop: String): Any? = when (prop) {
                "id" ->
                	id
                "owner" ->
                	owner
                "name" ->
                	name
                "dbType" ->
                	dbType
                "url" ->
                	url
                "username" ->
                	username
                "password" ->
                	password
                "driverClass" ->
                	driverClass
                "enabled" ->
                	enabled
                "description" ->
                	description
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.DatasourceConfig\": " + 
                    prop
                )

            }

            override fun __type(): ImmutableType = `$`.type
        }

        @GeneratedBy(type = DatasourceConfig::class)
        private class Impl : Implementor, Cloneable, Serializable {
            @get:JsonIgnore
            internal var __visibility: Visibility? = null

            @get:JsonIgnore
            internal var __idValue: Long = 0

            @get:JsonIgnore
            internal var __idLoaded: Boolean = false

            @get:JsonIgnore
            internal var __ownerValue: String? = null

            @get:JsonIgnore
            internal var __nameValue: String? = null

            @get:JsonIgnore
            internal var __dbTypeValue: String? = null

            @get:JsonIgnore
            internal var __urlValue: String? = null

            @get:JsonIgnore
            internal var __usernameValue: String? = null

            @get:JsonIgnore
            internal var __usernameLoaded: Boolean = false

            @get:JsonIgnore
            internal var __passwordValue: String? = null

            @get:JsonIgnore
            internal var __passwordLoaded: Boolean = false

            @get:JsonIgnore
            internal var __driverClassValue: String? = null

            @get:JsonIgnore
            internal var __driverClassLoaded: Boolean = false

            @get:JsonIgnore
            internal var __enabledValue: Boolean = false

            @get:JsonIgnore
            internal var __enabledLoaded: Boolean = false

            @get:JsonIgnore
            internal var __descriptionValue: String? = null

            @get:JsonIgnore
            internal var __descriptionLoaded: Boolean = false

            override val id: Long
                get() {
                    if (!__idLoaded) {
                        throw UnloadedException(DatasourceConfig::class.java, "id")
                    }
                    return __idValue
                }

            override val owner: String
                get() {
                    val __ownerValue = this.__ownerValue
                    if (__ownerValue === null) {
                        throw UnloadedException(DatasourceConfig::class.java, "owner")
                    }
                    return __ownerValue
                }

            override val name: String
                get() {
                    val __nameValue = this.__nameValue
                    if (__nameValue === null) {
                        throw UnloadedException(DatasourceConfig::class.java, "name")
                    }
                    return __nameValue
                }

            override val dbType: String
                get() {
                    val __dbTypeValue = this.__dbTypeValue
                    if (__dbTypeValue === null) {
                        throw UnloadedException(DatasourceConfig::class.java, "dbType")
                    }
                    return __dbTypeValue
                }

            override val url: String
                get() {
                    val __urlValue = this.__urlValue
                    if (__urlValue === null) {
                        throw UnloadedException(DatasourceConfig::class.java, "url")
                    }
                    return __urlValue
                }

            override val username: String?
                get() {
                    if (!__usernameLoaded) {
                        throw UnloadedException(DatasourceConfig::class.java, "username")
                    }
                    return __usernameValue
                }

            override val password: String?
                get() {
                    if (!__passwordLoaded) {
                        throw UnloadedException(DatasourceConfig::class.java, "password")
                    }
                    return __passwordValue
                }

            override val driverClass: String?
                get() {
                    if (!__driverClassLoaded) {
                        throw UnloadedException(DatasourceConfig::class.java, "driverClass")
                    }
                    return __driverClassValue
                }

            override val enabled: Boolean
                get() {
                    if (!__enabledLoaded) {
                        throw UnloadedException(DatasourceConfig::class.java, "enabled")
                    }
                    return __enabledValue
                }

            override val description: String?
                get() {
                    if (!__descriptionLoaded) {
                        throw UnloadedException(DatasourceConfig::class.java, "description")
                    }
                    return __descriptionValue
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
                SLOT_OWNER ->
                	__ownerValue !== null
                SLOT_NAME ->
                	__nameValue !== null
                SLOT_DB_TYPE ->
                	__dbTypeValue !== null
                SLOT_URL ->
                	__urlValue !== null
                SLOT_USERNAME ->
                	__usernameLoaded
                SLOT_PASSWORD ->
                	__passwordLoaded
                SLOT_DRIVER_CLASS ->
                	__driverClassLoaded
                SLOT_ENABLED ->
                	__enabledLoaded
                SLOT_DESCRIPTION ->
                	__descriptionLoaded
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.DatasourceConfig\": " + 
                    prop
                )

            }

            override fun __isLoaded(prop: String): Boolean = when (prop) {
                "id" ->
                	__idLoaded
                "owner" ->
                	__ownerValue !== null
                "name" ->
                	__nameValue !== null
                "dbType" ->
                	__dbTypeValue !== null
                "url" ->
                	__urlValue !== null
                "username" ->
                	__usernameLoaded
                "password" ->
                	__passwordLoaded
                "driverClass" ->
                	__driverClassLoaded
                "enabled" ->
                	__enabledLoaded
                "description" ->
                	__descriptionLoaded
                else -> throw IllegalArgumentException(
                    "Illegal property name" + 
                    " for \"site.addzero.vibepocket.model.DatasourceConfig\": " + 
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
                    SLOT_OWNER ->
                    	__visibility.visible(SLOT_OWNER)
                    SLOT_NAME ->
                    	__visibility.visible(SLOT_NAME)
                    SLOT_DB_TYPE ->
                    	__visibility.visible(SLOT_DB_TYPE)
                    SLOT_URL ->
                    	__visibility.visible(SLOT_URL)
                    SLOT_USERNAME ->
                    	__visibility.visible(SLOT_USERNAME)
                    SLOT_PASSWORD ->
                    	__visibility.visible(SLOT_PASSWORD)
                    SLOT_DRIVER_CLASS ->
                    	__visibility.visible(SLOT_DRIVER_CLASS)
                    SLOT_ENABLED ->
                    	__visibility.visible(SLOT_ENABLED)
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
                    "owner" ->
                    	__visibility.visible(SLOT_OWNER)
                    "name" ->
                    	__visibility.visible(SLOT_NAME)
                    "dbType" ->
                    	__visibility.visible(SLOT_DB_TYPE)
                    "url" ->
                    	__visibility.visible(SLOT_URL)
                    "username" ->
                    	__visibility.visible(SLOT_USERNAME)
                    "password" ->
                    	__visibility.visible(SLOT_PASSWORD)
                    "driverClass" ->
                    	__visibility.visible(SLOT_DRIVER_CLASS)
                    "enabled" ->
                    	__visibility.visible(SLOT_ENABLED)
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
                if (__ownerValue !== null) {
                    hash = 31 * hash + __ownerValue.hashCode()
                }
                if (__nameValue !== null) {
                    hash = 31 * hash + __nameValue.hashCode()
                }
                if (__dbTypeValue !== null) {
                    hash = 31 * hash + __dbTypeValue.hashCode()
                }
                if (__urlValue !== null) {
                    hash = 31 * hash + __urlValue.hashCode()
                }
                if (__usernameLoaded) {
                    hash = 31 * hash + (__usernameValue?.hashCode() ?: 0)
                }
                if (__passwordLoaded) {
                    hash = 31 * hash + (__passwordValue?.hashCode() ?: 0)
                }
                if (__driverClassLoaded) {
                    hash = 31 * hash + (__driverClassValue?.hashCode() ?: 0)
                }
                if (__enabledLoaded) {
                    hash = 31 * hash + __enabledValue.hashCode()
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
                if (__ownerValue !== null) {
                    hash = 31 * hash + __ownerValue.hashCode()
                }
                if (__nameValue !== null) {
                    hash = 31 * hash + __nameValue.hashCode()
                }
                if (__dbTypeValue !== null) {
                    hash = 31 * hash + __dbTypeValue.hashCode()
                }
                if (__urlValue !== null) {
                    hash = 31 * hash + __urlValue.hashCode()
                }
                if (__usernameLoaded) {
                    hash = 31 * hash + (__usernameValue?.hashCode() ?: 0)
                }
                if (__passwordLoaded) {
                    hash = 31 * hash + (__passwordValue?.hashCode() ?: 0)
                }
                if (__driverClassLoaded) {
                    hash = 31 * hash + (__driverClassValue?.hashCode() ?: 0)
                }
                if (__enabledLoaded) {
                    hash = 31 * hash + __enabledValue.hashCode()
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
                if (__isVisible(PropId.byIndex(SLOT_OWNER)) != __other.__isVisible(PropId.byIndex(SLOT_OWNER))) {
                    return false
                }
                val __ownerLoaded = 
                    this.__ownerValue !== null
                if (__ownerLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_OWNER)))) {
                    return false
                }
                if (__ownerLoaded && this.__ownerValue != __other.owner) {
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
                if (__isVisible(PropId.byIndex(SLOT_DB_TYPE)) != __other.__isVisible(PropId.byIndex(SLOT_DB_TYPE))) {
                    return false
                }
                val __dbTypeLoaded = 
                    this.__dbTypeValue !== null
                if (__dbTypeLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DB_TYPE)))) {
                    return false
                }
                if (__dbTypeLoaded && this.__dbTypeValue != __other.dbType) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_URL)) != __other.__isVisible(PropId.byIndex(SLOT_URL))) {
                    return false
                }
                val __urlLoaded = 
                    this.__urlValue !== null
                if (__urlLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_URL)))) {
                    return false
                }
                if (__urlLoaded && this.__urlValue != __other.url) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_USERNAME)) != __other.__isVisible(PropId.byIndex(SLOT_USERNAME))) {
                    return false
                }
                val __usernameLoaded = 
                    this.__usernameLoaded
                if (__usernameLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_USERNAME)))) {
                    return false
                }
                if (__usernameLoaded && this.__usernameValue != __other.username) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_PASSWORD)) != __other.__isVisible(PropId.byIndex(SLOT_PASSWORD))) {
                    return false
                }
                val __passwordLoaded = 
                    this.__passwordLoaded
                if (__passwordLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_PASSWORD)))) {
                    return false
                }
                if (__passwordLoaded && this.__passwordValue != __other.password) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_DRIVER_CLASS)) != __other.__isVisible(PropId.byIndex(SLOT_DRIVER_CLASS))) {
                    return false
                }
                val __driverClassLoaded = 
                    this.__driverClassLoaded
                if (__driverClassLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DRIVER_CLASS)))) {
                    return false
                }
                if (__driverClassLoaded && this.__driverClassValue != __other.driverClass) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_ENABLED)) != __other.__isVisible(PropId.byIndex(SLOT_ENABLED))) {
                    return false
                }
                val __enabledLoaded = 
                    this.__enabledLoaded
                if (__enabledLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_ENABLED)))) {
                    return false
                }
                if (__enabledLoaded && this.__enabledValue != __other.enabled) {
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
                if (__isVisible(PropId.byIndex(SLOT_OWNER)) != __other.__isVisible(PropId.byIndex(SLOT_OWNER))) {
                    return false
                }
                val __ownerLoaded = 
                    this.__ownerValue !== null
                if (__ownerLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_OWNER)))) {
                    return false
                }
                if (__ownerLoaded && this.__ownerValue != __other.owner) {
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
                if (__isVisible(PropId.byIndex(SLOT_DB_TYPE)) != __other.__isVisible(PropId.byIndex(SLOT_DB_TYPE))) {
                    return false
                }
                val __dbTypeLoaded = 
                    this.__dbTypeValue !== null
                if (__dbTypeLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DB_TYPE)))) {
                    return false
                }
                if (__dbTypeLoaded && this.__dbTypeValue != __other.dbType) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_URL)) != __other.__isVisible(PropId.byIndex(SLOT_URL))) {
                    return false
                }
                val __urlLoaded = 
                    this.__urlValue !== null
                if (__urlLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_URL)))) {
                    return false
                }
                if (__urlLoaded && this.__urlValue != __other.url) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_USERNAME)) != __other.__isVisible(PropId.byIndex(SLOT_USERNAME))) {
                    return false
                }
                val __usernameLoaded = 
                    this.__usernameLoaded
                if (__usernameLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_USERNAME)))) {
                    return false
                }
                if (__usernameLoaded && this.__usernameValue != __other.username) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_PASSWORD)) != __other.__isVisible(PropId.byIndex(SLOT_PASSWORD))) {
                    return false
                }
                val __passwordLoaded = 
                    this.__passwordLoaded
                if (__passwordLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_PASSWORD)))) {
                    return false
                }
                if (__passwordLoaded && this.__passwordValue != __other.password) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_DRIVER_CLASS)) != __other.__isVisible(PropId.byIndex(SLOT_DRIVER_CLASS))) {
                    return false
                }
                val __driverClassLoaded = 
                    this.__driverClassLoaded
                if (__driverClassLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_DRIVER_CLASS)))) {
                    return false
                }
                if (__driverClassLoaded && this.__driverClassValue != __other.driverClass) {
                    return false
                }
                if (__isVisible(PropId.byIndex(SLOT_ENABLED)) != __other.__isVisible(PropId.byIndex(SLOT_ENABLED))) {
                    return false
                }
                val __enabledLoaded = 
                    this.__enabledLoaded
                if (__enabledLoaded != (__other.__isLoaded(PropId.byIndex(SLOT_ENABLED)))) {
                    return false
                }
                if (__enabledLoaded && this.__enabledValue != __other.enabled) {
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

        @GeneratedBy(type = DatasourceConfig::class)
        internal class DraftImpl(
            ctx: DraftContext?,
            base: DatasourceConfig?,
        ) : Implementor,
            DatasourceConfigDraft,
            DraftSpi {
            private val __ctx: DraftContext? = ctx

            private val __base: Impl? = base as Impl?

            private var __modified: Impl? = if (base === null) Impl() else null

            private var __resolving: Boolean = false

            private var __resolved: DatasourceConfig? = null

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

            override var owner: String
                get() = (__modified ?: __base!!).owner
                set(owner) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__ownerValue = owner
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

            override var dbType: String
                get() = (__modified ?: __base!!).dbType
                set(dbType) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__dbTypeValue = dbType
                }

            override var url: String
                get() = (__modified ?: __base!!).url
                set(url) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__urlValue = url
                }

            override var username: String?
                get() = (__modified ?: __base!!).username
                set(username) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__usernameValue = username
                    __tmpModified.__usernameLoaded = true
                }

            override var password: String?
                get() = (__modified ?: __base!!).password
                set(password) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__passwordValue = password
                    __tmpModified.__passwordLoaded = true
                }

            override var driverClass: String?
                get() = (__modified ?: __base!!).driverClass
                set(driverClass) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__driverClassValue = driverClass
                    __tmpModified.__driverClassLoaded = true
                }

            override var enabled: Boolean
                get() = (__modified ?: __base!!).enabled
                set(enabled) {
                    if (__resolved != null) {
                        throw IllegalStateException("The current draft has been resolved so it cannot be modified")
                    }
                    val __tmpModified = (__modified ?: __base!!.clone())
                            .also { __modified = it }
                    __tmpModified.__enabledValue = enabled
                    __tmpModified.__enabledLoaded = true
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
                    SLOT_OWNER ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__ownerValue = null
                    SLOT_NAME ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__nameValue = null
                    SLOT_DB_TYPE ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__dbTypeValue = null
                    SLOT_URL ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__urlValue = null
                    SLOT_USERNAME ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__usernameValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__usernameLoaded = false
                        }
                    SLOT_PASSWORD ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__passwordValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__passwordLoaded = false
                        }
                    SLOT_DRIVER_CLASS ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__driverClassValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__driverClassLoaded = false
                        }
                    SLOT_ENABLED ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__enabledValue = false
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__enabledLoaded = false
                        }
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
                        " for \"site.addzero.vibepocket.model.DatasourceConfig\": " + 
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
                    "owner" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__ownerValue = null
                    "name" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__nameValue = null
                    "dbType" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__dbTypeValue = null
                    "url" ->
                    	(__modified ?: __base!!.clone())
                                .also { __modified = it }
                                .__urlValue = null
                    "username" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__usernameValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__usernameLoaded = false
                        }
                    "password" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__passwordValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__passwordLoaded = false
                        }
                    "driverClass" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__driverClassValue = null
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__driverClassLoaded = false
                        }
                    "enabled" ->
                    	 {
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__enabledValue = false
                            (__modified ?: __base!!.clone())
                                    .also { __modified = it }
                                    .__enabledLoaded = false
                        }
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
                        " for \"site.addzero.vibepocket.model.DatasourceConfig\": " + 
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
                    SLOT_OWNER ->
                    	this.owner = value as String?
                    	?: throw IllegalArgumentException("'owner cannot be null")
                    SLOT_NAME ->
                    	this.name = value as String?
                    	?: throw IllegalArgumentException("'name cannot be null")
                    SLOT_DB_TYPE ->
                    	this.dbType = value as String?
                    	?: throw IllegalArgumentException("'dbType cannot be null")
                    SLOT_URL ->
                    	this.url = value as String?
                    	?: throw IllegalArgumentException("'url cannot be null")
                    SLOT_USERNAME ->
                    	this.username = value as String?
                    SLOT_PASSWORD ->
                    	this.password = value as String?
                    SLOT_DRIVER_CLASS ->
                    	this.driverClass = value as String?
                    SLOT_ENABLED ->
                    	this.enabled = value as Boolean?
                    	?: throw IllegalArgumentException("'enabled cannot be null")
                    SLOT_DESCRIPTION ->
                    	this.description = value as String?
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.DatasourceConfig\": " + 
                        prop
                    )

                }
            }

            override fun __set(prop: String, `value`: Any?) {
                when (prop) {
                    "id" ->
                    	this.id = value as Long?
                    	?: throw IllegalArgumentException("'id cannot be null")
                    "owner" ->
                    	this.owner = value as String?
                    	?: throw IllegalArgumentException("'owner cannot be null")
                    "name" ->
                    	this.name = value as String?
                    	?: throw IllegalArgumentException("'name cannot be null")
                    "dbType" ->
                    	this.dbType = value as String?
                    	?: throw IllegalArgumentException("'dbType cannot be null")
                    "url" ->
                    	this.url = value as String?
                    	?: throw IllegalArgumentException("'url cannot be null")
                    "username" ->
                    	this.username = value as String?
                    "password" ->
                    	this.password = value as String?
                    "driverClass" ->
                    	this.driverClass = value as String?
                    "enabled" ->
                    	this.enabled = value as Boolean?
                    	?: throw IllegalArgumentException("'enabled cannot be null")
                    "description" ->
                    	this.description = value as String?
                    else -> throw IllegalArgumentException(
                        "Illegal property name" + 
                        " for \"site.addzero.vibepocket.model.DatasourceConfig\": " + 
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
                    SLOT_OWNER ->
                    	__visibility.show(SLOT_OWNER, visible)
                    SLOT_NAME ->
                    	__visibility.show(SLOT_NAME, visible)
                    SLOT_DB_TYPE ->
                    	__visibility.show(SLOT_DB_TYPE, visible)
                    SLOT_URL ->
                    	__visibility.show(SLOT_URL, visible)
                    SLOT_USERNAME ->
                    	__visibility.show(SLOT_USERNAME, visible)
                    SLOT_PASSWORD ->
                    	__visibility.show(SLOT_PASSWORD, visible)
                    SLOT_DRIVER_CLASS ->
                    	__visibility.show(SLOT_DRIVER_CLASS, visible)
                    SLOT_ENABLED ->
                    	__visibility.show(SLOT_ENABLED, visible)
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
                        Visibility.of(10).also{
                            (__modified ?: __base!!.clone())
                            .also { __modified = it }.__visibility = it}
                    }
                    ?: return
                when (prop) {
                    "id" ->
                    	__visibility.show(SLOT_ID, visible)
                    "owner" ->
                    	__visibility.show(SLOT_OWNER, visible)
                    "name" ->
                    	__visibility.show(SLOT_NAME, visible)
                    "dbType" ->
                    	__visibility.show(SLOT_DB_TYPE, visible)
                    "url" ->
                    	__visibility.show(SLOT_URL, visible)
                    "username" ->
                    	__visibility.show(SLOT_USERNAME, visible)
                    "password" ->
                    	__visibility.show(SLOT_PASSWORD, visible)
                    "driverClass" ->
                    	__visibility.show(SLOT_DRIVER_CLASS, visible)
                    "enabled" ->
                    	__visibility.show(SLOT_ENABLED, visible)
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

    @GeneratedBy(type = DatasourceConfig::class)
    public class Builder {
        private val __draft: `$`.DraftImpl

        public constructor(base: DatasourceConfig?) {
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

        public fun owner(owner: String?): Builder {
            if (owner !== null) {
                __draft.owner = owner
                __draft.__show(PropId.byIndex(`$`.SLOT_OWNER), true)
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

        public fun dbType(dbType: String?): Builder {
            if (dbType !== null) {
                __draft.dbType = dbType
                __draft.__show(PropId.byIndex(`$`.SLOT_DB_TYPE), true)
            }
            return this
        }

        public fun url(url: String?): Builder {
            if (url !== null) {
                __draft.url = url
                __draft.__show(PropId.byIndex(`$`.SLOT_URL), true)
            }
            return this
        }

        public fun username(username: String?): Builder {
            __draft.username = username
            __draft.__show(PropId.byIndex(`$`.SLOT_USERNAME), true)
            return this
        }

        public fun password(password: String?): Builder {
            __draft.password = password
            __draft.__show(PropId.byIndex(`$`.SLOT_PASSWORD), true)
            return this
        }

        public fun driverClass(driverClass: String?): Builder {
            __draft.driverClass = driverClass
            __draft.__show(PropId.byIndex(`$`.SLOT_DRIVER_CLASS), true)
            return this
        }

        public fun enabled(enabled: Boolean?): Builder {
            if (enabled !== null) {
                __draft.enabled = enabled
                __draft.__show(PropId.byIndex(`$`.SLOT_ENABLED), true)
            }
            return this
        }

        public fun description(description: String?): Builder {
            __draft.description = description
            __draft.__show(PropId.byIndex(`$`.SLOT_DESCRIPTION), true)
            return this
        }

        public fun build(): DatasourceConfig = __draft.__unwrap() as DatasourceConfig
    }
}

@GeneratedBy(type = DatasourceConfig::class)
public fun ImmutableCreator<DatasourceConfig>.`by`(resolveImmediately: Boolean = false, block: DatasourceConfigDraft.() -> Unit): DatasourceConfig = DatasourceConfigDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = DatasourceConfig::class)
public fun ImmutableCreator<DatasourceConfig>.`by`(base: DatasourceConfig?, resolveImmediately: Boolean = false): DatasourceConfig = DatasourceConfigDraft.`$`.produce(base, resolveImmediately)

@GeneratedBy(type = DatasourceConfig::class)
public fun ImmutableCreator<DatasourceConfig>.`by`(
    base: DatasourceConfig?,
    resolveImmediately: Boolean = false,
    block: DatasourceConfigDraft.() -> Unit,
): DatasourceConfig = DatasourceConfigDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = DatasourceConfig::class)
public fun DatasourceConfig(resolveImmediately: Boolean = false, block: DatasourceConfigDraft.() -> Unit): DatasourceConfig = DatasourceConfigDraft.`$`.produce(null, resolveImmediately, block)

@GeneratedBy(type = DatasourceConfig::class)
public fun DatasourceConfig(
    base: DatasourceConfig?,
    resolveImmediately: Boolean = false,
    block: DatasourceConfigDraft.() -> Unit,
): DatasourceConfig = DatasourceConfigDraft.`$`.produce(base, resolveImmediately, block)

@GeneratedBy(type = DatasourceConfig::class)
public fun MutableList<DatasourceConfigDraft>.addBy(resolveImmediately: Boolean = false, block: DatasourceConfigDraft.() -> Unit): MutableList<DatasourceConfigDraft> {
    add(DatasourceConfigDraft.`$`.produce(null, resolveImmediately, block) as DatasourceConfigDraft)
    return this
}

@GeneratedBy(type = DatasourceConfig::class)
public fun MutableList<DatasourceConfigDraft>.addBy(base: DatasourceConfig?, resolveImmediately: Boolean = false): MutableList<DatasourceConfigDraft> {
    add(DatasourceConfigDraft.`$`.produce(base, resolveImmediately) as DatasourceConfigDraft)
    return this
}

@GeneratedBy(type = DatasourceConfig::class)
public fun MutableList<DatasourceConfigDraft>.addBy(
    base: DatasourceConfig?,
    resolveImmediately: Boolean = false,
    block: DatasourceConfigDraft.() -> Unit,
): MutableList<DatasourceConfigDraft> {
    add(DatasourceConfigDraft.`$`.produce(base, resolveImmediately, block) as DatasourceConfigDraft)
    return this
}

@GeneratedBy(type = DatasourceConfig::class)
public fun DatasourceConfig.copy(resolveImmediately: Boolean = false, block: DatasourceConfigDraft.() -> Unit): DatasourceConfig = DatasourceConfigDraft.`$`.produce(this, resolveImmediately, block)
