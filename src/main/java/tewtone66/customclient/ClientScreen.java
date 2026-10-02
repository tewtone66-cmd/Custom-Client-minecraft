package tewtone66.customclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClientScreen extends Screen {
    private int panelX, panelY, panelW, panelH;
    private int anim;

    public ClientScreen() { super(Component.literal("TewPvP Custom Client")); }

    @Override protected void init() {
        panelW = Math.min(620, width - 40);
        panelH = Math.min(420, height - 40);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        anim = 0;
    }

    @Override public void tick() { if (anim < 12) anim++; }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // Draw our own dim background instead of calling Screen.renderBackground().
        g.fill(0, 0, width, height, 0xA0101018);
        int slide = 12 - Math.min(12, anim);
        int x = panelX, y = panelY + slide;
        g.fill(x + 4, y + 5, x + panelW + 4, y + panelH + 5, 0x66000000);
        g.fill(x, y, x + panelW, y + panelH, 0xF0101420);
        g.fill(x, y, x + panelW, y + 3, 0xFF2B8CFF);
        g.drawString(font, "§bTEW§fPVP", x + 20, y + 18, 0xFFFFFF, false);
        g.drawString(font, "§7Original PvP / FPS client", x + 20, y + 32, 0xFFFFFF, false);

        int col1 = x + 20, col2 = x + panelW / 2 + 8;
        toggle(g, "HUD", "hud", col1, y + 62, mouseX, mouseY);
        toggle(g, "FPS HUD", "fps", col1, y + 94, mouseX, mouseY);
        toggle(g, "Keystrokes", "keys", col1, y + 126, mouseX, mouseY);
        toggle(g, "Armor HUD", "armor", col1, y + 158, mouseX, mouseY);
        toggle(g, "Item Counters", "counter", col2, y + 62, mouseX, mouseY);
        toggle(g, "Coordinates", "coords", col2, y + 94, mouseX, mouseY);
        toggle(g, "Performance Mode", "perf", col2, y + 126, mouseX, mouseY);
        toggle(g, "Compact HUD", "compact", col2, y + 158, mouseX, mouseY);

        g.drawString(font, "§8Features", x + 20, y + 214, 0xFFFFFF, false);
        g.drawString(font, "§7• Animated click GUI", x + 20, y + 234, 0xFFFFFF, false);
        g.drawString(font, "§7• Crystal / Totem / Obsidian counters", x + 20, y + 250, 0xFFFFFF, false);
        g.drawString(font, "§7• FPS + coordinates + keystrokes", x + 20, y + 266, 0xFFFFFF, false);
        g.drawString(font, "§7• Lightweight client-side design", x + 20, y + 282, 0xFFFFFF, false);
        g.drawString(font, "§7Right Shift to open • ESC to close", x + 20, y + panelH - 24, 0xFFFFFF, false);
    }

    private void toggle(GuiGraphics g, String label, String key, int x, int y, int mx, int my) {
        boolean on = switch (key) {
            case "hud" -> ClientCore.CONFIG.hudEnabled;
            case "fps" -> ClientCore.CONFIG.fpsHud;
            case "keys" -> ClientCore.CONFIG.keystrokes;
            case "armor" -> ClientCore.CONFIG.armorHud;
            case "counter" -> ClientCore.CONFIG.itemCounters;
            case "coords" -> ClientCore.CONFIG.coords;
            case "perf" -> ClientCore.CONFIG.performanceMode;
            case "compact" -> ClientCore.CONFIG.compactHud;
            default -> false;
        };
        boolean hover = mx >= x && mx <= x + 260 && my >= y && my <= y + 25;
        g.fill(x, y, x + 260, y + 25, hover ? 0xFF242E42 : 0xFF1A2130);
        g.drawString(font, label, x + 10, y + 8, 0xFFFFFF, false);
        g.drawString(font, on ? "§aON" : "§cOFF", x + 226, y + 8, 0xFFFFFF, false);
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button != 0) return super.mouseClicked(event, doubleClick);
        int y = panelY;
        int x1 = panelX + 20, x2 = panelX + panelW / 2 + 8;
        String key = null;
        int[] ys = {y + 62, y + 94, y + 126, y + 158};
        String[] left = {"hud", "fps", "keys", "armor"};
        String[] right = {"counter", "coords", "perf", "compact"};
        for (int i = 0; i < 4; i++) {
            if (inside(mouseX, mouseY, x1, ys[i], 260, 25)) key = left[i];
            if (inside(mouseX, mouseY, x2, ys[i], 260, 25)) key = right[i];
        }
        if (key != null) { ClientCore.CONFIG.toggle(key); return true; }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }

    @Override public void onClose() { Minecraft.getInstance().setScreen(null); }
    @Override public boolean isPauseScreen() { return false; }
}
