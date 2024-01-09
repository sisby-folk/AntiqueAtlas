package folk.sisby.antique_atlas.client;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.gui.AtlasScreen;
import folk.sisby.antique_atlas.client.resource.MarkerTypes;
import folk.sisby.antique_atlas.client.resource.reloader.MarkerMipsConfig;
import folk.sisby.antique_atlas.client.resource.reloader.MarkerTextureConfig;
import folk.sisby.antique_atlas.client.resource.reloader.TextureConfig;
import folk.sisby.antique_atlas.client.resource.reloader.TextureSetConfig;
import folk.sisby.antique_atlas.client.resource.reloader.TileTextureReloader;
import folk.sisby.antique_atlas.client.resource.TileTextures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.Map;

public class AntiqueAtlasClient implements ClientModInitializer {
    private static AtlasScreen atlasScreen;

    public static AtlasScreen getAtlasScreen() {
        if (atlasScreen == null) {
            atlasScreen = new AtlasScreen();
            atlasScreen.setMapScale(AntiqueAtlas.CONFIG.Interface.defaultScale);
        }
        return atlasScreen;
    }

    public static void openAtlasScreen() {
        openAtlasScreen(getAtlasScreen().prepareToOpen());
    }

    private static void openAtlasScreen(AtlasScreen screen) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen == null) { // In-game screen
            atlasScreen.updateL18n();
            mc.setScreen(screen);
        }
    }

    /**
     * Assign default textures to biomes defined in the client world, but
     * not part of the BuiltinRegistries.BIOME. This happens for all biomes
     * defined in data packs. Also, as these are only available per world,
     * we need the ClientWorld loaded here.
     */
    public static void assignCustomBiomeTextures(ClientWorld world) {
        for (Map.Entry<RegistryKey<Biome>, Biome> biome : world.getRegistryManager().get(RegistryKeys.BIOME).getEntrySet()) {
            Identifier id = world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome.getValue());
            if (!TileTextures.getInstance().isRegistered(id)) {
                TileTextures.getInstance().autoRegister(id, biome.getKey());
            }
        }

        for (Map.Entry<RegistryKey<Biome>, Biome> entry : world.getRegistryManager().get(RegistryKeys.BIOME).getEntrySet()) {
            Identifier id = world.getRegistryManager().get(RegistryKeys.BIOME).getId(entry.getValue());
            if (!TileTextures.getInstance().isRegistered(id)) {
                TileTextures.getInstance().autoRegister(id, entry.getKey());
            }
        }
    }


    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new TextureConfig(AntiqueAtlasTextures.TILE_TEXTURES_MAP));
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new TextureSetConfig());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new TileTextureReloader());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new MarkerMipsConfig());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new MarkerTextureConfig());

        for (MarkerType type : MarkerTypes.REGISTRY) {
            type.initMips();
        }
        AntiqueAtlasKeybindings.init();
        AntiqueAtlasClientNetworking.init();
    }
}
