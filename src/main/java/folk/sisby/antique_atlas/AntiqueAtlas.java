package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.chunk.ChunkSummaryState;
import folk.sisby.antique_atlas.core.GlobalTileDataHandler;
import folk.sisby.antique_atlas.player.PlayerEventHandler;
import folk.sisby.antique_atlas.core.TileDataHandler;
import folk.sisby.antique_atlas.core.scanning.WorldScanner;
import folk.sisby.antique_atlas.data.StructureTiles;
import folk.sisby.antique_atlas.marker.GlobalMarkersDataHandler;
import folk.sisby.antique_atlas.marker.MarkersDataHandler;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import folk.sisby.antique_atlas.structure.BuiltinStructures;
import folk.sisby.antique_atlas.structure.StructureSummaryState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AntiqueAtlas implements ModInitializer {
    public static final String ID = "antique_atlas";
    public static final String NAME = "Antique Atlas";

    public static final Logger LOG = LogManager.getLogger(NAME);

    public static final WorldScanner worldScanner = new WorldScanner();
    public static final TileDataHandler tileData = new TileDataHandler();
    public static final MarkersDataHandler markersData = new MarkersDataHandler();

    public static final GlobalTileDataHandler globalTileData = new GlobalTileDataHandler();
    public static final GlobalMarkersDataHandler globalMarkersData = new GlobalMarkersDataHandler();

    public static final AntiqueAtlasConfig CONFIG = AntiqueAtlasConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", "antique-atlas", AntiqueAtlasConfig.class);

    public static Identifier id(String path) {
        return path.contains(":") ? new Identifier(path) : new Identifier(ID, path);
    }

    @Override
    public void onInitialize() {
        AntiqueAtlasNetworking.init();

        ServerPlayConnectionEvents.JOIN.register(globalMarkersData::onPlayerLogin);
        ServerPlayConnectionEvents.JOIN.register(globalTileData::onPlayerLogin);
        ServerPlayConnectionEvents.JOIN.register(PlayerEventHandler::onPlayerLogin);

        ServerWorldEvents.LOAD.register(globalMarkersData::onWorldLoad);
        ServerWorldEvents.LOAD.register(globalTileData::onWorldLoad);
        ServerWorldEvents.LOAD.register((s, world) -> ChunkSummaryState.getOrCreate(world));
        ServerWorldEvents.LOAD.register((s, world) -> StructureSummaryState.getOrCreate(world));

        ServerChunkEvents.CHUNK_LOAD.register(ChunkSummaryState::onChunkLoad);
        ServerChunkEvents.CHUNK_LOAD.register(StructureSummaryState::onChunkLoad);
        ServerChunkEvents.CHUNK_UNLOAD.register(ChunkSummaryState::onChunkUnload);

        ServerTickEvents.END_WORLD_TICK.register(world -> world.getPlayers().forEach(PlayerEventHandler::onPlayerTick));

        BuiltinStructures.init();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(StructureTiles.getInstance());
    }
}
