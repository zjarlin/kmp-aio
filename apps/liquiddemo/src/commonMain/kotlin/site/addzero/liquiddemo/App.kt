package site.addzero.liquiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CropLandscape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.liquiddemo.components.LiquidGlassButton
import site.addzero.liquiddemo.components.LiquidGlassButtonStyle
import site.addzero.liquiddemo.components.LiquidGlassCard
import site.addzero.liquiddemo.components.LiquidGlassCardHeader
import site.addzero.liquiddemo.components.LiquidGlassDefaults
import site.addzero.liquiddemo.components.LiquidGlassRoot
import site.addzero.liquiddemo.components.LiquidGlassSidebarItem
import site.addzero.liquiddemo.components.LiquidGlassSidebarMenu
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

    Row(
        modifier = modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        LiquidGlassSidebarMenu(
            title = "Liquid Library",
            items = pages.map {
                LiquidGlassSidebarItem(
                    id = it.id,
                    title = it.title,
                    subtitle = it.subtitle,
                    icon = it.icon,
                )
            },
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
                spec = LiquidGlassDefaults.heroCard,
            ) {
                LiquidGlassCardHeader(
                    title = "Apple-Inspired Liquid Glass",
                    subtitle = "按 Apple 官方的“rounded floating forms / lensing / monochrome controls”思路，把参数预调成默认可用的高阶材质。",
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
            .background(
                Brush.linearGradient(LiquidGlassDefaults.backgroundPalette),
            ),
    ) {
        Box(
            modifier = Modifier
                .padding(start = 88.dp, top = 72.dp)
                .size(320.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
                .blur(96.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 120.dp, end = 128.dp)
                .size(240.dp)
                .clip(CircleShape)
                .background(LiquidGlassDefaults.accentStrong.copy(alpha = 0.18f))
                .blur(86.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 160.dp, bottom = 140.dp)
                .width(420.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.Black.copy(alpha = 0.18f))
                .blur(72.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 180.dp)
                .width(300.dp)
                .height(300.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF8ACB).copy(alpha = 0.14f))
                .blur(120.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 120.dp, bottom = 110.dp)
                .width(340.dp)
                .height(140.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFF72F4FF).copy(alpha = 0.12f))
                .blur(98.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 36.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Liquid Material Kit",
                color = LiquidGlassDefaults.textPrimary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "开箱即用的 Button / Card / SidebarMenu，高阶材质参数已默认调优。",
                color = LiquidGlassDefaults.textSecondary,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
