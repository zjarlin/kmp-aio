package site.addzero.kcloud.shell.spi_impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.core.annotation.Single
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Text
import site.addzero.cupertino.workbench.menu.WorkbenchTopBarActionContributor
import site.addzero.cupertino.workbench.menu.WorkbenchTopBarActionsHost
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

@Single
class ScaffoldingImpl(private val contributors: List<WorkbenchTopBarActionContributor>) : ScaffoldingSpi {
    override val pageTitle = "KCloud"
    override val brandLabel = "OKMY DICS"

//    /**
//     [旗帜位] 顶栏最左边的一小块，像门头招牌，通常放品牌名或产品身份。
//     */

    // [旗帜位] 顶栏最左边的一小块，像门头招牌，通常放品牌名或产品身份。
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

    /**
    // [驾驶舱右手边] 顶栏最右侧的操作带，像驾驶位右手边按钮区，通常放全局顶栏动作。
     */
    @Composable
    override fun RowScope.RenderTopBarActions() {
        WorkbenchTopBarActionsHost(contributors)
//        with(topBarActionsRenderer) {
//            Render()
//        }
    }
    /**
    // [浮层位] 悬在主页面上方的一层，像舞台上方吊着的幕布，通常放弹窗、助手面板、全局覆盖层。
     */

    @Composable
    override fun RenderOverlay() {
//        Text("Overlay")
    }
}