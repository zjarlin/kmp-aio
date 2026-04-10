package site.addzero.kcloud.plugins.mcuconsole.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.annotation.RoutePlacement
import site.addzero.annotation.RouteScene
import site.addzero.component.text.TodoText
import site.addzero.cupertino.workbench.components.field.CupertinoTextField
import site.addzero.cupertino.workbench.components.panel.CupertinoPanel
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Text

/**
 * MCU 串口调试日志页面。
 */
@Route(
    value = "开发工具",
    title = "调试",
    routePath = "mcu/debug",
    icon = "BugReport",
    order = 20.0,
    placement = RoutePlacement(
        scene = RouteScene(
            name = "上位机",
            icon = "Build",
            order = 0,
        ),
    ),
)
@Composable
fun McuDebugScreen() {
    val viewModel = koinViewModel<McuDebugViewModel>()
    val state = viewModel.screenState
    val toolbarActionsSpi = koinInject<McuDebugToolbarActionsSpi>()
    val portQuickSelectSpi = koinInject<McuDebugPortQuickSelectSpi>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CupertinoPanel(
            title = "MCU 调试日志",
            subtitle = "默认按真实板卡 `/dev/cu.usbserial-2140 + 115200` 连接，日志通过 SSE 持续推到上位机。",
            actions = {
                toolbarActionsSpi.Render(
                    state = state,
                    viewModel = viewModel,
                )
            },
        ) {
            Text(
                text = when {
                    state.connecting -> "正在建立串口日志连接。"
                    state.streaming -> "当前已建立日志流连接。"
                    else -> "准备连接串口日志流。"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        CupertinoPanel(
            title = "串口配置",
            subtitle = "这里先保留最关键的端口和波特率，方便快速验证真实板卡日志流。",
        ) {
            CupertinoTextField(
                label = "串口路径",
                value = state.portName,
                onValueChange = viewModel::updatePortName,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "/dev/cu.usbserial-2140",
                singleLine = true,
            )
            CupertinoTextField(
                label = "波特率",
                value = state.baudRateInput,
                onValueChange = viewModel::updateBaudRateInput,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "115200",
                singleLine = true,
            )
        }

        CupertinoPanel(
            title = "可见串口",
            subtitle = "点击下面的端口卡片可以直接填入连接目标。",
        ) {
            if (state.ports.isEmpty()) {
                TodoText(
                    title = "暂无串口",
                    description = "当前没有探测到可用串口，或者后台串口枚举失败。",
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.ports.forEach { descriptor ->
                        portQuickSelectSpi.Render(
                            state = state,
                            descriptor = descriptor,
                            viewModel = viewModel,
                        )
                    }
                }
            }
        }

        state.errorMessage?.let { message ->
            McuDebugNoticePanel(
                title = "错误",
                text = message,
                color = MaterialTheme.colorScheme.error,
            )
        }
        state.noticeMessage?.let { message ->
            McuDebugNoticePanel(
                title = "状态",
                text = message,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        CupertinoPanel(
            title = "实时日志",
            subtitle = "服务端已经把串口日志封装成 SSE，当前面板直接消费文本事件。",
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp),
            ) {
                if (state.logs.isEmpty()) {
                    Text(
                        text = if (state.streaming || state.connecting) {
                            "已建立连接，等待设备日志..."
                        } else {
                            "点击“开始连接”后，这里会出现串口日志。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Text(
                        text = state.logs.joinToString(separator = "\n"),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

/**
 * 调试提示面板。
 */
@Composable
private fun McuDebugNoticePanel(
    title: String,
    text: String,
    color: androidx.compose.ui.graphics.Color,
) {
    CupertinoPanel(
        title = title,
        subtitle = "",
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
