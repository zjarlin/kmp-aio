package site.addzero.kbox.plugins.tools.storagetool.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.kbox.core.model.KboxRemoteOs
import site.addzero.kbox.core.model.KboxSshAuthMode
import site.addzero.kbox.plugins.tools.storagetool.KboxStorageToolState

@Composable
fun StorageSettingsTab(
    state: KboxStorageToolState,
    modifier: Modifier,
    onSave: () -> Unit,
    onTestSsh: () -> Unit,
) {
    val draft = state.draft
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTestSsh, enabled = !state.isBusy) {
                        Text("Test SSH")
                    }
                    Button(onClick = onSave, enabled = !state.isBusy) {
                        Text("Save")
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = draft.localAppDataOverride,
                    onValueChange = { value -> state.updateDraft { it.copy(localAppDataOverride = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Local app data override") },
                )
                Text(
                    text = state.migrationPreviewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                OutlinedTextField(
                    value = draft.installerScanRootsText,
                    onValueChange = { value -> state.updateDraft { it.copy(installerScanRootsText = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Installer scan roots") },
                    minLines = 3,
                )
                OutlinedTextField(
                    value = draft.largeFileScanRootsText,
                    onValueChange = { value -> state.updateDraft { it.copy(largeFileScanRootsText = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Large file scan roots") },
                    minLines = 3,
                )
                OutlinedTextField(
                    value = draft.largeFileThresholdGbText,
                    onValueChange = { value -> state.updateDraft { it.copy(largeFileThresholdGbText = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Large file threshold (GB)") },
                )

                ToggleRow(
                    label = "Enable SSH",
                    checked = draft.sshEnabled,
                    onCheckedChange = { checked -> state.updateDraft { it.copy(sshEnabled = checked) } },
                )
                OutlinedTextField(
                    value = draft.sshHost,
                    onValueChange = { value -> state.updateDraft { it.copy(sshHost = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("SSH host") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draft.sshPortText,
                        onValueChange = { value -> state.updateDraft { it.copy(sshPortText = value) } },
                        modifier = Modifier.weight(0.35f),
                        label = { Text("Port") },
                    )
                    OutlinedTextField(
                        value = draft.sshUsername,
                        onValueChange = { value -> state.updateDraft { it.copy(sshUsername = value) } },
                        modifier = Modifier.weight(0.65f),
                        label = { Text("Username") },
                    )
                }
                ChipChoices(
                    title = "Auth mode",
                    options = listOf(
                        "Password" to (draft.sshAuthMode == KboxSshAuthMode.PASSWORD),
                        "Private key" to (draft.sshAuthMode == KboxSshAuthMode.PRIVATE_KEY),
                    ),
                    onSelect = { selected ->
                        state.updateDraft {
                            it.copy(
                                sshAuthMode = if (selected == "Password") {
                                    KboxSshAuthMode.PASSWORD
                                } else {
                                    KboxSshAuthMode.PRIVATE_KEY
                                },
                            )
                        }
                    },
                )
                if (draft.sshAuthMode == KboxSshAuthMode.PASSWORD) {
                    OutlinedTextField(
                        value = draft.sshPassword,
                        onValueChange = { value -> state.updateDraft { it.copy(sshPassword = value) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                    )
                } else {
                    OutlinedTextField(
                        value = draft.sshPrivateKeyPath,
                        onValueChange = { value -> state.updateDraft { it.copy(sshPrivateKeyPath = value) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Private key path") },
                    )
                    OutlinedTextField(
                        value = draft.sshPrivateKeyPassphrase,
                        onValueChange = { value -> state.updateDraft { it.copy(sshPrivateKeyPassphrase = value) } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Private key passphrase") },
                    )
                }
                ToggleRow(
                    label = "Strict host key checking",
                    checked = draft.sshStrictHostKeyChecking,
                    onCheckedChange = { checked -> state.updateDraft { it.copy(sshStrictHostKeyChecking = checked) } },
                )
                ChipChoices(
                    title = "Remote OS",
                    options = listOf(
                        "macOS" to (draft.remoteOs == KboxRemoteOs.MACOS),
                        "Windows" to (draft.remoteOs == KboxRemoteOs.WINDOWS),
                        "Linux" to (draft.remoteOs == KboxRemoteOs.LINUX),
                    ),
                    onSelect = { selected ->
                        val remoteOs = when (selected) {
                            "Windows" -> KboxRemoteOs.WINDOWS
                            "Linux" -> KboxRemoteOs.LINUX
                            else -> KboxRemoteOs.MACOS
                        }
                        state.updateDraft { it.copy(remoteOs = remoteOs) }
                    },
                )
                OutlinedTextField(
                    value = draft.remoteUserHome,
                    onValueChange = { value -> state.updateDraft { it.copy(remoteUserHome = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Remote user home") },
                )
                OutlinedTextField(
                    value = draft.remoteLocalAppData,
                    onValueChange = { value -> state.updateDraft { it.copy(remoteLocalAppData = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Remote LOCALAPPDATA") },
                )
                OutlinedTextField(
                    value = draft.remoteAppData,
                    onValueChange = { value -> state.updateDraft { it.copy(remoteAppData = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Remote APPDATA") },
                )
                OutlinedTextField(
                    value = draft.remoteXdgDataHome,
                    onValueChange = { value -> state.updateDraft { it.copy(remoteXdgDataHome = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Remote XDG_DATA_HOME") },
                )
                OutlinedTextField(
                    value = draft.remoteAppName,
                    onValueChange = { value -> state.updateDraft { it.copy(remoteAppName = value) } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Remote appData name") },
                )
                SyncSettingsSection(state = state)
            }
        }
    }
}

@Composable
private fun SyncSettingsSection(
    state: KboxStorageToolState,
) {
    val draft = state.draft
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Sync",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        ToggleRow(
            label = "Enable SSH sync",
            checked = draft.syncEnabled,
            onCheckedChange = { checked -> state.updateDraft { it.copy(syncEnabled = checked) } },
        )
        ToggleRow(
            label = "Start on app launch",
            checked = draft.syncStartOnLaunch,
            onCheckedChange = { checked -> state.updateDraft { it.copy(syncStartOnLaunch = checked) } },
        )
        OutlinedTextField(
            value = draft.syncRemotePollSecondsText,
            onValueChange = { value -> state.updateDraft { it.copy(syncRemotePollSecondsText = value) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Remote poll seconds") },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Mappings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Button(onClick = { state.addSyncMappingDraft() }, enabled = !state.isBusy) {
                Text("Add mapping")
            }
        }
        if (draft.syncMappings.isEmpty()) {
            Text(
                text = "No sync mapping configured yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
        draft.syncMappings.forEachIndexed { index, mapping ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = mapping.enabled,
                                onCheckedChange = { checked ->
                                    state.updateSyncMappingDraft(index) { current ->
                                        current.copy(enabled = checked)
                                    }
                                },
                            )
                            Text("Enabled")
                        }
                        Button(
                            onClick = { state.removeSyncMappingDraft(index) },
                            enabled = !state.isBusy,
                        ) {
                            Text("Remove")
                        }
                    }
                    OutlinedTextField(
                        value = mapping.displayName,
                        onValueChange = { value ->
                            state.updateSyncMappingDraft(index) { current ->
                                current.copy(displayName = value)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Display name") },
                    )
                    OutlinedTextField(
                        value = mapping.localRoot,
                        onValueChange = { value ->
                            state.updateSyncMappingDraft(index) { current ->
                                current.copy(localRoot = value)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Local root") },
                    )
                    OutlinedTextField(
                        value = mapping.remoteRoot,
                        onValueChange = { value ->
                            state.updateSyncMappingDraft(index) { current ->
                                current.copy(remoteRoot = value)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Remote root") },
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ChipChoices(
    title: String,
    options: List<Pair<String, Boolean>>,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (label, selected) ->
                FilterChip(
                    selected = selected,
                    onClick = { onSelect(label) },
                    label = { Text(label) },
                )
            }
        }
    }
}
