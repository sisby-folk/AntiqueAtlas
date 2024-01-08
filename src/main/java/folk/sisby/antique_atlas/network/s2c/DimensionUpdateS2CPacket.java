package folk.sisby.antique_atlas.network.s2c;

import folk.sisby.antique_atlas.core.TileInfo;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record DimensionUpdateS2CPacket(int atlasID, RegistryKey<World> world, Collection<TileInfo> tiles) implements S2CPacket {
    public DimensionUpdateS2CPacket(PacketByteBuf buf) {
        this(buf.readVarInt(), RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier()), readTiles(buf));
    }

    private static List<TileInfo> readTiles(PacketByteBuf buf) {
        int tileCount = buf.readVarInt();
        List<TileInfo> tiles = new ArrayList<>();
        for (int i = 0; i < tileCount; ++i) {
            tiles.add(new TileInfo(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readIdentifier())
            );
        }
        return tiles;
    }

    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeVarInt(atlasID);
        buf.writeIdentifier(world.getValue());
        buf.writeVarInt(tiles.size());

        for (TileInfo tile : tiles) {
            buf.writeVarInt(tile.x());
            buf.writeVarInt(tile.z());
            buf.writeIdentifier(tile.id());
        }
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_DIMENSION_UPDATE;
    }
}
