package com.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClientScreen extends Screen {

    public ClientScreen() {
        super(Component.literal("Custom Client Menu"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // کشیدن پس‌زمینه نیمه‌شفاف تاریک
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        // رسم متن در مرکز صفحه
        guiGraphics.drawCenteredString(
                this.font,
                "=== CUSTOM CLIENT GUI ===",
                this.width / 2,
                this.height / 2 - 10,
                0xFFFFFF
        );

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // بازی حین باز بودن منو استپ نشود
    }
}
