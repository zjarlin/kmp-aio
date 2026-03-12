package com.kcloud.plugin

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

@PublishedApi
internal val storageLogger: Logger = Logger.getLogger("com.kcloud.plugin.KCloudJsonStorage")

val kcloudJson: Json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

inline fun <reified T> readKCloudJson(
    file: File,
    defaultValue: () -> T
): T {
    return try {
        if (!file.exists()) {
            defaultValue()
        } else {
            kcloudJson.decodeFromString(file.readText())
        }
    } catch (exception: Exception) {
        storageLogger.log(Level.WARNING, "Failed to read ${file.absolutePath}", exception)
        defaultValue()
    }
}

inline fun <reified T> writeKCloudJson(
    file: File,
    value: T
) {
    try {
        file.parentFile?.mkdirs()
        file.writeText(kcloudJson.encodeToString(value))
    } catch (exception: Exception) {
        storageLogger.log(Level.WARNING, "Failed to write ${file.absolutePath}", exception)
    }
}
