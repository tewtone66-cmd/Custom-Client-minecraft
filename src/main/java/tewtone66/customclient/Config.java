package tewtone66.customclient;

public final class Config {
    public boolean hudEnabled = true;
    public boolean fpsHud = true;
    public boolean keystrokes = true;
    public boolean armorHud = true;
    public boolean itemCounters = true;
    public boolean coords = true;
    public boolean performanceMode = false;
    public boolean compactHud = false;

    public void toggle(String key) {
        switch (key) {
            case "hud" -> hudEnabled = !hudEnabled;
            case "fps" -> fpsHud = !fpsHud;
            case "keys" -> keystrokes = !keystrokes;
            case "armor" -> armorHud = !armorHud;
            case "counter" -> itemCounters = !itemCounters;
            case "coords" -> coords = !coords;
            case "perf" -> performanceMode = !performanceMode;
            case "compact" -> compactHud = !compactHud;
        }
    }
}
