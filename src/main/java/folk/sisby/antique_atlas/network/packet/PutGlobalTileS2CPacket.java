package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

/**
 * Used to sync custom tiles from server to client.
 *
 * @author Hunternif
 * @author Haven King
 */
public class PutGlobalTileS2CPacket extends S2CPacket {
    public PutGlobalTileS2CPacket(RegistryKey<World> world, List<Map.Entry<ChunkPos, Identifier>> tiles) {
        this.writeIdentifier(world.getValue());
        this.writeVarInt(tiles.size());

        for (Map.Entry<ChunkPos, Identifier> entry : tiles) {
            this.writeVarInt(entry.getKey().x);
            this.writeVarInt(entry.getKey().z);
            this.writeIdentifier(entry.getValue());
        }
    }

    public PutGlobalTileS2CPacket(RegistryKey<World> world, int chunkX, int chunkZ, Identifier tileId) {
        this.writeIdentifier(world.getValue());
        this.writeVarInt(1);
        this.writeVarInt(chunkX);
        this.writeVarInt(chunkZ);
        this.writeIdentifier(tileId);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_PUT_GLOBAL_TILE;
    }
}
