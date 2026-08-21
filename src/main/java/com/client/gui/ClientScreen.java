package com.client.gui;

import com.client.ClientCore;
import com.client.module.Module;
import com.client.module.ModuleCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClientScreen extends Screen {
    private ModuleCategory selectedCategory = ModuleCategory.PERFORMANCE;

    public ClientScreen() { super(Component.literal("Custom Client")); }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        renderBackground(g, mouseX, mouseY, delta);

        int x = 35, y = 25, w = width - 70, h = height - 50;
        g.fill(x, y, x + w, y + h, 0xE0181818);
        g.drawString(font, "CUSTOM CLIENT", x + 16, y + 14, 0xFFFFFFFF);

        int cy = y + 42;
        for (ModuleCategory c : ModuleCategory.values()) {
            boolean selected = c == selectedCategory;
            g.fill(x + 10, cy - 3, x + 105, cy + 14, selected ? 0xFF3A5FFF : 0xFF242424);
            g.drawString(font, c.getName(), x + 18, cy, 0xFFFFFFFF);
            cy += 22;
        }

        int mx = x + 125, my = y + 42;
        List<Module> modules = ClientCore.getInstance().getModuleManager().getModulesByCategory(selectedCategory);
        for (Module m : modules) {
            g.fill(mx, my, mx + 220, my + 30, 0xFF252525);
            g.drawString(font, m.getName(), mx + 10, my + 7, 0xFFFFFFFF);
            g.drawString(font, m.isEnabled() ? "ON" : "OFF", mx + 180, my + 7,
                    m.isEnabled() ? 0xFF55FF88 : 0xFFAAAAAA);
            my += 36;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = 35, y = 25;
        int cy = y + 42;
        for (ModuleCategory c : ModuleCategory.values()) {
            if (mouseX >= x + 10 && mouseX <= x + 105 && mouseY >= cy - 3 && mouseY <= cy + 14) {
                selectedCategory = c;
                return true;
            }
            cy += 22;
        }

        int mx = x + 125, my = y + 42;
        for (Module m : ClientCore.getInstance().getModuleManager().getModulesByCategory(selectedCategory)) {
            if (mouseX >= mx && mouseX <= mx + 220 && mouseY >= my && mouseY <= my + 30) {
                m.toggle();
                ClientCore.getInstance().getConfigManager().saveConfig();
                return true;
            }
            my += 36;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }
             }
      
