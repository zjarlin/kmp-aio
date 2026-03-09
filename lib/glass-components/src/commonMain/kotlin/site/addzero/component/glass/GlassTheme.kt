package site.addzero.component.glass

import androidx.compose.ui.graphics.Color

/**
 * GlassTheme — 水玻璃设计系统的颜色与 Token 定义
 *
 * 提供统一的颜色调色板，包含深色背景、玻璃表面、霓虹强调色和文字颜色。
 * 所有颜色值与现有 GlassColors 保持兼容，同时扩展了更完整的 Token 体系。
 *
 * @see GlassColors 向后兼容的颜色别名
 */
object GlassTheme {

    // ── 背景层 ──────────────────────────────────────────────
    /** 最深层背景色，用于应用主背景 */
    val DarkBackground = Color(0xFF0F0F13)

    /** 次级深色表面，用于卡片/面板底层（保证 Desktop JVM 无模糊时的文字可读性） */
    val DarkSurface = Color(0xFF1E1E26)

    // ── 玻璃表面 ────────────────────────────────────────────
    /** 玻璃表面色 — 10% 白色半透明 */
    val GlassSurface = Color(0x1AFFFFFF)

    /** 玻璃表面悬停态 — 15% 白色半透明 */
    val GlassSurfaceHover = Color(0x26FFFFFF)

    /** 玻璃边框色 — 25% 白色半透明 */
    val GlassBorder = Color(0x40FFFFFF)

    /** 玻璃阴影色 */
    val GlassShadow = Color(0x20000000)

    // ── 无色水玻璃 Token ───────────────────────────────────
    /** 无色液态玻璃主表面（中性白） */
    val WaterSurfacePrimary = Color(0x14FFFFFF)

    /** 无色液态玻璃次表面（更浅） */
    val WaterSurfaceSecondary = Color(0x0DFFFFFF)

    /** 边缘折射高光（强） */
    val WaterRefractionEdgeStrong = Color(0xCCFFFFFF)

    /** 边缘折射高光（弱） */
    val WaterRefractionEdgeSoft = Color(0x55FFFFFF)

    /** 内缘折射线（用于双层边缘） */
    val WaterRefractionInner = Color(0x33FFFFFF)

    // ── JetBrains 紫色系 ────────────────────────────────────
    /** JetBrains 主紫色 */
    val JBPurple = Color(0xFF6B57FF)

    /** JetBrains 深紫底色（侧边栏底层） */
    val JBPurpleDark = Color(0xFF1A1525)

    /** JetBrains 紫色表面（半透明叠加层） */
    val JBPurpleSurface = Color(0xFF7B68EE)

    /** JetBrains 紫色选中高亮 */
    val JBPurpleHighlight = Color(0xFF8B78FF)

    // ── 霓虹强调色 ──────────────────────────────────────────
    /** 霓虹青色 */
    val NeonCyan = Color(0xFF00F0FF)

    /** 霓虹紫色 */
    val NeonPurple = Color(0xFFBD00FF)

    /** 霓虹品红 */
    val NeonMagenta = Color(0xFFFF0055)

    /** 霓虹粉色 */
    val NeonPink = Color(0xFFFF00AA)

    // ── 文字颜色 ────────────────────────────────────────────
    /** 主要文字 — 纯白 */
    val TextPrimary: Color = Color.White

    /** 次要文字 — 80% 白 */
    val TextSecondary: Color = Color.White.copy(alpha = 0.8f)

    /** 三级文字 — 60% 白 */
    val TextTertiary: Color = Color.White.copy(alpha = 0.6f)

    /** 禁用态文字 — 40% 白 */
    val TextDisabled: Color = Color.White.copy(alpha = 0.4f)
}
