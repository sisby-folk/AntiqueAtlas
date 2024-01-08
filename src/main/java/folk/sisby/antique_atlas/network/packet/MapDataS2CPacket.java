package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Used to sync atlas data from server to client.
 *
 * @author Hunternif
 * @author Haven King
 */
public record MapDataS2CPacket(int atlasID, NbtCompound data) implements S2CPacket {
    public MapDataS2CPacket(PacketByteBuf buf) {
        this(
            buf.readVarInt(),
            buf.readNbt()
        );
    }

    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeVarInt(atlasID);
        buf.writeNbt(data);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_MAP_DATA;
    }
}
