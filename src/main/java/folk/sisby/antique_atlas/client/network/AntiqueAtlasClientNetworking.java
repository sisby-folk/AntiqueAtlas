package folk.sisby.antique_atlas.client.network;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.AntiqueAtlasClient;
import folk.sisby.antique_atlas.client.MarkerType;
import folk.sisby.antique_atlas.client.gui.GuiAtlas;
import folk.sisby.antique_atlas.core.AtlasData;
import folk.sisby.antique_atlas.core.TileDataStorage;
import folk.sisby.antique_atlas.core.TileGroup;
import folk.sisby.antique_atlas.core.TileInfo;
import folk.sisby.antique_atlas.core.WorldData;
import folk.sisby.antique_atlas.marker.Marker;
import folk.sisby.antique_atlas.marker.MarkerData;
import folk.sisby.antique_atlas.marker.MarkersData;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.network.packet.DeleteGlobalTileS2CPacket;
import folk.sisby.antique_atlas.network.packet.DeleteMarkerS2CPacket;
import folk.sisby.antique_atlas.network.packet.DimensionUpdateS2CPacket;
import folk.sisby.antique_atlas.network.packet.MapDataS2CPacket;
import folk.sisby.antique_atlas.network.packet.PutGlobalTileS2CPacket;
import folk.sisby.antique_atlas.network.packet.PutMarkersS2CPacket;
import folk.sisby.antique_atlas.network.packet.PutTileS2CPacket;
import folk.sisby.antique_atlas.network.packet.TileGroupsS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.ChunkPos;

import java.util.Map;
import java.util.function.Function;

public class AntiqueAtlasClientNetworking {
    private static final int GLOBAL = -1;

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_DELETE_GLOBAL_TILE, (c, h, b, s) -> handleClient(b, DeleteGlobalTileS2CPacket::new, AntiqueAtlasClientNetworking::handleDeleteGlobalTile));
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_DELETE_MARKER, (c, h, b, s) -> handleClient(b, DeleteMarkerS2CPacket::new, AntiqueAtlasClientNetworking::handleDeleteMarker));
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_DIMENSION_UPDATE, (c, h, b, s) -> handleClient(b, DimensionUpdateS2CPacket::new, AntiqueAtlasClientNetworking::handleDimensionUpdate));
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_MAP_DATA, (c, h, b, s) -> handleClient(b, MapDataS2CPacket::new, AntiqueAtlasClientNetworking::handleMapData));
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_PUT_GLOBAL_TILE, (c, h, b, s) -> handleClient(b, PutGlobalTileS2CPacket::new, AntiqueAtlasClientNetworking::handlePutGlobalTile));
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_PUT_MARKERS, (c, h, b, s) -> handleClient(b, PutMarkersS2CPacket::new, AntiqueAtlasClientNetworking::handlePutMarkers));
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_PUT_TILE, (c, h, b, s) -> handleClient(b, PutTileS2CPacket::new, AntiqueAtlasClientNetworking::handlePutTile));
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_TILE_GROUPS, (c, h, b, s) -> handleClient(b, TileGroupsS2CPacket::new, AntiqueAtlasClientNetworking::handleTileGroups));
    }

    public static void handleDeleteGlobalTile(ClientWorld clientWorld, DeleteGlobalTileS2CPacket packet) {
        TileDataStorage data = AntiqueAtlas.globalTileData.getData(packet.world());
        data.removeTile(packet.chunkX(), packet.chunkZ());
    }

    public static void handleDeleteMarker(ClientWorld clientWorld, DeleteMarkerS2CPacket packet) {
        if (clientWorld == null) return;
        MarkersData data = packet.atlasID() == GLOBAL ? AntiqueAtlas.globalMarkersData.getData() : AntiqueAtlas.markersData.getMarkersData(packet.atlasID(), clientWorld);
        data.removeMarker(packet.markerID());
        AntiqueAtlasClient.getAtlasGUI().updateBookmarkerList();
    }

    public static void handleDimensionUpdate(ClientWorld clientWorld, DimensionUpdateS2CPacket packet) {
        if (clientWorld == null) return;
        AtlasData data = AntiqueAtlas.tileData.getData(packet.atlasID(), clientWorld);
        for (TileInfo info : packet.tiles()) {
            data.getWorldData(packet.world()).setTile(info.x, info.z, info.id);
        }
    }

    public static void handleMapData(ClientWorld clientWorld, MapDataS2CPacket packet) {
        if (packet.data() == null) return;
        if (clientWorld == null) return;
        AtlasData atlasData = AntiqueAtlas.tileData.getData(packet.atlasID(), clientWorld);
        atlasData.updateFromNbt(packet.data());
        if (AntiqueAtlas.CONFIG.Gameplay.doSaveBrowsingPos && MinecraftClient.getInstance().currentScreen instanceof GuiAtlas atlasScreen) {
            atlasScreen.loadSavedBrowsingPosition();
        }
    }

    public static void handlePutGlobalTile(ClientWorld clientWorld, PutGlobalTileS2CPacket packet) {
        TileDataStorage data = AntiqueAtlas.globalTileData.getData(packet.world());
        for (Pair<ChunkPos, Identifier> tile : packet.tiles()) {
            data.setTile(tile.getLeft().x, tile.getLeft().z, tile.getRight());
        }
    }

    public static void handlePutMarkers(ClientWorld clientWorld, PutMarkersS2CPacket packet) {
        MarkersData markersData = packet.atlasID() == GLOBAL ? AntiqueAtlas.globalMarkersData.getData() : AntiqueAtlas.markersData.getMarkersDataCached(packet.atlasID(), packet.world());
        for (Map.Entry<Identifier, MarkerData> entry : packet.markers().entries()) {
            MarkerType markerType = MarkerType.REGISTRY.get(entry.getKey());
            markersData.loadMarker(new Marker(MarkerType.REGISTRY.getId(markerType), packet.world(), entry.getValue()));
        }
        AntiqueAtlasClient.getAtlasGUI().updateBookmarkerList();
    }

    public static void handlePutTile(ClientWorld clientWorld, PutTileS2CPacket packet) {
        if (clientWorld == null) return;
        AtlasData data = AntiqueAtlas.tileData.getData(packet.atlasID(), clientWorld);
        data.setTile(packet.world(), packet.x(), packet.z(), packet.tile());
    }

    public static void handleTileGroups(ClientWorld clientWorld, TileGroupsS2CPacket packet) {
        if (clientWorld == null) return;
        AtlasData atlasData = AntiqueAtlas.tileData.getData(packet.atlasID(), clientWorld);
        WorldData worldData = atlasData.getWorldData(packet.world());
        for (TileGroup t : packet.tileGroups()) {
            worldData.putTileGroup(t);
        }
    }

    private static <T extends S2CPacket> void handleClient(PacketByteBuf buf, Function<PacketByteBuf, T> reader, ClientPacketHandler<T> handler) {
        handler.handle(MinecraftClient.getInstance().world, reader.apply(buf));
    }

    public interface ClientPacketHandler<T extends S2CPacket> {
        void handle(ClientWorld clientWorld, T packet);
    }
}
