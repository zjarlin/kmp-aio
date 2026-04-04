package site.addzero.kcloud.window.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.core.annotation.Single
import site.addzero.kcloud.shell.menu.TopBarActionsRenderer
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

@Single
class ScaffoldingImpl(
    private val topBarActionsRenderer: TopBarActionsRenderer,
) : ScaffoldingSpi {
    override val pageTitle = "KCloud"
    override val brandLabel = "OKMY DICS"

    @Composable
    override fun RowScope.RenderBrand() {
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

    @Composable
    override fun RowScope.RenderTopBarActions() {
        with(topBarActionsRenderer) {
            Render()
        }
    }

    @Composable
    override fun RenderOverlay() {
        DefaultOverlay()
    }
}
