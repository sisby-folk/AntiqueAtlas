package folk.sisby.antique_atlas.client.network.packet;

import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import folk.sisby.antique_atlas.client.network.C2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A request from a client to create a new marker. In order to prevent griefing,
 * the marker has to be local.
 *
 * @author Hunternif
 * @author Haven King
 */
public class PutMarkerC2SPacket extends C2SPacket {
    public PutMarkerC2SPacket(int atlasID, Identifier markerType, int x, int z, boolean visibleBeforeDiscovery, Text label) {
        this.writeVarInt(atlasID);
        this.writeIdentifier(markerType);
        this.writeVarInt(x);
        this.writeVarInt(z);
        this.writeBoolean(visibleBeforeDiscovery);
        this.writeText(label);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.C2S_PUT_MARKER;
    }
}
