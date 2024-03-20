package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.BiomeTileProviders;
import folk.sisby.surveyor.SurveyorWorld;
import folk.sisby.surveyor.terrain.ChunkSummary;
import folk.sisby.surveyor.terrain.LayerSummary;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
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
public class TerrainTiling {
    public static final int EMPTY_PRIORITY = 16;
    public static final int RAVINE_PRIORITY = 12;
    public static final int LAVA_PRIORITY = 6;
    public static final int WATER_PRIORITY = 4;
    public static final int ICE_PRIORITY = 3;
    public static final int BEACH_PRIORITY = 3;

    public static final List<Identifier> CUSTOM_TILES = List.of(
        FeatureTiles.BEDROCK_ROOF,
        FeatureTiles.EMPTY,
        FeatureTiles.END_VOID,
        FeatureTiles.WATER,
        FeatureTiles.ICE,
        FeatureTiles.TILE_RAVINE,
        FeatureTiles.SWAMP_WATER,
        FeatureTiles.TILE_LAVA,
        FeatureTiles.TILE_LAVA_SHORE
    );

    public static final int NETHER_SCAN_HEIGHT = 50;
    public static final Map<Biome, Integer> priorityCache = new Reference2IntArrayMap<>();
    public static final Map<Biome, Boolean> swampCache = new Reference2BooleanArrayMap<>();

    protected static int priorityForBiome(Registry<Biome> biomeRegistry, Biome biome) {
        if (!priorityCache.containsKey(biome)) {
            RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biome);
            if (biomeEntry.isIn(BiomeTags.IS_BEACH)) {
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

    protected static Pair<TerrainTileProvider, TileElevation> frequencyToTexture(ChunkPos pos, int[][] possibleTiles, Registry<Biome> biomeRegistry, IndexedIterable<Biome> biomePalette) {
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
        Identifier providerId = customTileIndex >= 0 ? CUSTOM_TILES.get(customTileIndex) : biomeRegistry.getId(biomePalette.get(biomeIndex));
        return Pair.of(BiomeTileProviders.getInstance().getTileProvider(providerId), elevationOrdinal == TileElevation.values().length ? null : TileElevation.values()[elevationOrdinal]);
    }

    public static Pair<TerrainTileProvider, TileElevation> terrainToTile(World world, ChunkPos pos) {
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        int defaultTile = CUSTOM_TILES.indexOf(world.getDimension().hasCeiling() ? FeatureTiles.BEDROCK_ROOF : (world.getRegistryKey() == World.END ? FeatureTiles.END_VOID : FeatureTiles.EMPTY));
        boolean checkRavines = world.getRegistryKey() == World.OVERWORLD;

        int worldHeight = world.getTopY();
        ChunkSummary chunk = ((SurveyorWorld) world).surveyor$getWorldSummary().terrain().get(pos);
        @Nullable LayerSummary.Raw summary = chunk.toSingleLayer(null, null, world.getTopY());
        IndexedIterable<Biome> biomePalette = ((SurveyorWorld) world).surveyor$getWorldSummary().terrain().getBiomePalette(pos);
        IndexedIterable<Block> blockPalette = ((SurveyorWorld) world).surveyor$getWorldSummary().terrain().getBlockPalette(pos);
        if (summary == null) return Pair.of(BiomeTileProviders.getInstance().getTileProvider(CUSTOM_TILES.get(defaultTile)), null);

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

            if (checkRavines && height - world.getSeaLevel() < -7) {
                possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(FeatureTiles.TILE_RAVINE)] += RAVINE_PRIORITY;
            } else if (summary.waterDepths()[i] > 0) {
                possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(isSwamp(biomeRegistry, biome) ? FeatureTiles.SWAMP_WATER : FeatureTiles.WATER)] += WATER_PRIORITY;
            } else if (block == Blocks.ICE) {
                possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(FeatureTiles.ICE)] += ICE_PRIORITY;
            } else if (block == Blocks.LAVA) {
                possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(FeatureTiles.TILE_LAVA)] += LAVA_PRIORITY;
            }
            possibleTiles[TileElevation.fromBlocksAboveSea(height - world.getSeaLevel()).ordinal()][summary.biomes()[i]] += priorityForBiome(biomeRegistry, biome);
        }

        return frequencyToTexture(pos, possibleTiles, biomeRegistry, biomePalette);
    }

    public static Pair<TerrainTileProvider, TileElevation> terrainToTileNether(World world, ChunkPos pos) {
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        int defaultTile = CUSTOM_TILES.indexOf(world.getDimension().hasCeiling() ? FeatureTiles.BEDROCK_ROOF : (world.getRegistryKey() == World.END ? FeatureTiles.END_VOID : FeatureTiles.EMPTY));

        ChunkSummary chunk = ((SurveyorWorld) world).surveyor$getWorldSummary().terrain().get(pos);
        @Nullable LayerSummary.Raw lowSummary = chunk.toSingleLayer(null, NETHER_SCAN_HEIGHT, world.getTopY());
        @Nullable LayerSummary.Raw fullSummary = chunk.toSingleLayer(null, world.getBottomY() + world.getDimension().logicalHeight() - 1, world.getTopY());
        IndexedIterable<Biome> biomePalette = ((SurveyorWorld) world).surveyor$getWorldSummary().terrain().getBiomePalette(pos);
        IndexedIterable<Block> blockPalette = ((SurveyorWorld) world).surveyor$getWorldSummary().terrain().getBlockPalette(pos);

        int elevationSize = TileElevation.values().length;
        int elevationCount = elevationSize + 1;
        int biomeCount = biomePalette.size();
        int baseTileCount = biomeCount + CUSTOM_TILES.size();
        int[][] possibleTiles = new int[elevationCount][baseTileCount];

        if (fullSummary == null) {
            return Pair.of(BiomeTileProviders.getInstance().getTileProvider(CUSTOM_TILES.get(defaultTile)), null);
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
                        possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(FeatureTiles.TILE_LAVA)] += LAVA_PRIORITY;
                    } else { // Low Floor
                        possibleTiles[elevationSize][biomeCount + CUSTOM_TILES.indexOf(FeatureTiles.TILE_LAVA_SHORE)] += BEACH_PRIORITY;
                    }
                }
            }
        }

        return frequencyToTexture(pos, possibleTiles, biomeRegistry, biomePalette);
    }
}