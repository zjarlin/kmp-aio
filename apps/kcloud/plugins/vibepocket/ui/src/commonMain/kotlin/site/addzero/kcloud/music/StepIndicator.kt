package site.addzero.kcloud.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Surface
import site.addzero.cupertino.workbench.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StepIndicator(currentStep: VibeStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepDot(
            number = "1",
            label = "歌词",
            isActive = true,
            isCurrent = currentStep == VibeStep.LYRICS,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    if (currentStep == VibeStep.PARAMS) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
        )

        StepDot(
            number = "2",
            label = "Vibe",
            isActive = currentStep == VibeStep.PARAMS,
            isCurrent = currentStep == VibeStep.PARAMS,
        )
    }
}

@Composable
private fun StepDot(
    number: String,
    label: String,
    isActive: Boolean,
    isCurrent: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = when {
                isCurrent -> MaterialTheme.colorScheme.primary
                isActive -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                isCurrent -> MaterialTheme.colorScheme.onPrimary
                isActive -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isCurrent) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
