package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * Used to sync atlas data from server to client.
 *
 * @author Hunternif
 * @author Haven King
 */
public class MapDataS2CPacket extends S2CPacket {
    public MapDataS2CPacket(int atlasID, NbtCompound data) {
        this.writeVarInt(atlasID);
        this.writeNbt(data);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_MAP_DATA;
    }
}
