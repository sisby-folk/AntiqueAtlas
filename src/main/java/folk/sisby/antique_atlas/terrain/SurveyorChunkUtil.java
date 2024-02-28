package folk.sisby.antique_atlas.terrain;

import folk.sisby.antique_atlas.tile.TileElevation;
import folk.sisby.antique_atlas.tile.TileType;
import folk.sisby.antique_atlas.tile.TileTypes;
import folk.sisby.surveyor.SurveyorWorld;
import folk.sisby.surveyor.chunk.ChunkSummary;
import folk.sisby.surveyor.chunk.LayerSummary;
import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Hottest class in the mod. Might get ugly.
 */
public class SurveyorChunkUtil {
    public static final int EMPTY_PRIORITY = 16;
    public static final int RAVINE_PRIORITY = 12;
    public static final int LAVA_PRIORITY = 6;
    public static final int WATER_PRIORITY = 4;
    public static final int BEACH_PRIORITY = 3;

    public static final List<TileType> CUSTOM_TILES = List.of(
        TileTypes.NETHER_WASTES,
        TileTypes.THE_VOID,
        TileTypes.RIVER,
        TileTypes.TILE_RAVINE,
        TileTypes.SWAMP_WATER,
        TileTypes.TILE_LAVA,
        TileTypes.TILE_LAVA_SHORE
    );

    public static final int NETHER_SCAN_HEIGHT = 50;
    public static final Map<Biome, Integer> priorityCache = new Reference2IntArrayMap<>();
    public static final Map<Biome, Boolean> swampCache = new Reference2BooleanArrayMap<>();

    protected static int priorityForBiome(Registry<Biome> biomeRegistry, Biome biome) {
        if (!priorityCache.containsKey(biome)) {
            RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biome);
            if (biomeEntry.isIn(BiomeTags.IS_OCEAN) || biomeEntry.isIn(BiomeTags.IS_RIVER) || biomeEntry.isIn(BiomeTags.IS_DEEP_OCEAN)) {
                priorityCache.put(biome, WATER_PRIORITY);
            } else if (biomeEntry.isIn(BiomeTags.IS_BEACH)) {
                priorityCache.put(biome, BEACH_PRIORITY);
            } else if (biomeEntry.isIn(BiomeTags.IS_NETHER)) {
                priorityCache.put(biome, 2);
            } else {
                priorityCache.put(biome, 1);
            }
        }
        return priorityCache.get(biome);
    }

    protected static boolean isSwamp(Registry<Biome> biomeRegistry, Biome biome) {
        if (!swampCache.containsKey(biome)) {
            RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biome);
            swampCache.put(biome, biomeEntry.isIn(ConventionalBiomeTags.SWAMP));
        }
        return swampCache.get(biome);
    }

    protected static TileType frequencyToTile(int[][] possibleTiles, Registry<Biome> biomeRegistry, IndexedIterable<Biome> biomePalette) {
        int elevationOrdinal = -1;
        int biomeIndex = -1;
        int bestFrequency = 0;
        for (int i = 0; i < possibleTiles.length; i++) {
            for (int j = 0; j < possibleTiles[i].length; j++) {
                if (possibleTiles[i][j] > bestFrequency) {
                    elevationOrdinal = i;
                    biomeIndex = j;
                    bestFrequency = possibleTiles[i][j];
                }
            }
        }
        if (bestFrequency == 0) return null;
        int customTileIndex = biomeIndex - possibleTiles[0].length + CUSTOM_TILES.size();
        if (customTileIndex >= 0) return CUSTOM_TILES.get(customTileIndex);
        if (elevationOrdinal == TileElevation.values().length) {
            return new TileType(biomeRegistry, biomePalette.get(biomeIndex));
        } else {
            return new TileType(biomeRegistry, biomePalette.get(biomeIndex), TileElevation.values()[elevationOrdinal]);
        }
    }

    public static TileType terrainToTile(World world, ChunkPos pos) {
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        int defaultTile = CUSTOM_TILES.indexOf(world.getDimension().hasCeiling() ? TileTypes.NETHER_WASTES : TileTypes.THE_VOID);

        int worldHeight = world.getTopY();
        ChunkSummary chunk = ((SurveyorWorld) world).surveyor$getWorldSummary().getChunk(pos);
        @Nullable LayerSummary.Raw summary = chunk.toSingleLayer(null, null, world.getTopY());
        IndexedIterable<Biome> biomePalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBiomePalette(pos);
        IndexedIterable<Block> blockPalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBlockPalette(pos);
        if (summary == null) return null;


        int elevationSize = TileElevation.values().length;
        int elevationCount = elevationSize + 1;
        int biomeCount = biomePalette.size();
        int baseTileCount = biomeCount + CUSTOM_TILES.size();
        int[][] possibleTiles = new int[elevationCount][baseTileCount];

        for (int i = 0; i < summary.depths().length; i++) {
            if (summary.depths()[i] == -1) {
                possibleTiles[elevationSize][defaultTile] += EMPTY_PRIORITY;
                continue;
            }
            int height = worldHeight - summary.depths()[i] + summary.waterDepths()[i];
            Block block = blockPalette.get(summary.blocks()[i]);
            Biome biome = biomePalette.get(summary.biomes()[i]);

            if (height - world.getSeaLevel() < -7) {
                possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(TileTypes.TILE_RAVINE)] += RAVINE_PRIORITY;
            } else if (summary.waterDepths()[i] > 0) {
                possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(isSwamp(biomeRegistry, biome) ? TileTypes.SWAMP_WATER : TileTypes.RIVER)] += WATER_PRIORITY;
            } else if (block == Blocks.LAVA) {
                possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(TileTypes.TILE_LAVA)] += LAVA_PRIORITY;
            }
            possibleTiles[TileElevation.fromBlocksAboveSea(height - world.getSeaLevel()).ordinal()][summary.biomes()[i]] += priorityForBiome(biomeRegistry, biome);
        }

        return frequencyToTile(possibleTiles, biomeRegistry, biomePalette);
    }

    public static TileType terrainToTileNether(World world, ChunkPos pos) {
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        int defaultTile = CUSTOM_TILES.indexOf(world.getDimension().hasCeiling() ? TileTypes.NETHER_WASTES : TileTypes.THE_VOID);

        ChunkSummary chunk = ((SurveyorWorld) world).surveyor$getWorldSummary().getChunk(pos);
        @Nullable LayerSummary.Raw lowSummary = chunk.toSingleLayer(null, NETHER_SCAN_HEIGHT, world.getTopY());
        @Nullable LayerSummary.Raw fullSummary = chunk.toSingleLayer(null, world.getBottomY() + world.getDimension().logicalHeight() - 1, world.getTopY());
        IndexedIterable<Biome> biomePalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBiomePalette(pos);
        IndexedIterable<Block> blockPalette = ((SurveyorWorld) world).surveyor$getWorldSummary().getBlockPalette(pos);

        int elevationSize = TileElevation.values().length;
        int elevationCount = elevationSize + 1;
        int biomeCount = biomePalette.size();
        int baseTileCount = biomeCount + CUSTOM_TILES.size();
        int[][] possibleTiles = new int[elevationCount][baseTileCount];

        if (fullSummary == null) {
            return CUSTOM_TILES.get(defaultTile);
        }

        if (lowSummary == null) {
            for (int i = 0; i < fullSummary.depths().length; i++) {
                if (fullSummary.depths()[i] == -1) {
                    possibleTiles[elevationSize][defaultTile] += EMPTY_PRIORITY;
                } else {
                    Biome biome = biomePalette.get(i);
                    possibleTiles[elevationSize][fullSummary.biomes()[i]] += priorityForBiome(biomeRegistry, biome);
                }
            }
        } else {
            for (int i = 0; i < lowSummary.depths().length; i++) {
                if (lowSummary.depths()[i] == -1) {
                    Biome biome = biomePalette.get(fullSummary.biomes()[i]);
                    possibleTiles[elevationSize][fullSummary.biomes()[i]] += priorityForBiome(biomeRegistry, biome);
                } else {
                    Block block = blockPalette.get(lowSummary.blocks()[i]);
                    if (block == Blocks.LAVA) { // Lava Sea
                        possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(TileTypes.TILE_LAVA)] += LAVA_PRIORITY;
                    } else { // Low Floor
                        possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(TileTypes.TILE_LAVA_SHORE)] += BEACH_PRIORITY;
                    }
                }
            }
        }

        return frequencyToTile(possibleTiles, biomeRegistry, biomePalette);
    }
}
