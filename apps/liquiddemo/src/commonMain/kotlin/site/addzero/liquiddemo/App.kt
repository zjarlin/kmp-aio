package site.addzero.liquiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CropLandscape
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import site.addzero.liquiddemo.components.*
import site.addzero.liquiddemo.demos.ButtonDemo
import site.addzero.liquiddemo.demos.CardDemo
import site.addzero.liquiddemo.demos.SidebarMenuDemo

private data class ShowcasePage(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val content: @Composable (Modifier) -> Unit,
)

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF050816),
        ) {
            LiquidGlassRoot(
                modifier = Modifier.fillMaxSize(),
                background = { DemoBackground() },
            ) {
                ShowcaseShell(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun ShowcaseShell(
    modifier: Modifier = Modifier,
) {
    val pages = remember {
        listOf(
            ShowcasePage(
                id = "buttons",
                title = "Buttons",
                subtitle = "高阶胶囊按钮",
                icon = Icons.Rounded.AutoAwesome,
                content = { ButtonDemo(it) },
            ),
            ShowcasePage(
                id = "cards",
                title = "Cards",
                subtitle = "高阶卡片材质",
                icon = Icons.Rounded.CropLandscape,
                content = { CardDemo(it) },
            ),
            ShowcasePage(
                id = "sidebar",
                title = "Sidebar",
                subtitle = "侧边菜单组件",
                icon = Icons.AutoMirrored.Rounded.MenuOpen,
                content = { SidebarMenuDemo(it) },
            ),
        )
    }
    var selectedPageId by remember { mutableStateOf("buttons") }
    val selectedPage = pages.first { it.id == selectedPageId }
    val sidebarItems = remember(pages) {
        listOf(
            LiquidGlassSidebarItem(
                id = "controls",
                title = "Controls",
                subtitle = "交互组件",
                icon = Icons.Rounded.Folder,
                selectable = false,
                children = listOf(
                    LiquidGlassSidebarItem(
                        id = "buttons",
                        title = "Buttons",
                        subtitle = "高阶胶囊按钮",
                        icon = Icons.Rounded.AutoAwesome,
                    ),
                ),
            ),
            LiquidGlassSidebarItem(
                id = "surfaces",
                title = "Surfaces",
                subtitle = "材质组件",
                icon = Icons.Rounded.Folder,
                selectable = false,
                children = listOf(
                    LiquidGlassSidebarItem(
                        id = "cards",
                        title = "Cards",
                        subtitle = "高阶卡片材质",
                        icon = Icons.Rounded.CropLandscape,
                    ),
                    LiquidGlassSidebarItem(
                        id = "sidebar",
                        title = "Sidebar",
                        subtitle = "树形侧边菜单",
                        icon = Icons.AutoMirrored.Rounded.MenuOpen,
                    ),
                ),
            ),
        )
    }

    Row(
        modifier = modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        LiquidGlassSidebarMenu(
            title = "Liquid Library",
            items = sidebarItems,
            selectedId = selectedPageId,
            modifier = Modifier.width(280.dp),
            onSelect = { selectedPageId = it },
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                LiquidGlassCardHeader(
                    title = "Apple-Inspired Liquid Glass",
                    subtitle = "现在背景已经换成你给的壁纸，重点是观察卡片与侧边栏在真实纹理上的折射、内高光和边缘通透度。",
                    badge = "Showcase",
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LiquidGlassButton(
                        text = "Primary Action",
                        icon = Icons.Rounded.AutoAwesome,
                        style = LiquidGlassButtonStyle.Primary,
                        onClick = {},
                    )
                    LiquidGlassButton(
                        text = selectedPage.title,
                        icon = selectedPage.icon,
                        onClick = {},
                    )
                }
            }

            selectedPage.content(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DemoBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(liquidWallpaperBasePalette())),
    ) {
        AsyncImage(
            model = DemoWallpaperUrl,
            contentDescription = null,
            modifier = wallpaperBaseImage(),
            contentScale = ContentScale.Crop,
        )
        AsyncImage(
            model = DemoWallpaperUrl,
            contentDescription = null,
            modifier = wallpaperDepthImage(),
            contentScale = ContentScale.Crop,
        )
        Box(modifier = wallpaperToneOverlay())

        WallpaperGlow(
            modifier = wallpaperTopLeftGlow(),
            colors = listOf(
                Color.White.copy(alpha = 0.16f),
                Color(0xFFBFE6FF).copy(alpha = 0.08f),
                Color.Transparent,
            ),
            blur = 150.dp,
        )
        WallpaperGlow(
            modifier = wallpaperTopRightGlow(),
            colors = listOf(
                Color(0xFF7DA9FF).copy(alpha = 0.12f),
                Color(0xFFB07DFF).copy(alpha = 0.08f),
                Color.Transparent,
            ),
            blur = 150.dp,
        )
        WallpaperGlow(
            modifier = wallpaperCenterRightGlow(),
            colors = listOf(
                Color(0xFF69E6FF).copy(alpha = 0.10f),
                Color(0xFF6B7DFF).copy(alpha = 0.06f),
                Color.Transparent,
            ),
            blur = 170.dp,
            shape = RoundedCornerShape(56.dp),
        )
        WallpaperGlow(
            modifier = wallpaperBottomRightGlow(),
            colors = listOf(
                Color(0xFF895FFF).copy(alpha = 0.14f),
                Color(0xFF5BE8FF).copy(alpha = 0.06f),
                Color.Transparent,
            ),
            blur = 170.dp,
        )
        WallpaperGlow(
            modifier = wallpaperBottomShadow(),
            colors = listOf(
                Color.Black.copy(alpha = 0.22f),
                Color.Black.copy(alpha = 0.10f),
                Color.Transparent,
            ),
            blur = 120.dp,
            shape = RoundedCornerShape(999.dp),
        )
        WallpaperPlane(
            modifier = wallpaperTopPlane(),
            colors = listOf(
                Color.White.copy(alpha = 0.08f),
                Color(0xFF98D9FF).copy(alpha = 0.04f),
                Color.Transparent,
            ),
            blur = 14.dp,
        )
        WallpaperPlane(
            modifier = wallpaperBottomPlane(),
            colors = listOf(
                Color.Transparent,
                Color(0xFFC8F0FF).copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.08f),
            ),
            blur = 14.dp,
        )
        WallpaperGlow(
            modifier = wallpaperBottomSpecular(),
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.Transparent,
            ),
            blur = 46.dp,
            shape = RoundedCornerShape(999.dp),
        )
        Box(modifier = wallpaperVignette())

        Column(
            modifier = wallpaperTitleBlock(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Liquid Material Kit",
                color = Color.White.copy(alpha = 0.94f),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "现在底图直接采样你给的壁纸，更适合观察玻璃材质是否够透、够像水。",
                color = Color.White.copy(alpha = 0.74f),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private fun liquidWallpaperBasePalette() = listOf(
    Color(0xFF040816),
    Color(0xFF0A1530),
    Color(0xFF102443),
    Color(0xFF233F73),
    Color(0xFF4C45AA),
)

/** 壁纸原图层，保留真实纹理与层次。 */
private fun BoxScope.wallpaperBaseImage(): Modifier = Modifier.fillMaxSize()
    .alpha(0.38f)

/** 壁纸虚化层，给玻璃折射提供更柔和的采样背景。 */
private fun BoxScope.wallpaperDepthImage(): Modifier = Modifier.fillMaxSize()
    .alpha(0.20f)
    .blur(28.dp)

/** 深色统一色调层，压住原图杂色，让玻璃材质更干净。 */
private fun BoxScope.wallpaperToneOverlay(): Modifier = Modifier.fillMaxSize()
    .background(
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF030713).copy(alpha = 0.74f),
                Color(0xFF08152D).copy(alpha = 0.58f),
                Color(0xFF183666).copy(alpha = 0.42f),
                Color(0xFF2C2B78).copy(alpha = 0.44f),
            ),
        ),
    )

/** 左上主背光，模拟壁纸远处的大面积光斑。 */
private fun BoxScope.wallpaperTopLeftGlow(): Modifier = Modifier.align(Alignment.TopStart)
    .offset(x = (-120).dp, y = (-90).dp)
    .size(520.dp)

/** 右上冷暖混合背光，用来拉开顶部层次。 */
private fun BoxScope.wallpaperTopRightGlow(): Modifier = Modifier.align(Alignment.TopEnd)
    .offset(x = 56.dp, y = (-16).dp)
    .size(500.dp)

/** 中右透光层，让玻璃折射看起来更有“水感”。 */
private fun BoxScope.wallpaperCenterRightGlow(): Modifier = Modifier.align(Alignment.CenterEnd)
    .offset(x = 84.dp, y = 18.dp)
    .width(640.dp)
    .height(520.dp)

/** 右下主色光斑，负责紫蓝色的景深氛围。 */
private fun BoxScope.wallpaperBottomRightGlow(): Modifier = Modifier.align(Alignment.BottomEnd)
    .offset(x = 24.dp, y = 96.dp)
    .size(560.dp)

/** 底部前景暗场，避免界面漂在一整片亮雾上。 */
private fun BoxScope.wallpaperBottomShadow(): Modifier = Modifier.align(Alignment.BottomStart)
    .offset(x = 90.dp, y = 84.dp)
    .width(620.dp)
    .height(250.dp)

/** 顶部斜向透光片，模拟壁纸里的镜头高光。 */
private fun BoxScope.wallpaperTopPlane(): Modifier = Modifier.align(Alignment.TopCenter)
    .offset(y = (-6).dp)
    .width(860.dp)
    .height(220.dp)
    .rotate(-12f)

/** 底部斜向反射片，给大面积玻璃提供更自然的采样层次。 */
private fun BoxScope.wallpaperBottomPlane(): Modifier = Modifier.align(Alignment.BottomEnd)
    .offset(x = 48.dp, y = 44.dp)
    .width(760.dp)
    .height(210.dp)
    .rotate(-15f)

/** 右下细长高光条，模拟掠过边缘的镜面反射。 */
private fun BoxScope.wallpaperBottomSpecular(): Modifier = Modifier.align(Alignment.BottomEnd)
    .offset(x = (-20).dp, y = (-12).dp)
    .width(300.dp)
    .height(92.dp)

/** 全局暗角层，用来统一画面边缘密度。 */
private fun BoxScope.wallpaperVignette(): Modifier = Modifier.fillMaxSize()
    .background(
        Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFF020510).copy(alpha = 0.16f),
                Color(0xFF01030B).copy(alpha = 0.38f),
            ),
        ),
    )

/** 左下标题区定位，让信息稳定压在前景。 */
private fun BoxScope.wallpaperTitleBlock(): Modifier = Modifier.align(Alignment.BottomStart)
    .padding(start = 36.dp, bottom = 28.dp)

@Composable
private fun BoxScope.WallpaperGlow(
    modifier: Modifier,
    colors: List<Color>,
    blur: Dp,
    shape: Shape = CircleShape,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.radialGradient(colors))
            .blur(blur),
    )
}

@Composable
private fun BoxScope.WallpaperPlane(
    modifier: Modifier,
    colors: List<Color>,
    blur: Dp,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Brush.linearGradient(colors))
            .blur(blur),
    )
}
