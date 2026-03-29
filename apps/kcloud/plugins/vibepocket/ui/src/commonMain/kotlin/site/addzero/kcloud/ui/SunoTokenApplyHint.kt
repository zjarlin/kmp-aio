package site.addzero.kcloud.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import site.addzero.kcloud.api.suno.SunoApiClient

@Composable
fun SunoTokenApplyHint(
    modifier: Modifier = Modifier,
    intro: String = "还没申请过 Suno API Token？",
    introStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    introColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    linkText: String = "前往 sunoapi.org 控制台申请 Token",
    linkStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    linkColor: Color = MaterialTheme.colorScheme.primary,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = intro,
            style = introStyle,
            color = introColor,
        )
        Text(
            text = linkText,
            modifier = Modifier.clickable {
                uriHandler.openUri(SunoApiClient.TOKEN_DASHBOARD_URL)
            },
            style = linkStyle,
            color = linkColor,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline,
        )
    }
}
