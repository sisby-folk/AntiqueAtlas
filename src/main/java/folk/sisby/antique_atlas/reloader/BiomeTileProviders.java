package folk.sisby.antique_atlas.reloader;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.TileElevation;
import folk.sisby.antique_atlas.TileProvider;
import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.antique_atlas.util.CodecUtil;
import folk.sisby.antique_atlas.util.ForgeTags;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BiomeTileProviders extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final BiomeTileProviders INSTANCE = new BiomeTileProviders();
    public static final Identifier ID = AntiqueAtlas.id("tile_provider/biome");

    public static BiomeTileProviders getInstance() {
        return INSTANCE;
    }

    private final Map<Identifier, TileProvider> tileProviders = new HashMap<>();
    private final Map<Identifier, Identifier> biomeFallbacks = new HashMap<>();

    public BiomeTileProviders() {
        super(new Gson(), "atlas/tiles");
    }

    public TileProvider getTileProvider(Identifier providerId) {
        return tileProviders.getOrDefault(providerId, tileProviders.getOrDefault(biomeFallbacks.get(providerId), TileProvider.DEFAULT));
    }

    /**
     * Register fallbacks for any biomes present in the client world that don't have explicit sets.
     * Doing this on world join catches data-biomes that might not be registered in other worlds.
     */
    public void registerFallbacks(Registry<Biome> biomeRegistry) {
        for (Biome biome : biomeRegistry) {
            Identifier biomeId = biomeRegistry.getId(biome);
            if (tileProviders.containsKey(biomeId)) continue;
            Identifier fallbackBiome = getFallbackBiome(biomeRegistry.getEntry(biome));
            if (fallbackBiome != null && tileProviders.containsKey(fallbackBiome)) {
                biomeFallbacks.put(biomeId, fallbackBiome);
                AntiqueAtlas.LOGGER.warn("[Antique Atlas] Set fallback biome for {} to {}. You can set a more fitting texture using a resource pack!", biomeId, fallbackBiome);
            } else if (fallbackBiome != null) {
                AntiqueAtlas.LOGGER.error("[Antique Atlas] Fallback biome for {} is {}, which has no defined tile provider.", biomeId, fallbackBiome);
            } else {
                AntiqueAtlas.LOGGER.error("[Antique Atlas] No fallback could be found for {}. This shouldn't happen! This means the biome is not in ANY conventional or vanilla tag on the client!", biomeId);
            }
        }
    }

    public void clearFallbacks() {
        biomeFallbacks.clear();
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

    protected Map<Identifier, TileTexture> applyTileTextures(ResourceManager manager) {
        Map<Identifier, TileTextureMeta> textureMeta = new HashMap<>();
        for (Map.Entry<Identifier, Resource> e : manager.findResources("textures/gui/tiles", id -> id.getPath().endsWith(".png")).entrySet()) {
            Identifier id = new Identifier(e.getKey().getNamespace(), e.getKey().getPath().substring("textures/gui/tiles/".length(), e.getKey().getPath().length() - ".png".length()));
            try {
                ResourceMetadata metadata = e.getValue().getMetadata();
                metadata.decode(TileTextureMeta.METADATA).ifPresentOrElse(meta -> {
                    textureMeta.put(id, meta);
                }, () -> {
                    AntiqueAtlas.LOGGER.info("[Antique Atlas] Metadata not present for {} - using defaults.", e.getKey());
                    textureMeta.put(id, TileTextureMeta.DEFAULT);
                });
            } catch (IOException ex) {
                AntiqueAtlas.LOGGER.error("[Antique Atlas] Failed to access tile texture metadata for {}", e.getKey(), ex);
                textureMeta.put(id, TileTextureMeta.DEFAULT);
            }
        }

        // Validate Parents
        Map<Identifier, Identifier> invalidParents = new HashMap<>();
        textureMeta.forEach((id, meta) -> {
            if (meta.parent.isPresent() && !textureMeta.containsKey(meta.parent.orElseThrow())) {
                invalidParents.put(id, meta.parent.orElseThrow());
                AntiqueAtlas.LOGGER.error("[Antique Atlas] Failed to reload a tile texture! {} had invalid parent {}", id, meta.parent);
            }
        });
        invalidParents.keySet().forEach(textureMeta::remove);

        // Propagate fields to children
        textureMeta.forEach((id, meta) -> {
            Optional<TileTextureMeta> parent = meta.parent.map(textureMeta::get);
            while (parent.isPresent()) {
                meta.inheritFromAncestor(parent.orElseThrow());
                parent = parent.orElseThrow().parent.map(textureMeta::get);
            }
        });

        // Populate Tags
        Map<Identifier, Set<Identifier>> textureTags = new HashMap<>();
        textureMeta.forEach((id, meta) -> meta.tags.forEach(tag -> textureTags.computeIfAbsent(tag, t -> new HashSet<>()).add(id)));

        // Substitute Tags
        textureMeta.forEach((id, meta) -> meta.substituteTags(id, textureTags));

        // Apply TilesToThis
        textureMeta.forEach((id, meta) -> meta.applyTilesToThis(id, textureMeta));

        // Create Builders
        Map<Identifier, TileTexture.Builder> textureBuilders = new HashMap<>();
        textureMeta.forEach((id, meta) -> textureBuilders.put(id, meta.toBuilder(id)));

        // Create Empty Textures
        Map<Identifier, TileTexture> textures = new HashMap<>();
        textureBuilders.forEach((id, builder) -> textures.put(id, TileTexture.empty(id, builder.innerBorder())));

        // Build Textures
        textureBuilders.forEach((id, builder) -> builder.build(textures));

        return textures;
    }

    protected TileTexture getTexture(Map<Identifier, TileTexture> textures, Identifier id) {
        if (textures.containsKey(id)) {
            return textures.get(id);
        } else {
            throw new IllegalStateException("texture %s is not present!".formatted(id));
        }
    }

    protected @Nullable List<TileTexture> resolveTextureJson(Map<Identifier, TileTexture> textures, JsonElement textureJson) {
        if (textureJson instanceof JsonPrimitive texturePrimitive && texturePrimitive.isString()) {
            return List.of(getTexture(textures, new Identifier(texturePrimitive.getAsString())));
        } else if (textureJson instanceof JsonArray textureArray) {
            return textureArray.asList().stream().map(je -> getTexture(textures, new Identifier(je.getAsString()))).toList();
        } else if (textureJson instanceof JsonObject textureObject) {
            if (Arrays.stream(TileElevation.values()).map(TileElevation::getName).noneMatch(textureObject::has)) {
                Multiset<TileTexture> outList = HashMultiset.create();
                textureObject.entrySet().forEach(e -> outList.add(getTexture(textures, new Identifier(e.getKey())), e.getValue().getAsInt()));
                return outList.stream().toList();
            }
        }
        return null;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        tileProviders.clear();
        Map<Identifier, TileTexture> textures = applyTileTextures(manager);
        Set<TileTexture> unusedTextures = new HashSet<>(textures.values());
        Map<Identifier, Identifier> providerParents = new HashMap<>();
        for (Map.Entry<Identifier, JsonElement> fileEntry : prepared.entrySet()) {
            Identifier fileId = fileEntry.getKey();
            try {
                JsonObject fileJson = fileEntry.getValue().getAsJsonObject();
                if (fileJson.has("parent")) {
                    Identifier parentId = new Identifier(fileJson.getAsJsonPrimitive("parent").getAsString());
                    providerParents.put(fileId, parentId);
                    continue;
                }
                JsonElement textureJson = fileJson.get("textures");
                List<TileTexture> defaultTextures = resolveTextureJson(textures, textureJson);
                if (defaultTextures != null) {
                    defaultTextures.forEach(unusedTextures::remove);
                    tileProviders.put(fileId, new TileProvider(defaultTextures));
                } else {
                    JsonObject textureObject = textureJson.getAsJsonObject();
                    Map<TileElevation, List<TileTexture>> textureElevations = new HashMap<>();
                    Set<TileElevation> skippedElevations = new HashSet<>();
                    List<TileTexture> elevationTextures = null;
                    for (TileElevation elevation : TileElevation.values()) {
                        if (textureObject.has(elevation.getName())) {
                            elevationTextures = resolveTextureJson(textures, textureObject.get(elevation.getName()));
                            if (elevationTextures == null) throw new IllegalStateException("Malformed elevation %s in textures object!".formatted(elevation.getName()));
                            elevationTextures.forEach(unusedTextures::remove);
                            textureElevations.put(elevation, elevationTextures);
                            for (TileElevation skipped : skippedElevations) {
                                textureElevations.put(skipped, elevationTextures);
                            }
                            skippedElevations.clear();
                        } else {
                            skippedElevations.add(elevation);
                        }
                    }
                    if (textureElevations.isEmpty()) {
                        throw new IllegalStateException("No elevation keys were found in the textures object!");
                    }
                    for (TileElevation elevation : skippedElevations) {
                        textureElevations.put(elevation, elevationTextures);
                    }
                    tileProviders.put(fileId, new TileProvider(textureElevations));
                }
            } catch (Exception e) {
                AntiqueAtlas.LOGGER.warn("[Antique Atlas] Error reading biome tile provider " + fileId + "!", e);
            }
        }
        providerParents.forEach((id, parentId) -> {
            if (tileProviders.containsKey(parentId)) {
                tileProviders.put(id, tileProviders.get(parentId));
            } else {
                AntiqueAtlas.LOGGER.warn("[Antique Atlas] Error reading biome tile provider " + id + "!", new IllegalStateException("Parent id %s doesn't exist".formatted(parentId)));
            }
        });

        for (TileTexture texture : unusedTextures) {
            if (texture.displayId().getPath().startsWith("test") || texture.displayId().getPath().startsWith("base")) continue;
            AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} isn't referenced by any tile provider!", texture.displayId());
        }
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public record TileTextureMeta(
        Optional<Identifier> parent,
        Optional<BorderType> borderType,
        Set<Identifier> tags,
        Set<Codecs.TagEntryId> tilesTo,
        Set<Codecs.TagEntryId> tilesToHorizontal,
        Set<Codecs.TagEntryId> tilesToVertical,
        Set<Codecs.TagEntryId> tilesToThis,
        Set<Codecs.TagEntryId> tilesToThisHorizontal,
        Set<Codecs.TagEntryId> tilesToThisVertical
    ) {
        public static final TileTextureMeta DEFAULT = new TileTextureMeta(Optional.empty(), Optional.empty(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of());

        public static final Codec<TileTextureMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("parent").forGetter(TileTextureMeta::parent),
            CodecUtil.ofEnum(BorderType.class).optionalFieldOf("borderType").forGetter(TileTextureMeta::borderType),
            CodecUtil.set(Identifier.CODEC).fieldOf("tags").orElse(new HashSet<>()).forGetter(TileTextureMeta::tags),
            CodecUtil.set(Codecs.TAG_ENTRY_ID).fieldOf("tilesTo").orElse(new HashSet<>()).forGetter(TileTextureMeta::tilesTo),
            CodecUtil.set(Codecs.TAG_ENTRY_ID).fieldOf("tilesToHorizontal").orElse(new HashSet<>()).forGetter(TileTextureMeta::tilesToHorizontal),
            CodecUtil.set(Codecs.TAG_ENTRY_ID).fieldOf("tilesToVertical").orElse(new HashSet<>()).forGetter(TileTextureMeta::tilesToVertical),
            CodecUtil.set(Codecs.TAG_ENTRY_ID).fieldOf("tilesToThis").orElse(new HashSet<>()).forGetter(TileTextureMeta::tilesToThis),
            CodecUtil.set(Codecs.TAG_ENTRY_ID).fieldOf("tilesToThisHorizontal").orElse(new HashSet<>()).forGetter(TileTextureMeta::tilesToThisHorizontal),
            CodecUtil.set(Codecs.TAG_ENTRY_ID).fieldOf("tilesToThisVertical").orElse(new HashSet<>()).forGetter(TileTextureMeta::tilesToThisVertical)
        ).apply(instance, TileTextureMeta::new));


        enum BorderType {
            outer,
            inner
        }

        public static final ResourceMetadataReader<TileTextureMeta> METADATA = new CodecUtil.CodecResourceMetadataSerializer<>(CODEC, AntiqueAtlas.id("tiling"));

        void inheritFromAncestor(TileTextureMeta other) {
            tags.addAll(other.tags);
            tilesTo.addAll(other.tilesTo);
            tilesToHorizontal.addAll(other.tilesToHorizontal);
            tilesToVertical.addAll(other.tilesToVertical);
            tilesToThis.addAll(other.tilesToThis);
            tilesToThisHorizontal.addAll(other.tilesToThisHorizontal);
            tilesToThisVertical.addAll(other.tilesToThisVertical);
        }

        void substituteTags(Identifier thisId, Map<Identifier, Set<Identifier>> tags) {
            for (Set<Codecs.TagEntryId> entrySet : List.of(tilesTo, tilesToHorizontal, tilesToVertical, tilesToThis, tilesToThisHorizontal, tilesToThisVertical)) {
                Set<Codecs.TagEntryId> entryTags = new HashSet<>();
                for (Codecs.TagEntryId entry : entrySet) {
                    if (entry.tag()) {
                        entryTags.add(entry);
                    }
                }
                if (!entryTags.isEmpty()) entrySet.removeAll(entryTags);
                for (Codecs.TagEntryId entry : entryTags) {
                    Set<Identifier> resolvedIds = tags.getOrDefault(entry.id(), Set.of());
                    if (resolvedIds.isEmpty()) {
                        AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} references tag {}, which is empty", thisId, entry.id());
                    } else {
                        entrySet.addAll(resolvedIds.stream().map(id -> new Codecs.TagEntryId(id, false)).toList());
                    }
                }
            }
        }

        void applyTilesToThis(Identifier thisId, Map<Identifier, TileTextureMeta> map) {
            for (Codecs.TagEntryId entryId : tilesToThis) {
                if (entryId.tag()) throw new IllegalStateException("tags must be resolved to apply tilesToThis!");
                if (map.containsKey(entryId.id())) {
                    map.get(entryId.id()).tilesTo.add(new Codecs.TagEntryId(thisId, false));
                } else {
                    AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} references texture {}, which is missing", thisId, entryId.id());
                }
            }
            for (Codecs.TagEntryId entryId : tilesToThisHorizontal) {
                if (entryId.tag()) throw new IllegalStateException("tags must be resolved to apply tilesToThis!");
                if (map.containsKey(entryId.id())) {
                    map.get(entryId.id()).tilesToHorizontal.add(new Codecs.TagEntryId(thisId, false));
                } else {
                    AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} references texture {}, which is missing", thisId, entryId.id());
                }
            }
            for (Codecs.TagEntryId entryId : tilesToThisVertical) {
                if (entryId.tag()) throw new IllegalStateException("tags must be resolved to apply tilesToThis!");
                if (map.containsKey(entryId.id())) {
                    map.get(entryId.id()).tilesToVertical.add(new Codecs.TagEntryId(thisId, false));
                } else {
                    AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} references texture {}, which is missing", thisId, entryId.id());
                }
            }
        }

        public TileTexture.Builder toBuilder(Identifier thisId) {
            return new TileTexture.Builder(
                thisId,
                borderType.orElse(BorderType.outer) == BorderType.inner,
                tilesTo.stream().map(Codecs.TagEntryId::id).collect(Collectors.toSet()),
                tilesToHorizontal.stream().map(Codecs.TagEntryId::id).collect(Collectors.toSet()),
                tilesToVertical.stream().map(Codecs.TagEntryId::id).collect(Collectors.toSet())
            );
        }
    }
}
