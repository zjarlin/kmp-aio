package site.addzero.kcloud

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import site.addzero.kcloud.app.kCloudWorkbenchModule
import site.addzero.kcloud.ui.MainWindow
import site.addzero.vibepocket.vibePocketPluginModule

@Composable
fun App() {
    KoinApplication(configuration = koinConfiguration(declaration = { modules(kCloudWorkbenchModule, vibePocketPluginModule) }), content = {
        MainWindow()
    })
}
