package site.addzero.kbox.core.service

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxSyncIndexRecord

@Single
class KboxSyncIndexStore(
    private val json: Json,
    private val pathService: KboxPathService,
) {
    fun readAll(): List<KboxSyncIndexRecord> {
        val file = pathService.syncIndexFile()
        if (!file.isFile) {
            return emptyList()
        }
        return json.decodeFromString(file.readText())
    }

    fun readMappingRecords(
        mappingId: String,
    ): Map<String, KboxSyncIndexRecord> {
        return readAll()
            .filter { record -> record.mappingId == mappingId }
            .associateBy { record -> record.relativePath }
    }

    fun upsert(
        record: KboxSyncIndexRecord,
    ) {
        val updated = readAll()
            .filterNot { existing ->
                existing.mappingId == record.mappingId &&
                    existing.relativePath == record.relativePath
            } + record
        writeAll(updated)
    }

    fun pruneMappings(
        activeMappingIds: Set<String>,
    ) {
        val updated = readAll()
            .filter { record -> activeMappingIds.contains(record.mappingId) }
        writeAll(updated)
    }

    private fun writeAll(
        records: List<KboxSyncIndexRecord>,
    ) {
        val file = pathService.syncIndexFile()
        file.parentFile?.mkdirs()
        file.writeText(
            json.encodeToString(
                records.sortedWith(compareBy(KboxSyncIndexRecord::mappingId, KboxSyncIndexRecord::relativePath)),
            ),
        )
    }
}
