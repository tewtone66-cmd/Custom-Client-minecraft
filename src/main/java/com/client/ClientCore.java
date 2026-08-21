package com.client;

import com.client.config.ConfigManager;
import com.client.gui.ClientScreen;
import com.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
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
    private KeyMapping openGuiKey;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Starting Custom Client Architecture...");

        this.moduleManager = new ModuleManager();
        this.configManager = new ConfigManager();

        this.moduleManager.init();
        this.configManager.loadConfig();

        try {
            openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                    "key.client.open_gui",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_RIGHT_SHIFT,
                    "category.client.general"
            ));
        } catch (Exception e) {
            LOGGER.error("Failed to register keybinding: ", e);
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey != null && client.getWindow() != null) {
                while (openGuiKey.consumeClick()) {
                    if (client.screen == null) {
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
