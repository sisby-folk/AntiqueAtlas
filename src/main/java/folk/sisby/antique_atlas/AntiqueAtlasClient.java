package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.client.gui.GuiAtlas;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class AntiqueAtlasClient implements ClientModInitializer {

    private static GuiAtlas guiAtlas;

    public static GuiAtlas getAtlasGUI() {
        if (guiAtlas == null) {
            guiAtlas = new GuiAtlas();
            guiAtlas.setMapScale(AntiqueAtlas.CONFIG.defaultScale);
        }
        return guiAtlas;
    }

    public static void openAtlasGUI() {
        openAtlasGUI(getAtlasGUI().prepareToOpen());
    }

    private static void openAtlasGUI(GuiAtlas gui) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen == null) { // In-game screen
            guiAtlas.updateL18n();
            mc.setScreen(gui);
        }
    }

    @Override
    public void onInitializeClient() {
        ClientProxy clientProxy = new ClientProxy();
        clientProxy.initClient();

        AntiqueAtlasNetworking.registerS2CListeners();
    }
}
