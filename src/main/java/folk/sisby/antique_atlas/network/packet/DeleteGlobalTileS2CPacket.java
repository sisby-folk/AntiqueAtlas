package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Sent from server to client to remove a custom global tile.
 *
 * @author Hunternif
 * @author Haven King
 */
public record DeleteGlobalTileS2CPacket(RegistryKey<World> world, int chunkX, int chunkZ) implements S2CPacket {
    public DeleteGlobalTileS2CPacket(PacketByteBuf buf) {
        this(
            RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier()),
            buf.readVarInt(),
            buf.readVarInt()
        );
    }

    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeIdentifier(world.getValue());
        buf.writeVarInt(chunkX);
        buf.writeVarInt(chunkZ);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_DELETE_GLOBAL_TILE;
    }
}
