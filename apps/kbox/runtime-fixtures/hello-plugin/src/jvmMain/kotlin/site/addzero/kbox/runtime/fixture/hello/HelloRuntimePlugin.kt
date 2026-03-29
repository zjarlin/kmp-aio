package site.addzero.kbox.runtime.fixture.hello

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import site.addzero.kbox.plugin.api.KboxPluginContext
import site.addzero.kbox.plugin.api.KboxRouteContribution
import site.addzero.kbox.plugin.api.KboxRuntimePlugin

class HelloRuntimePlugin : KboxRuntimePlugin {
    override fun koinModules(
        context: KboxPluginContext,
    ) = listOf(
        module {
            single {
                HelloGreetingService(
                    pluginDir = context.pluginDir,
                    appDataDir = context.appDataDir,
                )
            }
        },
    )

    override fun routes(
        context: KboxPluginContext,
    ): List<KboxRouteContribution> {
        return listOf(
            KboxRouteContribution(
                pluginId = context.manifest.pluginId,
                sceneName = "扩展插件",
                title = "Hello Runtime",
                routePath = "runtime/hello-runtime",
                parentName = "示例插件",
                iconName = "Extension",
                sceneIconName = "Extension",
                order = 500.0,
                sceneOrder = 500,
                defaultInScene = false,
                content = { HelloRuntimePluginScreen() },
            ),
        )
    }
}

private data class HelloGreetingService(
    val pluginDir: String,
    val appDataDir: String,
) {
    fun text(): String {
        return "动态插件已加载，当前目录：$pluginDir"
    }
}

@Composable
private fun HelloRuntimePluginScreen() {
    val service = remember {
        KoinPlatform.getKoin().get<HelloGreetingService>()
    }
    Card(
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Hello Runtime",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = service.text(),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "宿主 appDataDir：${service.appDataDir}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}
