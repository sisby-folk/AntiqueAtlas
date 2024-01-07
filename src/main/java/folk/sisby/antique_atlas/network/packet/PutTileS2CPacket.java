package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Puts biome tile into one atlas.
 *
 * @author Hunternif
 * @author Haven King
 */
public class PutTileS2CPacket extends S2CPacket {
    public PutTileS2CPacket(int atlasID, RegistryKey<World> world, int x, int z, Identifier tile) {
        this.writeInt(atlasID);
        this.writeIdentifier(world.getValue());
        this.writeVarInt(x);
        this.writeVarInt(z);
        this.writeIdentifier(tile);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_PUT_TILE;
    }
}
