package folk.sisby.antique_atlas.network.s2c;

import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Deletes a marker. A client sends a {@link AntiqueAtlasNetworking#C2S_DELETE_MARKER}
 * to the server as a request, and the server sends this back to all players as a response, including the
 * original sender.
 *
 * @author Hunternif
 * @author Haven King
 */
public record DeleteMarkerS2CPacket(int atlasID, int markerID) implements S2CPacket {
    public DeleteMarkerS2CPacket(PacketByteBuf buf) {
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
        return AntiqueAtlasNetworking.S2C_DELETE_MARKER;
    }
}
