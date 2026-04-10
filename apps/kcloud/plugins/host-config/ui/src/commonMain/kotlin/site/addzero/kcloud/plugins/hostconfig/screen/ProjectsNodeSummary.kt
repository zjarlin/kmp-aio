package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip
import site.addzero.kcloud.plugins.hostconfig.common.HostConfigNodeKind

/**
 * 表示nodemetricitem。
 *
 * @property label label。
 * @property value 值。
 */
internal data class NodeMetricItem(
    val label: String,
    val value: String,
)

/**
 * 处理主机配置node摘要。
 *
 * @param title title。
 * @param subtitle subtitle。
 * @param kind 类型。
 * @param badges badges。
 * @param metrics metrics。
 */
@Composable
internal fun HostConfigNodeSummary(
    title: String,
    subtitle: String,
    kind: HostConfigNodeKind,
    badges: List<String>,
    metrics: List<NodeMetricItem>,
) {
    val accent = kind.summaryAccentColor()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.18f),
                        CupertinoTheme.colorScheme.tertiarySystemGroupedBackground,
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.26f),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CupertinoText(text = title, style = CupertinoTheme.typography.title3)
                CupertinoText(
                    text = subtitle,
                    style = CupertinoTheme.typography.footnote,
                    color = CupertinoTheme.colorScheme.secondaryLabel,
                )
            }
            HostConfigSummaryBadge(text = kind.label(), accent = accent)
        }
        if (badges.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                badges.forEach { badge ->
                    HostConfigSummaryBadge(
                        text = badge,
                        accent = accent,
                        emphasized = false,
                    )
                }
            }
        }
        if (metrics.isNotEmpty()) {
            HostConfigMetricRow(metrics = metrics, accent = accent)
        }
    }
}

/**
 * 处理主机配置metricrow。
 *
 * @param metrics metrics。
 * @param accent accent。
 */
@Composable
private fun HostConfigMetricRow(
    metrics: List<NodeMetricItem>,
    accent: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        metrics.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(4) { index ->
                    val item = row.getOrNull(index)
                    if (item == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        HostConfigMetricCard(
                            item = item,
                            accent = accent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 处理主机配置metriccard。
 *
 * @param item 条目。
 * @param accent accent。
 * @param modifier modifier。
 */
@Composable
private fun HostConfigMetricCard(
    item: NodeMetricItem,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CupertinoTheme.colorScheme.secondarySystemGroupedBackground)
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.16f),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        CupertinoText(
            text = item.label,
            style = CupertinoTheme.typography.caption2,
            color = CupertinoTheme.colorScheme.secondaryLabel,
        )
        CupertinoText(
            text = item.value,
            style = CupertinoTheme.typography.footnote,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 处理主机配置摘要badge。
 *
 * @param text 文本。
 * @param accent accent。
 * @param emphasized emphasized。
 */
@Composable
private fun HostConfigSummaryBadge(
    text: String,
    accent: Color,
    emphasized: Boolean = true,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (emphasized) accent.copy(alpha = 0.16f)
                else CupertinoTheme.colorScheme.secondarySystemGroupedBackground,
            )
            .border(
                width = 1.dp,
                color = accent.copy(alpha = if (emphasized) 0.26f else 0.14f),
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        CupertinoText(
            text = text,
            style = CupertinoTheme.typography.caption2,
            color = if (emphasized) accent else CupertinoTheme.colorScheme.label,
        )
    }
}

/**
 * 处理主机配置densesection。
 *
 * @param title title。
 * @param subtitle subtitle。
 * @param content content。
 */
@Composable
internal fun HostConfigDenseSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CupertinoText(text = title, style = CupertinoTheme.typography.headline)
            subtitle?.takeIf { it.isNotBlank() }?.let { text ->
                CupertinoText(
                    text = text,
                    style = CupertinoTheme.typography.footnote,
                    color = CupertinoTheme.colorScheme.secondaryLabel,
                )
            }
        }
        content()
    }
}

/**
 * 处理主机配置denseinfosection。
 *
 * @param title title。
 * @param entries entries。
 * @param subtitle subtitle。
 * @param columns columns。
 * @param emptyText empty文本。
 */
@Composable
internal fun HostConfigDenseInfoSection(
    title: String,
    entries: List<Pair<String, String>>,
    subtitle: String? = null,
    columns: Int = 2,
    emptyText: String? = null,
) {
    HostConfigDenseSection(title = title, subtitle = subtitle) {
        if (entries.isEmpty()) {
            emptyText?.let { text -> CupertinoStatusStrip(text) }
            return@HostConfigDenseSection
        }
        HostConfigDenseInfoGrid(entries = entries, columns = columns)
    }
}

/**
 * 处理主机配置denseinfogrid。
 *
 * @param entries entries。
 * @param columns columns。
 */
@Composable
private fun HostConfigDenseInfoGrid(
    entries: List<Pair<String, String>>,
    columns: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entries.chunked(columns.coerceAtLeast(1)).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(columns.coerceAtLeast(1)) { index ->
                    val entry = row.getOrNull(index)
                    if (entry == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        HostConfigDenseInfoCell(
                            label = entry.first,
                            value = entry.second,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 处理主机配置denseinfocell。
 *
 * @param label label。
 * @param value 待解析的值。
 * @param modifier modifier。
 */
@Composable
private fun HostConfigDenseInfoCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CupertinoTheme.colorScheme.tertiarySystemGroupedBackground)
            .border(
                width = 1.dp,
                color = CupertinoTheme.colorScheme.separator.copy(alpha = 0.24f),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        CupertinoText(
            text = label,
            style = CupertinoTheme.typography.caption2,
            color = CupertinoTheme.colorScheme.secondaryLabel,
        )
        CupertinoText(
            text = value,
            style = CupertinoTheme.typography.footnote,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
