package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.core.TileGroup;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;


/**
 * Syncs tile groups to the client.
 *
 * @author Hunternif
 * @author Haven King
 */
public record TileGroupsS2CPacket(int atlasID, RegistryKey<World> world, List<TileGroup> tileGroups) implements S2CPacket {
    public static final int TILE_GROUPS_PER_PACKET = 100;

    public TileGroupsS2CPacket(PacketByteBuf buf) {
        this(
            buf.readVarInt(),
            RegistryKey.of(Registry.WORLD_KEY, buf.readIdentifier()),
            readTileGroups(buf)
        );
    }

    private static List<TileGroup> readTileGroups(PacketByteBuf buf) {
        int length = buf.readVarInt();
        List<TileGroup> tileGroups = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            NbtCompound tag = buf.readNbt();

            if (tag != null) {
                tileGroups.add(TileGroup.fromNBT(tag));
            }
        }
        return tileGroups;
    }

    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeVarInt(atlasID);
        buf.writeIdentifier(world.getValue());
        buf.writeVarInt(tileGroups.size());

        for (TileGroup tileGroup : tileGroups) {
            buf.writeNbt(tileGroup.writeToNBT(new NbtCompound()));
        }
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_TILE_GROUPS;
    }
}
