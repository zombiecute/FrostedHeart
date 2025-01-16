package com.teammoeg.frostedheart.bootstrap.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class FHKeyMappings {
    public static Lazy<KeyMapping> key_skipDialog = Lazy.of(() -> new KeyMapping("key.frostedheart.skip_dialog",
        GLFW.GLFW_KEY_Z, "key.categories.frostedheart"));
    public static Lazy<KeyMapping> key_InfraredView = Lazy.of(() -> new KeyMapping("key.frostedheart.infrared_view",
            GLFW.GLFW_KEY_I, "key.categories.frostedheart"));
    public static Lazy<KeyMapping> key_health = Lazy.of(() -> new KeyMapping("key.frostedheart.health",
            GLFW.GLFW_KEY_H, "key.categories.frostedheart"));

    public static void init() {

    }

    public static boolean hasSDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_S);
    }

}
