package tewtone66.customclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Items;

public final class Hud {
    private Hud() {}

    public static void render(GuiGraphics g) {
        Config c = ClientCore.CONFIG;
        if (!c.hudEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui || mc.screen != null) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int pad = c.compactHud ? 5 : 8;

        if (c.fpsHud) {
            panel(g, pad, pad, 86, 34, 0xB0101420);
            g.drawString(mc.font, "§bTEW§fPVP", pad + 7, pad + 5, 0xFFFFFF, false);
            g.drawString(mc.font, "§f" + mc.getFps() + " FPS", pad + 7, pad + 18, 0xFFFFFF, false);
        }

        if (c.coords) {
            String xyz = String.format("XYZ §f%d §7/ §f%d §7/ §f%d", (int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ());
            panel(g, pad, h - 25, Math.min(180, mc.font.width(xyz) + 14), 19, 0xB0101420);
            g.drawString(mc.font, xyz, pad + 7, h - 20, 0xFFFFFF, false);
        }

        if (c.itemCounters) {
            int x = w - 112;
            panel(g, x, pad, 104, 57, 0xB0101420);
            g.drawString(mc.font, "§dCrystal §f" + count(Items.END_CRYSTAL), x + 7, pad + 7, 0xFFFFFF, false);
            g.drawString(mc.font, "§6Totem §f" + count(Items.TOTEM_OF_UNDYING), x + 7, pad + 20, 0xFFFFFF, false);
            g.drawString(mc.font, "§8Obsidian §f" + count(Items.OBSIDIAN), x + 7, pad + 33, 0xFFFFFF, false);
        }

        if (c.keystrokes) {
            int x = w - 112;
            int y = h - 76;
            panel(g, x, y, 104, 69, 0xB0101420);
            key(g, mc, "W", x + 39, y + 6, mc.options.keyUp.isDown());
            key(g, mc, "A", x + 6, y + 29, mc.options.keyLeft.isDown());
            key(g, mc, "S", x + 39, y + 29, mc.options.keyDown.isDown());
            key(g, mc, "D", x + 72, y + 29, mc.options.keyRight.isDown());
            key(g, mc, "SPC", x + 34, y + 52, mc.options.keyJump.isDown());
        }
    }

    private static int count(net.minecraft.world.item.Item item) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        int total = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    private static void key(GuiGraphics g, Minecraft mc, String text, int x, int y, boolean down) {
        int bg = down ? 0xFF2B8CFF : 0xCC202838;
        g.fill(x, y, x + 28, y + 19, bg);
        int tw = mc.font.width(text);
        g.drawString(mc.font, text, x + (28 - tw) / 2, y + 6, 0xFFFFFF, false);
    }

    private static void panel(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x + 2, y + 2, x + w + 2, y + h + 2, 0x55000000);
        g.fill(x, y, x + w, y + h, color);
        g.fill(x, y, x + w, y + 1, 0xFF2B8CFF);
    }
}
