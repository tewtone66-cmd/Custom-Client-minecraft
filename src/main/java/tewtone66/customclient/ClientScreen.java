package tewtone66.customclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class ClientScreen extends Screen {
    private static final int BG = 0xFF080C16;
    private static final int CARD = 0xFF1A2436;
    private static final int CARD_HOVER = 0xFF24334B;
    private static final int MUTED = 0xFF91A0B8;
    private static final int WHITE = 0xFFF4F7FF;
    private static final int BLUE = 0xFF2B8CFF;
    private static final int GREEN = 0xFF38D996;
    private static final int RED = 0xFFFF5C73;

    private enum Tab {
        HOME, HUD, PERFORMANCE, MODS, PACKS, SCHEMATICS, COSMETICS, SUPPORT
    }

    private Tab tab = Tab.HOME;
    private EditBox urlBox;
    private int left;
    private int top;
    private int contentX;
    private int contentY;
    private int contentW;
    private int contentH;
    private long ticks;

    public ClientScreen() {
        super(Component.literal("TewPvP Lobby"));
    }

    @Override
    protected void init() {
        left = 18;
        top = 18;
        contentX = 196;
        contentY = 66;
        contentW = Math.max(360, width - contentX - 18);
        contentH = Math.max(260, height - contentY - 18);

        urlBox = new EditBox(font, contentX + 18, contentY + contentH - 42,
                Math.min(520, contentW - 155), 22, Component.literal("Download URL"));
        urlBox.setSuggestion("https://cdn.modrinth.com/...");
        urlBox.setMaxLength(500);
        urlBox.setVisible(tab == Tab.MODS || tab == Tab.PACKS || tab == Tab.SCHEMATICS);
        addRenderableWidget(urlBox);
    }

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        renderAnimatedLobby(g);
        drawShell(g);
        drawSidebar(g, mouseX, mouseY);
        drawContent(g, mouseX, mouseY);
    }

    private void renderAnimatedLobby(GuiGraphics g) {
        g.fill(0, 0, width, height, BG);
        if (!ClientCore.CONFIG.animatedBackground) return;

        int wave = (int) ((Math.sin(ticks * 0.035) + 1.0) * 18);
        int x = (int) ((Math.sin(ticks * 0.018) + 1) * Math.max(1, width - 160) / 2);
        int y = (int) ((Math.cos(ticks * 0.022) + 1) * Math.max(1, height - 100) / 2);
        g.fill(x, 0, Math.min(width, x + 160), height, 0x081A66FF);
        g.fill(0, y, width, Math.min(height, y + 100), 0x0600C8FF);
        g.fill(0, 0, width, 3 + wave / 6, 0x182B8CFF);
    }

    private void drawShell(GuiGraphics g) {
        int alpha = ClientCore.CONFIG.glassUi ? 0xE6101624 : 0xFF101722;
        g.fill(left, top, width - left, height - top, alpha);
        g.fill(left, top, width - left, top + 3, BLUE);
        g.fill(left + 174, top + 3, left + 175, height - top, 0xFF26344A);

        g.drawString(font, "TEW", left + 18, top + 18, BLUE, true);
        g.drawString(font, "PVP", left + 50, top + 18, WHITE, true);
        g.drawString(font, "CUSTOM CLIENT", left + 18, top + 34, MUTED, false);

        String status = Minecraft.getInstance().getFps() + " FPS  •  1.21.11";
        g.drawString(font, status, width - left - font.width(status) - 18, top + 18, WHITE, false);
    }

    private void drawSidebar(GuiGraphics g, int mx, int my) {
        String[] names = {"Lobby", "HUD", "Performance", "Mods", "Resource Packs", "Schematics", "Cosmetics", "Support"};
        Tab[] tabs = Tab.values();

        int y = top + 62;
        for (int i = 0; i < names.length; i++) {
            boolean selected = tab == tabs[i];
            boolean hover = inside(mx, my, left + 10, y, 154, 30);
            int color = selected ? 0xFF1F3554 : (hover ? CARD_HOVER : 0x00141B2A);
            g.fill(left + 10, y, left + 164, y + 30, color);
            if (selected) g.fill(left + 10, y, left + 13, y + 30, BLUE);
            g.drawString(font, names[i], left + 22, y + 10, selected ? WHITE : MUTED, false);
            y += 34;
        }

        g.drawString(font, "CLIENT STATUS", left + 18, height - 76, MUTED, false);
        g.drawString(font, "● ONLINE", left + 18, height - 59, GREEN, true);
        g.drawString(font, "Right Shift  •  Open", left + 18, height - 41, MUTED, false);
    }

    private void drawContent(GuiGraphics g, int mx, int my) {
        String title = switch (tab) {
            case HOME -> "Lobby";
            case HUD -> "HUD Studio";
            case PERFORMANCE -> "Performance";
            case MODS -> "Mod Manager";
            case PACKS -> "Resource Pack Manager";
            case SCHEMATICS -> "Schematic Manager";
            case COSMETICS -> "Cosmetics Lab";
            case SUPPORT -> "Support";
        };

        g.drawString(font, title, contentX + 18, top + 20, WHITE, true);
        g.drawString(font, subtitle(), contentX + 18, top + 37, MUTED, false);

        switch (tab) {
            case HOME -> home(g, mx, my);
            case HUD -> hud(g, mx, my);
            case PERFORMANCE -> performance(g, mx, my);
            case MODS -> mods(g, mx, my);
            case PACKS -> packs(g, mx, my);
            case SCHEMATICS -> schematics(g, mx, my);
            case COSMETICS -> cosmetics(g, mx, my);
            case SUPPORT -> support(g, mx, my);
        }
    }

    private String subtitle() {
        return switch (tab) {
            case HOME -> "Your custom Minecraft lobby — fast, clean and expandable.";
            case HUD -> "PvP HUD modules and layout controls.";
            case PERFORMANCE -> "Reduce visual overhead and tune the client.";
            case MODS -> "Install Fabric mods into your mods folder.";
            case PACKS -> "Download packs and reload them without restarting.";
            case SCHEMATICS -> "Keep .litematic, .schem and .schematic files organized.";
            case COSMETICS -> "Cosmetic previews and client-only style options.";
            case SUPPORT -> "Diagnostics, safety and project information.";
        };
    }

    private void home(GuiGraphics g, int mx, int my) {
        card(g, contentX + 18, contentY, contentW - 36, 92, mx, my);
        g.drawString(font, "Welcome to TewPvP", contentX + 34, contentY + 18, WHITE, true);
        g.drawString(font, "Original client UI • no proprietary Lunar/Badlion assets", contentX + 34, contentY + 36, MUTED, false);
        g.drawString(font, "Use the sidebar to configure modules, downloads and cosmetics.", contentX + 34, contentY + 53, MUTED, false);

        button(g, "HUD Studio", contentX + 18, contentY + 108, 150, 32, mx, my, BLUE);
        button(g, "Performance", contentX + 176, contentY + 108, 150, 32, mx, my, GREEN);
        button(g, "Mod Manager", contentX + 334, contentY + 108, 150, 32, mx, my, BLUE);

        int y = contentY + 160;
        stat(g, "FPS HUD", ClientCore.CONFIG.fpsHud, contentX + 18, y);
        stat(g, "Keystrokes", ClientCore.CONFIG.keystrokes, contentX + 188, y);
        stat(g, "Counters", ClientCore.CONFIG.itemCounters, contentX + 358, y);
        stat(g, "Animated UI", ClientCore.CONFIG.animatedBackground, contentX + 528, y);

        card(g, contentX + 18, y + 62, contentW - 36, 88, mx, my);
        g.drawString(font, "Quick Actions", contentX + 34, y + 80, WHITE, true);
        g.drawString(font, "Right Shift", contentX + 34, y + 99, BLUE, false);
        g.drawString(font, "opens this lobby from gameplay.", contentX + 105, y + 99, MUTED, false);
        g.drawString(font, "Resource packs can reload immediately after download.", contentX + 34, y + 119, GREEN, false);
        g.drawString(font, "New Fabric mods are installed for the next launch.", contentX + 34, y + 137, MUTED, false);
    }

    private void hud(GuiGraphics g, int mx, int my) {
        String[] keys = {"hud","fps","keys","armor","counter","coords","cps","ping","potion","target","hit","crosshair"};
        String[] labels = {"Master HUD","FPS","Keystrokes","Armor HUD","Item Counters","Coordinates","CPS","Ping","Potion Status","Target HUD","Hit Indicator","Custom Crosshair"};
        for (int i = 0; i < keys.length; i++) option(g, labels[i], keys[i], contentY + i * 34, mx, my);
    }

    private void performance(GuiGraphics g, int mx, int my) {
        String[] keys = {"perf","particles","weather","compact","animated","glass"};
        String[] labels = {"Performance Mode","Particles","Weather","Compact HUD","Animated Background","Glass UI"};
        for (int i = 0; i < keys.length; i++) option(g, labels[i], keys[i], contentY + i * 34, mx, my);

        int boxY = contentY + 222;
        card(g, contentX + 18, boxY, contentW - 36, 112, mx, my);
        g.drawString(font, "FPS Limit", contentX + 34, boxY + 20, WHITE, true);
        g.drawString(font, ClientCore.CONFIG.fpsLimit + " FPS", contentX + 34, boxY + 43, BLUE, true);
        button(g, "60", contentX + 140, boxY + 15, 58, 28, mx, my, BLUE);
        button(g, "120", contentX + 204, boxY + 15, 58, 28, mx, my, BLUE);
        button(g, "240", contentX + 268, boxY + 15, 58, 28, mx, my, BLUE);
        button(g, "Unlimited", contentX + 332, boxY + 15, 82, 28, mx, my, BLUE);
        g.drawString(font, "UI Scale: " + ClientCore.CONFIG.uiScale + "%", contentX + 34, boxY + 74, MUTED, false);
        button(g, "-", contentX + 190, boxY + 61, 30, 26, mx, my, BLUE);
        button(g, "+", contentX + 226, boxY + 61, 30, 26, mx, my, BLUE);
    }

    private void mods(GuiGraphics g, int mx, int my) {
        managerHeader(g, "Fabric Mod Center", "Downloads are stored in .minecraft/mods.");
        downloadCard(g, "Sodium", "Performance / rendering", "sodium-fabric-0.8.12+mc1.21.11.jar",
                contentY + 92, mx, my);
        downloadCard(g, "Mod Menu", "Installed-mod manager", "modmenu-17.0.0.jar",
                contentY + 166, mx, my);
        drawUrlDownload(g, "mod", "Download Mod URL", mx, my);
    }

    private void packs(GuiGraphics g, int mx, int my) {
        managerHeader(g, "Resource Pack Center", "Packs are stored in .minecraft/resourcepacks.");
        downloadPackCard(g, "Marlowww+", "Clean CPvP 32x pack", "Marlowww+-1.21.11.zip", contentY + 92, mx, my);
        drawUrlDownload(g, "resourcepack", "Download Resource Pack URL", mx, my);
    }

    private void schematics(GuiGraphics g, int mx, int my) {
        managerHeader(g, "Schematic Library", "Supports .litematic, .schem and .schematic.");
        Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve("schematics");
        long count = 0;
        try {
            if (Files.exists(dir)) {
                try (var stream = Files.list(dir)) {
                    count = stream.filter(p -> {
                        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        return n.endsWith(".litematic") || n.endsWith(".schem") || n.endsWith(".schematic");
                    }).count();
                }
            }
        } catch (Exception ignored) {}
        g.drawString(font, "Local schematics: " + count, contentX + 34, contentY + 105, BLUE, false);
        g.drawString(font, "Downloads are saved for compatible schematic mods to use.", contentX + 34, contentY + 124, MUTED, false);
        drawUrlDownload(g, "schematic", "Download Schematic URL", mx, my);
    }

    private void managerHeader(GuiGraphics g, String title, String line) {
        card(g, contentX + 18, contentY, contentW - 36, 64, -1, -1);
        g.drawString(font, title, contentX + 34, contentY + 15, WHITE, true);
        g.drawString(font, line, contentX + 34, contentY + 35, MUTED, false);
    }

    private void downloadCard(GuiGraphics g, String name, String desc, String file, int y, int mx, int my) {
        card(g, contentX + 18, y, contentW - 36, 62, mx, my);
        g.fill(contentX + 30, y + 12, contentX + 66, y + 48, 0xFF203A62);
        g.drawString(font, name.substring(0, 1), contentX + 43, y + 23, WHITE, true);
        g.drawString(font, name, contentX + 78, y + 12, WHITE, true);
        g.drawString(font, desc, contentX + 78, y + 29, MUTED, false);
        g.drawString(font, file, contentX + 78, y + 44, 0xFF71809A, false);
        button(g, "INSTALL", contentX + contentW - 112, y + 18, 82, 25, mx, my, BLUE);
    }

    private void downloadPackCard(GuiGraphics g, String name, String desc, String file, int y, int mx, int my) {
        card(g, contentX + 18, y, contentW - 36, 62, mx, my);
        g.fill(contentX + 30, y + 12, contentX + 66, y + 48, 0xFF294A3C);
        g.drawString(font, "P", contentX + 43, y + 23, WHITE, true);
        g.drawString(font, name, contentX + 78, y + 12, WHITE, true);
        g.drawString(font, desc, contentX + 78, y + 29, MUTED, false);
        g.drawString(font, file, contentX + 78, y + 44, 0xFF71809A, false);
        button(g, "DOWNLOAD", contentX + contentW - 124, y + 18, 94, 25, mx, my, GREEN);
    }

    private void cosmetics(GuiGraphics g, int mx, int my) {
        card(g, contentX + 18, contentY, contentW - 36, 92, mx, my);
        g.drawString(font, "Cosmetics Lab", contentX + 34, contentY + 18, WHITE, true);
        g.drawString(font, "Client-only cosmetic previews. No gameplay advantage.", contentX + 34, contentY + 38, MUTED, false);
        g.drawString(font, "Current style: " + (ClientCore.CONFIG.cosmetics ? "Enabled" : "Disabled"), contentX + 34, contentY + 57, BLUE, false);
        button(g, ClientCore.CONFIG.cosmetics ? "Disable Cosmetics" : "Enable Cosmetics",
                contentX + 34, contentY + 66, 150, 26, mx, my, BLUE);

        String[] cosmetics = {"Neon Crosshair", "Blue Trail", "Minimal Hit Marker", "Lobby Glow"};
        int y = contentY + 112;
        for (int i = 0; i < cosmetics.length; i++) {
            int bx = contentX + 18 + (i % 2) * ((contentW - 52) / 2);
            int by = y + (i / 2) * 62;
            card(g, bx, by, (contentW - 52) / 2 - 10, 50, mx, my);
            g.drawString(font, "◆", bx + 16, by + 17, BLUE, true);
            g.drawString(font, cosmetics[i], bx + 32, by + 17, WHITE, false);
        }
    }

    private void support(GuiGraphics g, int mx, int my) {
        card(g, contentX + 18, contentY, contentW - 36, 112, mx, my);
        g.drawString(font, "Support & Diagnostics", contentX + 34, contentY + 18, WHITE, true);
        g.drawString(font, "Send the crash log and Minecraft/Fabric versions if something breaks.", contentX + 34, contentY + 39, MUTED, false);
        g.drawString(font, "Safe mode: disable the last downloaded mod and restart.", contentX + 34, contentY + 58, MUTED, false);
        g.drawString(font, "TewPvP Custom Client • Minecraft 1.21.11", contentX + 34, contentY + 79, BLUE, false);
        button(g, "Create Diagnostics Folder", contentX + 34, contentY + 88, 174, 28, mx, my, BLUE);

        card(g, contentX + 18, contentY + 130, contentW - 36, 74, mx, my);
        g.drawString(font, "Important", contentX + 34, contentY + 148, RED, true);
        g.drawString(font, "Only install files you trust. The manager rejects unapproved hosts.", contentX + 34, contentY + 169, MUTED, false);
        g.drawString(font, "No telemetry or remote code execution is built into the manager.", contentX + 34, contentY + 187, MUTED, false);
    }

    private void drawUrlDownload(GuiGraphics g, String type, String label, int mx, int my) {
        int y = contentY + contentH - 74;
        g.drawString(font, label, contentX + 18, y - 18, MUTED, false);
        button(g, "DOWNLOAD", contentX + contentW - 112, y - 2, 94, 24, mx, my, GREEN);
    }

    private void stat(GuiGraphics g, String label, boolean on, int x, int y) {
        g.fill(x, y, x + 150, y + 48, CARD);
        g.drawString(font, label, x + 10, y + 9, MUTED, false);
        g.drawString(font, on ? "ON" : "OFF", x + 10, y + 27, on ? GREEN : RED, true);
    }

    private void option(GuiGraphics g, String label, String key, int y, int mx, int my) {
        int x = contentX + 18;
        int w = Math.min(520, contentW - 36);
        boolean on = switch (key) {
            case "hud" -> ClientCore.CONFIG.hudEnabled;
            case "fps" -> ClientCore.CONFIG.fpsHud;
            case "keys" -> ClientCore.CONFIG.keystrokes;
            case "armor" -> ClientCore.CONFIG.armorHud;
            case "counter" -> ClientCore.CONFIG.itemCounters;
            case "coords" -> ClientCore.CONFIG.coords;
            case "perf" -> ClientCore.CONFIG.performanceMode;
            case "compact" -> ClientCore.CONFIG.compactHud;
            case "particles" -> ClientCore.CONFIG.particles;
            case "weather" -> ClientCore.CONFIG.weather;
            case "hit" -> ClientCore.CONFIG.hitIndicator;
            case "cps" -> ClientCore.CONFIG.cpsHud;
            case "ping" -> ClientCore.CONFIG.pingHud;
            case "potion" -> ClientCore.CONFIG.potionHud;
            case "target" -> ClientCore.CONFIG.targetHud;
            case "crosshair" -> ClientCore.CONFIG.customCrosshair;
            case "animated" -> ClientCore.CONFIG.animatedBackground;
            case "glass" -> ClientCore.CONFIG.glassUi;
            default -> false;
        };
        boolean hover = inside(mx, my, x, y, w, 28);
        g.fill(x, y, x + w, y + 28, hover ? CARD_HOVER : CARD);
        g.drawString(font, label, x + 12, y + 9, WHITE, false);
        g.drawString(font, on ? "ON" : "OFF", x + w - 38, y + 9, on ? GREEN : RED, true);
    }

    private void card(GuiGraphics g, int x, int y, int w, int h, int mx, int my) {
        boolean hover = mx >= 0 && inside(mx, my, x, y, w, h);
        g.fill(x + 2, y + 2, x + w + 2, y + h + 2, 0x44000000);
        g.fill(x, y, x + w, y + h, hover ? CARD_HOVER : CARD);
    }

    private void button(GuiGraphics g, String text, int x, int y, int w, int h, int mx, int my, int accent) {
        boolean hover = inside(mx, my, x, y, w, h);
        g.fill(x, y, x + w, y + h, hover ? accent : 0xFF1D2B40);
        g.drawString(font, text, x + (w - font.width(text)) / 2, y + (h - 8) / 2, WHITE, true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        if (event.button() != 0) return false;

        double mx = event.x();
        double my = event.y();

        String[] names = {"Lobby", "HUD", "Performance", "Mods", "Resource Packs", "Schematics", "Cosmetics", "Support"};
        Tab[] tabs = Tab.values();
        int y = top + 62;
        for (int i = 0; i < names.length; i++) {
            if (inside(mx, my, left + 10, y, 154, 30)) {
                tab = tabs[i];
                urlBox.setVisible(tab == Tab.MODS || tab == Tab.PACKS || tab == Tab.SCHEMATICS);
                return true;
            }
            y += 34;
        }

        if (tab == Tab.HOME) {
            if (inside(mx, my, contentX + 18, contentY + 108, 150, 32)) tab = Tab.HUD;
            else if (inside(mx, my, contentX + 176, contentY + 108, 150, 32)) tab = Tab.PERFORMANCE;
            else if (inside(mx, my, contentX + 334, contentY + 108, 150, 32)) tab = Tab.MODS;
            urlBox.setVisible(false);
            return true;
        }

        if (tab == Tab.HUD) return clickOption(mx, my, new String[]{"hud","fps","keys","armor","counter","coords","cps","ping","potion","target","hit","crosshair"}, contentY);

        if (tab == Tab.PERFORMANCE) {
            if (clickOption(mx, my, new String[]{"perf","particles","weather","compact","animated","glass"}, contentY)) return true;
            int boxY = contentY + 222;
            if (inside(mx, my, contentX + 140, boxY + 15, 58, 28)) ClientCore.CONFIG.fpsLimit = 60;
            else if (inside(mx, my, contentX + 204, boxY + 15, 58, 28)) ClientCore.CONFIG.fpsLimit = 120;
            else if (inside(mx, my, contentX + 268, boxY + 15, 58, 28)) ClientCore.CONFIG.fpsLimit = 240;
            else if (inside(mx, my, contentX + 332, boxY + 15, 82, 28)) ClientCore.CONFIG.fpsLimit = 1000;
            else if (inside(mx, my, contentX + 190, boxY + 61, 30, 26)) ClientCore.CONFIG.uiScale = Math.max(75, ClientCore.CONFIG.uiScale - 5);
            else if (inside(mx, my, contentX + 226, boxY + 61, 30, 26)) ClientCore.CONFIG.uiScale = Math.min(125, ClientCore.CONFIG.uiScale + 5);
            return true;
        }

        if (tab == Tab.MODS) {
            if (inside(mx, my, contentX + contentW - 112, contentY + 110, 82, 25)) {
                DownloadManager.downloadMod("https://cdn.modrinth.com/data/AANobbMI/versions/NFkjnzWE/sodium-fabric-0.8.12%2Bmc1.21.11.jar", "sodium-fabric-0.8.12+mc1.21.11.jar");
            } else if (inside(mx, my, contentX + contentW - 112, contentY + 184, 82, 25)) {
                DownloadManager.downloadMod("https://cdn.modrinth.com/data/mOgUt4GM/versions/Tyk71iSw/modmenu-17.0.0.jar", "modmenu-17.0.0.jar");
            } else if (inside(mx, my, contentX + contentW - 112, contentY + contentH - 76, 94, 24)) {
                downloadFromBox("mod");
            }
            return true;
        }

        if (tab == Tab.PACKS) {
            if (inside(mx, my, contentX + contentW - 124, contentY + 110, 94, 25)) {
                DownloadManager.downloadResourcePack("https://cdn.modrinth.com/data/roQwWt6y/versions/F5ruBnE0/Vanilla%2B%201.21.11.zip", "Marlowww+-1.21.11.zip");
            } else if (inside(mx, my, contentX + contentW - 112, contentY + contentH - 76, 94, 24)) {
                downloadFromBox("resourcepack");
            }
            return true;
        }

        if (tab == Tab.SCHEMATICS) {
            if (inside(mx, my, contentX + contentW - 112, contentY + contentH - 76, 94, 24)) downloadFromBox("schematic");
            return true;
        }

        if (tab == Tab.COSMETICS && inside(mx, my, contentX + 34, contentY + 66, 150, 26)) {
            ClientCore.CONFIG.cosmetics = !ClientCore.CONFIG.cosmetics;
            return true;
        }

        if (tab == Tab.SUPPORT && inside(mx, my, contentX + 34, contentY + 88, 174, 28)) {
            try {
                Files.createDirectories(Minecraft.getInstance().gameDirectory.toPath().resolve("tewpvp-diagnostics"));
                ClientCore.notify(Minecraft.getInstance(), "پوشه diagnostics ساخته شد.");
            } catch (Exception ignored) {}
            return true;
        }

        return true;
    }

    private boolean clickOption(double mx, double my, String[] keys, int startY) {
        int y = startY;
        for (String key : keys) {
            if (inside(mx, my, contentX + 18, y, Math.min(520, contentW - 36), 28)) {
                ClientCore.CONFIG.toggle(key);
                return true;
            }
            y += 34;
        }
        return false;
    }

    private void downloadFromBox(String type) {
        String url = urlBox.getValue().trim();
        if (url.isBlank()) {
            ClientCore.notify(Minecraft.getInstance(), "اول لینک دانلود را وارد کن.");
            return;
        }

        String lower = url.toLowerCase(Locale.ROOT);
        String fileName = lower.substring(lower.lastIndexOf('/') + 1);
        int query = fileName.indexOf('?');
        if (query >= 0) fileName = fileName.substring(0, query);
        if (fileName.isBlank()) fileName = "downloaded-file";

        if ("mod".equals(type) && !fileName.endsWith(".jar")) fileName += ".jar";
        if ("resourcepack".equals(type) && !fileName.endsWith(".zip")) fileName += ".zip";
        if ("schematic".equals(type) && !(fileName.endsWith(".litematic") || fileName.endsWith(".schem") || fileName.endsWith(".schematic"))) {
            ClientCore.notify(Minecraft.getInstance(), "پسوند شماتیک معتبر نیست.");
            return;
        }

        DownloadManager.downloadCustom(url, fileName, type);
        urlBox.setValue("");
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
