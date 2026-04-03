package site.addzero.kcloud.plugins.mcuconsole.workbench

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.Button as ShadcnButton
import site.addzero.component.ButtonSize as ShadcnButtonSize
import site.addzero.component.ButtonVariant as ShadcnButtonVariant
import site.addzero.kcloud.plugins.mcuconsole.*

/**
 * Cupertino 场景下 LocalIndication 还是旧 Indication 实现，不能再走默认 clickable 重载。
 */
@Composable
internal fun Modifier.mcuClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    return clickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
internal fun rememberMcuWorkbenchState(
    state: McuConsoleWorkbenchState,
): McuConsoleWorkbenchState {
    LaunchedEffect(state) {
        state.refreshAll()
    }

    LaunchedEffect(state) {
        while (currentCoroutineContext().isActive) {
            val isFlashBusy = state.flashStatus.state != McuFlashRunState.IDLE
            if (state.hasActiveSession || isFlashBusy) {
                state.pollEvents()
            }
            delay(
                if (state.hasActiveSession) {
                    320
                } else if (isFlashBusy) {
                    800
                } else {
                    1600
                },
            )
        }
    }

    LaunchedEffect(state) {
        while (currentCoroutineContext().isActive) {
            val isFlashBusy = state.flashStatus.state != McuFlashRunState.IDLE
            if (state.hasActiveSession) {
                state.refreshSession()
                if (state.activeSessionTransportKind == McuTransportKind.SERIAL) {
                    state.refreshScriptStatus()
                    state.refreshRuntimeStatus()
                }
            }
            state.refreshFlashStatus()
            delay(
                if (state.hasActiveSession || isFlashBusy) {
                    1100
                } else {
                    1800
                },
            )
        }
    }

    return state
}

@Composable
internal fun rememberMcuActionRunner(): (suspend () -> Unit) -> Unit {
    val scope = rememberCoroutineScope()
    return remember(scope) {
        { block ->
            if (scope.isActive) {
                runCatching {
                    scope.launch {
                        block()
                    }
                }.onFailure { throwable ->
                    if (throwable is CancellationException) {
                        return@onFailure
                    }
                    if (throwable.message?.contains("left the composition", ignoreCase = true) == true) {
                        return@onFailure
                    }
                    throw throwable
                }
            }
        }
    }
}

@Composable
internal fun McuWorkbenchFrame(
    state: McuConsoleWorkbenchState,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (actions != null) {
            McuToolbar(actions = actions)
        }
        content()
        state.feedbackMessage?.takeIf { it.isNotBlank() }?.let { message ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (state.feedbackIsError) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.feedbackIsError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                )
            }
        }
    }
}

@Composable
internal fun McuPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val darkThemeEnabled = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (darkThemeEnabled) {
                Color.White.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f)
            },
        ),
        color = if (darkThemeEnabled) {
            Color(0xFF141A25).copy(alpha = 0.96f)
        } else {
            Color.White.copy(alpha = 0.98f)
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                content()
            },
        )
    }
}

@Composable
internal fun McuToolbar(
    actions: @Composable RowScope.() -> Unit,
) {
    val darkThemeEnabled = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (darkThemeEnabled) {
                Color.White.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)
            },
        ),
        color = if (darkThemeEnabled) {
            Color(0xFF111722).copy(alpha = 0.94f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

@Composable
internal fun McuPortBrowser(
    state: McuConsoleWorkbenchState,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AddSearchBar(
            keyword = state.portQuery,
            onKeyWordChanged = { state.portQuery = it },
            onSearch = onRefresh,
            placeholder = "串口 / 描述",
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "已发现 ${state.ports.size} 个串口，当前显示 ${state.filteredPorts.size} 个",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .heightIn(min = 190.dp),
        ) {
            if (state.filteredPorts.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "当前筛选条件下没有串口",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = state.filteredPorts,
                        key = { port -> port.portPath },
                    ) { port ->
                        McuPortRow(
                            port = port,
                            selected = state.selectedPortPath == port.portPath,
                            onClick = { state.selectPort(port.portPath) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun McuFlashProfileBrowser(
    profiles: List<McuFlashProfileSummary>,
    selectedProfileId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (profiles.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "暂无烧录能力包",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = profiles,
            key = { profile -> profile.id },
        ) { profile ->
            McuFlashProfileRow(
                profile = profile,
                selected = profile.id == selectedProfileId,
                onClick = { onSelect(profile.id) },
            )
        }
    }
}

@Composable
internal fun McuFlashProbeBrowser(
    probes: List<McuFlashProbeSummary>,
    selectedSerialNumber: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (probes.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "未发现 ST-Link 探针",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = probes,
            key = { probe -> probe.serialNumber ?: "${probe.vendorId}:${probe.productId}" },
        ) { probe ->
            McuFlashProbeRow(
                probe = probe,
                selected = when {
                    selectedSerialNumber != null -> probe.serialNumber == selectedSerialNumber
                    probes.size == 1 -> true
                    else -> false
                },
                onClick = { onSelect(probe.serialNumber) },
            )
        }
    }
}

@Composable
internal fun McuRuntimeBundleBrowser(
    bundles: List<McuRuntimeBundleSummary>,
    selectedBundleId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (bundles.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "暂无运行时包",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = bundles,
            key = { bundle -> bundle.bundleId },
        ) { bundle ->
            McuRuntimeBundleRow(
                bundle = bundle,
                selected = bundle.bundleId == selectedBundleId,
                onClick = { onSelect(bundle.bundleId) },
            )
        }
    }
}

@Composable
private fun McuPortRow(
    port: McuPortSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val darkThemeEnabled = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val background = if (selected) {
        if (darkThemeEnabled) {
            Color.White.copy(alpha = 0.96f)
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    } else {
        if (darkThemeEnabled) {
            Color.White.copy(alpha = 0.04f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        }
    }
    val contentColor = if (selected) {
        if (darkThemeEnabled) {
            Color(0xFF0B0F16)
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier.fillMaxWidth().mcuClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                if (darkThemeEnabled) {
                    Color.White.copy(alpha = 0.92f)
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
                }
            } else if (darkThemeEnabled) {
                Color.White.copy(alpha = 0.06f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f)
            },
        ),
        color = background,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                McuNodeBadge(
                    label = "串口设备",
                    selected = selected,
                )
                Spacer(modifier = Modifier.weight(1f))
                port.kind
                    .takeIf { it.isNotBlank() }
                    ?.let { kind ->
                        Text(
                            text = kind,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.72f),
                        )
                    }
            }
            port.remark
                .takeIf { it.isNotBlank() }
                ?.let { remark ->
                    Text(
                        text = remark,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            Text(
                text = port.portPath,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOf(
                    port.portName,
                    port.descriptiveName,
                    port.description,
                    port.displayRemarkBindingKey(),
                )
                    .filter { it.isNotBlank() }
                    .joinToString(" / ")
                    .ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.76f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun McuPortSummary.displayRemarkBindingKey(): String {
    serialNumber.takeIf { it.isNotBlank() }?.let { serial ->
        return "SN:$serial"
    }
    val vendorIdValue = vendorId
    val productIdValue = productId
    if (vendorIdValue != null && productIdValue != null) {
        return "VID:PID=${vendorIdValue.toString(16)}:${productIdValue.toString(16)}"
    }
    return deviceKey.ifBlank { "未提供稳定键" }
}

@Composable
private fun McuFlashProfileRow(
    profile: McuFlashProfileSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier.fillMaxWidth().mcuClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = background,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = profile.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${profile.runtimeKind.name} / ${profile.mcuFamily} / 0x${profile.defaultStartAddress.toString(16).uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.76f),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (profile.description.isNotBlank()) {
                Text(
                    text = profile.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.68f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun McuFlashProbeRow(
    probe: McuFlashProbeSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier.fillMaxWidth().mcuClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = background,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = probe.productName ?: "ST-Link",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = probe.serialNumber ?: "serial: unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.76f),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append("VID:PID=")
                    append(probe.vendorId.toString(16).uppercase().padStart(4, '0'))
                    append(':')
                    append(probe.productId.toString(16).uppercase().padStart(4, '0'))
                    probe.manufacturerName?.takeIf { it.isNotBlank() }?.let { manufacturer ->
                        append(" / ")
                        append(manufacturer)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.68f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun McuNodeBadge(
    label: String,
    selected: Boolean,
) {
    val darkThemeEnabled = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) {
            if (darkThemeEnabled) {
                Color(0x140F172A)
            } else {
                Color.White.copy(alpha = 0.12f)
            }
        } else if (darkThemeEnabled) {
            Color.White.copy(alpha = 0.06f)
        } else {
            Color(0x0A0F172A)
        },
        contentColor = if (selected) {
            if (darkThemeEnabled) {
                Color(0xFF0B0F16)
            } else {
                Color.White
            }
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun McuRuntimeBundleRow(
    bundle: McuRuntimeBundleSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier.fillMaxWidth().mcuClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = background,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = bundle.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${bundle.runtimeKind.name} / ${bundle.mcuFamily} / ${bundle.defaultFlashProfileId}",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.76f),
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun McuSummaryTable(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.width(78.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value.ifBlank { "-" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = if (value.contains('/') || value.contains('\\') || value.contains('{')) {
                        FontFamily.Monospace
                    } else {
                        FontFamily.Default
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun McuTransportProfileList(
    state: McuConsoleWorkbenchState,
    modifier: Modifier = Modifier,
    onSave: () -> Unit,
    onApply: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShadcnButton(
                onClick = onSave,
                enabled = !state.isSubmitting,
                modifier = Modifier.heightIn(min = 38.dp),
                variant = ShadcnButtonVariant.Default,
                size = ShadcnButtonSize.Default,
                shape = RoundedCornerShape(12.dp),
                content = { Text("保存当前配置") },
            )
            Text(
                text = "已保存 ${state.transportProfiles.size} 个",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.transportProfiles.isEmpty()) {
            McuInfoNotice("当前还没有已保存的连接配置。")
            return
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = state.transportProfiles,
                key = { profile -> profile.profileKey },
            ) { profile ->
                val selected = profile.profileKey == state.transportDraft.profileKey &&
                    profile.profileKey.isNotBlank()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    },
                    contentColor = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = profile.name.ifBlank { profile.transportKind.displayName() },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = buildString {
                                append(profile.transportKind.displayName())
                                profile.deviceKey?.takeIf { it.isNotBlank() }?.let { value ->
                                    append(" / ")
                                    append(value)
                                }
                                profile.portPathHint?.takeIf { it.isNotBlank() }?.let { value ->
                                    append(" / ")
                                    append(value)
                                }
                                profile.host?.takeIf { it.isNotBlank() }?.let { value ->
                                    append(" / ")
                                    append(value)
                                    profile.port?.let { port ->
                                        append(':')
                                        append(port)
                                    }
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ShadcnButton(
                                onClick = { onApply(profile.profileKey) },
                                enabled = !state.isSubmitting,
                                modifier = Modifier.heightIn(min = 38.dp),
                                variant = ShadcnButtonVariant.Default,
                                size = ShadcnButtonSize.Default,
                                shape = RoundedCornerShape(12.dp),
                                content = { Text("载入") },
                            )
                            ShadcnButton(
                                onClick = { onDelete(profile.profileKey) },
                                enabled = !state.isSubmitting,
                                modifier = Modifier.heightIn(min = 38.dp),
                                variant = ShadcnButtonVariant.Outline,
                                size = ShadcnButtonSize.Default,
                                shape = RoundedCornerShape(12.dp),
                                content = { Text("删除") },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun McuEventFeed(
    events: List<McuEventEnvelope>,
    modifier: Modifier = Modifier,
    autoScrollToLatest: Boolean = false,
) {
    if (events.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "暂无事件",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val listState = rememberLazyListState()

    LaunchedEffect(events.size, autoScrollToLatest) {
        if (autoScrollToLatest && events.isNotEmpty()) {
            listState.scrollToItem(events.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = events,
            key = { event -> event.seq },
        ) { event ->
            McuEventCard(event = event)
        }
    }
}

@Composable
internal fun McuTerminalFeed(
    events: List<McuEventEnvelope>,
    modifier: Modifier = Modifier,
    autoScrollToLatest: Boolean = false,
) {
    if (events.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "等待设备输出",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val listState = rememberLazyListState()

    LaunchedEffect(events.size, autoScrollToLatest) {
        if (autoScrollToLatest && events.isNotEmpty()) {
            listState.scrollToItem(events.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        items(
            items = events,
            key = { event -> event.seq },
        ) { event ->
            McuTerminalEventRow(event = event)
        }
    }
}

@Composable
private fun McuTerminalEventRow(
    event: McuEventEnvelope,
) {
    var expanded by rememberSaveable(event.seq) {
        mutableStateOf(event.kind == McuEventKind.ERROR)
    }
    val accent = event.terminalAccentColor()
    val timeText = event.timestamp.substringAfter('T').substringBefore('.')
    val preview = event.raw?.takeIf { it.isNotBlank() } ?: event.message
    val rawText = event.raw?.takeIf { it.isNotBlank() && it != preview }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .background(accent, RoundedCornerShape(999.dp)),
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accent.copy(alpha = 0.14f),
                    contentColor = accent,
                ) {
                    Text(
                        text = event.kind.displayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text(
                    text = preview,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "#${event.seq}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
            }
            if (expanded && rawText != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
                ) {
                    Text(
                        text = rawText,
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun McuEventCard(
    event: McuEventEnvelope,
) {
    var expanded by rememberSaveable(event.seq) {
        mutableStateOf(event.kind == McuEventKind.ERROR)
    }
    val marker = when (event.kind) {
        McuEventKind.ERROR -> MaterialTheme.colorScheme.error
        McuEventKind.TX_TEXT -> MaterialTheme.colorScheme.secondary
        McuEventKind.TX_FRAME -> MaterialTheme.colorScheme.primary
        McuEventKind.RX_FRAME -> MaterialTheme.colorScheme.tertiary
        McuEventKind.FLASH -> MaterialTheme.colorScheme.secondary
        McuEventKind.LOG -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val timeText = event.timestamp.substringAfter('T').substringBefore('.')
    val rawText = event.raw?.takeIf { it.isNotBlank() }
    val messageFont = if (event.kind == McuEventKind.LOG || rawText != null) {
        FontFamily.Monospace
    } else {
        FontFamily.Default
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .animateContentSize()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.width(6.dp).height(22.dp)
                        .background(marker, RoundedCornerShape(99.dp)),
                )
                Text(
                    text = "#${event.seq}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = event.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = marker.copy(alpha = 0.14f),
                    contentColor = marker,
                ) {
                    Text(
                        text = event.kind.displayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = if (expanded) "收起" else "展开",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = event.message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = messageFont,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (expanded && rawText != null && rawText != event.message) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
                ) {
                    Text(
                        text = rawText,
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun McuEventKind.displayName(): String {
    return when (this) {
        McuEventKind.SYSTEM -> "系统"
        McuEventKind.LOG -> "日志"
        McuEventKind.TX_TEXT -> "TX 文本"
        McuEventKind.TX_FRAME -> "TX 协议"
        McuEventKind.RX_FRAME -> "RX 协议"
        McuEventKind.FLASH -> "烧录"
        McuEventKind.ERROR -> "错误"
    }
}

@Composable
private fun McuEventEnvelope.terminalAccentColor(): Color {
    return when (kind) {
        McuEventKind.ERROR -> MaterialTheme.colorScheme.error
        McuEventKind.TX_TEXT -> MaterialTheme.colorScheme.secondary
        McuEventKind.TX_FRAME -> MaterialTheme.colorScheme.primary
        McuEventKind.RX_FRAME -> MaterialTheme.colorScheme.tertiary
        McuEventKind.FLASH -> MaterialTheme.colorScheme.secondary
        McuEventKind.LOG -> MaterialTheme.colorScheme.outline
        McuEventKind.SYSTEM -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
internal fun McuCompactInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        supportingText = supportingText
            ?.takeIf { it.isNotBlank() }
            ?.let { text ->
                {
                    Text(
                        text = text,
                        maxLines = if (singleLine) {
                            1
                        } else {
                            2
                        },
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
        ),
    )
}

@Composable
internal fun <T> McuChoiceChipRow(
    items: List<T>,
    selectedItem: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            val selected = item == selectedItem
            Surface(
                modifier = Modifier.clickable { onSelect(item) },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                },
                contentColor = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ) {
                Text(
                    text = labelOf(item),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = if (labelOf(item).contains("UUID") || labelOf(item).contains("TCP")) {
                        FontFamily.Monospace
                    } else {
                        FontFamily.Default
                    },
                )
            }
        }
    }
}

@Composable
internal fun McuTransportSelector(
    selectedKind: McuTransportKind,
    onSelect: (McuTransportKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    McuChoiceChipRow(
        items = McuTransportKind.entries,
        selectedItem = selectedKind,
        labelOf = { kind -> kind.displayName() },
        onSelect = onSelect,
        modifier = modifier,
    )
}

@Composable
internal fun McuInfoNotice(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun McuScriptPreview(
    script: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = script.ifBlank { "-" },
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
