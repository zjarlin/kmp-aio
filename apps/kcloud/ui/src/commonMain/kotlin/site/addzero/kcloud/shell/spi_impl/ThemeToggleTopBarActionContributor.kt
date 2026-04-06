package site.addzero.kcloud.shell.spi_impl

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.button.WorkbenchIconButton
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.cupertino.workbench.menu.WorkbenchTopBarActionContributor
import site.addzero.kcloud.theme.ShellThemeMode
import site.addzero.kcloud.theme.ShellThemeState
import site.addzero.kcloud.theme.resolveDarkTheme

/**
 * 主题切换按钮
 * @author zjarlin
 * @date 2026/04/06
 * @constructor 创建[ThemeToggleTopBarActionContributor]
 * @param [shellThemeState]
 */
@Single
class ThemeToggleTopBarActionContributor(
    private val shellThemeState: ShellThemeState,
) : WorkbenchTopBarActionContributor {
    override val order = 20

    @Composable
    override fun RowScope.Render() {
        val darkTheme = shellThemeState.themeMode.resolveDarkTheme(
            systemDarkTheme = isSystemInDarkTheme(),
        )
        WorkbenchIconButton(
            onClick = {
                shellThemeState.updateThemeMode(
                    if (darkTheme) {
                        ShellThemeMode.LIGHT
                    } else {
                        ShellThemeMode.DARK
                    },
                )
            },
            modifier = androidx.compose.ui.Modifier,
            tooltip = if (darkTheme) "切换到浅色" else "切换到深色",
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