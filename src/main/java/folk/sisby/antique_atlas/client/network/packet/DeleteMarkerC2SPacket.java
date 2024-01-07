package folk.sisby.antique_atlas.client.network.packet;

import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import folk.sisby.antique_atlas.client.network.C2SPacket;
import net.minecraft.util.Identifier;

/**
 * Deletes a marker. A client sends this packet to the server as a request,
 * and the server sends to all players as a response, including the
 * original sender.
 *
 * @author Hunternif
 */
public class DeleteMarkerC2SPacket extends C2SPacket {
    public DeleteMarkerC2SPacket(int atlasID, int markerID) {
        this.writeVarInt(atlasID);
        this.writeVarInt(markerID);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.C2S_DELETE_MARKER;
    }
}
