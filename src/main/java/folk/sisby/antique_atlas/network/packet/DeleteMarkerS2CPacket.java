package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.util.Identifier;

/**
 * Deletes a marker. A client sends a {@link AntiqueAtlasNetworking#C2S_DELETE_MARKER}
 * to the server as a request, and the server sends this back to all players as a response, including the
 * original sender.
 *
 * @author Hunternif
 * @author Haven King
 */
public class DeleteMarkerS2CPacket extends S2CPacket {
    public DeleteMarkerS2CPacket(int atlasID, int markerID) {
        this.writeVarInt(atlasID);
        this.writeVarInt(markerID);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_DELETE_MARKER;
    }
}
