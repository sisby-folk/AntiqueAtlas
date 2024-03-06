package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.BiomeTextures;
import folk.sisby.antique_atlas.reloader.MarkerTypes;
import folk.sisby.antique_atlas.reloader.TextureSets;
import folk.sisby.antique_atlas.gui.AtlasScreen;
import folk.sisby.antique_atlas.reloader.StructureTiles;
import folk.sisby.antique_atlas.structure.BuiltinStructures;
import folk.sisby.surveyor.SurveyorEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class AntiqueAtlas implements ClientModInitializer {
    public static final String ID = "antique_atlas";
    public static final String NAME = "Antique Atlas";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static final AntiqueAtlasConfig CONFIG = AntiqueAtlasConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", "antique-atlas", AntiqueAtlasConfig.class);

    private static AtlasScreen atlasScreen;

    public static Identifier id(String path) {
        return path.contains(":") ? new Identifier(path) : new Identifier(ID, path);
    }


    public static AtlasScreen getAtlasScreen() {
        if (atlasScreen == null) {
            atlasScreen = new AtlasScreen();
            atlasScreen.setMapScale(CONFIG.ui.defaultScale);
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
     * Register fallback texture sets for any biomes present in the client world that don't have explicit sets.
     * Doing this on world join catches data-biomes that might not be registered in other worlds.
     */
    public static void registerFallbackTextures(ClientWorld world) {
        for (Map.Entry<RegistryKey<Biome>, Biome> biome : world.getRegistryManager().get(RegistryKeys.BIOME).getEntrySet()) {
            Identifier id = world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome.getValue());
            if (!BiomeTextures.getInstance().contains(id)) {
                BiomeTextures.getInstance().registerFallback(id, world.getRegistryManager().get(RegistryKeys.BIOME).entryOf(biome.getKey()));
            }
        }
    }


    @Override
    public void onInitializeClient() {
        AntiqueAtlasKeybindings.init();
        BuiltinStructures.init();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(TextureSets.getInstance());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(BiomeTextures.getInstance());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(MarkerTypes.getInstance());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(StructureTiles.getInstance());

        SurveyorEvents.Register.clientWorldLoad(id("world_data"), (world, ws) -> ((AntiqueAtlasWorld) world).antiqueAtlas$getData());
        SurveyorEvents.Register.terrainUpdated(id("world_data"), (world, terrain, chunks) -> {
            if (MinecraftClient.getInstance().world != null) ((AntiqueAtlasWorld) MinecraftClient.getInstance().world).antiqueAtlas$getData().onTerrainUpdated(MinecraftClient.getInstance().world, terrain, chunks);
        });
        SurveyorEvents.Register.structuresAdded(id("world_data"), (world, structures, summaries) -> {
            if (MinecraftClient.getInstance().world != null) ((AntiqueAtlasWorld) MinecraftClient.getInstance().world).antiqueAtlas$getData().onStructuresAdded(MinecraftClient.getInstance().world, structures, summaries);
        });
        SurveyorEvents.Register.landmarksAdded(id("world_data"), (world, worldLandmarks, landmarks) -> {
            if (MinecraftClient.getInstance().world != null) ((AntiqueAtlasWorld) MinecraftClient.getInstance().world).antiqueAtlas$getData().onLandmarksAdded(MinecraftClient.getInstance().world, worldLandmarks, landmarks);
        });
        SurveyorEvents.Register.landmarksRemoved(id("world_data"), (world, worldLandmarks, landmarks) -> {
            if (MinecraftClient.getInstance().world != null) ((AntiqueAtlasWorld) MinecraftClient.getInstance().world).antiqueAtlas$getData().onLandmarksRemoved(MinecraftClient.getInstance().world, worldLandmarks, landmarks);
        });
        ClientTickEvents.END_WORLD_TICK.register((world -> ((AntiqueAtlasWorld) world).antiqueAtlas$getData().tick(world)));
    }
}
