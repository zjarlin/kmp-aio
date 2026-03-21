package site.addzero.system.config.feature

import kotlinx.serialization.json.Json
import site.addzero.system.common.dto.PageResult
import site.addzero.system.common.exception.DuplicateResourceException
import site.addzero.system.common.exception.ResourceNotFoundException
import site.addzero.system.config.dto.*
import site.addzero.system.config.spi.SystemConfigSpi
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 基于内存的系统配置服务默认实现
 */
open class InMemoryConfigService : SystemConfigSpi {

    protected val configStore = ConcurrentHashMap<String, ConfigDTO>()
    protected val cache = ConcurrentHashMap<String, String>()
    protected val idGenerator = AtomicLong(1)
    protected val json = Json { ignoreUnknownKeys = true }

    init {
        // 初始化一些默认配置
        initDefaults()
    }

    protected open fun initDefaults() {
        val defaults = listOf(
            ConfigCreateRequest("system.name", "VibePocket", "系统名称", "system"),
            ConfigCreateRequest("system.version", "1.0.0", "系统版本", "system", isSystem = true),
            ConfigCreateRequest("user.default_avatar", "/avatar/default.png", "默认头像", "user"),
            ConfigCreateRequest("file.max_size", "10485760", "文件上传大小限制(10MB)", "file", valueType = ValueType.INTEGER),
            ConfigCreateRequest("file.allowed_types", "jpg,png,gif,pdf,doc,docx", "允许上传的文件类型", "file")
        )
        defaults.forEach { createInternal(it) }
    }

    override fun getString(key: String, defaultValue: String?): String? {
        return cache[key] ?: configStore[key]?.value?.also { cache[key] = it } ?: defaultValue
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getString(key)?.toIntOrNull() ?: defaultValue
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getString(key)?.toBooleanStrictOrNull() ?: defaultValue
    }

    override fun <T> getObject(key: String, clazz: Class<T>): T? {
        val value = getString(key) ?: return null
        return try {
            json.decodeFromString(serializer(clazz), value)
        } catch (e: Exception) {
            null
        }
    }

    override fun set(key: String, value: String, description: String?): String {
        val existing = configStore.values.find { it.key == key }
        return if (existing != null) {
            if (!existing.editable) {
                throw IllegalStateException("Config $key is not editable")
            }
            val updated = existing.copy(
                value = value,
                description = description ?: existing.description,
                updatedAt = Instant.now()
            )
            configStore[existing.id] = updated
            cache[key] = value
            existing.id
        } else {
            createInternal(ConfigCreateRequest(key, value, description))
        }
    }

    override fun setBatch(configs: Map<String, String>) {
        configs.forEach { (k, v) -> set(k, v) }
    }

    override fun delete(key: String) {
        val config = configStore.values.find { it.key == key }
            ?: throw ResourceNotFoundException("Config", key)
        if (config.isSystem) {
            throw IllegalStateException("System config cannot be deleted: $key")
        }
        configStore.remove(config.id)
        cache.remove(key)
    }

    override fun deleteBatch(keys: List<String>) {
        keys.forEach { delete(it) }
    }

    override fun getById(id: String): ConfigDTO? = configStore[id]

    override fun getByKey(key: String): ConfigDTO? =
        configStore.values.find { it.key == key }

    override fun page(query: ConfigQuery): PageResult<ConfigDTO> {
        val filtered = configStore.values.filter { config ->
            (query.keyword == null ||
                    config.key.contains(query.keyword, ignoreCase = true) ||
                    config.description?.contains(query.keyword, ignoreCase = true) == true) &&
                    (query.category == null || config.category == query.category) &&
                    (query.valueType == null || config.valueType == query.valueType) &&
                    (query.isSystem == null || config.isSystem == query.isSystem)
        }.sortedBy { it.sortOrder }

        val total = filtered.size.toLong()
        val offset = query.offset().toInt()
        val limit = query.limit()
        val list = filtered.drop(offset).take(limit)

        return PageResult(list, total, query.pageNum, query.pageSize)
    }

    override fun listByCategory(category: String): List<ConfigDTO> {
        return configStore.values
            .filter { it.category == category }
            .sortedBy { it.sortOrder }
    }

    override fun getAll(): Map<String, String> {
        return configStore.values.associate { it.key to it.value }
    }

    override fun exists(key: String): Boolean {
        return configStore.values.any { it.key == key }
    }

    override fun refresh() {
        cache.clear()
        configStore.values.forEach { cache[it.key] = it.value }
    }

    override fun export(): String {
        val exportData = configStore.values
            .filter { !it.isEncrypted }
            .associate { it.key to it.value }
        return json.encodeToString(exportData)
    }

    override fun import(configJson: String): Int {
        val data = json.decodeFromString<Map<String, String>>(configJson)
        data.forEach { (k, v) ->
            if (!exists(k)) {
                set(k, v)
            }
        }
        return data.size
    }

    // === 内部方法 ===

    private fun createInternal(request: ConfigCreateRequest): String {
        if (configStore.values.any { it.key == request.key }) {
            throw DuplicateResourceException("Config", "key")
        }

        val config = ConfigDTO(
            id = idGenerator.getAndIncrement().toString(),
            key = request.key,
            value = request.value,
            description = request.description,
            category = request.category,
            valueType = request.valueType,
            isEncrypted = request.isEncrypted,
            isSystem = request.isSystem,
            editable = request.editable,
            sortOrder = request.sortOrder,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        configStore[config.id] = config
        cache[config.key] = config.value
        return config.id
    }
}
