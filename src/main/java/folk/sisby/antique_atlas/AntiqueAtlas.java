package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.BiomeTileProviders;
import folk.sisby.antique_atlas.reloader.MarkerTextures;
import folk.sisby.antique_atlas.reloader.StructureTileProviders;
import folk.sisby.antique_atlas.reloader.TileTextures;
import folk.sisby.surveyor.SurveyorEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AntiqueAtlas implements ClientModInitializer {
    public static final String ID = "antique_atlas";
    public static final String NAME = "Antique Atlas";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static final AntiqueAtlasConfig CONFIG = AntiqueAtlasConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", "antique-atlas", AntiqueAtlasConfig.class);

    public static Identifier id(String path) {
        return path.contains(":") ? new Identifier(path) : new Identifier(ID, path);
    }

    @Override
    public void onInitializeClient() {
        AntiqueAtlasKeybindings.init();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(TileTextures.getInstance());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(StructureTileProviders.getInstance());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(BiomeTileProviders.getInstance());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(MarkerTextures.getInstance());

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
        CommonLifecycleEvents.TAGS_LOADED.register(((manager, client) -> BiomeTileProviders.getInstance().registerFallbacks(manager.get(RegistryKeys.BIOME))));
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> BiomeTileProviders.getInstance().clearFallbacks()));
    }
}
