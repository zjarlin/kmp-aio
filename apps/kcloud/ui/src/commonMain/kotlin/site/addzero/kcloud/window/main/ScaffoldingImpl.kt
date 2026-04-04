package site.addzero.kcloud.window.main

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import org.koin.core.annotation.Single
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

@Single(
    binds = [
        ScaffoldingSpi::class,
    ],
)
class ScaffoldingImpl : ScaffoldingSpi {
    override val pageTitle: String = "KCloud"
    override val brandLabel: String = "OKMY DICS"

    @Composable
    override fun RowScope.RenderBrand() {
        KCloudDefaultBrandSlot()
    }

    @Composable
    override fun RowScope.RenderUserActions(
        darkTheme: Boolean,
        onThemeToggle: () -> Unit,
    ) {
        KCloudDefaultUserActions(
            darkTheme = darkTheme,
            onThemeToggle = onThemeToggle,
        )
    }

    @Composable
    override fun RenderOverlay() {
        KCloudDefaultOverlay()
    }
}
