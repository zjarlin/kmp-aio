package site.addzero.kcloud.plugins.system.configcenter.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import site.addzero.component.button.AddButton
import site.addzero.component.button.AddIconButton
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.kcloud.plugins.system.configcenter.ConfigCenterWorkbenchState

@Composable
internal fun rememberConfigCenterWorkbenchState(): ConfigCenterWorkbenchState {
    return remember {
        KoinPlatform.getKoin().get<ConfigCenterWorkbenchState>()
    }
}

@Composable
internal fun ConfigCenterPageFrame(
    title: String,
    state: ConfigCenterWorkbenchState,
    onRefresh: suspend () -> Unit,
    onCreate: () -> Unit,
    onSave: suspend () -> Unit,
    onDelete: (suspend () -> Unit)? = null,
    searchBar: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        state.ensureLoaded()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AddIconButton(
                            text = "刷新",
                            imageVector = Icons.Default.Refresh,
                        ) {
                            scope.launch { onRefresh() }
                        }
                        AddIconButton(
                            text = "新建",
                            imageVector = Icons.Default.Add,
                        ) {
                            onCreate()
                        }
                        AddIconButton(
                            text = "保存",
                            imageVector = Icons.Default.Save,
                        ) {
                            scope.launch { onSave() }
                        }
                        if (onDelete != null) {
                            AddIconButton(
                                text = "删除",
                                imageVector = Icons.Default.Delete,
                            ) {
                                scope.launch { onDelete() }
                            }
                        }
                    }
                }
                if (searchBar != null) {
                    searchBar()
                }
                if (state.message.isNotBlank()) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        content()
    }
}

@Composable
internal fun ConfigCenterPanel(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            content()
        }
    }
}

@Composable
internal fun ConfigCenterListItem(
    selected: Boolean,
    title: String,
    subtitle: String,
    trailing: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                },
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun EnumSelectRow(
    label: String,
    values: List<Enum<*>>,
    currentValue: Enum<*>,
    onValueChange: (Enum<*>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = currentValue.name,
                onValueChange = {},
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                label = { Text(label) },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                values.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun PreviewTextArea(
    value: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
            .padding(12.dp),
    ) {
        Text(
            text = value.ifBlank { "暂无预览" },
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun ConfigCenterEntryFilters(
    state: ConfigCenterWorkbenchState,
    onRefresh: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AddSearchBar(
            keyword = state.entryKeyword,
            onKeyWordChanged = { state.entryKeyword = it },
            onSearch = {
                scope.launch { onRefresh() }
            },
            placeholder = "搜索 key 或描述",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = state.namespaceFilter,
                onValueChange = { state.namespaceFilter = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("命名空间") },
            )
            OutlinedTextField(
                value = state.profileFilter,
                onValueChange = { state.profileFilter = it },
                modifier = Modifier.width(180.dp),
                singleLine = true,
                label = { Text("Profile") },
            )
        }
    }
}
