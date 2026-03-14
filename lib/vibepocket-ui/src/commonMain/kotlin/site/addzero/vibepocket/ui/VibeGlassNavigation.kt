package site.addzero.vibepocket.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Immutable
data class VibeGlassNavItem(
    val key: String,
    val title: String,
    val icon: String? = null,
)

@Composable
fun VibeGlassWorkspace(
    brand: String,
    strapline: String,
    navigationItems: List<VibeGlassNavItem>,
    selectedKey: String,
    onSelect: (VibeGlassNavItem) -> Unit,
    modifier: Modifier = Modifier,
    sidebarFooter: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = VibeGlassTheme.palette
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxWidth < 920.dp
        VibeGlassBackdrop {
            if (compact) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    VibeGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                    ) {
                        Text(
                            text = brand,
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.ink,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strapline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.inkSoft,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            navigationItems.forEach { item ->
                                VibeGlassChoiceChip(
                                    label = item.title,
                                    selected = item.key == selectedKey,
                                    onClick = { onSelect(item) },
                                    leading = item.icon,
                                )
                            }
                        }
                    }
                    VibeGlassPanel(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
                        content = content,
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    VibeGlassPanel(
                        modifier = Modifier
                            .width(282.dp)
                            .fillMaxHeight(),
                    ) {
                        Text(
                            text = brand,
                            style = MaterialTheme.typography.headlineLarge,
                            color = palette.ink,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = strapline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.inkSoft,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            navigationItems.forEach { item ->
                                VibeGlassChoiceChip(
                                    label = item.title,
                                    selected = item.key == selectedKey,
                                    onClick = { onSelect(item) },
                                    leading = item.icon,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(22.dp))
                        sidebarFooter()
                    }
                    VibeGlassPanel(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                        content = content,
                    )
                }
            }
        }
    }
}
