package folk.sisby.antique_atlas.network.c2s;

import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Puts biome tile into one atlas. When sent to server, forwards it to every
 * client that has this atlas' data synced.
 *
 * @author Hunternif
 * @author Haven King
 */
public record PutTileC2SPacket(int atlasID, int x, int z, Identifier tile) implements C2SPacket {
    public PutTileC2SPacket(PacketByteBuf buf) {
        this(
            buf.readInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readIdentifier()
        );
    }

    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeInt(atlasID);
        buf.writeVarInt(x);
        buf.writeVarInt(z);
        buf.writeIdentifier(tile);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.C2S_PUT_TILE;
    }
}
