package folk.sisby.antique_atlas.reloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.TextureSet;
import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.antique_atlas.gui.tiles.SubTile;
import folk.sisby.antique_atlas.tile.TileElevation;
import folk.sisby.antique_atlas.util.ForgeTags;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps biome IDs (or pseudo IDs) to textures. <i>Not thread-safe!</i>
 * <p>If several textures are set for one ID, one will be chosen at random when
 * putting tile into Atlas.</p>
 *
 * <p>Must be loaded after {@link TextureSets}!</p>
 *
 * @author Hunternif
 */
public class BiomeTextures extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final BiomeTextures INSTANCE = new BiomeTextures();

    public static final Identifier ID = AntiqueAtlas.id("biome_textures");
    public static final int VERSION = 2;

    public static BiomeTextures getInstance() {
        return INSTANCE;
    }

    /**
     * This map stores the pseudo biome texture mappings, any biome with ID <0 is assumed to be a pseudo biome
     */
    private final Map<Identifier, TextureSet> map = new HashMap<>();

    public BiomeTextures() {
        super(new Gson(), "atlas/tiles");
    }

    public boolean contains(Identifier id) {
        return map.containsKey(id);
    }

    public TextureSet getTextureSet(Identifier tile) {
        return tile == null ? TextureSets.getInstance().getDefault() : map.getOrDefault(tile, TextureSets.getInstance().getDefault());
    }

    public TileTexture getTexture(SubTile subTile) {
        return getTextureSet(subTile.tile).getTexture(subTile.variationNumber);
    }

    /**
     * Set a standard texture for the biome based on tags.
     */
    public void registerFallback(Identifier id, RegistryEntry<Biome> biome) {
        if (biome == null || id == null) {
            AntiqueAtlas.LOGGER.error("Given biome is null. Cannot autodetect a suitable texture set for that.");
            return;
        }

        Identifier fallbackBiome = getFallbackBiome(biome);

        if (fallbackBiome != null && contains(fallbackBiome)) {
            map.put(id, map.get(fallbackBiome));
            AntiqueAtlas.LOGGER.warn("[Antique Atlas] Set fallback biome for {} to {}. You can set a more fitting texture using a resource pack!", id, fallbackBiome);
        } else {
            AntiqueAtlas.LOGGER.error("[Antique Atlas] No fallback could be found for {}. This shouldn't happen! This means the biome is not in ANY conventional or vanilla tag on the client!", id.toString());
            setAllTextures(id, TextureSets.getInstance().getDefault());
        }
    }

    /**
     * Assign texture set to pseudo biome
     */
    private void setTexture(Identifier tileId, TextureSet textureSet) {
        if (tileId == null) return;

        if (textureSet == null) {
            if (map.remove(tileId) != null) {
                AntiqueAtlas.LOGGER.warn("Removing old texture for {}", tileId);
            }
            return;
        }

        map.put(tileId, textureSet);
    }

    /**
     * Assign the same texture set to all height variations of the tileId
     */
    private void setAllTextures(Identifier tileId, TextureSet textureSet) {
        setTexture(tileId, textureSet);

        for (TileElevation layer : TileElevation.values()) {
            setTexture(Identifier.tryParse(tileId + "_" + layer), textureSet);
        }
    }

    private static Identifier getFallbackBiome(RegistryEntry<Biome> biome) {
        if (biome.isIn(ConventionalBiomeTags.VOID) || biome.isIn(ForgeTags.Biomes.IS_VOID)) {
            return BiomeKeys.THE_VOID.getValue();
        } else if (biome.isIn(BiomeTags.IS_END) || biome.isIn(ConventionalBiomeTags.IN_THE_END) || biome.isIn(ConventionalBiomeTags.END_ISLANDS)) {
            if (biome.isIn(ConventionalBiomeTags.VEGETATION_DENSE) || biome.isIn(ConventionalBiomeTags.VEGETATION_SPARSE) || biome.isIn(ForgeTags.Biomes.IS_LUSH)) return BiomeKeys.END_HIGHLANDS.getValue();
            return BiomeKeys.END_BARRENS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.NETHER_FORESTS)) {
            return BiomeKeys.WARPED_FOREST.getValue();
        } else if (biome.isIn(BiomeTags.IS_NETHER) || biome.isIn(ConventionalBiomeTags.IN_NETHER)) {
            return BiomeKeys.SOUL_SAND_VALLEY.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.SWAMP) || biome.isIn(ForgeTags.Biomes.IS_SWAMP)) {
            return BiomeKeys.SWAMP.getValue();
        } else if (biome.isIn(BiomeTags.IS_OCEAN) || biome.isIn(BiomeTags.IS_DEEP_OCEAN) || biome.isIn(ConventionalBiomeTags.DEEP_OCEAN) || biome.isIn(ConventionalBiomeTags.OCEAN) || biome.isIn(ConventionalBiomeTags.SHALLOW_OCEAN) || biome.isIn(BiomeTags.IS_RIVER) || biome.isIn(ConventionalBiomeTags.RIVER) || biome.isIn(ConventionalBiomeTags.AQUATIC) || biome.isIn(ConventionalBiomeTags.AQUATIC_ICY) || biome.isIn(ForgeTags.Biomes.IS_WATER)) {
            if (biome.isIn(ConventionalBiomeTags.ICY) || biome.isIn(ConventionalBiomeTags.AQUATIC_ICY)) return BiomeKeys.FROZEN_RIVER.getValue();
            return BiomeKeys.RIVER.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.STONY_SHORES)) {
            return BiomeKeys.STONY_SHORE.getValue();
        } else if (biome.isIn(BiomeTags.IS_BEACH) || biome.isIn(ConventionalBiomeTags.BEACH)) {
            return BiomeKeys.BEACH.getValue();
        } else if (biome.isIn(BiomeTags.IS_JUNGLE) || biome.isIn(ConventionalBiomeTags.JUNGLE) || biome.isIn(ConventionalBiomeTags.TREE_JUNGLE)) {
            return BiomeKeys.JUNGLE.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.FLOWER_FORESTS) || biome.isIn(ConventionalBiomeTags.FLORAL)) {
            return BiomeKeys.FLOWER_FOREST.getValue();
        } else if (biome.isIn(BiomeTags.IS_SAVANNA) || biome.isIn(ConventionalBiomeTags.SAVANNA) || biome.isIn(ConventionalBiomeTags.TREE_SAVANNA)) {
            return BiomeKeys.SAVANNA.getValue();
        } else if (biome.isIn(BiomeTags.IS_BADLANDS) || biome.isIn((ConventionalBiomeTags.BADLANDS)) || biome.isIn((ConventionalBiomeTags.MESA))) {
            return BiomeKeys.BADLANDS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.TREE_CONIFEROUS) || biome.isIn(ForgeTags.Biomes.IS_CONIFEROUS) || biome.isIn(BiomeTags.IS_TAIGA) || biome.isIn(ConventionalBiomeTags.TAIGA)) {
            if (biome.isIn(ConventionalBiomeTags.ICY) || biome.isIn(ConventionalBiomeTags.SNOWY)) return BiomeKeys.SNOWY_TAIGA.getValue();
            return BiomeKeys.TAIGA.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.BIRCH_FOREST) || biome.isIn(ConventionalBiomeTags.TREE_DECIDUOUS)) {
            return BiomeKeys.BIRCH_FOREST.getValue();
        } else if (biome.isIn(BiomeTags.IS_FOREST) || biome.isIn(ConventionalBiomeTags.FOREST)) {
            return BiomeKeys.FOREST.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.PLAINS) || biome.isIn(ConventionalBiomeTags.SNOWY_PLAINS) || biome.isIn(ForgeTags.Biomes.IS_PLAINS) || biome.isIn(ConventionalBiomeTags.SNOWY) || biome.isIn(ForgeTags.Biomes.IS_SNOWY)) {
            if (biome.isIn(ConventionalBiomeTags.ICY) || biome.isIn(ConventionalBiomeTags.SNOWY)) return BiomeKeys.SNOWY_PLAINS.getValue();
            return BiomeKeys.PLAINS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.DESERT) || biome.isIn(ConventionalBiomeTags.WASTELAND) || biome.isIn(ConventionalBiomeTags.DEAD) || biome.isIn(ForgeTags.Biomes.IS_SANDY) || biome.isIn(ForgeTags.Biomes.IS_DESERT) || biome.isIn(ForgeTags.Biomes.IS_DEAD) || biome.isIn(ForgeTags.Biomes.IS_WASTELAND)) {
            return BiomeKeys.DESERT.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.ICY)) {
            return BiomeKeys.FROZEN_OCEAN.getValue();
        } else if (biome.isIn(ForgeTags.Biomes.IS_PLATEAU)) {
            return BiomeKeys.MEADOW.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.EXTREME_HILLS) || biome.isIn(ConventionalBiomeTags.WINDSWEPT)) {
            return BiomeKeys.WINDSWEPT_HILLS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.MOUNTAIN_PEAK) || biome.isIn(ForgeTags.Biomes.IS_PEAK)) {
            return BiomeKeys.JAGGED_PEAKS.getValue();
        } else if (biome.isIn(BiomeTags.IS_MOUNTAIN) || biome.isIn(ConventionalBiomeTags.MOUNTAIN) || biome.isIn(ConventionalBiomeTags.MOUNTAIN_SLOPE) || biome.isIn(ForgeTags.Biomes.IS_SLOPE) || biome.isIn(ForgeTags.Biomes.IS_MOUNTAIN)) {
            return BiomeKeys.STONY_PEAKS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.MUSHROOM) || biome.isIn(ForgeTags.Biomes.IS_MUSHROOM)) {
            return BiomeKeys.MUSHROOM_FIELDS.getValue();
        } else if (biome.isIn(BiomeTags.IS_HILL)) {
            return BiomeKeys.WINDSWEPT_GRAVELLY_HILLS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.CAVES) || biome.isIn(ConventionalBiomeTags.UNDERGROUND) || biome.isIn(ForgeTags.Biomes.IS_UNDERGROUND) || biome.isIn(ForgeTags.Biomes.IS_CAVE)) {
            return BiomeKeys.DRIPSTONE_CAVES.getValue();
        } else if (biome.isIn(ForgeTags.Biomes.IS_SPOOKY)) {
            return BiomeKeys.DARK_FOREST.getValue();
        } else if (biome.isIn(ForgeTags.Biomes.IS_MAGICAL)) {
            return BiomeKeys.MUSHROOM_FIELDS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.VEGETATION_DENSE) || biome.isIn(ForgeTags.Biomes.IS_DENSE)) {
            return BiomeKeys.FOREST.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.VEGETATION_SPARSE) || biome.isIn(ForgeTags.Biomes.IS_SPARSE)) {
            return BiomeKeys.PLAINS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.CLIMATE_HOT) || biome.isIn(ForgeTags.Biomes.IS_HOT)) {
            return BiomeKeys.DESERT.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.CLIMATE_COLD) || biome.isIn(ForgeTags.Biomes.IS_COLD)) {
            return BiomeKeys.SNOWY_PLAINS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.CLIMATE_TEMPERATE)) {
            return BiomeKeys.PLAINS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.CLIMATE_DRY) || biome.isIn(ForgeTags.Biomes.IS_DRY)) {
            return BiomeKeys.BADLANDS.getValue();
        } else if (biome.isIn(ConventionalBiomeTags.CLIMATE_WET) || biome.isIn(ForgeTags.Biomes.IS_WET)) {
            return BiomeKeys.SWAMP.getValue();
        }
        return null;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, Identifier> outMap = new HashMap<>();
        for (Map.Entry<Identifier, JsonElement> fileEntry : prepared.entrySet()) {
            Identifier fileId = fileEntry.getKey();
            try {
                JsonObject fileJson = fileEntry.getValue().getAsJsonObject();

                int version = fileJson.getAsJsonPrimitive("version").getAsInt();
                if (version == 1) {
                    Identifier textureSet = new Identifier(fileJson.get("texture_set").getAsString());

                    outMap.put(fileId, textureSet);

                    for (TileElevation layer : TileElevation.values()) {
                        outMap.put(Identifier.tryParse(fileId + "_" + layer.getName()), textureSet);
                    }
                } else if (version == VERSION) {
                    Identifier defaultSet = TextureSets.DEFAULT;

                    try {
                        defaultSet = new Identifier(fileJson.getAsJsonObject("texture_sets").get("default").getAsString());
                    } catch (Exception ignored) {
                    }

                    // insert the old-style texture set with the default one
                    outMap.put(fileId, defaultSet);

                    for (TileElevation layer : TileElevation.values()) {
                        Identifier textureSet = defaultSet;

                        try {
                            textureSet = new Identifier(fileJson.getAsJsonObject("texture_sets").get(layer.getName()).getAsString());
                        } catch (Exception ignored) {
                        }

                        outMap.put(Identifier.tryParse(fileId + "_" + layer), textureSet);
                    }
                } else {
                    throw new RuntimeException("Incompatible version (" + VERSION + " != " + version + ")");
                }
            } catch (Exception e) {
                AntiqueAtlas.LOGGER.warn("Error reading biome tile " + fileId + "!", e);
            }
        }

        outMap.forEach((id, setName) -> {
            TextureSet set = TextureSets.getInstance().get(setName);

            if (set == null) {
                AntiqueAtlas.LOGGER.error("Missing texture set `{}` for tile `{}`. Using default.", setName, id);
                set = TextureSets.getInstance().getDefault();
            }

            setTexture(id, set);
            if (AntiqueAtlas.CONFIG.debug.debugRespack) {
                AntiqueAtlas.LOGGER.info("Loaded tile {} with texture set {}", id, set.id);
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Collections.singleton(TextureSets.ID);
    }
}
