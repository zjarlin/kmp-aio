package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.koin.compose.koinInject
import site.addzero.component.button.AddIconButton
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.kcloud.plugins.mcuconsole.*
import site.addzero.kcloud.plugins.mcuconsole.client.McuConsoleWorkbenchState
import site.addzero.kcloud.plugins.mcuconsole.client.displayName

@Composable
internal fun rememberMcuWorkbenchState(): McuConsoleWorkbenchState {
    val state: McuConsoleWorkbenchState = koinInject()

    LaunchedEffect(state) {
        state.refreshAll()
    }

    LaunchedEffect(state) {
        while (currentCoroutineContext().isActive) {
            if (state.hasActiveSession) {
                state.pollEvents()
                state.refreshSession()
                if (state.activeSessionTransportKind == McuTransportKind.SERIAL) {
                    state.refreshScriptStatus()
                    state.refreshRuntimeStatus()
                }
            }
            state.refreshFlashStatus()
            delay(
                if (state.hasActiveSession || state.flashStatus.totalBytes > 0) {
                    900
                } else {
                    1600
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
    actions: List<McuToolbarAction>,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (actions.isNotEmpty()) {
            McuToolbar(actions)
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
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
    actions: List<McuToolbarAction>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEach { action ->
                val tint = if (action.enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
                }
                AddIconButton(
                    text = action.label,
                    imageVector = action.icon,
                    tint = tint,
                    modifier = Modifier.width(18.dp).height(18.dp),
                    onClick = {
                        if (action.enabled) {
                            action.onClick()
                        }
                    },
                )
            }
        }
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = background,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = port.portPath,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOf(port.portName, port.descriptiveName, port.description)
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
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
                text = "${profile.runtimeKind.name} / ${profile.strategyKind.name} / ${profile.mcuFamily}",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.76f),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (profile.description.isNotBlank()) {
                Text(
                    text = buildString {
                        append(profile.description)
                        append(" / ")
                        append(if (profile.requiresPort) "串口必选" else "串口可选")
                    },
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
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
internal fun McuEventFeed(
    events: List<McuEventEnvelope>,
    modifier: Modifier = Modifier,
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = events,
            key = { event -> event.seq },
        ) { event ->
            val marker = when (event.kind) {
                McuEventKind.ERROR -> MaterialTheme.colorScheme.error
                McuEventKind.TX_FRAME -> MaterialTheme.colorScheme.primary
                McuEventKind.RX_FRAME -> MaterialTheme.colorScheme.tertiary
                McuEventKind.FLASH -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.outline
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.width(6.dp).height(24.dp)
                                .background(marker, RoundedCornerShape(99.dp)),
                        )
                        Text(
                            text = "#${event.seq} ${event.title}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = event.timestamp.substringAfter('T').substringBefore('.'),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = event.message,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    event.raw?.takeIf { it.isNotBlank() }?.let { raw ->
                        Text(
                            text = raw,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
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
        modifier = modifier.fillMaxWidth(),
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

internal data class McuToolbarAction(
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

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
