package folk.sisby.antique_atlas.chunk;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import folk.sisby.antique_atlas.tile.TileTypes;
import folk.sisby.antique_atlas.tile.TileElevation;
import folk.sisby.antique_atlas.tile.TileType;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;

import java.util.Arrays;

public class OverworldChunkSummary implements ChunkSummary {
    public static final ChunkSummaryFactory<OverworldChunkSummary> FACTORY = new ChunkSummaryFactory<>() {
        @Override
        public OverworldChunkSummary fromChunk(World world, Chunk chunk) {
            return new OverworldChunkSummary(world, chunk);
        }

        @Override
        public OverworldChunkSummary fromNbt(NbtCompound nbt) {
            return new OverworldChunkSummary(nbt);
        }
    };

    private static final String KEY_HEIGHT = "height";
    private static final String KEY_BIOME = "biome";
    private static final String KEY_BLOCK = "block";

    protected final int[][] xzHeights = new int[16][16];
    protected final Identifier[][] xzBiomes = new Identifier[16][16];
    protected final Identifier[][] xzTopBlocks = new Identifier[16][16];

    public OverworldChunkSummary(World world, Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                xzBiomes[x][z] = world.getRegistryManager().get(RegistryKeys.BIOME).getId(chunk.getBiomeForNoiseGen(x, world.getSeaLevel(), z).value());
                xzHeights[x][z] = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING).get(x, z);
                xzTopBlocks[x][z] = world.getRegistryManager().get(RegistryKeys.BLOCK).getId(chunk.getBlockState(new BlockPos(x, xzHeights[x][z] - 1, z)).getBlock());
            }
        }
    }

    public OverworldChunkSummary(NbtCompound nbt) {
        int[] heightArray = nbt.getIntArray(KEY_HEIGHT);
        for (int i = 0; i < heightArray.length; i++) {
            xzHeights[i / 16][i % 16] = heightArray[i];
        }
        NbtList biomeList = nbt.getList(KEY_BIOME, NbtElement.STRING_TYPE);
        for (int i = 0; i < biomeList.size(); i++) {
            xzBiomes[i / 16][i % 16] = new Identifier(biomeList.get(i).asString());
        }
        NbtList blockList = nbt.getList(KEY_BLOCK, NbtElement.STRING_TYPE);
        for (int i = 0; i < blockList.size(); i++) {
            xzTopBlocks[i / 16][i % 16] = new Identifier(blockList.get(i).asString());
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtIntArray heightArray = new NbtIntArray(Arrays.stream(xzHeights).flatMapToInt(Arrays::stream).toArray());
        nbt.put(KEY_HEIGHT, heightArray);

        NbtList biomeList = new NbtList();
        for (Identifier[] x : xzBiomes) {
            for (Identifier z : x) {
                biomeList.add(NbtString.of(z.toString()));
            }
        }
        nbt.put(KEY_BIOME, biomeList);

        NbtList blockList = new NbtList();
        for (Identifier[] x : xzTopBlocks) {
            for (Identifier z : x) {
                blockList.add(NbtString.of(z.toString()));
            }
        }
        nbt.put(KEY_BLOCK, blockList);
        return nbt;
    }

    @Override
    public TileType toTileType(World world) {
        Multiset<TileType> possibleTiles = HashMultiset.create(world.getRegistryManager().get(RegistryKeys.BIOME).size());
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = xzHeights[x][z];
                RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biomeRegistry.get(xzBiomes[x][z]));
                Block block = world.getRegistryManager().get(RegistryKeys.BLOCK).get(xzTopBlocks[x][z]);

                if (height == world.getBottomY()) {
                    possibleTiles.add(TileType.of(BiomeKeys.THE_VOID), 16);
                } else if (height - world.getSeaLevel() < -7) {
                    possibleTiles.add(TileTypes.TILE_RAVINE, 12);
                } else if (block == Blocks.WATER) {
                    possibleTiles.add(biomeEntry.isIn(ConventionalBiomeTags.SWAMP) ? TileTypes.SWAMP_WATER : TileType.of(BiomeKeys.RIVER), 4);
                } else if (block == Blocks.LAVA) {
                    possibleTiles.add(TileTypes.TILE_LAVA, 6);
                }
                possibleTiles.add(new TileType(xzBiomes[x][z], TileElevation.fromBlocksAboveSea(height - world.getSeaLevel())), priorityForBiome(biomeEntry));
            }
        }

        if (possibleTiles.isEmpty()) return null;
        return possibleTiles.entrySet().stream().max(Ordering.natural().onResultOf(Multiset.Entry::getCount)).orElseThrow().getElement();
    }
}
