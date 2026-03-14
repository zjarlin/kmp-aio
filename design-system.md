# VibePocket Waterglass Design System

## Direction

VibePocket is being rebuilt around a waterglass visual language:

- less neon cyberpunk, more refracted liquid light
- bright glass layers over a deep blue stage
- soft aqua and coral accents instead of purple-heavy gradients
- large rounded geometry, thicker panels, fewer tiny borders
- typography that feels editorial, not dashboard-default

The goal is to make Vibepocket feel like a creative instrument panel instead of a generic admin system.

## Visual Tokens

### Core palette

- `midnight`: app background base
- `abyss`: secondary background depth
- `panelTop`: glass highlight for upper surface
- `panelBottom`: lower diffusion tint
- `panelEdge`: outer border and refraction rim
- `ink`: primary foreground text
- `inkSoft`: secondary copy
- `inkMuted`: placeholder and tertiary labels
- `aqua`: primary action accent
- `glacier`: cool secondary accent
- `coral`: warm counter-accent
- `sunrise`: soft warm highlight for hero moments

### Geometry

- frame radius: 38dp
- panel radius: 30dp
- control radius: 22dp
- pill radius: fully rounded

### Spacing rhythm

- dense controls stay on 10dp/12dp gaps
- panels breathe at 20dp to 24dp padding
- screen sections separate at 18dp to 24dp

## Components

### Foundation

- `VibeGlassAppTheme`
- `VibeGlassBackdrop`
- `VibeGlassPanel`
- `VibeGlassScreenPanel`

### Inputs and actions

- `VibeGlassTextField`
- `VibeGlassButton`
- `VibeGlassChoiceChip`

### Navigation

- `VibeGlassWorkspace`
- `VibeGlassNavItem`

### Informational

- `VibeGlassTag`
- `VibeGlassMetricCard`
- `VibeGlassEmptyState`

## Migration Map

- old `GlassCard` -> new `VibeGlassPanel`
- old `NeonGlassButton` -> new `VibeGlassButton(style = Primary)`
- old `GlassButton` -> new `VibeGlassButton(style = Ghost or Secondary)`
- old `GlassTextField` -> new `VibeGlassTextField`
- old sidebar shell -> new `VibeGlassWorkspace`

## Page Rules

- every major screen should sit inside one large glass panel, not many small unrelated boxes
- the page header must carry intent: title, short explanation, then actions
- avoid emoji-only decoration unless it improves scanability
- use aqua for the main action, coral only as supporting emphasis
- keep copy short and product-like, not placeholder-heavy

## Figma Mapping Notes

When a Figma source becomes available, map these code components 1:1 to Figma components of the same role:

- shell/sidebar
- hero panel
- form field
- action button
- empty state
- tab chip

Figma exports should be treated as visual references; keep Kotlin Compose structure aligned to these tokens instead of copying raw generated styling.
