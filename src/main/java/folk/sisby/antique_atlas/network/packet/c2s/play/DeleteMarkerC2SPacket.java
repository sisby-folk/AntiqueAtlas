package folk.sisby.antique_atlas.network.packet.c2s.play;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.api.AtlasAPI;
import folk.sisby.antique_atlas.network.packet.c2s.C2SPacket;
import folk.sisby.antique_atlas.util.Log;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Deletes a marker. A client sends this packet to the server as a request,
 * and the server sends to all players as a response, including the
 * original sender.
 *
 * @author Hunternif
 */
public class DeleteMarkerC2SPacket extends C2SPacket {
    public static final Identifier ID = AntiqueAtlas.id("packet.c2s.marker.delete");

    private static final int GLOBAL = -1;

    public DeleteMarkerC2SPacket(int atlasID, int markerID) {
        this.writeVarInt(atlasID);
        this.writeVarInt(markerID);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        int markerID = buf.readVarInt();

        if (AtlasAPI.getPlayerAtlasId(player) != atlasID) {
            Log.warn("Player %s attempted to delete marker from someone else's Atlas #%d", player.getName(), atlasID);
            return;
        }

        AtlasAPI.getMarkerAPI().deleteMarker(player.getEntityWorld(), atlasID, markerID);
    }
}
