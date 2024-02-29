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
            atlasScreen.setMapScale(CONFIG.Interface.defaultScale);
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

        SurveyorEvents.Register.clientWorldLoad(id("world_tiles"), (world, ws) -> ((AntiqueAtlasWorld) world).antiqueAtlas$getWorldTiles());
        SurveyorEvents.Register.clientWorldLoad(id("world_markers"), (world, ws) -> ((AntiqueAtlasWorld) world).antiqueAtlas$getWorldMarkers());
        SurveyorEvents.Register.chunkAdded(id("world_tiles"), (world, terrain, pos, chunk) -> {
            if (MinecraftClient.getInstance().world != null) ((AntiqueAtlasWorld) MinecraftClient.getInstance().world).antiqueAtlas$getWorldTiles().onChunkAdded(MinecraftClient.getInstance().world, terrain, pos, chunk);
        });
        SurveyorEvents.Register.structureAdded(id("world_tiles"), (world, structures, structure) -> {
            if (MinecraftClient.getInstance().world != null) ((AntiqueAtlasWorld) MinecraftClient.getInstance().world).antiqueAtlas$getWorldTiles().onStructureAdded(MinecraftClient.getInstance().world, structures, structure);
        });
        SurveyorEvents.Register.landmarkAdded(id("world_markers"), (world, landmarks, landmark) -> {
            if (MinecraftClient.getInstance().world != null) ((AntiqueAtlasWorld) MinecraftClient.getInstance().world).antiqueAtlas$getWorldMarkers().onLandmarkAdded(MinecraftClient.getInstance().world, landmarks, landmark);
        });
        SurveyorEvents.Register.landmarkRemoved(id("world_markers"), (world, landmarks, type, pos) -> {
            if (MinecraftClient.getInstance().world != null) ((AntiqueAtlasWorld) MinecraftClient.getInstance().world).antiqueAtlas$getWorldMarkers().onLandmarkRemoved(MinecraftClient.getInstance().world, landmarks, type, pos);
        });
        ClientTickEvents.END_WORLD_TICK.register((world -> ((AntiqueAtlasWorld) world).antiqueAtlas$getWorldTiles().tick(world)));
    }
}
