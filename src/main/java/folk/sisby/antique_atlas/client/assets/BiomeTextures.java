package folk.sisby.antique_atlas.client.assets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.BuiltinTextureSets;
import folk.sisby.antique_atlas.client.TextureSet;
import folk.sisby.antique_atlas.client.gui.tiles.SubTile;
import folk.sisby.antique_atlas.client.texture.TileTexture;
import folk.sisby.antique_atlas.tile.TileElevation;
import folk.sisby.antique_atlas.util.ForgeTags;
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

        if (biome.isIn(ConventionalBiomeTags.VOID) || biome.isIn(ForgeTags.Biomes.IS_VOID)) {
            return Optional.of(BuiltinTextureSets.THE_VOID);
        }

        if (biome.isIn(BiomeTags.IS_END) || biome.isIn(ConventionalBiomeTags.IN_THE_END) || biome.isIn(ConventionalBiomeTags.END_ISLANDS)) {
            if (biome.isIn(ConventionalBiomeTags.VEGETATION_DENSE) || biome.isIn(ConventionalBiomeTags.VEGETATION_SPARSE)) {
                return Optional.of(BuiltinTextureSets.END_HIGHLANDS);
            } else {
                return Optional.of(BuiltinTextureSets.END_BARRENS);
            }
        }

        if (biome.isIn(BiomeTags.IS_NETHER) || biome.isIn(ConventionalBiomeTags.IN_NETHER)) {
            return Optional.of(BuiltinTextureSets.SOUL_SAND_VALLEY);
        }

        if (biome.isIn(ConventionalBiomeTags.SWAMP) || biome.isIn(ForgeTags.Biomes.IS_SWAMP)) {
            if (biome.isIn(BiomeTags.IS_HILL)) {
                return Optional.of(BuiltinTextureSets.SWAMP_HIGH);
            } else {
                return Optional.of(BuiltinTextureSets.SWAMP);
            }
        }

        if (biome.isIn(BiomeTags.IS_OCEAN)
            || biome.isIn(BiomeTags.IS_DEEP_OCEAN)
            || biome.isIn(BiomeTags.IS_RIVER)
            || biome.isIn(ConventionalBiomeTags.AQUATIC)
            || biome.isIn(ForgeTags.Biomes.IS_WATER)
        ) {
            if (biome.isIn(ConventionalBiomeTags.ICY))
                return Optional.of(BuiltinTextureSets.ICE);

            return Optional.of(BuiltinTextureSets.WATER);
        }

        if (biome.isIn(BiomeTags.IS_BEACH) || biome.isIn(ConventionalBiomeTags.BEACH)) {
            return Optional.of(BuiltinTextureSets.BEACH);
        }

        if (biome.isIn(BiomeTags.IS_JUNGLE) || biome.isIn(ConventionalBiomeTags.JUNGLE) || biome.isIn(ConventionalBiomeTags.TREE_JUNGLE)) {
            if (biome.isIn(BiomeTags.IS_HILL)) {
                return Optional.of(BuiltinTextureSets.JUNGLE_HIGH);
            } else {
                return Optional.of(BuiltinTextureSets.JUNGLE);
            }
        }

        if (biome.isIn(BiomeTags.IS_SAVANNA) || biome.isIn(ConventionalBiomeTags.SAVANNA) || biome.isIn(ConventionalBiomeTags.TREE_SAVANNA)) {
            return Optional.of(BuiltinTextureSets.SAVANNA);
        }

        if (biome.isIn(BiomeTags.IS_BADLANDS) || biome.isIn((ConventionalBiomeTags.BADLANDS)) || biome.isIn((ConventionalBiomeTags.MESA))) {
            return Optional.of(BuiltinTextureSets.BADLANDS);
        }

        if (biome.isIn(BiomeTags.IS_FOREST) || biome.isIn(ConventionalBiomeTags.TREE_DECIDUOUS) || biome.isIn(ForgeTags.Biomes.IS_CONIFEROUS) || biome.isIn(BiomeTags.IS_TAIGA) || biome.isIn(ConventionalBiomeTags.TAIGA)) {
            if (biome.isIn(ConventionalBiomeTags.ICY) || biome.isIn(ConventionalBiomeTags.SNOWY)) {
                if (biome.isIn(BiomeTags.IS_HILL)) {
                    return Optional.of(BuiltinTextureSets.TAIGA_HIGH);
                } else {
                    return Optional.of(BuiltinTextureSets.TAIGA);
                }
            } else {
                if (biome.isIn(BiomeTags.IS_HILL)) {
                    return Optional.of(BuiltinTextureSets.FOREST_HIGH);
                } else {
                    return Optional.of(BuiltinTextureSets.FOREST);
                }
            }
        }

        if (biome.isIn(ConventionalBiomeTags.PLAINS) || biome.isIn(ConventionalBiomeTags.SNOWY_PLAINS) || biome.isIn(ForgeTags.Biomes.IS_PLAINS)) {
            if (biome.isIn(ConventionalBiomeTags.ICY) || biome.isIn(ConventionalBiomeTags.SNOWY)) {
                if (biome.isIn(BiomeTags.IS_HILL)) {
                    return Optional.of(BuiltinTextureSets.SNOWY_PLAINS_HIGH);
                } else {
                    return Optional.of(BuiltinTextureSets.SNOWY_PLAINS);
                }
            } else {
                if (biome.isIn(BiomeTags.IS_HILL)) {
                    return Optional.of(BuiltinTextureSets.PLAINS_HIGH);
                } else {
                    return Optional.of(BuiltinTextureSets.PLAINS);
                }
            }
        }

        if (biome.isIn(ConventionalBiomeTags.ICY)) {
            if (biome.isIn(BiomeTags.IS_HILL)) {
                return Optional.of(BuiltinTextureSets.GROVE);
            } else {
                return Optional.of(BuiltinTextureSets.ICE_SPIKES);
            }
        }

        if (biome.isIn(ConventionalBiomeTags.DESERT) || biome.isIn(ForgeTags.Biomes.IS_SANDY) || biome.isIn(ForgeTags.Biomes.IS_DESERT)) {
            if (biome.isIn(BiomeTags.IS_HILL)) {
                return Optional.of(BuiltinTextureSets.DESERT_HIGH);
            } else {
                return Optional.of(BuiltinTextureSets.DESERT);
            }
        }

        if (biome.isIn(ConventionalBiomeTags.SNOWY) || biome.isIn(ConventionalBiomeTags.SNOWY_PLAINS) || biome.isIn(ForgeTags.Biomes.IS_SNOWY)) {
            return Optional.of(BuiltinTextureSets.SNOWY_PLAINS);
        }

        if (biome.isIn(ConventionalBiomeTags.EXTREME_HILLS)) {
            return Optional.of(BuiltinTextureSets.WINDSWEPT_HILLS);
        }

        if (biome.isIn(ConventionalBiomeTags.MOUNTAIN_PEAK) || biome.isIn(ForgeTags.Biomes.IS_PEAK)) {
            return Optional.of(BuiltinTextureSets.JAGGED_PEAKS);
        }

        if (biome.isIn(BiomeTags.IS_MOUNTAIN) || biome.isIn(ConventionalBiomeTags.MOUNTAIN) || biome.isIn(ConventionalBiomeTags.MOUNTAIN_SLOPE) || biome.isIn(ForgeTags.Biomes.IS_SLOPE) || biome.isIn(ForgeTags.Biomes.IS_MOUNTAIN)) {
            return Optional.of(BuiltinTextureSets.STONY_PEAKS);
        }

        if (biome.isIn(ConventionalBiomeTags.MUSHROOM) || biome.isIn(ForgeTags.Biomes.IS_MUSHROOM)) {
            return Optional.of(BuiltinTextureSets.MUSHROOM_FIELDS);
        }

        if (biome.isIn(BiomeTags.IS_HILL)) {
            return Optional.of(BuiltinTextureSets.WINDSWEPT_GRAVELLY_HILLS);
        }

        if (biome.isIn(ConventionalBiomeTags.UNDERGROUND) || biome.isIn(ForgeTags.Biomes.IS_UNDERGROUND) || biome.isIn(ForgeTags.Biomes.IS_CAVE)) {
            return Optional.of(BuiltinTextureSets.RAVINE);
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
