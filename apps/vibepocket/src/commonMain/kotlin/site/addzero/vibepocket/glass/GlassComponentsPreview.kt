package site.addzero.vibepocket.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.addzero.component.glass.*
import site.addzero.ioc.annotation.Bean

val color = GlassColors.DarkBackground

/**
 * 玻璃按钮预览
 */
@Preview(name = "Glass Button", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
@Bean(tags = ["screen"])
fun GlassButtonPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            GlassButton(
                text = "Click Me",
                onClick = {}
            )
            GlassButton(
                text = "Disabled",
                onClick = {},
                enabled = false
            )
        }
    }
}

/**
 * 霓虹玻璃按钮预览
 */
@Preview(name = "Neon Glass Button", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun NeonGlassButtonPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            NeonGlassButton(
                text = "Neon Cyan",
                onClick = {},
                glowColor = GlassColors.NeonCyan
            )
            NeonGlassButton(
                text = "Neon Purple",
                onClick = {},
                glowColor = GlassColors.NeonPurple
            )
            NeonGlassButton(
                text = "Neon Magenta",
                onClick = {},
                glowColor = GlassColors.NeonMagenta
            )
        }
    }
}

/**
 * 液体玻璃按钮预览
 */
@Preview(name = "Liquid Glass Button", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun LiquidGlassButtonPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            LiquidGlassButton(
                text = "Liquid Glass",
                onClick = {}
            )
            LiquidGlassButton(
                text = "Disabled",
                onClick = {},
                enabled = false
            )
        }
    }
}

/**
 * 玻璃卡片预览
 */
@Preview(name = "Glass Card", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun GlassCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(

            modifier = Modifier
                .width(300.dp)
                .height(150.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Glass Card",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Beautiful glass effect",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 霓虹玻璃卡片预览
 */
@Preview(name = "Neon Glass Card", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun NeonGlassCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            NeonGlassCard(
                modifier = Modifier
                    .width(280.dp)
                    .height(120.dp),
                glowColor = GlassColors.NeonCyan
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Neon Cyan",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
            NeonGlassCard(
                modifier = Modifier
                    .width(280.dp)
                    .height(120.dp),
                glowColor = GlassColors.NeonPurple
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Neon Purple",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * 液体玻璃卡片预览
 */
@Preview(name = "Liquid Glass Card", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun LiquidGlassCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .width(300.dp)
                .height(150.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Liquid Glass",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Smooth gradient effect",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 信息卡片预览
 */
@Preview(name = "Glass Info Card", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun GlassInfoCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        GlassInfoCard(
            title = "Welcome",
            content = "This is a glass info card with title and content. It provides a beautiful way to display information.",
            modifier = Modifier
                .width(300.dp)
        )
    }
}

/**
 * 统计卡片预览
 */
@Preview(name = "Glass Stat Card", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun GlassStatCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            GlassStatCard(
                value = "1,234",
                label = "Users",
                modifier = Modifier
                    .width(120.dp)
                    .height(100.dp),
                glowColor = GlassColors.NeonCyan
            )
            GlassStatCard(
                value = "5.6K",
                label = "Views",
                modifier = Modifier
                    .width(120.dp)
                    .height(100.dp),
                glowColor = GlassColors.NeonPurple
            )
            GlassStatCard(
                value = "89%",
                label = "Growth",
                modifier = Modifier
                    .width(120.dp)
                    .height(100.dp),
                glowColor = GlassColors.NeonMagenta
            )
        }
    }
}

/**
 * 功能卡片预览
 */
@Preview(name = "Glass Feature Card", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun GlassFeatureCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        GlassFeatureCard(
            title = "Fast Performance",
            description = "Optimized for speed and efficiency with smooth animations and transitions.",
            modifier = Modifier
                .width(300.dp),
            primaryColor = GlassColors.NeonPurple,
            secondaryColor = GlassColors.NeonCyan,
            icon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(GlassColors.NeonCyan.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 20.sp)
                }
            }
        )
    }
}

/**
 * 玻璃输入框预览
 */
@Preview(name = "Glass TextField", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun GlassTextFieldPreview() {
    var text by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp)
        ) {
            GlassTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = "Enter text...",
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Input: $text",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 搜索框预览
 */
@Preview(name = "Glass Search Field", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun GlassSearchFieldPreview() {
    var searchText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        GlassSearchField(
            value = searchText,
            onValueChange = { searchText = it },
            onSearch = {},
            modifier = Modifier
                .width(300.dp)
        )
    }
}

/**
 * 多行文本框预览
 */
@Preview(name = "Glass TextArea", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun GlassTextAreaPreview() {
    var text by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.TopCenter
    ) {
        GlassTextArea(
            value = text,
            onValueChange = { text = it },
            placeholder = "Enter your message...",
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp)
        )
    }
}

/**
 * 完整组件展示预览
 */
@Preview(name = "Glass Components Showcase", showBackground = true, backgroundColor = 0xFF0F0F13)
@Composable
fun GlassComponentsShowcasePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Text(
                text = "Glass Components",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 按钮组
            Text(
                text = "Buttons",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassButton(text = "Basic", onClick = {})
                NeonGlassButton(text = "Neon", onClick = {})
                LiquidGlassButton(text = "Liquid", onClick = {})
            }

            // 卡片组
            Text(
                text = "Cards",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("Basic Glass Card", color = Color.White)
                }
            }

            // 输入框
            Text(
                text = "Input Fields",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            var inputText by remember { mutableStateOf("") }
            GlassTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = "Type something...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
