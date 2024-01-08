package folk.sisby.antique_atlas.network.c2s;

import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A request from a client to create a new marker. In order to prevent griefing,
 * the marker has to be local.
 *
 * @author Hunternif
 * @author Haven King
 */
public record PutMarkerC2SPacket(int atlasID, Identifier markerType, int x, int z, boolean visibleBeforeDiscovery, Text label) implements C2SPacket {
    public PutMarkerC2SPacket(PacketByteBuf buf) {
        this(
            buf.readVarInt(),
            buf.readIdentifier(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readBoolean(),
            buf.readText()
        );
    }

    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeVarInt(atlasID);
        buf.writeIdentifier(markerType);
        buf.writeVarInt(x);
        buf.writeVarInt(z);
        buf.writeBoolean(visibleBeforeDiscovery);
        buf.writeText(label);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.C2S_PUT_MARKER;
    }
}
