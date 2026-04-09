@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.kcloud.plugins.hostconfig.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.theme.CupertinoTheme
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.components.panel.CupertinoStatusStrip

@Composable
internal fun FeatureUnavailableScreen(
    title: String,
    subtitle: String,
    notice: String = "暂未开放",
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CupertinoPanel(
            title = title,
            subtitle = subtitle,
        ) {
            CupertinoStatusStrip(notice)
            CupertinoText(
                text = "当前入口先保留在元数据配置分组，等后续能力开放后再补正式界面。",
                style = CupertinoTheme.typography.body,
            )
        }
    }
}
