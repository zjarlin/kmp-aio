package site.addzero.kcloud.shell.menu

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.component.chat.AddChatOverlayState
import site.addzero.kcloud.theme.ShellThemeMode
import site.addzero.kcloud.theme.ShellThemeState
import site.addzero.kcloud.theme.resolveDarkTheme
import site.addzero.workbench.design.button.WorkbenchButtonVariant
import site.addzero.workbench.design.button.WorkbenchIconButton
import site.addzero.workbench.shell.menu.WorkbenchTopBarActionContributor
import site.addzero.workbench.shell.menu.WorkbenchTopBarActionsHost

/**
 * 壳层动作聚合器。
 *
 * 脚手架只依赖这一层，具体按钮通过 IoC 自动扩展。
 */
@Single
class TopBarActionsRenderer(
    private val contributors: List<WorkbenchTopBarActionContributor>,
) {
    @Composable
    fun RowScope.Render() {
        WorkbenchTopBarActionsHost(contributors)
    }
}

@Single
class AiAssistantTopBarActionContributor(
    private val aiOverlayState: AddChatOverlayState,
) : WorkbenchTopBarActionContributor {
    override val order: Int = 10

    @Composable
    override fun RowScope.Render() {
        ShellIconButton(
            tooltip = if (aiOverlayState.visible) "关闭 AI 助手" else "打开 AI 助手",
            onClick = aiOverlayState::toggle,
            variant = if (aiOverlayState.visible) {
                WorkbenchButtonVariant.Secondary
            } else {
                WorkbenchButtonVariant.Outline
            },
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
            )
        }
    }
}

@Single
class ThemeToggleTopBarActionContributor(
    private val shellThemeState: ShellThemeState,
) : WorkbenchTopBarActionContributor {
    override val order: Int = 20

    @Composable
    override fun RowScope.Render() {
        val darkTheme = shellThemeState.themeMode.resolveDarkTheme(
            systemDarkTheme = isSystemInDarkTheme(),
        )
        ShellIconButton(
            tooltip = if (darkTheme) "切换到浅色" else "切换到深色",
            onClick = {
                shellThemeState.updateThemeMode(
                    if (darkTheme) {
                        ShellThemeMode.LIGHT
                    } else {
                        ShellThemeMode.DARK
                    },
                )
            },
            variant = if (darkTheme) {
                WorkbenchButtonVariant.Secondary
            } else {
                WorkbenchButtonVariant.Outline
            },
        ) {
            Icon(
                imageVector = if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = null,
            )
        }
    }
}

@Composable
internal fun ShellIconButton(
    tooltip: String,
    onClick: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    variant: WorkbenchButtonVariant = WorkbenchButtonVariant.Outline,
    content: @Composable RowScope.() -> Unit,
) {
    WorkbenchIconButton(
        onClick = onClick,
        modifier = modifier,
        tooltip = tooltip,
        variant = variant,
        content = content,
    )
}
