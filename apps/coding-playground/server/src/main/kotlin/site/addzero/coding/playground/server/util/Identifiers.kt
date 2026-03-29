package site.addzero.coding.playground.server.util

import java.security.MessageDigest

private val kotlinIdentifierRegex = Regex("[A-Za-z_][A-Za-z0-9_]*")
private val kotlinPackageRegex = Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*")

fun String.toPascalIdentifier(): String {
    val normalized = trim()
        .replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
        .replace(Regex("[^A-Za-z0-9]+"), " ")
    val parts = normalized.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) {
        return "Generated"
    }
    return parts.joinToString("") { part ->
        part.lowercase().replaceFirstChar { it.uppercase() }
    }
}

fun String.toLowerCamelIdentifier(): String {
    val pascal = toPascalIdentifier()
    return pascal.replaceFirstChar { it.lowercase() }
}

fun String.toKebabCase(): String {
    val normalized = trim()
        .replace(Regex("([a-z0-9])([A-Z])"), "$1-$2")
        .replace(Regex("[^A-Za-z0-9]+"), "-")
        .trim('-')
    return normalized.lowercase().ifBlank { "generated" }
}

fun String.isValidKotlinIdentifier(): Boolean = matches(kotlinIdentifierRegex)

fun String.isValidKotlinPackage(): Boolean = matches(kotlinPackageRegex)

fun String.ensureKtFileName(): String {
    val trimmed = trim()
    return if (trimmed.endsWith(".kt")) {
        trimmed
    } else {
        "$trimmed.kt"
    }
}

fun String.packagePath(): String = replace('.', '/')

fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(toByteArray()).joinToString("") { "%02x".format(it) }
}
