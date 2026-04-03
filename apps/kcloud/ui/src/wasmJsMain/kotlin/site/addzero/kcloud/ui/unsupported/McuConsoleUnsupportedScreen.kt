package site.addzero.kcloud.ui.unsupported

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
internal fun McuConsoleUnsupportedScreen(
    featureName: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$featureName 暂不支持浏览器端",
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}
