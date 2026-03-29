package site.addzero.kcpi18ndemo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.plugin.module.dsl.koinConfiguration

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration<KcpI18nDemoKoinApplication>(),
    ) {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                DemoHomeScreen()
            }
        }
    }
}

@Composable
private fun DemoHomeScreen(
    state: DemoTextState = koinInject(),
) {
    var locale by remember { mutableStateOf("zh") }

    LaunchedEffect(locale) {
        DemoI18nLocaleBridge.applyLocale(locale)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LocaleButton(
                label = "ZH",
                targetLocale = "zh",
                currentLocale = locale,
                onSelect = { locale = it },
            )
            LocaleButton(
                label = "EN",
                targetLocale = "en",
                currentLocale = locale,
                onSelect = { locale = it },
            )
            LocaleButton(
                label = "JA",
                targetLocale = "ja",
                currentLocale = locale,
                onSelect = { locale = it },
            )
        }
        Text(text = state.titleText())
        Text(text = state.bodyText())
        Button(onClick = state::recordClick) {
            Text(text = state.buttonText())
        }
        Text(text = state.statusText())
    }
}

@Composable
private fun LocaleButton(
    label: String,
    targetLocale: String,
    currentLocale: String,
    onSelect: (String) -> Unit,
) {
    Button(
        onClick = { onSelect(targetLocale) },
        enabled = currentLocale != targetLocale,
    ) {
        Text(text = label)
    }
}
