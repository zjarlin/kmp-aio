package site.addzero.kbox.core.service

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxTrashRecord
import site.addzero.kbox.core.support.moveFileReplacing
import site.addzero.kbox.core.support.stableShortHash
import java.awt.Desktop
import java.io.File

@Single
class KboxTrashService(
    private val json: Json,
    private val pathService: KboxPathService,
) {
    fun moveToTrash(
        file: File,
    ): KboxTrashRecord {
        require(file.exists()) {
            "File does not exist: ${file.absolutePath}"
        }
        val deletedAtMillis = System.currentTimeMillis()
        if (Desktop.isDesktopSupported() && tryMoveToSystemTrash(file)) {
            return appendRecord(
                KboxTrashRecord(
                    sourcePath = file.absolutePath,
                    trashPath = "<system-trash>",
                    deletedAtMillis = deletedAtMillis,
                ),
            )
        }
        val fallbackTarget = File(
            pathService.trashDir(),
            "${deletedAtMillis}-${stableShortHash(file.absolutePath)}-${file.name}",
        )
        moveFileReplacing(file, fallbackTarget)
        return appendRecord(
            KboxTrashRecord(
                sourcePath = file.absolutePath,
                trashPath = fallbackTarget.absolutePath,
                deletedAtMillis = deletedAtMillis,
            ),
        )
    }

    private fun tryMoveToSystemTrash(
        file: File,
    ): Boolean {
        if (!Desktop.isDesktopSupported()) {
            return false
        }
        return runCatching {
            val desktop = Desktop.getDesktop()
            val method = desktop.javaClass.methods.firstOrNull { candidate ->
                candidate.name == "moveToTrash" &&
                    candidate.parameterTypes.contentEquals(arrayOf(File::class.java))
            } ?: return false
            (method.invoke(desktop, file) as? Boolean) == true
        }.getOrDefault(false)
    }

    fun readRecords(): List<KboxTrashRecord> {
        val file = pathService.trashIndexFile()
        if (!file.isFile) {
            return emptyList()
        }
        return json.decodeFromString(file.readText())
    }

    private fun appendRecord(
        record: KboxTrashRecord,
    ): KboxTrashRecord {
        val file = pathService.trashIndexFile()
        val records = readRecords() + record
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(records))
        return record
    }
}
