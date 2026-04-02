package site.addzero.configcenter

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.Properties
import java.util.UUID

class JdbcConfigCenterValueService(
    private val settings: ConfigCenterJdbcSettings,
) : ConfigCenterAdminService {
    private val dialect: ConfigCenterJdbcDialect = settings.detectDialect()

    init {
        settings.driver?.let { driver ->
            Class.forName(driver)
        }
        if (settings.autoDdl) {
            withConnection { connection ->
                ensureSchema(connection)
            }
        }
    }

    override fun listValues(
        namespace: String?,
        active: String?,
        keyword: String?,
        limit: Int,
    ): List<ConfigCenterValueDto> {
        return withConnection { connection ->
            listValues(
                connection = connection,
                namespace = namespace,
                active = active,
                keyword = keyword,
                limit = limit,
            )
        }
    }

    override fun readValue(
        namespace: String,
        key: String,
        active: String,
    ): ConfigCenterValueDto {
        val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
        val normalizedActive = normalizeConfigCenterActive(active)
        val normalizedKey = key.trim()
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedKey.isNotBlank()) { "key cannot be blank" }

        return withConnection { connection ->
            val selectedColumns = selectValueColumnsSql(connection)
            connection.prepareStatement(
                """
                SELECT $selectedColumns
                FROM config_center_value
                WHERE namespace = ?
                  AND active_profile = ?
                  AND config_key = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, normalizedNamespace)
                statement.setString(2, normalizedActive)
                statement.setString(3, normalizedKey)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        resultSet.toValueDto()
                    } else {
                        ConfigCenterValueDto(
                            namespace = normalizedNamespace,
                            active = normalizedActive,
                            key = normalizedKey,
                        )
                    }
                }
            }
        }
    }

    override fun writeValue(
        request: ConfigCenterValueWriteRequest,
    ): ConfigCenterValueDto {
        val normalizedNamespace = normalizeConfigCenterNamespace(request.namespace)
        val normalizedActive = normalizeConfigCenterActive(request.active)
        val normalizedKey = request.key.trim()
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedKey.isNotBlank()) { "key cannot be blank" }

        val now = System.currentTimeMillis()
        withConnection { connection ->
            val createdAt = lookupExistingCreateTime(
                connection = connection,
                namespace = normalizedNamespace,
                active = normalizedActive,
                key = normalizedKey,
                tableName = "config_center_value",
            ) ?: now
            connection.prepareStatement(
                """
                INSERT INTO config_center_value (
                    id, namespace, active_profile, config_key, config_value, comment, create_time, update_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(namespace, active_profile, config_key) DO UPDATE SET
                    config_value = excluded.config_value,
                    comment = excluded.comment,
                    update_time = excluded.update_time
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, UUID.randomUUID().toString())
                statement.setString(2, normalizedNamespace)
                statement.setString(3, normalizedActive)
                statement.setString(4, normalizedKey)
                statement.setString(5, request.value)
                statement.setString(6, request.comment)
                statement.setLong(7, createdAt)
                statement.setLong(8, now)
                statement.executeUpdate()
            }
        }

        return readValue(
            namespace = normalizedNamespace,
            key = normalizedKey,
            active = normalizedActive,
        )
    }

    override fun deleteValue(
        namespace: String,
        key: String,
        active: String,
    ): Boolean {
        val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
        val normalizedActive = normalizeConfigCenterActive(active)
        val normalizedKey = key.trim()
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedKey.isNotBlank()) { "key cannot be blank" }

        return withConnection { connection ->
            connection.prepareStatement(
                """
                DELETE FROM config_center_value
                WHERE namespace = ?
                  AND active_profile = ?
                  AND config_key = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, normalizedNamespace)
                statement.setString(2, normalizedActive)
                statement.setString(3, normalizedKey)
                statement.executeUpdate() > 0
            }
        }
    }

    override fun listEntries(
        namespace: String?,
        active: String?,
        keyword: String?,
        limit: Int,
    ): List<ConfigCenterEntryDto> {
        return withConnection { connection ->
            val definitions = listDefinitions(
                connection = connection,
                namespace = namespace,
                keyword = keyword,
                limit = limit,
            )
            val values = listValues(
                connection = connection,
                namespace = namespace,
                active = active,
                keyword = keyword,
                limit = limit,
            )
            mergeEntries(
                definitions = definitions,
                values = values,
                active = active,
                limit = limit,
            )
        }
    }

    override fun readEntry(
        namespace: String,
        key: String,
        active: String,
    ): ConfigCenterEntryDto {
        val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
        val normalizedKey = key.trim()
        val normalizedActive = normalizeConfigCenterActive(active)
        return withConnection { connection ->
            val definition = findDefinition(
                connection = connection,
                namespace = normalizedNamespace,
                key = normalizedKey,
            )
            val value = readValue(
                namespace = normalizedNamespace,
                key = normalizedKey,
                active = normalizedActive,
            )
            definition?.toEntry(
                active = normalizedActive,
                value = value,
                resolvedKey = normalizedKey,
            )
                ?: value.toEntry()
        }
    }

    override fun writeEntry(
        request: ConfigCenterEntryWriteRequest,
    ): ConfigCenterEntryDto {
        val normalizedNamespace = normalizeConfigCenterNamespace(request.namespace)
        val normalizedActive = normalizeConfigCenterActive(request.active)
        val normalizedKey = request.key.trim()
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedKey.isNotBlank()) { "key cannot be blank" }

        val valueType = request.valueType
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: "kotlin.String"

        upsertDefinitions(
            listOf(
                ConfigCenterKeyDefinition(
                    namespace = normalizedNamespace,
                    key = normalizedKey,
                    valueType = valueType,
                    comment = request.comment,
                    defaultValue = request.defaultValue,
                    required = request.required ?: false,
                ),
            ),
        )

        request.value?.let { rawValue ->
            writeValue(
                ConfigCenterValueWriteRequest(
                    namespace = normalizedNamespace,
                    active = normalizedActive,
                    key = normalizedKey,
                    value = rawValue,
                    comment = request.comment,
                ),
            )
        }

        return readEntry(
            namespace = normalizedNamespace,
            key = normalizedKey,
            active = normalizedActive,
        )
    }

    override fun deleteEntry(
        namespace: String,
        key: String,
        active: String,
    ): Boolean {
        val normalizedNamespace = normalizeConfigCenterNamespace(namespace)
        val normalizedActive = normalizeConfigCenterActive(active)
        val normalizedKey = key.trim()
        require(normalizedNamespace.isNotBlank()) { "namespace cannot be blank" }
        require(normalizedKey.isNotBlank()) { "key cannot be blank" }

        return withConnection { connection ->
            val deletedValue = connection.prepareStatement(
                """
                DELETE FROM config_center_value
                WHERE namespace = ?
                  AND active_profile = ?
                  AND config_key = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, normalizedNamespace)
                statement.setString(2, normalizedActive)
                statement.setString(3, normalizedKey)
                statement.executeUpdate() > 0
            }
            val deletedDefinition = connection.prepareStatement(
                """
                DELETE FROM config_center_definition
                WHERE namespace = ?
                  AND config_key = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, normalizedNamespace)
                statement.setString(2, normalizedKey)
                statement.executeUpdate() > 0
            }
            deletedValue || deletedDefinition
        }
    }

    fun upsertDefinitions(
        definitions: List<ConfigCenterKeyDefinition>,
    ): Int {
        if (definitions.isEmpty()) {
            return 0
        }
        return withConnection { connection ->
            val deduplicated = LinkedHashMap<String, ConfigCenterKeyDefinition>()
            definitions.forEach { definition ->
                val normalizedNamespace = normalizeConfigCenterNamespace(definition.namespace)
                val normalizedKey = definition.key.trim()
                if (normalizedNamespace.isBlank() || normalizedKey.isBlank()) {
                    return@forEach
                }
                deduplicated["$normalizedNamespace::$normalizedKey"] = definition.copy(
                    namespace = normalizedNamespace,
                    key = normalizedKey,
                )
            }
            val now = System.currentTimeMillis()
            deduplicated.values.forEach { definition ->
                val createdAt = lookupExistingCreateTime(
                    connection = connection,
                    namespace = definition.namespace,
                    key = definition.key,
                    tableName = "config_center_definition",
                ) ?: now
                connection.prepareStatement(
                    """
                    INSERT INTO config_center_definition (
                        id, namespace, config_key, value_type, comment, default_value, required, source, create_time, update_time
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(namespace, config_key) DO UPDATE SET
                        value_type = excluded.value_type,
                        comment = excluded.comment,
                        default_value = excluded.default_value,
                        required = excluded.required,
                        source = excluded.source,
                        update_time = excluded.update_time
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, UUID.randomUUID().toString())
                    statement.setString(2, definition.namespace)
                    statement.setString(3, definition.key)
                    statement.setString(4, definition.valueType.ifBlank { "kotlin.String" })
                    statement.setString(5, definition.comment)
                    statement.setString(6, definition.defaultValue)
                    statement.setBoolean(7, definition.required)
                    statement.setString(8, "ksp")
                    statement.setLong(9, createdAt)
                    statement.setLong(10, now)
                    statement.executeUpdate()
                }
            }
            deduplicated.size
        }
    }

    private fun listValues(
        connection: Connection,
        namespace: String?,
        active: String?,
        keyword: String?,
        limit: Int,
    ): List<ConfigCenterValueDto> {
        val selectedColumns = selectValueColumnsSql(connection)
        val clauses = mutableListOf<String>()
        val values = mutableListOf<Any>()
        namespace?.trim()?.takeIf(String::isNotBlank)?.let { rawNamespace ->
            clauses += "namespace = ?"
            values += normalizeConfigCenterNamespace(rawNamespace)
        }
        active?.trim()?.takeIf(String::isNotBlank)?.let { rawActive ->
            clauses += "active_profile = ?"
            values += normalizeConfigCenterActive(rawActive)
        }
        keyword?.trim()?.takeIf(String::isNotBlank)?.let { rawKeyword ->
            clauses += "(config_key LIKE ? OR config_value LIKE ? OR COALESCE(comment, description, '') LIKE ?)"
            val likeValue = "%$rawKeyword%"
            values += likeValue
            values += likeValue
            values += likeValue
        }
        val whereClause = if (clauses.isEmpty()) "" else clauses.joinToString(
            prefix = " WHERE ",
            separator = " AND ",
        )
        return connection.prepareStatement(
            """
            SELECT $selectedColumns
            FROM config_center_value
            $whereClause
            ORDER BY namespace ASC, active_profile ASC, config_key ASC
            LIMIT ?
            """.trimIndent(),
        ).use { statement ->
            var parameterIndex = 1
            for (value in values) {
                statement.setObject(parameterIndex++, value)
            }
            statement.setInt(parameterIndex, limit.coerceIn(1, 2_000))
            statement.executeQuery().use(::readValueRows)
        }
    }

    private fun listDefinitions(
        connection: Connection,
        namespace: String?,
        keyword: String?,
        limit: Int,
    ): List<StoredDefinition> {
        val clauses = mutableListOf<String>()
        val values = mutableListOf<Any>()
        namespace?.trim()?.takeIf(String::isNotBlank)?.let { rawNamespace ->
            clauses += "namespace = ?"
            values += normalizeConfigCenterNamespace(rawNamespace)
        }
        keyword?.trim()?.takeIf(String::isNotBlank)?.let { rawKeyword ->
            clauses += "(config_key LIKE ? OR COALESCE(comment, '') LIKE ? OR COALESCE(default_value, '') LIKE ?)"
            val likeValue = "%$rawKeyword%"
            values += likeValue
            values += likeValue
            values += likeValue
        }
        val whereClause = if (clauses.isEmpty()) "" else clauses.joinToString(
            prefix = " WHERE ",
            separator = " AND ",
        )
        return connection.prepareStatement(
            """
            SELECT id, namespace, config_key, value_type, comment, default_value, required, source, create_time, update_time
            FROM config_center_definition
            $whereClause
            ORDER BY namespace ASC, config_key ASC
            LIMIT ?
            """.trimIndent(),
        ).use { statement ->
            var parameterIndex = 1
            for (value in values) {
                statement.setObject(parameterIndex++, value)
            }
            statement.setInt(parameterIndex, limit.coerceIn(1, 2_000))
            statement.executeQuery().use { resultSet ->
                val definitions = mutableListOf<StoredDefinition>()
                while (resultSet.next()) {
                    definitions += resultSet.toDefinition()
                }
                definitions
            }
        }
    }

    private fun findDefinition(
        connection: Connection,
        namespace: String,
        key: String,
    ): StoredDefinition? {
        connection.prepareStatement(
            """
            SELECT id, namespace, config_key, value_type, comment, default_value, required, source, create_time, update_time
            FROM config_center_definition
            WHERE namespace = ?
              AND config_key = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, namespace)
            statement.setString(2, key)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.toDefinition()
                }
            }
        }
        return findTemplateDefinition(
            connection = connection,
            namespace = namespace,
            key = key,
        )
    }

    private fun findTemplateDefinition(
        connection: Connection,
        namespace: String,
        key: String,
    ): StoredDefinition? {
        return connection.prepareStatement(
            """
            SELECT id, namespace, config_key, value_type, comment, default_value, required, source, create_time, update_time
            FROM config_center_definition
            WHERE namespace = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, namespace)
            statement.executeQuery().use { resultSet ->
                val definitions = mutableListOf<StoredDefinition>()
                while (resultSet.next()) {
                    definitions += resultSet.toDefinition()
                }
                definitions.findBestMatchingDefinition(
                    namespace = namespace,
                    key = key,
                )
            }
        }
    }

    private fun lookupExistingCreateTime(
        connection: Connection,
        namespace: String,
        key: String,
        tableName: String,
        active: String? = null,
    ): Long? {
        val sql = buildString {
            append("SELECT create_time FROM ")
            append(tableName)
            append(" WHERE namespace = ?")
            if (active != null) {
                append(" AND active_profile = ?")
            }
            append(" AND config_key = ?")
        }
        connection.prepareStatement(sql).use { statement ->
            var parameterIndex = 1
            statement.setString(parameterIndex++, namespace)
            if (active != null) {
                statement.setString(parameterIndex++, active)
            }
            statement.setString(parameterIndex, key)
            statement.executeQuery().use { resultSet ->
                return if (resultSet.next()) resultSet.getLong(1) else null
            }
        }
    }

    private fun ensureSchema(
        connection: Connection,
    ) {
        connection.createStatement().use { statement ->
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS config_center_value (
                    id TEXT PRIMARY KEY,
                    namespace TEXT NOT NULL,
                    active_profile TEXT NOT NULL,
                    config_key TEXT NOT NULL,
                    config_value TEXT NOT NULL,
                    comment TEXT,
                    create_time BIGINT NOT NULL,
                    update_time BIGINT NOT NULL
                )
                """.trimIndent(),
            )
            statement.executeUpdate(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS idx_config_center_value_lookup
                    ON config_center_value(namespace, active_profile, config_key)
                """.trimIndent(),
            )
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS config_center_definition (
                    id TEXT PRIMARY KEY,
                    namespace TEXT NOT NULL,
                    config_key TEXT NOT NULL,
                    value_type TEXT NOT NULL,
                    comment TEXT,
                    default_value TEXT,
                    required BOOLEAN NOT NULL DEFAULT FALSE,
                    source TEXT,
                    create_time BIGINT NOT NULL,
                    update_time BIGINT NOT NULL
                )
                """.trimIndent(),
            )
            statement.executeUpdate(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS idx_config_center_definition_lookup
                    ON config_center_definition(namespace, config_key)
                """.trimIndent(),
            )
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS config_center_meta (
                    meta_key TEXT PRIMARY KEY,
                    meta_value TEXT NOT NULL
                )
                """.trimIndent(),
            )
            statement.executeUpdate(
                """
                INSERT INTO config_center_meta(meta_key, meta_value)
                VALUES ('table_note', '${settings.tableNote.replace("'", "''")}')
                ON CONFLICT(meta_key) DO UPDATE SET meta_value = excluded.meta_value
                """.trimIndent(),
            )
            if (dialect == ConfigCenterJdbcDialect.POSTGRES) {
                statement.executeUpdate(
                    """
                    COMMENT ON TABLE config_center_value IS '${settings.tableNote.replace("'", "''")}'
                    """.trimIndent(),
                )
                statement.executeUpdate(
                    """
                    COMMENT ON TABLE config_center_definition IS '${settings.tableNote.replace("'", "''")}'
                    """.trimIndent(),
                )
                statement.executeUpdate(
                    """
                    COMMENT ON TABLE config_center_meta IS '${settings.tableNote.replace("'", "''")}'
                    """.trimIndent(),
                )
            }
        }
        ensureCompatibleValueColumns(connection)
        ensureCompatibleDefinitionColumns(connection)
    }

    private fun <T> withConnection(
        block: (Connection) -> T,
    ): T {
        return openConnection().use(block)
    }

    private fun openConnection(): Connection {
        if (settings.username.isNullOrBlank() && settings.password.isNullOrBlank()) {
            return DriverManager.getConnection(settings.url)
        }
        val properties = Properties()
        settings.username?.let { properties["user"] = it }
        settings.password?.let { properties["password"] = it }
        return DriverManager.getConnection(settings.url, properties)
    }

    private fun ensureCompatibleValueColumns(
        connection: Connection,
    ) {
        val columns = existingColumns(connection, "config_center_value")
        connection.createStatement().use { statement ->
            if ("comment" !in columns) {
                statement.executeUpdate("ALTER TABLE config_center_value ADD COLUMN comment TEXT")
            }
            if ("create_time" !in columns) {
                statement.executeUpdate(
                    "ALTER TABLE config_center_value ADD COLUMN create_time BIGINT NOT NULL DEFAULT 0",
                )
            }
            if ("update_time" !in columns) {
                statement.executeUpdate(
                    "ALTER TABLE config_center_value ADD COLUMN update_time BIGINT NOT NULL DEFAULT 0",
                )
            }
        }
    }

    private fun ensureCompatibleDefinitionColumns(
        connection: Connection,
    ) {
        val columns = existingColumns(connection, "config_center_definition")
        connection.createStatement().use { statement ->
            if ("comment" !in columns) {
                statement.executeUpdate("ALTER TABLE config_center_definition ADD COLUMN comment TEXT")
            }
            if ("default_value" !in columns) {
                statement.executeUpdate("ALTER TABLE config_center_definition ADD COLUMN default_value TEXT")
            }
            if ("required" !in columns) {
                statement.executeUpdate(
                    "ALTER TABLE config_center_definition ADD COLUMN required BOOLEAN NOT NULL DEFAULT FALSE",
                )
            }
            if ("source" !in columns) {
                statement.executeUpdate("ALTER TABLE config_center_definition ADD COLUMN source TEXT")
            }
            if ("create_time" !in columns) {
                statement.executeUpdate(
                    "ALTER TABLE config_center_definition ADD COLUMN create_time BIGINT NOT NULL DEFAULT 0",
                )
            }
            if ("update_time" !in columns) {
                statement.executeUpdate(
                    "ALTER TABLE config_center_definition ADD COLUMN update_time BIGINT NOT NULL DEFAULT 0",
                )
            }
        }
    }

    private fun existingColumns(
        connection: Connection,
        tableName: String,
    ): Set<String> {
        val metadata = connection.metaData
        metadata.getColumns(null, null, tableName, null).use { resultSet ->
            val columns = linkedSetOf<String>()
            while (resultSet.next()) {
                resultSet.getString("COLUMN_NAME")
                    ?.trim()
                    ?.lowercase()
                    ?.takeIf(String::isNotBlank)
                    ?.let(columns::add)
            }
            if (columns.isNotEmpty()) {
                return columns
            }
        }
        metadata.getColumns(null, null, tableName.uppercase(), null).use { resultSet ->
            val columns = linkedSetOf<String>()
            while (resultSet.next()) {
                resultSet.getString("COLUMN_NAME")
                    ?.trim()
                    ?.lowercase()
                    ?.takeIf(String::isNotBlank)
                    ?.let(columns::add)
            }
            return columns
        }
    }

    private fun selectValueColumnsSql(
        connection: Connection,
    ): String {
        val columns = existingColumns(connection, "config_center_value")
        val commentColumn = when {
            "comment" in columns && "description" in columns -> "COALESCE(comment, description) AS comment"
            "comment" in columns -> "comment"
            "description" in columns -> "description AS comment"
            else -> "NULL AS comment"
        }
        val createTimeColumn = if ("create_time" in columns) "create_time" else "NULL AS create_time"
        val updateTimeColumn = if ("update_time" in columns) "update_time" else "NULL AS update_time"
        return listOf(
            "id",
            "namespace",
            "active_profile",
            "config_key",
            "config_value",
            commentColumn,
            createTimeColumn,
            updateTimeColumn,
        ).joinToString(", ")
    }
}

private data class StoredDefinition(
    val namespace: String,
    val key: String,
    val valueType: String,
    val comment: String?,
    val defaultValue: String?,
    val required: Boolean,
    val createTimeMillis: Long?,
    val updateTimeMillis: Long?,
) {
    val isTemplate: Boolean
        get() = TEMPLATE_KEY_REGEX.containsMatchIn(key)

    fun toEntry(
        active: String,
        value: ConfigCenterValueDto? = null,
        resolvedKey: String = key,
    ): ConfigCenterEntryDto {
        return ConfigCenterEntryDto(
            namespace = namespace,
            active = active,
            key = resolvedKey,
            value = value?.value,
            comment = comment ?: value?.comment,
            defaultValue = defaultValue,
            valueType = valueType,
            required = required,
            createTimeMillis = value?.createTimeMillis ?: createTimeMillis,
            updateTimeMillis = value?.updateTimeMillis ?: updateTimeMillis,
        )
    }
}

private fun mergeEntries(
    definitions: List<StoredDefinition>,
    values: List<ConfigCenterValueDto>,
    active: String?,
    limit: Int,
): List<ConfigCenterEntryDto> {
    val merged = LinkedHashMap<String, ConfigCenterEntryDto>()
    val normalizedActive = active?.trim()?.takeIf(String::isNotBlank)?.let(::normalizeConfigCenterActive)
        ?: DEFAULT_CONFIG_CENTER_ACTIVE

    definitions.forEach { definition ->
        val key = "${definition.namespace}::${definition.key}"
        merged[key] = definition.toEntry(active = normalizedActive)
    }
    values.forEach { value ->
        val key = "${value.namespace}::${value.key}"
        val existing = merged[key]
        val matchedDefinition = if (existing == null) {
            definitions.findBestMatchingDefinition(
                namespace = value.namespace,
                key = value.key,
            )
        } else {
            null
        }
        merged[key] = if (existing != null) {
            existing.copy(
                active = value.active,
                value = value.value,
                comment = existing.comment ?: value.comment,
                createTimeMillis = value.createTimeMillis ?: existing.createTimeMillis,
                updateTimeMillis = value.updateTimeMillis ?: existing.updateTimeMillis,
            )
        } else if (matchedDefinition != null) {
            matchedDefinition.toEntry(
                active = value.active,
                value = value,
                resolvedKey = value.key,
            )
        } else {
            value.toEntry()
        }
    }
    return merged.values.take(limit.coerceIn(1, 2_000))
}

private fun readValueRows(
    resultSet: ResultSet,
): List<ConfigCenterValueDto> {
    val values = mutableListOf<ConfigCenterValueDto>()
    while (resultSet.next()) {
        values += resultSet.toValueDto()
    }
    return values
}

private fun ResultSet.toValueDto(): ConfigCenterValueDto {
    return ConfigCenterValueDto(
        id = getString("id").orEmpty(),
        namespace = getString("namespace").orEmpty(),
        active = getString("active_profile").orEmpty(),
        key = getString("config_key").orEmpty(),
        value = getString("config_value"),
        comment = getString("comment"),
        createTimeMillis = getLong("create_time").takeIf { !wasNull() },
        updateTimeMillis = getLong("update_time").takeIf { !wasNull() },
    )
}

private fun ResultSet.toDefinition(): StoredDefinition {
    return StoredDefinition(
        namespace = getString("namespace").orEmpty(),
        key = getString("config_key").orEmpty(),
        valueType = getString("value_type").orEmpty().ifBlank { "kotlin.String" },
        comment = getString("comment"),
        defaultValue = getString("default_value"),
        required = getBoolean("required").takeIf { !wasNull() } ?: false,
        createTimeMillis = getLong("create_time").takeIf { !wasNull() },
        updateTimeMillis = getLong("update_time").takeIf { !wasNull() },
    )
}

private fun ConfigCenterValueDto.toEntry(): ConfigCenterEntryDto {
    return ConfigCenterEntryDto(
        namespace = namespace,
        active = active,
        key = key,
        value = value,
        comment = comment,
        createTimeMillis = createTimeMillis,
        updateTimeMillis = updateTimeMillis,
    )
}

private fun List<StoredDefinition>.findBestMatchingDefinition(
    namespace: String,
    key: String,
): StoredDefinition? {
    return asSequence()
        .filter { definition -> definition.namespace == namespace }
        .filter { definition -> definition.matchesKey(key) }
        .maxByOrNull(StoredDefinition::matchScore)
}

private fun StoredDefinition.matchesKey(
    actualKey: String,
): Boolean {
    if (key == actualKey) {
        return true
    }
    if (!isTemplate) {
        return false
    }
    return key.templateKeyRegex().matches(actualKey)
}

private fun StoredDefinition.matchScore(): Int {
    return key.replace(TEMPLATE_KEY_REGEX, "").length
}

private fun String.templateKeyRegex(): Regex {
    val pattern = buildString {
        append("^")
        var startIndex = 0
        TEMPLATE_KEY_REGEX.findAll(this@templateKeyRegex).forEach { matchResult ->
            append(Regex.escape(this@templateKeyRegex.substring(startIndex, matchResult.range.first)))
            append("[^.]+")
            startIndex = matchResult.range.last + 1
        }
        append(Regex.escape(this@templateKeyRegex.substring(startIndex)))
        append("$")
    }
    return Regex(pattern)
}

private val TEMPLATE_KEY_REGEX = Regex("\\{[A-Za-z_][A-Za-z0-9_]*}")
