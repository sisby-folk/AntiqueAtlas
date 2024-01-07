package folk.sisby.antique_atlas.network;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.api.AtlasAPI;
import folk.sisby.antique_atlas.util.Log;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class AntiqueAtlasNetworking {
    public static final Identifier C2S_DELETE_MARKER = AntiqueAtlas.id("packet.c2s.marker.delete");
    public static final Identifier C2S_PUT_BROWSING_POSITION = AntiqueAtlas.id("packet.c2s.browsing_position.put");
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


    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(C2S_DELETE_MARKER, AntiqueAtlasNetworking::handleDeleteMarker);
        ServerPlayNetworking.registerGlobalReceiver(C2S_PUT_BROWSING_POSITION, AntiqueAtlasNetworking::handlePutBrowsingPosition);
        ServerPlayNetworking.registerGlobalReceiver(C2S_PUT_MARKER, AntiqueAtlasNetworking::handlePutMarker);
        ServerPlayNetworking.registerGlobalReceiver(C2S_PUT_TILE, AntiqueAtlasNetworking::handlePutTile);
    }

    public static void handleDeleteMarker(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        int markerID = buf.readVarInt();

        if (AtlasAPI.getPlayerAtlasId(player) != atlasID) {
            Log.warn("Player %s attempted to delete marker from someone else's Atlas #%d", player.getName(), atlasID);
            return;
        }

        AtlasAPI.getMarkerAPI().deleteMarker(player.getEntityWorld(), atlasID, markerID);
    }

    public static void handlePutBrowsingPosition(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        RegistryKey<World> world = RegistryKey.of(Registry.WORLD_KEY, buf.readIdentifier());
        int x = buf.readVarInt();
        int y = buf.readVarInt();
        double zoom = buf.readDouble();

        if (AtlasAPI.getPlayerAtlasId(player) != atlasID) {
            Log.warn("Player %s attempted to put position marker into someone else's Atlas #%d", player.getCommandSource().getName(), atlasID);
            return;
        }

        AntiqueAtlas.tileData.getData(atlasID, player.getEntityWorld()).getWorldData(world).setBrowsingPosition(x, y, zoom);
    }

    public static void handlePutMarker(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        Identifier markerType = buf.readIdentifier();
        int x = buf.readVarInt();
        int z = buf.readVarInt();
        boolean visibleBeforeDiscovery = buf.readBoolean();
        Text label = buf.readText();

        if (AtlasAPI.getPlayerAtlasId(player) != atlasID) {
            AntiqueAtlas.LOG.warn("Player {} attempted to put marker into someone else's Atlas #{}}", player.getName(), atlasID);
            return;
        }

        AtlasAPI.getMarkerAPI().putMarker(player.getEntityWorld(), visibleBeforeDiscovery, atlasID, markerType, label, x, z);
    }


    public static void handlePutTile(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        int x = buf.readVarInt();
        int z = buf.readVarInt();
        Identifier tile = buf.readIdentifier();

        if (AtlasAPI.getPlayerAtlasId(player) != atlasID) {
            Log.warn("Player %s attempted to modify someone else's Atlas #%d",
                player.getName(), atlasID);
            return;
        }

        AtlasAPI.getTileAPI().putTile(player.getEntityWorld(), atlasID, tile, x, z);
    }
}
