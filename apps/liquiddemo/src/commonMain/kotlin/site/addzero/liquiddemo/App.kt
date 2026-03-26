package site.addzero.liquiddemo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.KoinApplication
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.plugin.module.dsl.koinConfiguration



@KoinApplication
object KoinApplication

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration<KoinApplication>(),
    ) {
        MaterialTheme {
            RenderSidebarShowcaseShell(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

}
