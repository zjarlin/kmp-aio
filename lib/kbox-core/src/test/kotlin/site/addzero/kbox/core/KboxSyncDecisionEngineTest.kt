package site.addzero.kbox.core

import site.addzero.kbox.core.model.KboxRemoteFileInfo
import site.addzero.kbox.core.model.KboxSyncAction
import site.addzero.kbox.core.model.KboxSyncDecision
import site.addzero.kbox.core.model.KboxSyncFileSnapshot
import site.addzero.kbox.core.model.KboxSyncIndexRecord
import site.addzero.kbox.core.service.KboxSyncDecisionEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KboxSyncDecisionEngineTest {
    private val engine = KboxSyncDecisionEngine()

    @Test
    fun `same md5 should suggest releasing local space`() {
        val outcome = engine.decide(
            local = localFile(md5 = "same"),
            remote = remoteFile(md5 = "same"),
            previous = null,
        )

        assertEquals(KboxSyncDecision.RELEASE_LOCAL, outcome?.decision)
        assertEquals(KboxSyncAction.RELEASE_LOCAL, outcome?.recommendedAction)
    }

    @Test
    fun `local only file should auto upload on first sync`() {
        val outcome = engine.decide(
            local = localFile(md5 = "local-only"),
            remote = null,
            previous = null,
        )

        assertEquals(KboxSyncDecision.UPLOAD_TO_REMOTE, outcome?.decision)
        assertTrue(outcome?.autoExecutable == true)
    }

    @Test
    fun `remote only file should auto download on first sync`() {
        val outcome = engine.decide(
            local = null,
            remote = remoteFile(md5 = "remote-only"),
            previous = null,
        )

        assertEquals(KboxSyncDecision.DOWNLOAD_TO_LOCAL, outcome?.decision)
        assertTrue(outcome?.autoExecutable == true)
    }

    @Test
    fun `larger remote file should recommend keeping remote when both changed`() {
        val outcome = engine.decide(
            local = localFile(md5 = "local-new", sizeBytes = 128),
            remote = remoteFile(md5 = "remote-new", sizeBytes = 256),
            previous = KboxSyncIndexRecord(
                mappingId = "mapping",
                relativePath = "demo.txt",
                localMd5 = "baseline",
                remoteMd5 = "baseline",
                localSizeBytes = 64,
                remoteSizeBytes = 64,
                syncedAtMillis = 1,
            ),
        )

        assertEquals(KboxSyncDecision.KEEP_REMOTE, outcome?.decision)
        assertEquals(KboxSyncAction.KEEP_REMOTE, outcome?.recommendedAction)
    }

    @Test
    fun `same size but different md5 should require content compare`() {
        val outcome = engine.decide(
            local = localFile(md5 = "left", sizeBytes = 128),
            remote = remoteFile(md5 = "right", sizeBytes = 128),
            previous = null,
        )

        assertEquals(KboxSyncDecision.COMPARE_CONTENT, outcome?.decision)
    }

    @Test
    fun `intentionally released local file should stay quiet when remote is unchanged`() {
        val outcome = engine.decide(
            local = null,
            remote = remoteFile(md5 = "released"),
            previous = KboxSyncIndexRecord(
                mappingId = "mapping",
                relativePath = "demo.txt",
                localMd5 = "released",
                remoteMd5 = "released",
                localSizeBytes = 64,
                remoteSizeBytes = 64,
                syncedAtMillis = 1,
                localReleased = true,
            ),
        )

        assertNull(outcome)
    }

    private fun localFile(
        md5: String,
        sizeBytes: Long = 64,
    ) = KboxSyncFileSnapshot(
        absolutePath = "C:/local/demo.txt",
        relativePath = "demo.txt",
        sizeBytes = sizeBytes,
        lastModifiedMillis = 1,
        md5 = md5,
    )

    private fun remoteFile(
        md5: String,
        sizeBytes: Long = 64,
    ) = KboxRemoteFileInfo(
        absolutePath = "/remote/demo.txt",
        relativePath = "demo.txt",
        sizeBytes = sizeBytes,
        lastModifiedMillis = 1,
        md5 = md5,
    )
}
