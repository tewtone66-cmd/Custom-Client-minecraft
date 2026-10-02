# TewPvP Custom Client

An original lightweight Fabric client-side mod for **Minecraft Java 1.21.11**, designed around PvP HUD customization, a clean animated settings screen, and low-overhead overlays.

## Included

- Animated TewPvP click/settings GUI
- FPS display
- Coordinates HUD
- Keystrokes HUD
- Crystal, Totem and Obsidian inventory counters
- Toggleable HUD components
- Compact HUD mode
- Client-only architecture with no remote code loading or telemetry
- Right Shift opens the menu

## Build

Use Java 21 and Gradle with Fabric Loom Remap for Minecraft 1.21.11.

```bash
./gradlew build
```

The built JAR will be in `build/libs/`.

## Design

This is an original implementation and does not include proprietary Badlion or Lunar code/assets.

## Compatibility

Target: Minecraft Java **1.21.11** + Fabric Loader/Fabric API.
