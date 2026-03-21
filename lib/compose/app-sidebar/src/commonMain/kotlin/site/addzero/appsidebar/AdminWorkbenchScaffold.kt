package site.addzero.appsidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AdminWorkbenchScaffold(
    sidebar: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    breadcrumb: List<String> = emptyList(),
    pageTitle: String,
    pageSubtitle: String? = null,
    pageActions: @Composable RowScope.() -> Unit = {},
    detail: (@Composable BoxScope.() -> Unit)? = null,
    defaultSidebarRatio: Float = 0.22f,
    state: WorkbenchScaffoldState = rememberWorkbenchScaffoldState(defaultSidebarRatio),
    minSidebarWidth: androidx.compose.ui.unit.Dp = 248.dp,
    maxSidebarWidth: androidx.compose.ui.unit.Dp = 360.dp,
    detailWidth: androidx.compose.ui.unit.Dp = 320.dp,
    outerPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
    detailPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
    onGlobalSearchClick: (() -> Unit)? = null,
    languageLabel: String? = null,
    onLanguageClick: (() -> Unit)? = null,
    isDarkTheme: Boolean? = null,
    onThemeToggle: (() -> Unit)? = null,
    notificationCount: Int? = null,
    onNotificationsClick: (() -> Unit)? = null,
    userLabel: String? = null,
    onUserClick: (() -> Unit)? = null,
) {
    WorkbenchScaffold(
        sidebar = sidebar,
        content = content,
        modifier = modifier,
        detail = detail,
        defaultSidebarRatio = defaultSidebarRatio,
        state = state,
        minSidebarWidth = minSidebarWidth,
        maxSidebarWidth = maxSidebarWidth,
        detailWidth = detailWidth,
        outerPadding = outerPadding,
        contentPadding = contentPadding,
        detailPadding = detailPadding,
        contentHeaderScrollable = false,
        contentHeader = {
            AdminWorkbenchHeader(
                breadcrumb = breadcrumb,
                pageTitle = pageTitle,
                pageSubtitle = pageSubtitle,
                pageActions = pageActions,
                onGlobalSearchClick = onGlobalSearchClick,
                languageLabel = languageLabel,
                onLanguageClick = onLanguageClick,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                notificationCount = notificationCount,
                onNotificationsClick = onNotificationsClick,
                userLabel = userLabel,
                onUserClick = onUserClick,
            )
        },
    )
}

@Composable
fun WorkbenchSearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "搜索",
) {
    WorkbenchUtilityButton(
        label = label,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
fun WorkbenchLanguageButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WorkbenchUtilityButton(
        label = label,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
fun WorkbenchThemeToggleButton(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkLabel: String = "深色",
    lightLabel: String = "浅色",
) {
    WorkbenchUtilityButton(
        label = if (isDarkTheme) darkLabel else lightLabel,
        modifier = modifier,
        onClick = onClick,
        highlighted = true,
    )
}

@Composable
fun WorkbenchNotificationButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "通知",
) {
    WorkbenchUtilityButton(
        label = label,
        modifier = modifier,
        onClick = onClick,
        badge = count.toNotificationBadge(),
    )
}

@Composable
fun WorkbenchUserButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WorkbenchUtilityButton(
        label = label,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun RowScope.AdminWorkbenchHeader(
    breadcrumb: List<String>,
    pageTitle: String,
    pageSubtitle: String?,
    pageActions: @Composable RowScope.() -> Unit,
    onGlobalSearchClick: (() -> Unit)?,
    languageLabel: String?,
    onLanguageClick: (() -> Unit)?,
    isDarkTheme: Boolean?,
    onThemeToggle: (() -> Unit)?,
    notificationCount: Int?,
    onNotificationsClick: (() -> Unit)?,
    userLabel: String?,
    onUserClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AdminWorkbenchTitleBlock(
            breadcrumb = breadcrumb,
            pageTitle = pageTitle,
            pageSubtitle = pageSubtitle,
            modifier = Modifier.weight(1f),
        )

        Row(
            modifier = Modifier.widthIn(max = 440.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = pageActions,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onGlobalSearchClick != null) {
                WorkbenchSearchButton(
                    onClick = onGlobalSearchClick,
                )
            }
            if (!languageLabel.isNullOrBlank() && onLanguageClick != null) {
                WorkbenchLanguageButton(
                    label = languageLabel,
                    onClick = onLanguageClick,
                )
            }
            if (isDarkTheme != null && onThemeToggle != null) {
                WorkbenchThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onClick = onThemeToggle,
                )
            }
            if (notificationCount != null && onNotificationsClick != null) {
                WorkbenchNotificationButton(
                    count = notificationCount,
                    onClick = onNotificationsClick,
                )
            }
            if (!userLabel.isNullOrBlank() && onUserClick != null) {
                WorkbenchUserButton(
                    label = userLabel,
                    onClick = onUserClick,
                )
            }
        }
    }
}

@Composable
private fun AdminWorkbenchTitleBlock(
    breadcrumb: List<String>,
    pageTitle: String,
    pageSubtitle: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (breadcrumb.isNotEmpty()) {
            Text(
                text = breadcrumb.joinToString(" / "),
                color = AdminWorkbenchTokens.textMuted,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(
            text = pageTitle,
            color = AdminWorkbenchTokens.textPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
        if (pageSubtitle != null) {
            Text(
                text = pageSubtitle,
                color = AdminWorkbenchTokens.textMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun WorkbenchUtilityButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    highlighted: Boolean = false,
) {
    Row(
        modifier = modifier.utilityButtonFrame(
            highlighted = highlighted,
        ).clickable(
            onClick = onClick,
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = AdminWorkbenchTokens.textPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (badge != null) {
            Box(
                modifier = Modifier.utilityBadgeFrame(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge,
                    color = AdminWorkbenchTokens.textPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** 工具按钮底板：默认是后台工具条里的紧凑胶囊按钮，不抢主操作的风头。 */
private fun Modifier.utilityButtonFrame(
    highlighted: Boolean,
): Modifier {
    val background = if (highlighted) {
        AdminWorkbenchTokens.highlightedBackground
    } else {
        AdminWorkbenchTokens.buttonBackground
    }
    val border = if (highlighted) {
        AdminWorkbenchTokens.highlightedBorder
    } else {
        AdminWorkbenchTokens.buttonBorder
    }
    return background(
        color = background,
        shape = RoundedCornerShape(999.dp),
    ).border(
        width = 1.dp,
        color = border,
        shape = RoundedCornerShape(999.dp),
    ).padding(horizontal = 12.dp, vertical = 8.dp)
}

/** 角标胶囊：用于通知数这类轻量全局状态，不把按钮撑成大块。 */
private fun Modifier.utilityBadgeFrame(): Modifier {
    return size(width = 24.dp, height = 18.dp)
        .background(
            color = AdminWorkbenchTokens.badgeBackground,
            shape = CircleShape,
        ).padding(horizontal = 4.dp, vertical = 2.dp)
}

private fun Int.toNotificationBadge(): String? {
    return when {
        this <= 0 -> null
        this > 99 -> "99+"
        else -> toString()
    }
}

private object AdminWorkbenchTokens {
    val buttonBackground = Color.White.copy(alpha = 0.05f)
    val buttonBorder = Color.White.copy(alpha = 0.07f)
    val highlightedBackground = Color(0xFF2D64E3).copy(alpha = 0.22f)
    val highlightedBorder = Color(0xFF9ABEFF).copy(alpha = 0.22f)
    val badgeBackground = Color.White.copy(alpha = 0.10f)
    val textPrimary = Color.White.copy(alpha = 0.96f)
    val textMuted = Color.White.copy(alpha = 0.62f)
}
