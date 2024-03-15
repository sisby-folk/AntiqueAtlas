package folk.sisby.antique_atlas.core.scanning;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.tile.TileTypes;
import folk.sisby.antique_atlas.tile.TileElevation;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;

/**
 * Detects the 256 vanilla biomes, water pools and lava pools.
 * Water and beach biomes are given priority because shore line is the defining
 * feature of the map, and so that rivers are more connected.
 *
 * @author Hunternif
 */
public class TileDetectorBase implements BiomeScanner {
    /**
     * Biome used for occasional pools of water.
     * This used our own representation of biomes, but this was switched to Minecraft biomes.
     * So in absence of a better idea, this will just count as River from now on.
     */
    private static final Identifier waterPoolBiome = BiomeKeys.RIVER.getValue();

    /**
     * Increment the counter for water biomes by this much during iteration.
     * This is done so that water pools are more visible.
     */
    private static final int priorityRavine = 12;
    private static final int priorityWaterPool = 4;
    private static final int priorityIce = 3;
    private static final int priorityLavaPool = 6;

    /**
     * Minimum depth in the ground to be considered a ravine
     */
    private static final int ravineMinDepth = 7;

    protected static int priorityForBiome(RegistryEntry<Biome> biomeTag) {
        if (biomeTag.isIn(BiomeTags.IS_OCEAN) || biomeTag.isIn(BiomeTags.IS_RIVER) || biomeTag.isIn(BiomeTags.IS_DEEP_OCEAN)) {
            return 4;
        } else if (biomeTag.isIn(BiomeTags.IS_BEACH)) {
            return 3;
        } else {
            return 1;
        }
    }

    protected static TileElevation getHeightTypeFromY(int y, int seaLevel) {
        if (y < seaLevel + 10) {
            return TileElevation.VALLEY;
        }
        if (y < seaLevel + 20) {
            return TileElevation.LOW;
        }
        if (y < seaLevel + 35) {
            return TileElevation.MID;
        }
        if (y < seaLevel + 50) {
            return TileElevation.HIGH;
        }
        return TileElevation.PEAK;
    }

    protected static Identifier getBiomeIdentifier(World world, Biome biome) {
        return world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
    }

    protected static void updateOccurrencesMap(Multiset<Identifier> map, Identifier biome, int weight) {
        map.add(biome, weight);
    }

    protected static void updateOccurrencesMap(Multiset<Identifier> map, World world, Biome biome, TileElevation type, int weight) {
        Identifier id = getBiomeIdentifier(world, biome);
        id = new Identifier(id.getNamespace(), id.getPath() + "_" + type.getName());
        map.add(id, weight);
    }

    @Override
    public int getScanRadius() {
        return AntiqueAtlas.CONFIG.Performance.scanRadius;
    }

    /**
     * If no valid biome ID is found, returns null.
     *
     * @return the detected biome ID for the given chunk
     */
    @Override
    public Identifier getBiomeID(World world, Chunk chunk) {
        Multiset<Identifier> biomeOccurrences = HashMultiset.create(world.getRegistryManager().get(RegistryKeys.BIOME).size());
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // biomes seems to be changing with height as well. Let's scan at sea level.
                Biome biome = chunk.getBiomeForNoiseGen(x, world.getSeaLevel(), z).value();
                RegistryEntry<Biome> biomeTag = biomeRegistry.entryOf(biomeRegistry.getKey(biome).orElse(null));

                // get top block
                int y = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING).get(x, z);

                if (AntiqueAtlas.CONFIG.Performance.doScanPonds) {
                    if (y > 0) {
                        Block topBlock = chunk.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
                        // Check if there's surface of water at (x, z), but not swamp
                        if (topBlock == Blocks.WATER) {

                            if (biomeTag.isIn(ConventionalBiomeTags.SWAMP)) {
                                updateOccurrencesMap(biomeOccurrences, TileTypes.SWAMP_WATER.getId(), priorityWaterPool);
                            } else {
                                updateOccurrencesMap(biomeOccurrences, waterPoolBiome, priorityWaterPool);
                            }
                        } else if (topBlock == Blocks.ICE) {
                            updateOccurrencesMap(biomeOccurrences, TileTypes.FROZEN_RIVER.getId(), priorityIce);
                        } else if (topBlock == Blocks.LAVA) {
                            updateOccurrencesMap(biomeOccurrences, TileTypes.TILE_LAVA.getId(), priorityLavaPool);
                        }
                    }
                }

                if (AntiqueAtlas.CONFIG.Performance.doScanRavines) {
                    if (y > 0 && y < world.getSeaLevel() - ravineMinDepth) {
                        updateOccurrencesMap(biomeOccurrences, TileTypes.TILE_RAVINE.getId(), priorityRavine);
                    }
                }

                updateOccurrencesMap(biomeOccurrences, world, biome, getHeightTypeFromY(y, world.getSeaLevel()), priorityForBiome(biomeTag));
            }
        }

        if (biomeOccurrences.isEmpty()) return null;
        return biomeOccurrences.entrySet().stream().max(Ordering.natural().onResultOf(Multiset.Entry::getCount)).orElseThrow().getElement();
    }
}