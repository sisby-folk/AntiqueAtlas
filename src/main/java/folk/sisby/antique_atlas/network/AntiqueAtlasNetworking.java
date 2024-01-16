package folk.sisby.antique_atlas.network;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.api.AtlasAPI;
import folk.sisby.antique_atlas.network.c2s.C2SPacket;
import folk.sisby.antique_atlas.network.c2s.DeleteMarkerC2SPacket;
import folk.sisby.antique_atlas.network.c2s.PutMarkerC2SPacket;
import folk.sisby.antique_atlas.network.c2s.PutTileC2SPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;

public class AntiqueAtlasNetworking {
    public static final Identifier C2S_DELETE_MARKER = AntiqueAtlas.id("packet.c2s.marker.delete");
    public static final Identifier C2S_PUT_MARKER = AntiqueAtlas.id("packet.c2s.marker.put");
    public static final Identifier C2S_PUT_TILE = AntiqueAtlas.id("packet.c2s.tile.put");

    public static final Identifier S2C_DELETE_GLOBAL_TILE = AntiqueAtlas.id("packet.c2s.global_tile.delete");
    public static final Identifier S2C_DELETE_MARKER = AntiqueAtlas.id("packet.s2c.marker.delete");
    public static final Identifier S2C_DIMENSION_UPDATE = AntiqueAtlas.id("packet.s2c.dimension.update");
    public static final Identifier S2C_MAP_DATA = AntiqueAtlas.id("packet.s2c.map.data");
    public static final Identifier S2C_PUT_GLOBAL_TILE = AntiqueAtlas.id("packet.s2c.global_tile.put");
    public static final Identifier S2C_PUT_MARKERS = AntiqueAtlas.id("packet.s2c.marker.put");
    public static final Identifier S2C_PUT_TILE = AntiqueAtlas.id("packet.s2c.tile.put");
    public static final Identifier S2C_TILE_GROUPS = AntiqueAtlas.id("packet.s2c.tile.groups");

    public static Consumer<C2SPacket> C2S_SENDER = p -> {};

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(C2S_DELETE_MARKER, (sv, p, h, b, se) -> handleServer(p, b, DeleteMarkerC2SPacket::new, AntiqueAtlasNetworking::handleDeleteMarker));
        ServerPlayNetworking.registerGlobalReceiver(C2S_PUT_MARKER, (sv, p, h, b, se) -> handleServer(p, b, PutMarkerC2SPacket::new, AntiqueAtlasNetworking::handlePutMarker));
        ServerPlayNetworking.registerGlobalReceiver(C2S_PUT_TILE, (sv, p, h, b, se) -> handleServer(p, b, PutTileC2SPacket::new, AntiqueAtlasNetworking::handlePutTile));
    }

    public static void handleDeleteMarker(ServerWorld world, DeleteMarkerC2SPacket packet) {
        AtlasAPI.getMarkerAPI().deleteMarker(world, packet.atlasID(), packet.markerID());
    }

    public static void handlePutMarker(ServerWorld world, PutMarkerC2SPacket packet) {
        AtlasAPI.getMarkerAPI().putMarker(world, packet.visibleBeforeDiscovery(), packet.atlasID(), packet.markerType(), packet.label(), packet.x(), packet.z());
    }

    public static void handlePutTile(ServerWorld world, PutTileC2SPacket packet) {
        AtlasAPI.getTileAPI().putTile(world, packet.atlasID(), packet.tile(), packet.x(), packet.z());
    }

    private static <T extends C2SPacket> void handleServer(ServerPlayerEntity player, PacketByteBuf buf, Function<PacketByteBuf, T> reader, ServerPacketHandler<T> handler) {
        T packet = reader.apply(buf);
        if (AtlasAPI.getPlayerAtlasId(player) != packet.atlasID()) {
            AntiqueAtlas.LOG.warn("Player {} attempted to modify someone else's Atlas #{}", player.getName(), packet.atlasID());
            return;
        }
        handler.handle(player.getServerWorld(), packet);
    }

    public interface ServerPacketHandler<T extends C2SPacket> {
        void handle(ServerWorld world, T packet);
    }
}
