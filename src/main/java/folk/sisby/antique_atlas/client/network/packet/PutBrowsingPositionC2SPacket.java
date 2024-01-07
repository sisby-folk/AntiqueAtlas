package folk.sisby.antique_atlas.client.network.packet;

import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import folk.sisby.antique_atlas.client.network.C2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Packet used to save the last browsing position for a dimension in an atlas.
 *
 * @author Hunternif
 * @author Haven King
 */
public class PutBrowsingPositionC2SPacket extends C2SPacket {
    public PutBrowsingPositionC2SPacket(int atlasID, RegistryKey<World> world, int x, int y, double zoom) {
        this.writeVarInt(atlasID);
        this.writeIdentifier(world.getValue());
        this.writeVarInt(x);
        this.writeVarInt(y);
        this.writeDouble(zoom);
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.C2S_PUT_BROWSING_POSITION;
    }
}
