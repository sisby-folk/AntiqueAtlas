package folk.sisby.antique_atlas.chunk;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import folk.sisby.antique_atlas.tile.TileType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;

public class EndChunkSummary extends OverworldChunkSummary implements ChunkSummary {
    public static final ChunkSummaryFactory<EndChunkSummary> FACTORY = new ChunkSummaryFactory<>() {
        @Override
        public EndChunkSummary fromChunk(World world, Chunk chunk) {
            return new EndChunkSummary(world, chunk);
        }

        @Override
        public EndChunkSummary fromNbt(NbtCompound nbt) {
            return new EndChunkSummary(nbt);
        }
    };

    public EndChunkSummary(World world, Chunk chunk) {
        super(world, chunk);
    }

    public EndChunkSummary(NbtCompound nbt) {
        super(nbt);
    }

    @Override
    public TileType toTileType(World world) {
        Multiset<TileType> possibleTiles = HashMultiset.create(world.getRegistryManager().get(RegistryKeys.BIOME).size());

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Block block = world.getRegistryManager().get(RegistryKeys.BLOCK).get(xzTopBlocks[x][z]);

                if (xzBiomes[x][z] == BiomeKeys.THE_VOID.getValue() || block == Blocks.AIR) {
                    possibleTiles.add(TileType.of(BiomeKeys.THE_VOID), 1);
                } else {
                    possibleTiles.add(TileType.of(xzBiomes[x][z]), 3);
                }
            }
        }

        if (possibleTiles.isEmpty()) return null;
        return possibleTiles.entrySet().stream().max(Ordering.natural().onResultOf(Multiset.Entry::getCount)).orElseThrow().getElement();
    }
}
