package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.core.TileGroup;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;


/**
 * Syncs tile groups to the client.
 *
 * @author Hunternif
 * @author Haven King
 */
public class TileGroupsS2CPacket extends S2CPacket {
    public static final int TILE_GROUPS_PER_PACKET = 100;

    public TileGroupsS2CPacket(int atlasID, RegistryKey<World> world, List<TileGroup> tileGroups) {
        this.writeVarInt(atlasID);
        this.writeIdentifier(world.getValue());
        this.writeVarInt(tileGroups.size());

        for (TileGroup tileGroup : tileGroups) {
            this.writeNbt(tileGroup.writeToNBT(new NbtCompound()));
        }
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_TILE_GROUPS;
    }
}
