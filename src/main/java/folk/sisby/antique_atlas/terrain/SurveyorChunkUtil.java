package folk.sisby.antique_atlas.terrain;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import folk.sisby.antique_atlas.tile.TileElevation;
import folk.sisby.antique_atlas.tile.TileType;
import folk.sisby.antique_atlas.tile.TileTypes;
import folk.sisby.surveyor.SurveyorWorld;
import folk.sisby.surveyor.chunk.ChunkSummary;
import folk.sisby.surveyor.chunk.LayerSummary;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class SurveyorChunkUtil {
    public static final int RAVINE_PRIORITY = 12;
    public static final int LAVA_PRIORITY = 6;
    public static final int WATER_PRIORITY = 4;
    public static final int BEACH_PRIORITY = 3;

    public static final int NETHER_SCAN_HEIGHT = 50;

    protected static int priorityForBiome(RegistryEntry<Biome> biomeTag) {
        if (biomeTag.isIn(BiomeTags.IS_OCEAN) || biomeTag.isIn(BiomeTags.IS_RIVER) || biomeTag.isIn(BiomeTags.IS_DEEP_OCEAN)) {
            return WATER_PRIORITY;
        } else if (biomeTag.isIn(BiomeTags.IS_BEACH)) {
            return BEACH_PRIORITY;
        } else if (biomeTag.isIn(BiomeTags.IS_NETHER)) {
            return 2;
        } else {
            return 1;
        }
    }

    public static TileType terrainToTile(World world, ChunkPos pos) {
        Multiset<TileType> possibleTiles = HashMultiset.create(world.getRegistryManager().get(RegistryKeys.BIOME).size());
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        boolean hasCeiling = world.getDimension().hasCeiling();

        ChunkSummary chunk = ((SurveyorWorld) world).surveyor$getWorldSummary().getChunk(pos);
        LayerSummary summary = chunk.toSingleLayer(null, null, world.getTopY());
        IndexedIterable<Biome> biomePalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBiomePalette(pos);
        IndexedIterable<Block> blockPalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBlockPalette(pos);
        if (summary == null) return null;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (summary.isEmpty(x, z)) {
                    possibleTiles.add(hasCeiling ? TileType.of(BiomeKeys.NETHER_WASTES) : TileType.of(BiomeKeys.THE_VOID), 16);
                    continue;
                }
                int height = summary.getHeight(x, z, world.getTopY(), false);
                Biome biome = summary.getBiome(x, z, biomePalette);
                RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biome);
                Block block = summary.getBlock(x, z, blockPalette, false);

                if (height - world.getSeaLevel() < -7) {
                    possibleTiles.add(TileTypes.TILE_RAVINE, RAVINE_PRIORITY);
                } else if (block == Blocks.WATER) {
                    possibleTiles.add(biomeEntry.isIn(ConventionalBiomeTags.SWAMP) ? TileTypes.SWAMP_WATER : TileType.of(BiomeKeys.RIVER), WATER_PRIORITY);
                } else if (block == Blocks.LAVA) {
                    possibleTiles.add(TileTypes.TILE_LAVA, LAVA_PRIORITY);
                }
                possibleTiles.add(new TileType(biomeRegistry.getId(biome), TileElevation.fromBlocksAboveSea(height - world.getSeaLevel())), priorityForBiome(biomeEntry));
            }
        }

        if (possibleTiles.isEmpty()) return null;
        return possibleTiles.entrySet().stream().max(Ordering.natural().onResultOf(Multiset.Entry::getCount)).orElseThrow().getElement();
    }

    public static TileType terrainToTileNether(World world, ChunkPos pos) {
        Multiset<TileType> possibleTiles = HashMultiset.create(world.getRegistryManager().get(RegistryKeys.BIOME).size());
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        boolean hasCeiling = world.getDimension().hasCeiling();

        ChunkSummary chunk = ((SurveyorWorld) world).surveyor$getWorldSummary().getChunk(pos);
        LayerSummary lowSummary = chunk.toSingleLayer(null, NETHER_SCAN_HEIGHT, world.getTopY());
        LayerSummary fullSummary = chunk.toSingleLayer(null, world.getBottomY() + world.getDimension().logicalHeight() - 1, world.getTopY());
        IndexedIterable<Biome> biomePalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBiomePalette(pos);
        IndexedIterable<Block> blockPalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBlockPalette(pos);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (fullSummary == null || fullSummary.isEmpty(x, z)) {
                    possibleTiles.add(hasCeiling ? TileType.of(BiomeKeys.NETHER_WASTES) : TileType.of(BiomeKeys.THE_VOID), 16);
                    continue;
                }
                Biome biome = fullSummary.getBiome(x, z, biomePalette);
                RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biome);

                if (lowSummary == null || lowSummary.isEmpty(x, z)) {
                    possibleTiles.add(TileType.of(biomeRegistry.getId(biome)), priorityForBiome(biomeEntry));
                } else {
                    Block block = lowSummary.getBlock(x, z, blockPalette, true);
                    if (block == Blocks.LAVA) { // Lava Sea
                        possibleTiles.add(TileTypes.TILE_LAVA, LAVA_PRIORITY);
                    } else { // Low Floor
                        possibleTiles.add(TileTypes.TILE_LAVA_SHORE, BEACH_PRIORITY);
                    }
                }
            }
        }

        if (possibleTiles.isEmpty()) return null;
        return possibleTiles.entrySet().stream().max(Ordering.natural().onResultOf(Multiset.Entry::getCount)).orElseThrow().getElement();
    }
}