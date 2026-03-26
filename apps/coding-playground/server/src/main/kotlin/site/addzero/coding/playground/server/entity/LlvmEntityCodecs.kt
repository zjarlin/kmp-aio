package site.addzero.coding.playground.server.entity

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

private val llvmEntityJson = Json {
    ignoreUnknownKeys = true
}

fun encodeStringList(value: List<String>): String? = value.takeIf { it.isNotEmpty() }?.let(llvmEntityJson::encodeToString)
fun decodeStringList(value: String?): List<String> = value?.takeIf { it.isNotBlank() }?.let {
    llvmEntityJson.decodeFromString<List<String>>(it)
} ?: emptyList()

fun encodeStringMap(value: Map<String, String>): String? = value.takeIf { it.isNotEmpty() }?.let(llvmEntityJson::encodeToString)
fun decodeStringMap(value: String?): Map<String, String> = value?.takeIf { it.isNotBlank() }?.let {
    llvmEntityJson.decodeFromString<Map<String, String>>(it)
} ?: emptyMap()

