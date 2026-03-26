package site.addzero.liquiddemo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.koin.compose.KoinApplication
import org.koin.plugin.module.dsl.koinConfiguration

private val LiquiddemoDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8DC7FF),
    onPrimary = Color(0xFF04203C),
    secondary = Color(0xFF78D8C1),
    background = Color(0xFF060B14),
    surface = Color(0xFF0B1220),
    surfaceVariant = Color(0xFF101A2A),
    onBackground = Color(0xFFE8EEF8),
    onSurface = Color(0xFFE8EEF8),
)

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration<LiquiddemoKoinApplication>(),
    ) {
        MaterialTheme(
            colorScheme = LiquiddemoDarkColorScheme,
        ) {
            RenderSidebarShowcaseShell(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
