package folk.sisby.antique_atlas.client.assets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.TextureSet;
import folk.sisby.antique_atlas.client.gui.tiles.SubTile;
import folk.sisby.antique_atlas.client.texture.TileTexture;
import folk.sisby.antique_atlas.tile.TileElevation;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.biome.Biome;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
            AntiqueAtlas.LOG.error("Given biome is null. Cannot autodetect a suitable texture set for that.");
            return;
        }

        Optional<Identifier> textureSet = guessFittingTextureSet(biome);

        if (textureSet.isPresent()) {
            setAllTextures(id, TextureSets.getInstance().get(textureSet.get()));
            AntiqueAtlas.LOG.info("Auto-registered standard texture set for biome {}: {}", id, textureSet.get());
        } else {
            AntiqueAtlas.LOG.error("Failed to auto-register a standard texture set for the biome '{}'. This is most likely caused by errors in the TextureSet configurations, check your resource packs first before reporting it as an issue!", id.toString());
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
                AntiqueAtlas.LOG.warn("Removing old texture for {}", tileId);
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

    private static Optional<Identifier> guessFittingTextureSet(RegistryEntry<Biome> biome) {
        if (MinecraftClient.getInstance().world == null) return Optional.empty();

        if (biome.isIn(ConventionalBiomeTags.VOID)) {
            return Optional.of(AntiqueAtlas.id("end_void"));
        }

        if (biome.isIn(BiomeTags.IS_END) || biome.isIn(ConventionalBiomeTags.IN_THE_END) || biome.isIn(ConventionalBiomeTags.END_ISLANDS)) {
            if (biome.isIn(ConventionalBiomeTags.VEGETATION_DENSE) || biome.isIn(ConventionalBiomeTags.VEGETATION_SPARSE)) {
                return Optional.of(AntiqueAtlas.id("end_island_plants"));
            } else {
                return Optional.of(AntiqueAtlas.id("end_island"));
            }
        }

        if (biome.isIn(BiomeTags.IS_NETHER) || biome.isIn(ConventionalBiomeTags.IN_NETHER)) {
            return Optional.of(AntiqueAtlas.id("soul_sand_valley"));
        }

        if (biome.isIn(ConventionalBiomeTags.SWAMP)) {
            if (biome.isIn(BiomeTags.IS_HILL)) {
                return Optional.of(AntiqueAtlas.id("swamp_hills"));
            } else {
                return Optional.of(AntiqueAtlas.id("swamp"));
            }
        }

        if (biome.isIn(BiomeTags.IS_OCEAN)
            || biome.isIn(BiomeTags.IS_DEEP_OCEAN)
            || biome.isIn(BiomeTags.IS_RIVER)
            || biome.isIn(ConventionalBiomeTags.AQUATIC)) {
            if (biome.isIn(ConventionalBiomeTags.ICY))
                return Optional.of(AntiqueAtlas.id("ice"));

            return Optional.of(AntiqueAtlas.id("water"));
        }

        if (biome.isIn(BiomeTags.IS_BEACH) || biome.isIn(ConventionalBiomeTags.BEACH)) {
            return Optional.of(AntiqueAtlas.id("shore"));
        }

        if (biome.isIn(BiomeTags.IS_JUNGLE) || biome.isIn(ConventionalBiomeTags.JUNGLE) || biome.isIn(ConventionalBiomeTags.TREE_JUNGLE)) {
            if (biome.isIn(BiomeTags.IS_HILL)) {
                return Optional.of(AntiqueAtlas.id("jungle_hills"));
            } else {
                return Optional.of(AntiqueAtlas.id("jungle"));
            }
        }

        if (biome.isIn(BiomeTags.IS_SAVANNA) || biome.isIn(ConventionalBiomeTags.SAVANNA) || biome.isIn(ConventionalBiomeTags.TREE_SAVANNA)) {
            return Optional.of(AntiqueAtlas.id("savana"));
        }

        if (biome.isIn(BiomeTags.IS_BADLANDS) || biome.isIn((ConventionalBiomeTags.BADLANDS)) || biome.isIn((ConventionalBiomeTags.MESA))) {
            return Optional.of(AntiqueAtlas.id("mesa"));
        }

        if (biome.isIn(BiomeTags.IS_FOREST) || biome.isIn(ConventionalBiomeTags.TREE_DECIDUOUS)) {
            if (biome.isIn(ConventionalBiomeTags.ICY) || biome.isIn(ConventionalBiomeTags.SNOWY)) {
                if (biome.isIn(BiomeTags.IS_HILL)) {
                    return Optional.of(AntiqueAtlas.id("snow_pines_hills"));
                } else {
                    return Optional.of(AntiqueAtlas.id("snow_pines"));
                }
            } else {
                if (biome.isIn(BiomeTags.IS_HILL)) {
                    return Optional.of(AntiqueAtlas.id("forest_hills"));
                } else {
                    return Optional.of(AntiqueAtlas.id("forest"));
                }
            }
        }

        if (biome.isIn(ConventionalBiomeTags.PLAINS) || biome.isIn(ConventionalBiomeTags.SNOWY_PLAINS)) {
            if (biome.isIn(ConventionalBiomeTags.ICY) || biome.isIn(ConventionalBiomeTags.SNOWY)) {
                if (biome.isIn(BiomeTags.IS_HILL)) {
                    return Optional.of(AntiqueAtlas.id("snow_hills"));
                } else {
                    return Optional.of(AntiqueAtlas.id("snow"));
                }
            } else {
                if (biome.isIn(BiomeTags.IS_HILL)) {
                    return Optional.of(AntiqueAtlas.id("hills"));
                } else {
                    return Optional.of(AntiqueAtlas.id("plains"));
                }
            }
        }

        if (biome.isIn(ConventionalBiomeTags.ICY)) {
            if (biome.isIn(BiomeTags.IS_HILL)) {
                return Optional.of(AntiqueAtlas.id("mountains_snow_caps"));
            } else {
                return Optional.of(AntiqueAtlas.id("ice_spikes"));
            }
        }

        if (biome.isIn(ConventionalBiomeTags.DESERT)) {
            if (biome.isIn(BiomeTags.IS_HILL)) {
                return Optional.of(AntiqueAtlas.id("desert_hills"));
            } else {
                return Optional.of(AntiqueAtlas.id("desert"));
            }
        }

        if (biome.isIn(BiomeTags.IS_TAIGA) || biome.isIn(ConventionalBiomeTags.TAIGA)) { // should this be any snowy biome as a fallback?
            return Optional.of(AntiqueAtlas.id("snow"));
        }

        if (biome.isIn(ConventionalBiomeTags.EXTREME_HILLS)) {
            return Optional.of(AntiqueAtlas.id("hills"));
        }

        if (biome.isIn(ConventionalBiomeTags.MOUNTAIN_PEAK)) {
            return Optional.of(AntiqueAtlas.id("mountains_snow_caps"));
        }

        if (biome.isIn(BiomeTags.IS_MOUNTAIN) || biome.isIn(ConventionalBiomeTags.MOUNTAIN) || biome.isIn(ConventionalBiomeTags.MOUNTAIN_SLOPE)) {
            return Optional.of(AntiqueAtlas.id("mountains"));
        }

        if (biome.isIn(ConventionalBiomeTags.MUSHROOM)) {
            return Optional.of(AntiqueAtlas.id("mushroom"));
        }

        if (biome.isIn(BiomeTags.IS_HILL)) {
            return Optional.of(AntiqueAtlas.id("hills"));
        }

        if (biome.isIn(ConventionalBiomeTags.UNDERGROUND)) {
            AntiqueAtlas.LOG.warn("Underground biomes aren't supported yet.");
        }

        return Optional.empty();
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
                AntiqueAtlas.LOG.warn("Error reading biome tile " + fileId + "!", e);
            }
        }

        outMap.forEach((id, setName) -> {
            TextureSet set = TextureSets.getInstance().get(setName);

            if (set == null) {
                AntiqueAtlas.LOG.error("Missing texture set `{}` for tile `{}`. Using default.", setName, id);
                set = TextureSets.getInstance().getDefault();
            }

            setTexture(id, set);
            if (AntiqueAtlas.CONFIG.Performance.resourcePackLogging) {
                AntiqueAtlas.LOG.info("Loaded tile {} with texture set {}", id, set.id);
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
