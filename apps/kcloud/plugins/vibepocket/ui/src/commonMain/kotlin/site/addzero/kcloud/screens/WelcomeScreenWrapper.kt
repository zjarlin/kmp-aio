package site.addzero.kcloud.screens

import androidx.compose.runtime.Composable
import site.addzero.kcloud.auth.WelcomeScreen

@Composable
fun WelcomeScreenWrapper(
    onSetupComplete: (token: String, baseUrl: String) -> Unit,
) {
    WelcomeScreen(
        onSetupComplete = onSetupComplete,
    )
}
