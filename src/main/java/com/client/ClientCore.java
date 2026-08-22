package com.client;

import com.client.config.ConfigManager;
import com.client.gui.ClientScreen;
import com.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientCore implements ClientModInitializer {
    public static final String MOD_ID = "client";
    public static final Logger LOGGER = LoggerFactory.getLogger("CustomClient");

    private static ClientCore instance;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Starting Custom Client Architecture...");

        this.moduleManager = new ModuleManager();
        this.configManager = new ConfigManager();

        this.moduleManager.init();
        this.configManager.loadConfig();

        try {
            // ساخت دکمه بر اساس ساختار جدید KeyBinding.Category در 1.21.11
            openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.client.open_gui",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_RIGHT_SHIFT,
                    KeyBinding.Category.MISC
            ));
        } catch (Throwable t) {
            LOGGER.error("Failed to register keybinding: ", t);
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey != null && client != null) {
                while (openGuiKey.wasPressed()) {
                    if (client.currentScreen == null) {
                        client.setScreen(new ClientScreen());
                    }
                }
            }
        });

        LOGGER.info("Client initialization complete!");
    }

    public static ClientCore getInstance() {
        return instance;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
