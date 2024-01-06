package folk.sisby.antique_atlas;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import folk.sisby.antique_atlas.core.AtlasIdData;
import folk.sisby.antique_atlas.core.GlobalTileDataHandler;
import folk.sisby.antique_atlas.core.PlayerEventHandler;
import folk.sisby.antique_atlas.core.TileDataHandler;
import folk.sisby.antique_atlas.core.scanning.WorldScanner;
import folk.sisby.antique_atlas.marker.GlobalMarkersDataHandler;
import folk.sisby.antique_atlas.marker.MarkersDataHandler;
import folk.sisby.antique_atlas.mixinhooks.NewPlayerConnectionCallback;
import folk.sisby.antique_atlas.mixinhooks.NewServerConnectionCallback;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import folk.sisby.antique_atlas.structure.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AntiqueAtlas implements ModInitializer {
    public static final String ID = "antique_atlas";
    public static final String NAME = "Antique Atlas";

    public static Logger LOG = LogManager.getLogger(NAME);

    public static final WorldScanner worldScanner = new WorldScanner();
    public static final TileDataHandler tileData = new TileDataHandler();
    public static final MarkersDataHandler markersData = new MarkersDataHandler();

    public static final GlobalTileDataHandler globalTileData = new GlobalTileDataHandler();
    public static final GlobalMarkersDataHandler globalMarkersData = new GlobalMarkersDataHandler();

    public static AntiqueAtlasConfig CONFIG = AntiqueAtlasConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", "antique-atlas", AntiqueAtlasConfig.class);

    public static Identifier id(String... path) {
        return path[0].contains(":") ? new Identifier(String.join(".", path)) : new Identifier(ID, String.join(".", path));
    }

    public static AtlasIdData getAtlasIdData(World world) {
        if (world.isClient()) {
            LOG.warn("Tried to access server only data from client.");
            return null;
        }

        return ((ServerWorld) world).getPersistentStateManager().getOrCreate(AtlasIdData::fromNbt, AtlasIdData::new, "antique_atlas:global_atlas_data");
    }

    @Override
    public void onInitialize() {
        AntiqueAtlasNetworking.registerC2SListeners();

        NewServerConnectionCallback.EVENT.register(tileData::onClientConnectedToServer);
        NewServerConnectionCallback.EVENT.register(markersData::onClientConnectedToServer);
        NewServerConnectionCallback.EVENT.register(globalMarkersData::onClientConnectedToServer);

        NewPlayerConnectionCallback.EVENT.register(globalMarkersData::onPlayerLogin);
        NewPlayerConnectionCallback.EVENT.register(globalTileData::onPlayerLogin);
        NewPlayerConnectionCallback.EVENT.register(PlayerEventHandler::onPlayerLogin);

        LifecycleEvent.SERVER_LEVEL_LOAD.register(globalMarkersData::onWorldLoad);
        LifecycleEvent.SERVER_LEVEL_LOAD.register(globalTileData::onWorldLoad);

        StructurePieceAddedCallback.EVENT.register(StructureHandler::resolve);
        StructureAddedCallback.EVENT.register(StructureHandler::resolve);

        NetherFortress.registerPieces();
        EndCity.registerMarkers();
        Village.registerMarkers();
        Overworld.registerPieces();

        JigsawConfig jigsawConfig = new JigsawConfig();
        ReloadListenerRegistry.register(ResourceType.SERVER_DATA, jigsawConfig, jigsawConfig.getId());
    }
}
