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
- `LiquidButton`
- `LiquidBottomTab`
- `LiquidBottomTabs`
- `LiquidSlider`
- `LiquidToggle`

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
- 已将 AndroidLiquidGlass 所需的 `backdrop` runtime 源码内置到当前模块，避免外部镜像仓库缺包导致无法编译
- 额外依赖 `io.github.kyant0:shapes`，用于迁入 AndroidLiquidGlass 风格控制组件
- 组件代码放在 `commonMain`，适合 Compose Multiplatform 复用
