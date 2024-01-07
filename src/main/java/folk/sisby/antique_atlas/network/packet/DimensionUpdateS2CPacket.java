package folk.sisby.antique_atlas.network.packet;

import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.core.TileInfo;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Collection;

public class DimensionUpdateS2CPacket extends S2CPacket {
    public DimensionUpdateS2CPacket(int atlasID, RegistryKey<World> world, Collection<TileInfo> tiles) {
        this.writeVarInt(atlasID);
        this.writeIdentifier(world.getValue());
        this.writeVarInt(tiles.size());

        for (TileInfo tile : tiles) {
            this.writeVarInt(tile.x);
            this.writeVarInt(tile.z);
            this.writeIdentifier(tile.id);
        }
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_DIMENSION_UPDATE;
    }
}
