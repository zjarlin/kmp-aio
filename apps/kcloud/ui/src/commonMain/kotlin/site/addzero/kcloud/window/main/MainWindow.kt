package site.addzero.kcloud.window.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import org.koin.core.annotation.Single
import site.addzero.kcloud.theme.Theme
import site.addzero.kcloud.theme.ShellThemeMode
import site.addzero.kcloud.theme.ShellThemeState
import site.addzero.kcloud.theme.resolveDarkTheme
import site.addzero.kcloud.window.spi.KCloudBrandSlotSpi
import site.addzero.kcloud.window.spi.KCloudOverlaySlotSpi
import site.addzero.kcloud.window.spi.KCloudUserSlotSpi
import site.addzero.kcloud.window.spi.KCloudWorkbenchScaffoldingSpi
import site.addzero.kcloud.window.spi.MainWindowSpi

@Single(
    binds = [
        MainWindowSpi::class,
    ],
)
class KCloudMainWindow(
    private val shellThemeState: ShellThemeState,
    private val scaffolding: KCloudWorkbenchScaffoldingSpi,
    private val overlaySlot: KCloudOverlaySlotSpi,
) : MainWindowSpi {
    @Composable
    override fun Render() {
        val themeMode = shellThemeState.themeMode
        val darkTheme = themeMode.resolveDarkTheme(
            systemDarkTheme = isSystemInDarkTheme(),
        )
        val toggleTheme = remember(shellThemeState, darkTheme) {
            {
                shellThemeState.updateThemeMode(
                    if (darkTheme) {
                        ShellThemeMode.LIGHT
                    } else {
                        ShellThemeMode.DARK
                    },
                )
            }
        }

        Theme(
            darkTheme = darkTheme,
        ) {
            scaffolding.Render(
                darkTheme = darkTheme,
                onThemeToggle = toggleTheme,
            )
            overlaySlot.Render()
        }
    }
}

@Composable
private fun RowScope.KCloudBrandSlot() {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "OKMY DICS",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.8.sp,
        )
    }
}

@Single(
    binds = [
        KCloudBrandSlotSpi::class,
    ],
)
class DefaultKCloudBrandSlot : KCloudBrandSlotSpi {
    @Composable
    override fun RowScope.Render() {
        KCloudBrandSlot()
    }
}

@Single(
    binds = [
        KCloudUserSlotSpi::class,
    ],
)
class DefaultKCloudUserSlot(
) : KCloudUserSlotSpi {
    @Composable
    override fun Render(
        darkTheme: Boolean,
        onThemeToggle: () -> Unit,
    ) {
        site.addzero.kcloud.shell.menu.KCloudShellActions(
            darkTheme = darkTheme,
            onThemeToggle = onThemeToggle,
        )
    }
}
