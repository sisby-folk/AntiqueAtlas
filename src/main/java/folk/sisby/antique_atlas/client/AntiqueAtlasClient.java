package folk.sisby.antique_atlas.client;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.gui.GuiAtlas;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.Map;

public class AntiqueAtlasClient implements ClientModInitializer {
    private static GuiAtlas guiAtlas;

    public static GuiAtlas getAtlasGUI() {
        if (guiAtlas == null) {
            guiAtlas = new GuiAtlas();
            guiAtlas.setMapScale(AntiqueAtlas.CONFIG.Interface.defaultScale);
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

    /**
     * Assign default textures to biomes defined in the client world, but
     * not part of the BuiltinRegistries.BIOME. This happens for all biomes
     * defined in data packs. Also, as these are only available per world,
     * we need the ClientWorld loaded here.
     */
    public static void assignCustomBiomeTextures(ClientWorld world) {
        for (Map.Entry<RegistryKey<Biome>, Biome> biome : BuiltinRegistries.BIOME.getEntrySet()) {
            Identifier id = BuiltinRegistries.BIOME.getId(biome.getValue());
            if (!TileTextureMap.instance().isRegistered(id)) {
                TileTextureMap.instance().autoRegister(id, biome.getKey());
            }
        }

        for (Map.Entry<RegistryKey<Biome>, Biome> entry : world.getRegistryManager().get(Registry.BIOME_KEY).getEntrySet()) {
            Identifier id = world.getRegistryManager().get(Registry.BIOME_KEY).getId(entry.getValue());
            if (!TileTextureMap.instance().isRegistered(id)) {
                TileTextureMap.instance().autoRegister(id, entry.getKey());
            }
        }
    }


    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new TextureConfig(Textures.TILE_TEXTURES_MAP));
        TextureSetMap textureSetMap = TextureSetMap.instance();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new TextureSetConfig(textureSetMap));
        TileTextureMap tileTextureMap = TileTextureMap.instance();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new TileTextureConfig(tileTextureMap, textureSetMap));
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new MarkerMipsConfig());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new MarkerTextureConfig());

        for (MarkerType type : MarkerType.REGISTRY) {
            type.initMips();
        }
        KeyHandler.registerBindings();
        ClientTickEvents.END_CLIENT_TICK.register(KeyHandler::onClientTick);
        AntiqueAtlasClientNetworking.init();
    }
}
