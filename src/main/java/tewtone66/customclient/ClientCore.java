package tewtone66.customclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientCore implements ClientModInitializer {
    public static final String MOD_ID = "customclient";
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "main"));
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Config CONFIG = new Config();
    private static KeyMapping openGui;

    @Override
    public void onInitializeClient() {
        openGui = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.customclient.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.consumeClick()) {
                if (client.screen == null) client.setScreen(new ClientScreen());
            }
        });

        HudRenderCallback.EVENT.register((graphics, delta) -> Hud.render(graphics));
        LOGGER.info("TewPvP Custom Client initialized for Minecraft 1.21.11");
    }

    public static void notify(Minecraft client, String message) {
        if (client.player != null) client.player.displayClientMessage(Component.literal("§bTewPvP §8» §f" + message), true);
    }
}
