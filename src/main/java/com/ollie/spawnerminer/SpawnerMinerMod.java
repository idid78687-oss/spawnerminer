package com.ollie.spawnerminer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
public class SpawnerMinerMod implements ClientModInitializer {
    public static KeyBinding menuKey;
    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.spawnerminer.menu", InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_INSERT, "category.spawnerminer"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null)
                    client.setScreen(new SpawnerMinerScreen());
            }
            SpawnerMinerDetector.tick(client);
        });
    }
}
