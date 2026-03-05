package site.addzero.vibepocket

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kyant.backdrop.backdrops.rememberLayerBackdrop


@Composable
@Preview
fun App() {
    val backdrop = rememberLayerBackdrop()
    GlassSlider(backdrop)
LiquidButton()
}
