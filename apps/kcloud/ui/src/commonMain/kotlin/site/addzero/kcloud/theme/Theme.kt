package site.addzero.kcloud.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import site.addzero.themes.ShadcnColors
import site.addzero.themes.ShadcnTheme

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    surfaceTint = PrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    surfaceTint = PrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
)

private val KCloudLightShadcnColors = object : ShadcnColors {
    override val background = BackgroundLight
    override val foreground = OnBackgroundLight
    override val card = SurfaceLight
    override val cardForeground = OnSurfaceLight
    override val popover = SurfaceLight
    override val popoverForeground = OnSurfaceLight
    override val primary = PrimaryLight
    override val primaryForeground = OnPrimaryLight
    override val secondary = SecondaryContainerLight
    override val secondaryForeground = OnSecondaryContainerLight
    override val muted = SurfaceVariantLight
    override val mutedForeground = OnSurfaceVariantLight
    override val accent = PrimaryContainerLight
    override val accentForeground = OnPrimaryContainerLight
    override val destructive = ErrorLight
    override val destructiveForeground = OnPrimaryLight
    override val border = OutlineVariantLight
    override val input = OutlineLight
    override val ring = PrimaryLight
    override val chart1 = PrimaryLight
    override val chart2 = SecondaryLight
    override val chart3 = TertiaryLight
    override val chart4 = InfoLight
    override val chart5 = WarningLight
    override val sidebar = SurfaceLight
    override val sidebarForeground = OnSurfaceLight
    override val sidebarPrimary = PrimaryLight
    override val sidebarPrimaryForeground = OnPrimaryLight
    override val sidebarAccent = SurfaceVariantLight
    override val sidebarAccentForeground = OnSurfaceLight
    override val sidebarBorder = OutlineVariantLight
    override val sidebarRing = PrimaryLight
    override val snackbar = SurfaceLight
}

private val KCloudDarkShadcnColors = object : ShadcnColors {
    override val background = BackgroundDark
    override val foreground = OnBackgroundDark
    override val card = SurfaceDark
    override val cardForeground = OnSurfaceDark
    override val popover = SurfaceVariantDark
    override val popoverForeground = OnSurfaceDark
    override val primary = PrimaryDark
    override val primaryForeground = OnPrimaryDark
    override val secondary = SecondaryContainerDark
    override val secondaryForeground = OnSecondaryContainerDark
    override val muted = SurfaceVariantDark
    override val mutedForeground = OnSurfaceVariantDark
    override val accent = PrimaryContainerDark
    override val accentForeground = OnPrimaryContainerDark
    override val destructive = ErrorDark
    override val destructiveForeground = OnPrimaryDark
    override val border = OutlineVariantDark
    override val input = OutlineDark
    override val ring = PrimaryDark
    override val chart1 = PrimaryDark
    override val chart2 = SecondaryDark
    override val chart3 = TertiaryDark
    override val chart4 = InfoDark
    override val chart5 = WarningDark
    override val sidebar = SurfaceDark
    override val sidebarForeground = OnSurfaceDark
    override val sidebarPrimary = PrimaryDark
    override val sidebarPrimaryForeground = OnPrimaryDark
    override val sidebarAccent = SurfaceVariantDark
    override val sidebarAccentForeground = OnSurfaceDark
    override val sidebarBorder = OutlineVariantDark
    override val sidebarRing = PrimaryDark
    override val snackbar = SurfaceVariantDark
}

@Composable
fun Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    ShadcnTheme(
        isDarkTheme = darkTheme,
        shadcnLightColors = KCloudLightShadcnColors,
        shadcnDarkColors = KCloudDarkShadcnColors,
        materialLightColors = LightColorScheme,
        materialDarkColors = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
