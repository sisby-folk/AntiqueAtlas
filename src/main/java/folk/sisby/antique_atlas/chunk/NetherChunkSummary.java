package folk.sisby.antique_atlas.chunk;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import folk.sisby.antique_atlas.tile.TileTypes;
import folk.sisby.antique_atlas.tile.TileType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class NetherChunkSummary implements ChunkSummary {
    public static final ChunkSummaryFactory<NetherChunkSummary> FACTORY = new ChunkSummaryFactory<>() {
        @Override
        public NetherChunkSummary fromChunk(World world, Chunk chunk) {
            return new NetherChunkSummary(world, chunk);
        }

        @Override
        public NetherChunkSummary fromNbt(NbtCompound nbt) {
            return new NetherChunkSummary(nbt);
        }
    };

    private static final int SEA_LEVEL = 31;
    private static final int CAVERN_LEVEL = 50;

    private static final String KEY_BIOME = "biome";
    private static final String KEY_SEA_BLOCK = "seaBlock";
    private static final String KEY_CAVERN_BLOCK = "cavernBlock";

    private final Identifier[][] xzBiomes = new Identifier[16][16];
    private final Identifier[][] xzSeaBlocks = new Identifier[16][16];
    private final Identifier[][] xzCavernBlocks = new Identifier[16][16];

    public NetherChunkSummary(World world, Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                xzBiomes[x][z] = world.getRegistryManager().get(RegistryKeys.BIOME).getId(chunk.getBiomeForNoiseGen(x, world.getSeaLevel(), z).value());
                xzSeaBlocks[x][z] = world.getRegistryManager().get(RegistryKeys.BLOCK).getId(chunk.getBlockState(new BlockPos(x, SEA_LEVEL, z)).getBlock());
                xzCavernBlocks[x][z] = world.getRegistryManager().get(RegistryKeys.BLOCK).getId(chunk.getBlockState(new BlockPos(x, CAVERN_LEVEL, z)).getBlock());
            }
        }
    }

    public NetherChunkSummary(NbtCompound nbt) {
        NbtList biomeList = nbt.getList(KEY_BIOME, NbtElement.STRING_TYPE);
        for (int i = 0; i < biomeList.size(); i++) {
            xzBiomes[i / 16][i % 16] = new Identifier(biomeList.get(i).asString());
        }
        NbtList seaBlockList = nbt.getList(KEY_SEA_BLOCK, NbtElement.STRING_TYPE);
        for (int i = 0; i < seaBlockList.size(); i++) {
            xzSeaBlocks[i / 16][i % 16] = new Identifier(seaBlockList.get(i).asString());
        }
        NbtList cavernBlockList = nbt.getList(KEY_CAVERN_BLOCK, NbtElement.STRING_TYPE);
        for (int i = 0; i < cavernBlockList.size(); i++) {
            xzCavernBlocks[i / 16][i % 16] = new Identifier(cavernBlockList.get(i).asString());
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList biomeList = new NbtList();
        for (Identifier[] x : xzBiomes) {
            for (Identifier z : x) {
                biomeList.add(NbtString.of(z.toString()));
            }
        }
        nbt.put(KEY_BIOME, biomeList);

        NbtList seaBlockList = new NbtList();
        for (Identifier[] x : xzSeaBlocks) {
            for (Identifier z : x) {
                seaBlockList.add(NbtString.of(z.toString()));
            }
        }
        nbt.put(KEY_SEA_BLOCK, seaBlockList);

        NbtList cavernBlockList = new NbtList();
        for (Identifier[] x : xzCavernBlocks) {
            for (Identifier z : x) {
                cavernBlockList.add(NbtString.of(z.toString()));
            }
        }
        nbt.put(KEY_CAVERN_BLOCK, cavernBlockList);
        return nbt;
    }

    @Override
    public TileType toTileType(World world) {
        Multiset<TileType> possibleTiles = HashMultiset.create(world.getRegistryManager().get(RegistryKeys.BIOME).size());
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biomeRegistry.get(xzBiomes[x][z]));
                Block seaBlock = world.getRegistryManager().get(RegistryKeys.BLOCK).get(xzSeaBlocks[x][z]);
                Block cavernBlock = world.getRegistryManager().get(RegistryKeys.BLOCK).get(xzCavernBlocks[x][z]);

                if (biomeEntry.isIn(BiomeTags.IS_NETHER)) {
                    if (seaBlock == Blocks.LAVA) {
                        possibleTiles.add(TileTypes.TILE_LAVA, 6);
                    } else if (cavernBlock == Blocks.AIR) {
                        possibleTiles.add(TileTypes.TILE_LAVA_SHORE, 1);
                    } else {
                        possibleTiles.add(TileType.of(xzBiomes[x][z]), 2);
                    }
                } else {
                    possibleTiles.add(TileType.of(xzBiomes[x][z]), priorityForBiome(biomeEntry));
                }
            }
        }

        if (possibleTiles.isEmpty()) return null;
        return possibleTiles.entrySet().stream().max(Ordering.natural().onResultOf(Multiset.Entry::getCount)).orElseThrow().getElement();
    }
}
