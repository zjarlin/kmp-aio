package site.addzero.kbox.core.service

import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxRemoteFileInfo
import site.addzero.kbox.core.model.KboxSyncAction
import site.addzero.kbox.core.model.KboxSyncDecision
import site.addzero.kbox.core.model.KboxSyncFileSnapshot
import site.addzero.kbox.core.model.KboxSyncIndexRecord

data class KboxSyncDecisionOutcome(
    val decision: KboxSyncDecision,
    val recommendedAction: KboxSyncAction?,
    val reason: String,
    val autoExecutable: Boolean,
)

@Single
class KboxSyncDecisionEngine {
    fun decide(
        local: KboxSyncFileSnapshot?,
        remote: KboxRemoteFileInfo?,
        previous: KboxSyncIndexRecord?,
    ): KboxSyncDecisionOutcome? {
        if (local == null && remote == null) {
            return null
        }
        if (
            local == null &&
            remote != null &&
            previous?.localReleased == true &&
            previous.remoteMd5.isNotBlank() &&
            previous.remoteMd5 == remote.md5
        ) {
            return null
        }
        if (local != null && remote != null && local.md5 == remote.md5) {
            return outcome(
                decision = KboxSyncDecision.RELEASE_LOCAL,
                action = KboxSyncAction.RELEASE_LOCAL,
                reason = "Local and remote content are identical",
                autoExecutable = false,
            )
        }

        return when {
            local != null && remote == null -> {
                if (previous == null) {
                    outcome(
                        decision = KboxSyncDecision.UPLOAD_TO_REMOTE,
                        action = KboxSyncAction.UPLOAD,
                        reason = "Local file exists only on this device",
                        autoExecutable = true,
                    )
                } else {
                    outcome(
                        decision = KboxSyncDecision.KEEP_LOCAL,
                        action = KboxSyncAction.KEEP_LOCAL,
                        reason = "Remote file is missing after a previous sync",
                        autoExecutable = false,
                    )
                }
            }

            local == null && remote != null -> {
                if (previous == null) {
                    outcome(
                        decision = KboxSyncDecision.DOWNLOAD_TO_LOCAL,
                        action = KboxSyncAction.DOWNLOAD,
                        reason = "Remote file exists only on the server",
                        autoExecutable = true,
                    )
                } else {
                    outcome(
                        decision = KboxSyncDecision.KEEP_REMOTE,
                        action = KboxSyncAction.KEEP_REMOTE,
                        reason = "Local file is missing after a previous sync",
                        autoExecutable = false,
                    )
                }
            }

            local != null && remote != null && previous != null -> {
                val localChanged = hasLocalChanged(local, previous)
                val remoteChanged = hasRemoteChanged(remote, previous)
                when {
                    localChanged && !remoteChanged -> outcome(
                        decision = KboxSyncDecision.UPLOAD_TO_REMOTE,
                        action = KboxSyncAction.UPLOAD,
                        reason = "Local file changed since the last sync",
                        autoExecutable = true,
                    )

                    !localChanged && remoteChanged -> outcome(
                        decision = KboxSyncDecision.DOWNLOAD_TO_LOCAL,
                        action = KboxSyncAction.DOWNLOAD,
                        reason = "Remote file changed since the last sync",
                        autoExecutable = true,
                    )

                    else -> sizeBasedConflict(local.sizeBytes, remote.sizeBytes)
                }
            }

            local != null && remote != null -> sizeBasedConflict(local.sizeBytes, remote.sizeBytes)
            else -> null
        }
    }

    private fun hasLocalChanged(
        local: KboxSyncFileSnapshot,
        previous: KboxSyncIndexRecord,
    ): Boolean {
        val baseline = previous.localMd5.ifBlank { previous.remoteMd5 }
        return baseline.isBlank() || local.md5 != baseline
    }

    private fun hasRemoteChanged(
        remote: KboxRemoteFileInfo,
        previous: KboxSyncIndexRecord,
    ): Boolean {
        val baseline = previous.remoteMd5.ifBlank { previous.localMd5 }
        return baseline.isBlank() || remote.md5 != baseline
    }

    private fun sizeBasedConflict(
        localSizeBytes: Long,
        remoteSizeBytes: Long,
    ): KboxSyncDecisionOutcome {
        return when {
            remoteSizeBytes > localSizeBytes -> outcome(
                decision = KboxSyncDecision.KEEP_REMOTE,
                action = KboxSyncAction.KEEP_REMOTE,
                reason = "Remote file is larger than the local file",
                autoExecutable = false,
            )

            remoteSizeBytes < localSizeBytes -> outcome(
                decision = KboxSyncDecision.KEEP_LOCAL,
                action = KboxSyncAction.KEEP_LOCAL,
                reason = "Local file is larger than the remote file",
                autoExecutable = false,
            )

            else -> outcome(
                decision = KboxSyncDecision.COMPARE_CONTENT,
                action = KboxSyncAction.COMPARE_CONTENT,
                reason = "Sizes match but checksums are different",
                autoExecutable = false,
            )
        }
    }

    private fun outcome(
        decision: KboxSyncDecision,
        action: KboxSyncAction?,
        reason: String,
        autoExecutable: Boolean,
    ): KboxSyncDecisionOutcome {
        return KboxSyncDecisionOutcome(
            decision = decision,
            recommendedAction = action,
            reason = reason,
            autoExecutable = autoExecutable,
        )
    }
}
