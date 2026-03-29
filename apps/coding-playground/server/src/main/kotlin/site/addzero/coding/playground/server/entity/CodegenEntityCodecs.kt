package site.addzero.coding.playground.server.entity

import kotlinx.serialization.json.Json
import site.addzero.coding.playground.shared.dto.FunctionParameterDto

private val codegenEntityJson = Json {
    ignoreUnknownKeys = true
}

fun encodeStringList(value: List<String>): String? = value.takeIf { it.isNotEmpty() }?.let(codegenEntityJson::encodeToString)

fun decodeStringList(value: String?): List<String> = value?.takeIf { it.isNotBlank() }?.let {
    codegenEntityJson.decodeFromString<List<String>>(it)
} ?: emptyList()

fun encodeStringMap(value: Map<String, String>): String? = value.takeIf { it.isNotEmpty() }?.let(codegenEntityJson::encodeToString)

fun decodeStringMap(value: String?): Map<String, String> = value?.takeIf { it.isNotBlank() }?.let {
    codegenEntityJson.decodeFromString<Map<String, String>>(it)
} ?: emptyMap()

fun encodeFunctionParameters(value: List<FunctionParameterDto>): String? =
    value.takeIf { it.isNotEmpty() }?.let(codegenEntityJson::encodeToString)

fun decodeFunctionParameters(value: String?): List<FunctionParameterDto> = value?.takeIf { it.isNotBlank() }?.let {
    codegenEntityJson.decodeFromString<List<FunctionParameterDto>>(it)
} ?: emptyList()
