package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Sent from server to client to remove a custom global tile.
 *
 * @author Hunternif
 * @author Haven King
 */
public class DeleteGlobalTileS2CPacket extends S2CPacket {
    public DeleteGlobalTileS2CPacket(RegistryKey<World> world, int chunkX, int chunkZ) {
        this.writeIdentifier(world.getValue());
        this.writeVarInt(chunkX);
        this.writeVarInt(chunkZ);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_DELETE_GLOBAL_TILE;
    }
}
