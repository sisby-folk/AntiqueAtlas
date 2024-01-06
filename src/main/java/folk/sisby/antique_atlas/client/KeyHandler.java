package folk.sisby.antique_atlas.client;

import folk.sisby.antique_atlas.AntiqueAtlasClient;
import folk.sisby.antique_atlas.client.gui.GuiAtlas;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;


@Environment(EnvType.CLIENT)
public class KeyHandler {
    public static final KeyBinding ATLAS_KEYMAPPING = new KeyBinding("key.openatlas.desc", InputUtil.Type.KEYSYM, 77, "key.antique_atlas.category");

    public static void registerBindings() {
        KeyBindingHelper.registerKeyBinding(ATLAS_KEYMAPPING);
    }

    public static void onClientTick(MinecraftClient client) {
        while (ATLAS_KEYMAPPING.wasPressed()) {
            Screen currentScreen = MinecraftClient.getInstance().currentScreen;
            if (currentScreen instanceof GuiAtlas) {
                currentScreen.close();
            } else {
                AntiqueAtlasClient.openAtlasGUI();
            }
        }
    }
}
