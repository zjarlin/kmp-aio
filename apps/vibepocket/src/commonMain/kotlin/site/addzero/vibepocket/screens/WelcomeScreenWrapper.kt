package site.addzero.vibepocket.screens

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import site.addzero.vibepocket.auth.WelcomePage
import site.addzero.vibepocket.navigation.RouteKey

@Composable
fun WelcomeScreenWrapper(
    backStack: SnapshotStateList<RouteKey>,
    homeRoute: RouteKey,
    onSetupComplete: (token: String, baseUrl: String) -> Unit
) {
    NavDisplay(
        backStack = backStack,
        onBack = {},
        entryProvider = { routeKey ->
            NavEntry(routeKey) {
                WelcomePage(
                    onEnter = { token, url ->
                        onSetupComplete(token, url)
                        backStack.clear()
                        backStack.add(homeRoute)
                    },
                )
            }
        },
    )
}