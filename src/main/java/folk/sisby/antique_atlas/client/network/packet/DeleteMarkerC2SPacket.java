package folk.sisby.antique_atlas.client.network.packet;

import folk.sisby.antique_atlas.client.network.C2SPacket;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Deletes a marker. A client sends this packet to the server as a request,
 * and the server sends to all players as a response, including the
 * original sender.
 *
 * @author Hunternif
 */
public record DeleteMarkerC2SPacket(int atlasID, int markerID) implements C2SPacket {
    public DeleteMarkerC2SPacket(PacketByteBuf buf) {
        this(
            buf.readVarInt(),
            buf.readVarInt()
        );
    }

    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeVarInt(atlasID);
        buf.writeVarInt(markerID);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.C2S_DELETE_MARKER;
    }
}
