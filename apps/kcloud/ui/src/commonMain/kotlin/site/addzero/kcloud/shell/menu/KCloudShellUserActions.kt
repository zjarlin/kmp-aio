package site.addzero.kcloud.shell.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.component.chat.AddChatOverlayState
import site.addzero.kcloud.design.button.KCloudButtonVariant
import site.addzero.kcloud.shell.KCloudShellState
import site.addzero.kcloud.shell.navigation.KCloudRouteCatalog
import site.addzero.kcloud.theme.ShellThemeMode
import site.addzero.kcloud.theme.ShellThemeState
import site.addzero.kcloud.theme.resolveDarkTheme

/**
 * 壳层右上角动作贡献点。
 *
 * 每个动作自己决定依赖和渲染，不让脚手架实现类绑定具体按钮。
 */
interface KCloudShellUserActionContributor {
    val order: Int
        get() = 0

    @Composable
    fun RowScope.Render()
}

/**
 * 壳层动作聚合器。
 *
 * 脚手架只依赖这一层，具体按钮通过 IoC 自动扩展。
 */
@Single
class KCloudShellUserActionsRenderer(
    private val contributors: List<KCloudShellUserActionContributor>,
) {
    @Composable
    fun RowScope.Render() {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            contributors
                .sortedBy(KCloudShellUserActionContributor::order)
                .forEach { contributor ->
                    with(contributor) {
                        Render()
                    }
                }
        }
    }
}

@Single(
    binds = [
        KCloudShellUserActionContributor::class,
    ],
)
class KCloudAiAssistantUserActionContributor(
    private val aiOverlayState: AddChatOverlayState,
) : KCloudShellUserActionContributor {
    override val order: Int = 10

    @Composable
    override fun RowScope.Render() {
        KCloudShellIconButton(
            tooltip = if (aiOverlayState.visible) "关闭 AI 助手" else "打开 AI 助手",
            onClick = aiOverlayState::toggle,
            variant = if (aiOverlayState.visible) {
                KCloudButtonVariant.Secondary
            } else {
                KCloudButtonVariant.Outline
            },
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
            )
        }
    }
}

@Single(
    binds = [
        KCloudShellUserActionContributor::class,
    ],
)
class KCloudThemeToggleUserActionContributor(
    private val shellThemeState: ShellThemeState,
) : KCloudShellUserActionContributor {
    override val order: Int = 20

    @Composable
    override fun RowScope.Render() {
        val darkTheme = shellThemeState.themeMode.resolveDarkTheme(
            systemDarkTheme = isSystemInDarkTheme(),
        )
        KCloudShellIconButton(
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
                KCloudButtonVariant.Secondary
            } else {
                KCloudButtonVariant.Outline
            },
        ) {
            Icon(
                imageVector = if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = null,
            )
        }
    }
}

@Single(
    binds = [
        KCloudShellUserActionContributor::class,
    ],
)
class KCloudWorkbenchMenuUserActionContributor(
    private val shellState: KCloudShellState,
    private val routeCatalog: KCloudRouteCatalog,
) : KCloudShellUserActionContributor {
    override val order: Int = 30

    @Composable
    override fun RowScope.Render() {
        KCloudUserMenu(
            shellState = shellState,
            routeCatalog = routeCatalog,
        )
    }
}
