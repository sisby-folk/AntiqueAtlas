package folk.sisby.antique_atlas.client;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.assets.BiomeTextures;
import folk.sisby.antique_atlas.client.assets.MarkerTypes;
import folk.sisby.antique_atlas.client.assets.TextureSets;
import folk.sisby.antique_atlas.client.gui.AtlasScreen;
import folk.sisby.antique_atlas.core.PlayerEventHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceType;

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

    @Override
    public void onInitializeClient() {
        AntiqueAtlasKeybindings.init();
        AntiqueAtlasClientNetworking.init();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(TextureSets.getInstance());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(BiomeTextures.getInstance());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(MarkerTypes.getInstance());

        ClientPlayConnectionEvents.INIT.register((h, c) -> AntiqueAtlas.tileData.onClientConnectedToServer());
        ClientPlayConnectionEvents.INIT.register((h, c) -> AntiqueAtlas.markersData.onClientConnectedToServer());
        ClientPlayConnectionEvents.INIT.register((h, c) -> {if (!c.isIntegratedServerRunning()) AntiqueAtlas.globalMarkersData.onClientConnectedToRemoteServer();});

        ClientTickEvents.END_WORLD_TICK.register(world -> world.getPlayers().forEach(PlayerEventHandler::onPlayerTick));
        CommonLifecycleEvents.TAGS_LOADED.register(((manager, client) -> BiomeTextures.getInstance().registerFallbacks(manager.get(RegistryKeys.BIOME))));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> BiomeTextures.getInstance().clearFallbacks());
    }
}
