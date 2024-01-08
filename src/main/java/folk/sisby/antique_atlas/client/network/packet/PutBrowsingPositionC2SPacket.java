package folk.sisby.antique_atlas.client.network.packet;

import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import folk.sisby.antique_atlas.client.network.C2SPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Packet used to save the last browsing position for a dimension in an atlas.
 *
 * @author Hunternif
 * @author Haven King
 */
public record PutBrowsingPositionC2SPacket(int atlasID, RegistryKey<World> world, int x, int y, double zoom) implements C2SPacket {
    public PutBrowsingPositionC2SPacket(PacketByteBuf buf) {
        this(
            buf.readVarInt(),
            RegistryKey.of(Registry.WORLD_KEY, buf.readIdentifier()),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readDouble()
        );
    }

    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeVarInt(atlasID);
        buf.writeIdentifier(world.getValue());
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        buf.writeDouble(zoom);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.C2S_PUT_BROWSING_POSITION;
    }
}
