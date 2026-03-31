package site.addzero.kbox.plugins.tools.storagetool

import site.addzero.kbox.core.KBOX_APP_NAME
import site.addzero.kbox.core.KBOX_DEFAULT_LARGE_FILE_THRESHOLD_BYTES
import site.addzero.kbox.core.model.KboxRemoteOs
import site.addzero.kbox.core.model.KboxRemotePathConfig
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.model.KboxSshAuthMode
import site.addzero.kbox.core.model.KboxSshConfig
import site.addzero.kbox.core.model.KboxSyncMappingConfig
import site.addzero.kbox.core.support.KboxDefaults
import java.util.Locale

data class KboxSyncMappingDraft(
    val mappingId: String = "",
    val displayName: String = "",
    val localRoot: String = "",
    val remoteRoot: String = "",
    val enabled: Boolean = true,
)

data class KboxSettingsDraft(
    val localAppDataOverride: String = "",
    val installerScanRootsText: String = "",
    val largeFileScanRootsText: String = "",
    val largeFileThresholdGbText: String = "1.00",
    val sshEnabled: Boolean = false,
    val sshHost: String = "",
    val sshPortText: String = "22",
    val sshUsername: String = "",
    val sshAuthMode: KboxSshAuthMode = KboxSshAuthMode.PASSWORD,
    val sshPassword: String = "",
    val sshPrivateKeyPath: String = "",
    val sshPrivateKeyPassphrase: String = "",
    val sshStrictHostKeyChecking: Boolean = false,
    val remoteOs: KboxRemoteOs = KboxRemoteOs.MACOS,
    val remoteUserHome: String = "",
    val remoteLocalAppData: String = "",
    val remoteAppData: String = "",
    val remoteXdgDataHome: String = "",
    val remoteAppName: String = KBOX_APP_NAME,
    val syncEnabled: Boolean = false,
    val syncStartOnLaunch: Boolean = true,
    val syncRemotePollSecondsText: String = "30",
    val syncMappings: List<KboxSyncMappingDraft> = emptyList(),
)

fun KboxSettings.toDraft(): KboxSettingsDraft {
    val normalized = KboxDefaults.normalize(this)
    return KboxSettingsDraft(
        localAppDataOverride = normalized.localAppDataOverride,
        installerScanRootsText = normalized.installerScanRoots.joinToString("\n"),
        largeFileScanRootsText = normalized.largeFileScanRoots.joinToString("\n"),
        largeFileThresholdGbText = String.format(
            Locale.US,
            "%.2f",
            normalized.largeFileThresholdBytes.toDouble() / KBOX_DEFAULT_LARGE_FILE_THRESHOLD_BYTES,
        ),
        sshEnabled = normalized.ssh.enabled,
        sshHost = normalized.ssh.host,
        sshPortText = normalized.ssh.port.toString(),
        sshUsername = normalized.ssh.username,
        sshAuthMode = normalized.ssh.authMode,
        sshPassword = normalized.ssh.password,
        sshPrivateKeyPath = normalized.ssh.privateKeyPath,
        sshPrivateKeyPassphrase = normalized.ssh.privateKeyPassphrase,
        sshStrictHostKeyChecking = normalized.ssh.strictHostKeyChecking,
        remoteOs = normalized.ssh.remotePath.os,
        remoteUserHome = normalized.ssh.remotePath.userHome,
        remoteLocalAppData = normalized.ssh.remotePath.localAppData,
        remoteAppData = normalized.ssh.remotePath.appData,
        remoteXdgDataHome = normalized.ssh.remotePath.xdgDataHome,
        remoteAppName = normalized.ssh.remotePath.appName,
        syncEnabled = normalized.syncEnabled,
        syncStartOnLaunch = normalized.syncStartOnLaunch,
        syncRemotePollSecondsText = normalized.syncRemotePollSeconds.toString(),
        syncMappings = normalized.syncMappings.map { mapping ->
            KboxSyncMappingDraft(
                mappingId = mapping.mappingId,
                displayName = mapping.displayName,
                localRoot = mapping.localRoot,
                remoteRoot = mapping.remoteRoot,
                enabled = mapping.enabled,
            )
        },
    )
}

fun KboxSettingsDraft.toSettings(): KboxSettings {
    val defaults = KboxDefaults.defaultSettings()
    return KboxSettings(
        localAppDataOverride = localAppDataOverride.trim(),
        installerScanRoots = installerScanRootsText.toPathLines().ifEmpty { defaults.installerScanRoots },
        largeFileScanRoots = largeFileScanRootsText.toPathLines().ifEmpty { defaults.largeFileScanRoots },
        installerRules = defaults.installerRules,
        largeFileThresholdBytes = parseThresholdBytes(),
        ssh = KboxSshConfig(
            enabled = sshEnabled,
            host = sshHost.trim(),
            port = sshPortText.toIntOrNull() ?: 22,
            username = sshUsername.trim(),
            authMode = sshAuthMode,
            password = sshPassword,
            privateKeyPath = sshPrivateKeyPath.trim(),
            privateKeyPassphrase = sshPrivateKeyPassphrase,
            strictHostKeyChecking = sshStrictHostKeyChecking,
            remotePath = KboxRemotePathConfig(
                os = remoteOs,
                userHome = remoteUserHome.trim(),
                localAppData = remoteLocalAppData.trim(),
                appData = remoteAppData.trim(),
                xdgDataHome = remoteXdgDataHome.trim(),
                appName = remoteAppName.trim().ifBlank { KBOX_APP_NAME },
            ),
        ),
        syncEnabled = syncEnabled,
        syncStartOnLaunch = syncStartOnLaunch,
        syncRemotePollSeconds = parseSyncRemotePollSeconds(),
        syncMappings = syncMappings.map { mapping ->
            KboxSyncMappingConfig(
                mappingId = mapping.mappingId.trim(),
                displayName = mapping.displayName.trim(),
                localRoot = mapping.localRoot.trim(),
                remoteRoot = mapping.remoteRoot.trim(),
                enabled = mapping.enabled,
            )
        },
    )
}

private fun KboxSettingsDraft.parseThresholdBytes(): Long {
    val gb = largeFileThresholdGbText.trim().toDoubleOrNull()
    if (gb == null || gb <= 0) {
        return KboxDefaults.defaultSettings().largeFileThresholdBytes
    }
    return (gb * KBOX_DEFAULT_LARGE_FILE_THRESHOLD_BYTES).toLong()
}

private fun KboxSettingsDraft.parseSyncRemotePollSeconds(): Int {
    return syncRemotePollSecondsText.trim().toIntOrNull()
        ?.takeIf { value -> value > 0 }
        ?: 30
}

private fun String.toPathLines(): List<String> {
    return lineSequence()
        .map { line -> line.trim() }
        .filter { line -> line.isNotEmpty() }
        .toList()
}
