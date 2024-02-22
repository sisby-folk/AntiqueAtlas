package folk.sisby.antique_atlas.terrain;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import folk.sisby.antique_atlas.AntiqueAtlas;
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
    protected static int priorityForBiome(RegistryEntry<Biome> biomeTag) {
        if (biomeTag.isIn(BiomeTags.IS_OCEAN) || biomeTag.isIn(BiomeTags.IS_RIVER) || biomeTag.isIn(BiomeTags.IS_DEEP_OCEAN)) {
            return 4;
        } else if (biomeTag.isIn(BiomeTags.IS_BEACH)) {
            return 3;
        } else {
            return 1;
        }
    }

    public static TileType terrainToTile(World world, ChunkPos pos) {
        Multiset<TileType> possibleTiles = HashMultiset.create(world.getRegistryManager().get(RegistryKeys.BIOME).size());
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);

        ChunkSummary chunk = ((SurveyorWorld) world).surveyor$getWorldSummary().getChunk(pos);
        if (chunk == null) {
            AntiqueAtlas.LOGGER.info("what the hell man at {}", pos);
            return null;
        }
        LayerSummary summary = null;
        try {
            summary = chunk.toSingleLayer(null, null, world.getTopY());
        } catch (Exception e) {
            return null;
        }
        IndexedIterable<Biome> biomePalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBiomePalette(pos);
        IndexedIterable<Block> blockPalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBlockPalette(pos);
        if (summary == null) return null;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (summary.isEmpty(x, z)) continue;
                int height = summary.getHeight(x, z, world.getTopY());
                Biome biome = summary.getBiome(x, z, biomePalette);
                RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biome);
                Block block = summary.getBlock(x, z, blockPalette);

                if (height == world.getBottomY()) {
                    possibleTiles.add(TileType.of(BiomeKeys.THE_VOID), 16);
                } else if (height - world.getSeaLevel() < -7) {
                    possibleTiles.add(TileTypes.TILE_RAVINE, 12);
                } else if (block == Blocks.WATER) {
                    possibleTiles.add(biomeEntry.isIn(ConventionalBiomeTags.SWAMP) ? TileTypes.SWAMP_WATER : TileType.of(BiomeKeys.RIVER), 4);
                } else if (block == Blocks.LAVA) {
                    possibleTiles.add(TileTypes.TILE_LAVA, 6);
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

        LayerSummary summary = ((SurveyorWorld) world).surveyor$getWorldSummary().getChunk(pos).toSingleLayer(null, 50, world.getTopY());
        IndexedIterable<Biome> biomePalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBiomePalette(pos);
        IndexedIterable<Block> blockPalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBlockPalette(pos);
        if (summary == null) return null;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Biome biome = summary.getBiome(x, z, biomePalette);
                Block block = summary.getBlock(x, z, blockPalette);
                if (block == Blocks.LAVA) { // Lava Sea
                    possibleTiles.add(TileTypes.TILE_LAVA, 6);
                } else if (!summary.isEmpty(x, z)) { // Low Floor
                    possibleTiles.add(TileTypes.TILE_LAVA_SHORE, 1);
                } else { // Solid block - biome above
                    possibleTiles.add(TileType.of(biomeRegistry.getId(biome)), 2);
                }
            }
        }

        if (possibleTiles.isEmpty()) return null;
        return possibleTiles.entrySet().stream().max(Ordering.natural().onResultOf(Multiset.Entry::getCount)).orElseThrow().getElement();
    }

    public static TileType terrainToTileEnd(World world, ChunkPos pos) {
        Multiset<TileType> possibleTiles = HashMultiset.create(world.getRegistryManager().get(RegistryKeys.BIOME).size());
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);

        LayerSummary summary = ((SurveyorWorld) world).surveyor$getWorldSummary().getChunk(pos).toSingleLayer(null, null, world.getTopY());
        IndexedIterable<Biome> biomePalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBiomePalette(pos);
        if (summary == null) return null;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Biome biome = summary.getBiome(x, z, biomePalette);
                if (summary.isEmpty(x, z)) {
                    possibleTiles.add(TileType.of(BiomeKeys.THE_VOID), 1);
                } else {
                    possibleTiles.add(TileType.of(biomeRegistry.getId(biome)), 3);
                }
            }
        }

        if (possibleTiles.isEmpty()) return null;
        return possibleTiles.entrySet().stream().max(Ordering.natural().onResultOf(Multiset.Entry::getCount)).orElseThrow().getElement();
    }
}
