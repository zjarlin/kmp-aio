# liquid-glass

Apple 风格液态玻璃 Compose Multiplatform 组件库，提供开箱即用的按钮、卡片、树形侧边栏和液态材质宿主。

- Maven coordinate: `site.addzero:liquid-glass`
- Local module path: `lib/compose/liquid-glass`

## Components

- `LiquidGlassRoot`
- `LiquidGlassButton`
- `LiquidGlassIconButton`
- `LiquidGlassCard`
- `LiquidGlassSidebarMenu`
- `LiquidGlassSidebarItem`

## Usage

```kotlin
import site.addzero.liquidglass.LiquidGlassButton
import site.addzero.liquidglass.LiquidGlassCard
import site.addzero.liquidglass.LiquidGlassCardHeader
import site.addzero.liquidglass.LiquidGlassRoot

LiquidGlassRoot(
    background = {
        // 背景内容
    },
) {
    LiquidGlassCard {
        LiquidGlassCardHeader(
            title = "Liquid Glass",
            subtitle = "Compose 组件库",
        )
        LiquidGlassButton(
            text = "Primary",
            onClick = {},
        )
    }
}
```

## Notes

- 目前依赖 `io.github.fletchmckee.liquid:liquid`
- 组件代码放在 `commonMain`，适合 Compose Multiplatform 复用
