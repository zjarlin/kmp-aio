package site.addzero.kcloud.window.spi

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

interface KCloudBrandSlotSpi {
    // [旗帜位] 顶栏最左边的一小块，像门头招牌，通常放品牌名或产品身份。
    @Composable
    fun RowScope.Render()
}
