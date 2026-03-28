package site.addzero.appsidebar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
    titleContent: (@Composable ColumnScope.() -> Unit)? = null,
    detail: (@Composable BoxScope.() -> Unit)? = null,
    brandLabel: String = "Addzero Admin",
    welcomeLabel: String = "欢迎进入后台工作台",
    defaultSidebarRatio: Float = 0.22f,
    state: WorkbenchScaffoldState = rememberWorkbenchScaffoldState(defaultSidebarRatio),
    minSidebarWidth: androidx.compose.ui.unit.Dp = 248.dp,
    maxSidebarWidth: androidx.compose.ui.unit.Dp = 360.dp,
    detailWidth: androidx.compose.ui.unit.Dp = 320.dp,
    outerPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
    detailPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
    onGlobalSearchClick: (() -> Unit)? = null,
    githubLabel: String? = null,
    onGithubClick: (() -> Unit)? = null,
    languageLabel: String? = null,
    onLanguageClick: (() -> Unit)? = null,
    isDarkTheme: Boolean? = null,
    onThemeToggle: (() -> Unit)? = null,
    notificationCount: Int? = null,
    onNotificationsClick: (() -> Unit)? = null,
    userLabel: String? = null,
    onUserClick: (() -> Unit)? = null,
) {
    val windowFrame = LocalWorkbenchWindowFrame.current
    val topBarDecorator = LocalWorkbenchTopBarDecorator.current

    Column(
        modifier = modifier.fillMaxSize().background(AdminWorkbenchTokens.pageBackground),
    ) {
        topBarDecorator.Decorate(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AdminWorkbenchGlobalBar(
                brandLabel = brandLabel,
                welcomeLabel = welcomeLabel,
                onGlobalSearchClick = onGlobalSearchClick,
                githubLabel = githubLabel,
                onGithubClick = onGithubClick,
                languageLabel = languageLabel,
                onLanguageClick = onLanguageClick,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                notificationCount = notificationCount,
                onNotificationsClick = onNotificationsClick,
                userLabel = userLabel,
                onUserClick = onUserClick,
                topBarHeight = windowFrame.topBarHeight,
                leadingInset = windowFrame.leadingInset,
                trailingInset = windowFrame.trailingInset,
                immersiveTopBar = windowFrame.immersiveTopBar,
            )
        }

        WorkbenchScaffold(
            sidebar = sidebar,
            content = content,
            modifier = Modifier.weight(1f).fillMaxWidth(),
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
            sidebarContainerModifier = Modifier.fillMaxHeight()
                .background(AdminWorkbenchTokens.sidebarBackground),
            mainContainerModifier = Modifier.background(AdminWorkbenchTokens.pageBackground),
            headerContainerModifier = Modifier.background(AdminWorkbenchTokens.headerBackground),
            detailContainerModifier = Modifier.background(AdminWorkbenchTokens.detailBackground),
            dividerColor = AdminWorkbenchTokens.dividerColor,
            thumbColor = AdminWorkbenchTokens.resizeThumbColor,
            thumbBorderColor = AdminWorkbenchTokens.resizeThumbBorder,
            contentHeader = {
                AdminWorkbenchContentHeader(
                    breadcrumb = breadcrumb,
                    pageTitle = pageTitle,
                    pageSubtitle = pageSubtitle,
                    pageActions = pageActions,
                    titleContent = titleContent,
                )
            },
        )
    }
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
fun WorkbenchGitHubButton(
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
        leading = {
            Icon(
                imageVector = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                contentDescription = null,
                tint = AdminWorkbenchTokens.textPrimary,
            )
        },
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
        leading = {
            WorkbenchUserAvatar(
                initials = label.toAvatarInitials(),
            )
        },
    )
}

@Composable
private fun AdminWorkbenchGlobalBar(
    brandLabel: String,
    welcomeLabel: String,
    onGlobalSearchClick: (() -> Unit)?,
    githubLabel: String?,
    onGithubClick: (() -> Unit)?,
    languageLabel: String?,
    onLanguageClick: (() -> Unit)?,
    isDarkTheme: Boolean?,
    onThemeToggle: (() -> Unit)?,
    notificationCount: Int?,
    onNotificationsClick: (() -> Unit)?,
    userLabel: String?,
    onUserClick: (() -> Unit)?,
    topBarHeight: Dp,
    leadingInset: Dp,
    trailingInset: Dp,
    immersiveTopBar: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .height(topBarHeight)
            .background(
                AdminWorkbenchTokens.topBarBackground.copy(
                    alpha = if (immersiveTopBar) 0.96f else 1f,
                ),
            )
            .padding(
                start = 18.dp + leadingInset,
                end = 18.dp + trailingInset,
            ),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdminWorkbenchBrand(
                label = brandLabel,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            )
            {
                Text(
                    text = brandLabel,
                    color = AdminWorkbenchTokens.topBarTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = welcomeLabel,
                    color = AdminWorkbenchTokens.topBarTextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Row(
            modifier = Modifier.widthIn(max = 720.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onGlobalSearchClick != null) {
                WorkbenchSearchButton(
                    onClick = onGlobalSearchClick,
                )
            }
            if (isDarkTheme != null && onThemeToggle != null) {
                WorkbenchThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onClick = onThemeToggle,
                )
            }
            if (!githubLabel.isNullOrBlank() && onGithubClick != null) {
                WorkbenchGitHubButton(
                    label = githubLabel,
                    onClick = onGithubClick,
                )
            }
            if (!languageLabel.isNullOrBlank() && onLanguageClick != null) {
                WorkbenchLanguageButton(
                    label = languageLabel,
                    onClick = onLanguageClick,
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
private fun AdminWorkbenchBrand(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(28.dp)
            .background(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(9.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.size(18.dp),
        ) {
            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.20f,
                center = center.copy(x = size.width * 0.36f),
            )
            drawCircle(
                color = Color(0xFFFF6B6B),
                radius = size.minDimension * 0.20f,
                center = center.copy(x = size.width * 0.64f),
            )
        }
    }
}

@Composable
private fun RowScope.AdminWorkbenchContentHeader(
    breadcrumb: List<String>,
    pageTitle: String,
    pageSubtitle: String?,
    pageActions: @Composable RowScope.() -> Unit,
    titleContent: (@Composable ColumnScope.() -> Unit)?,
) {
    if (titleContent != null) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = titleContent,
        )
    } else {
        AdminWorkbenchTitleBlock(
            breadcrumb = breadcrumb,
            pageTitle = pageTitle,
            pageSubtitle = pageSubtitle,
            modifier = Modifier.weight(1f),
        )
    }

    Row(
        modifier = Modifier.widthIn(max = 440.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = pageActions,
    )
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
                color = AdminWorkbenchTokens.headerTextMuted,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(
            text = pageTitle,
            color = AdminWorkbenchTokens.headerTextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
        if (pageSubtitle != null) {
            Text(
                text = pageSubtitle,
                color = AdminWorkbenchTokens.headerTextMuted,
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
    leading: (@Composable () -> Unit)? = null,
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
        leading?.invoke()
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

@Composable
private fun WorkbenchUserAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(24.dp).background(
            color = AdminWorkbenchTokens.avatarBackground,
            shape = CircleShape,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.size(24.dp),
        ) {
            drawCircle(
                color = AdminWorkbenchTokens.avatarHalo,
                radius = size.minDimension / 2f,
            )
        }
        Text(
            text = initials,
            color = AdminWorkbenchTokens.textPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
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

private fun String.toAvatarInitials(): String {
    val source = substringBefore("@")
        .split('.', '-', '_', ' ')
        .filter(String::isNotBlank)
    if (source.isEmpty()) {
        return "U"
    }
    val initials = source
        .take(2)
        .joinToString(separator = "") { token ->
            token.first().uppercase()
        }
    return initials.ifBlank { "U" }
}

private object AdminWorkbenchTokens {
    val topBarBackground = Color(0xFF2387E7)
    val topBarTextPrimary = Color.White.copy(alpha = 0.98f)
    val topBarTextSecondary = Color.White.copy(alpha = 0.84f)
    val pageBackground = Color(0xFFF2F5F9)
    val sidebarBackground = Color.White
    val headerBackground = Color.White
    val detailBackground = Color.White
    val dividerColor = Color(0xFFE4EAF2)
    val resizeThumbColor = Color(0xFF7EB9F5)
    val resizeThumbBorder = Color(0xFFB7D9FB)
    val buttonBackground = Color.White.copy(alpha = 0.12f)
    val buttonBorder = Color.White.copy(alpha = 0.18f)
    val highlightedBackground = Color.White.copy(alpha = 0.18f)
    val highlightedBorder = Color.White.copy(alpha = 0.26f)
    val badgeBackground = Color(0xFFFF5A5F)
    val avatarBackground = Color.White.copy(alpha = 0.20f)
    val avatarHalo = Color.White.copy(alpha = 0.18f)
    val textPrimary = Color.White.copy(alpha = 0.96f)
    val headerTextPrimary = Color(0xFF1F2A37)
    val headerTextMuted = Color(0xFF6B7280)
}
