package site.addzero.vibepocket.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.addzero.component.glass.GlassColors
import site.addzero.component.glass.GlassTheme

@Composable
fun StepIndicator(currentStep: VibeStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step 1
        StepDot(
            number = "1",
            label = "歌词",
            isActive = true,
            isCurrent = currentStep == VibeStep.LYRICS
        )

        // 连接线
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(
                    if (currentStep == VibeStep.PARAMS) GlassColors.NeonCyan
                    else Color.White.copy(alpha = 0.2f)
                )
        )

        // Step 2
        StepDot(
            number = "2",
            label = "Vibe",
            isActive = currentStep == VibeStep.PARAMS,
            isCurrent = currentStep == VibeStep.PARAMS
        )
    }
}

@Composable
private fun StepDot(number: String, label: String, isActive: Boolean, isCurrent: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isCurrent) GlassColors.NeonCyan
                    else if (isActive) GlassColors.NeonCyan.copy(alpha = 0.6f)
                    else Color.White.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = if (isActive) Color.Black else GlassTheme.TextTertiary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isCurrent) GlassColors.NeonCyan else GlassTheme.TextTertiary,
            fontSize = 11.sp
        )
    }
}
