package folk.sisby.antique_atlas.network.s2c;

import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to sync custom tiles from server to client.
 *
 * @author Hunternif
 * @author Haven King
 */
public record PutGlobalTileS2CPacket(RegistryKey<World> world, List<Pair<ChunkPos, Identifier>> tiles) implements S2CPacket {
    public PutGlobalTileS2CPacket(RegistryKey<World> world, int chunkX, int chunkZ, Identifier tileId) {
        this(world, List.of(new Pair<>(new ChunkPos(chunkX, chunkZ), tileId)));
    }

    public PutGlobalTileS2CPacket(PacketByteBuf buf) {
        this(RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier()), readTiles(buf));
    }

    private static List<Pair<ChunkPos, Identifier>> readTiles(PacketByteBuf buf) {
        int tileCount = buf.readVarInt();

        List<Pair<ChunkPos, Identifier>> list = new ArrayList<>();
        for (int i = 0; i < tileCount; ++i) {
            list.add(new Pair<>(new ChunkPos(buf.readVarInt(), buf.readVarInt()), buf.readIdentifier()));
        }
        return list;
    }


    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeIdentifier(world.getValue());
        buf.writeVarInt(tiles.size());

        for (Pair<ChunkPos, Identifier> entry : tiles) {
            buf.writeVarInt(entry.getLeft().x);
            buf.writeVarInt(entry.getLeft().z);
            buf.writeIdentifier(entry.getRight());
        }
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_PUT_GLOBAL_TILE;
    }
}
