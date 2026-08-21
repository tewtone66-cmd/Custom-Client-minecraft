package com.client.module;

public enum ModuleCategory {
    PERFORMANCE("Performance"), PVP("PvP"), HUD("HUD"), VISUAL("Visual"),
    WORLD("World"), UTILITY("Utility"), MISC("Misc");

    private final String name;
    ModuleCategory(String name) { this.name = name; }
    public String getName() { return name; }
}
