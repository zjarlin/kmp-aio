package site.addzero.liquiddemo.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import site.addzero.liquiddemo.DemoWallpaperUrl

@Composable
fun LiquidScreen(
    modifier: Modifier = Modifier,
    liquidState: LiquidState = rememberLiquidState(),
) {
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = DemoWallpaperUrl,
            contentDescription = null,
            modifier = Modifier.liquidWallpaperSource(liquidState),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = liquidDemoPanel(liquidState),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Liquid Screen",
                color = Color.White.copy(alpha = 0.96f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "这里保留一个最小可运行示例：直接用真实壁纸做液态取样源，上层浮一块液态面板。",
                color = Color.White.copy(alpha = 0.76f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** 液态取样源，整张壁纸都参与玻璃折射。 */
private fun Modifier.liquidWallpaperSource(
    liquidState: LiquidState,
): Modifier = fillMaxSize()
    .liquefiable(liquidState)

/** 左上液态演示面板，集中展示原始 liquid 参数效果。 */
private fun BoxScope.liquidDemoPanel(
    liquidState: LiquidState,
): Modifier = Modifier.align(Alignment.TopStart)
    .padding(32.dp)
    .width(320.dp)
    .liquid(liquidState) {
        frost = 12.dp
        shape = RoundedCornerShape(28.dp)
        refraction = 0.62f
        curve = 0.44f
        edge = 0.03f
        tint = Color.White.copy(alpha = 0.10f)
        saturation = 1.12f
        dispersion = 0.08f
    }
    .clip(RoundedCornerShape(28.dp))
    .background(Color.White.copy(alpha = 0.04f))
    .padding(horizontal = 22.dp, vertical = 20.dp)
