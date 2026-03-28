package site.addzero.kcloud.plugins.mcuconsole.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import site.addzero.annotation.Route
import site.addzero.component.button.AddIconButton

@Route(
    value = "设备",
    title = "控制台",
    routePath = "mcu/control",
    icon = "PowerSettingsNew",
    order = 0.0,
)
@Composable
fun McuControlScreen() {
    McuActionDock(
        rows = listOf(
            listOf(
                McuToolAction("扫描", Icons.Default.Search),
                McuToolAction("同步", Icons.Default.Sync),
                McuToolAction("启动", Icons.Default.PlayArrow),
                McuToolAction("停止", Icons.Default.Stop),
            ),
            listOf(
                McuToolAction("上电", Icons.Default.PowerSettingsNew),
                McuToolAction("复位", Icons.Default.Refresh),
                McuToolAction("配置", Icons.Default.Settings),
                McuToolAction("调参", Icons.Default.Tune),
            ),
        ),
    )
}

@Route(
    value = "设备",
    title = "烧录",
    routePath = "mcu/flash",
    icon = "Upload",
    order = 10.0,
)
@Composable
fun McuFlashScreen() {
    McuActionDock(
        rows = listOf(
            listOf(
                McuToolAction("上传", Icons.Default.Upload),
                McuToolAction("下载", Icons.Default.Download),
                McuToolAction("构建", Icons.Default.Build),
                McuToolAction("同步", Icons.Default.Sync),
            ),
            listOf(
                McuToolAction("启动", Icons.Default.PlayArrow),
                McuToolAction("停止", Icons.Default.Stop),
                McuToolAction("复位", Icons.Default.Refresh),
                McuToolAction("配置", Icons.Default.Settings),
            ),
        ),
    )
}

@Route(
    value = "设备",
    title = "调试",
    routePath = "mcu/debug",
    icon = "BugReport",
    order = 20.0,
)
@Composable
fun McuDebugScreen() {
    McuActionDock(
        rows = listOf(
            listOf(
                McuToolAction("扫描", Icons.Default.Search),
                McuToolAction("调试", Icons.Default.BugReport),
                McuToolAction("构建", Icons.Default.Build),
                McuToolAction("调参", Icons.Default.Tune),
            ),
            listOf(
                McuToolAction("启动", Icons.Default.PlayArrow),
                McuToolAction("停止", Icons.Default.Stop),
                McuToolAction("同步", Icons.Default.Sync),
                McuToolAction("复位", Icons.Default.Refresh),
            ),
        ),
    )
}

@Composable
private fun McuActionDock(
    rows: List<List<McuToolAction>>,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        rows.forEach { actions ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    actions.forEach { action ->
                        Box(
                            modifier = Modifier.weight(1f).height(72.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AddIconButton(
                                text = action.label,
                                imageVector = action.icon,
                                modifier = Modifier.size(24.dp),
                                onClick = {},
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class McuToolAction(
    val label: String,
    val icon: ImageVector,
)
