package com.kcloud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kcloud.feature.ShellThemeMode
import com.kcloud.feature.ShellSettingsService
import com.kcloud.ui.theme.KCloudTheme
import org.koin.compose.koinInject
import site.addzero.workbenchshell.RenderWorkbenchScaffold

@Composable
fun MainWindow(
    shellSettingsService: ShellSettingsService = koinInject(),
) {
    val themeMode by shellSettingsService.themeMode.collectAsState()

    KCloudTheme(
        darkTheme = when (themeMode) {
            ShellThemeMode.LIGHT -> false
            ShellThemeMode.DARK -> true
            ShellThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        },
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Box(
                modifier = Modifier.fillMaxSize().workbenchBackdrop(),
            ) {
                RenderWorkbenchScaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentHeaderScrollable = false,
                    minSidebarWidth = 248.dp,
                    maxSidebarWidth = 340.dp,
                )
            }
        }
    }
}

/** 工作台底色：用深海蓝渐变替掉整片 IDE 灰，让模块页在上面更统一。 */
private fun Modifier.workbenchBackdrop(): Modifier {
    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF08111C),
                Color(0xFF091725),
                Color(0xFF06101A),
            ),
        ),
    ).background(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF245B8F).copy(alpha = 0.26f),
                Color.Transparent,
            ),
            radius = 520f,
        ),
    )
}
