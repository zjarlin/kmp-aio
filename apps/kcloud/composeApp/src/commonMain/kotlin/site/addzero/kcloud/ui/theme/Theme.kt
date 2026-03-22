package site.addzero.kcloud.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = _root_ide_package_.site.addzero.kcloud.ui.theme.PrimaryLight,
    onPrimary = _root_ide_package_.site.addzero.kcloud.ui.theme.OnPrimaryLight,
    secondary = _root_ide_package_.site.addzero.kcloud.ui.theme.SecondaryLight,
    background = _root_ide_package_.site.addzero.kcloud.ui.theme.BackgroundLight,
    surface = _root_ide_package_.site.addzero.kcloud.ui.theme.SurfaceLight,
    surfaceVariant = _root_ide_package_.site.addzero.kcloud.ui.theme.SurfaceVariantLight,
    onSurface = _root_ide_package_.site.addzero.kcloud.ui.theme.OnSurfaceLight,
    onSurfaceVariant = _root_ide_package_.site.addzero.kcloud.ui.theme.OnSurfaceVariantLight,
    outline = _root_ide_package_.site.addzero.kcloud.ui.theme.OutlineLight,
    error = _root_ide_package_.site.addzero.kcloud.ui.theme.ErrorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = _root_ide_package_.site.addzero.kcloud.ui.theme.PrimaryDark,
    onPrimary = _root_ide_package_.site.addzero.kcloud.ui.theme.OnPrimaryDark,
    secondary = _root_ide_package_.site.addzero.kcloud.ui.theme.SecondaryDark,
    background = _root_ide_package_.site.addzero.kcloud.ui.theme.BackgroundDark,
    surface = _root_ide_package_.site.addzero.kcloud.ui.theme.SurfaceDark,
    surfaceVariant = _root_ide_package_.site.addzero.kcloud.ui.theme.SurfaceVariantDark,
    onSurface = _root_ide_package_.site.addzero.kcloud.ui.theme.OnSurfaceDark,
    onSurfaceVariant = _root_ide_package_.site.addzero.kcloud.ui.theme.OnSurfaceVariantDark,
    outline = _root_ide_package_.site.addzero.kcloud.ui.theme.OutlineDark,
    error = _root_ide_package_.site.addzero.kcloud.ui.theme.ErrorDark
)

@Composable
fun KCloudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) _root_ide_package_.site.addzero.kcloud.ui.theme.DarkColorScheme else _root_ide_package_.site.addzero.kcloud.ui.theme.LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = _root_ide_package_.site.addzero.kcloud.ui.theme.Typography,
        content = content
    )
}
