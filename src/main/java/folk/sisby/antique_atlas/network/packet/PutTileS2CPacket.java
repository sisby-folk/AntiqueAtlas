package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Puts biome tile into one atlas.
 *
 * @author Hunternif
 * @author Haven King
 */
public record PutTileS2CPacket(int atlasID, RegistryKey<World> world, int x, int z, Identifier tile) implements S2CPacket {
    public PutTileS2CPacket(PacketByteBuf buf) {
        this(
            buf.readInt(),
            RegistryKey.of(Registry.WORLD_KEY, buf.readIdentifier()),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readIdentifier()
        );
    }

    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeInt(atlasID);
        buf.writeIdentifier(world.getValue());
        buf.writeVarInt(x);
        buf.writeVarInt(z);
        buf.writeIdentifier(tile);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_PUT_TILE;
    }
}
