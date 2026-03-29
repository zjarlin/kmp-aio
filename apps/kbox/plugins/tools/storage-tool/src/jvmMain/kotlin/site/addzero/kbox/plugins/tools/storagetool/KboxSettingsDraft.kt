package site.addzero.kbox.plugins.tools.storagetool

import site.addzero.kbox.core.KBOX_APP_NAME
import site.addzero.kbox.core.KBOX_DEFAULT_LARGE_FILE_THRESHOLD_BYTES
import site.addzero.kbox.core.model.KboxRemoteOs
import site.addzero.kbox.core.model.KboxRemotePathConfig
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.model.KboxSshAuthMode
import site.addzero.kbox.core.model.KboxSshConfig
import site.addzero.kbox.core.support.KboxDefaults
import java.util.Locale

data class KboxSettingsDraft(
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
)

fun KboxSettings.toDraft(): KboxSettingsDraft {
    val normalized = KboxDefaults.normalize(this)
    return KboxSettingsDraft(
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
    )
}

fun KboxSettingsDraft.toSettings(): KboxSettings {
    val defaults = KboxDefaults.defaultSettings()
    return KboxSettings(
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
    )
}

private fun KboxSettingsDraft.parseThresholdBytes(): Long {
    val gb = largeFileThresholdGbText.trim().toDoubleOrNull()
    if (gb == null || gb <= 0) {
        return KboxDefaults.defaultSettings().largeFileThresholdBytes
    }
    return (gb * KBOX_DEFAULT_LARGE_FILE_THRESHOLD_BYTES).toLong()
}

private fun String.toPathLines(): List<String> {
    return lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toList()
}
