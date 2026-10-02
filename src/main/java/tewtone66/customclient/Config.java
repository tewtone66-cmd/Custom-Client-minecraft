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

    public boolean particles = true;
    public boolean weather = true;
    public boolean hitIndicator = true;
    public boolean cpsHud = true;
    public boolean pingHud = true;
    public boolean potionHud = true;
    public boolean targetHud = false;
    public boolean customCrosshair = true;
    public boolean sprintToggle = false;
    public boolean animatedBackground = true;
    public boolean glassUi = true;
    public boolean cosmetics = true;

    public int fpsLimit = 120;
    public int uiScale = 100;
    public int accent = 0xFF2B8CFF;

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
            case "particles" -> particles = !particles;
            case "weather" -> weather = !weather;
            case "hit" -> hitIndicator = !hitIndicator;
            case "cps" -> cpsHud = !cpsHud;
            case "ping" -> pingHud = !pingHud;
            case "potion" -> potionHud = !potionHud;
            case "target" -> targetHud = !targetHud;
            case "crosshair" -> customCrosshair = !customCrosshair;
            case "sprint" -> sprintToggle = !sprintToggle;
            case "animated" -> animatedBackground = !animatedBackground;
            case "glass" -> glassUi = !glassUi;
            case "cosmetics" -> cosmetics = !cosmetics;
        }
    }
}
