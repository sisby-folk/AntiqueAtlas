package folk.sisby.antique_atlas.reloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.Marker;
import folk.sisby.antique_atlas.StructureTileProvider;
import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.surveyor.landmark.SimplePointLandmark;
import folk.sisby.surveyor.structure.JigsawPieceSummary;
import folk.sisby.surveyor.structure.StructurePieceSummary;
import folk.sisby.surveyor.structure.StructureSummary;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static folk.sisby.antique_atlas.reloader.BiomeTileProviders.resolveTextureJson;

public class StructureTileProviders extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final StructureTileProviders INSTANCE = new StructureTileProviders();

    public static final Identifier ID = AntiqueAtlas.id("structures");

    public static StructureTileProviders getInstance() {
        return INSTANCE;
    }

    private final Map<Identifier, Pair<Identifier, Text>> structureMarkers = new HashMap<>(); // Structure Start
    private final Map<TagKey<Structure>, Pair<Identifier, Text>> structureTagMarkers = new HashMap<>(); // Structure Start

    private final Map<Identifier, StructureTileProvider> startProviders = new HashMap<>();
    private final Map<Identifier, StructureTileProvider> startTagProviders = new HashMap<>();
    private final Map<Identifier, StructureTileProvider> startTypeProviders = new HashMap<>();
    private final Map<Identifier, StructureTileProvider> pieceTypeProviders = new HashMap<>();
    private final Map<Identifier, StructureTileProvider> singleJigsawProviders = new HashMap<>();
    private final Map<Identifier, StructureTileProvider> featureJigsawProviders = new HashMap<>();

    public StructureTileProviders() {
        super(new Gson(), "atlas/structure");
    }

    public void registerMarker(StructureType<?> structureFeature, Identifier markerType, Text name) {
        structureMarkers.put(Registries.STRUCTURE_TYPE.getId(structureFeature), Pair.of(markerType, name));
    }

    public void registerMarker(TagKey<Structure> structureTag, Identifier markerType, Text name) {
        structureTagMarkers.put(structureTag, Pair.of(markerType, name));
    }

    public Map<ChunkPos, TileTexture> resolve(Map<ChunkPos, TileTexture> outTiles, Map<ChunkPos, StructureTileProvider> structureProviders, Map<ChunkPos, String> tilePredicates, StructurePieceSummary piece, World world) {
        if (piece instanceof JigsawPieceSummary jigsawPiece) {
            if (singleJigsawProviders.containsKey(jigsawPiece.getId())) {
                StructureTileProvider provider = (jigsawPiece.getElementType() == StructurePoolElementType.FEATURE_POOL_ELEMENT ? featureJigsawProviders : singleJigsawProviders).get(jigsawPiece.getId());
                provider.getTextures(world, jigsawPiece.getBoundingBox(), jigsawPiece.getJunctions(), tilePredicates).forEach((pos, texture) -> {
                    outTiles.put(pos, texture);
                    structureProviders.put(pos, provider);
                });
                return outTiles;
            }
        }

        Identifier structurePieceId = Registries.STRUCTURE_PIECE.getId(piece.getType());
        if (pieceTypeProviders.containsKey(structurePieceId)) {
            StructureTileProvider provider = pieceTypeProviders.get(structurePieceId);
            provider.getTextures(world, piece.getBoundingBox(), tilePredicates).forEach((pos, texture) -> {
                outTiles.put(pos, texture);
                structureProviders.put(pos, provider);
            });
        }
        return outTiles;
    }

    public void resolve(Map<ChunkPos, TileTexture> outTiles, Map<ChunkPos, StructureTileProvider> debugStructures, Map<ChunkPos, String> debugPredicates, Map<RegistryKey<Structure>, Map<ChunkPos, Marker>> outMarkers, World world, RegistryKey<Structure> key, ChunkPos pos, StructureSummary summary, RegistryKey<StructureType<?>> type, Collection<TagKey<Structure>> tags) {
        Pair<Identifier, Text> foundMarker = structureMarkers.get(key.getValue());
        if (foundMarker == null) {
            foundMarker = structureTagMarkers.entrySet().stream().filter(entry -> tags.contains(entry.getKey())).findFirst().map(Map.Entry::getValue).orElse(null);
        }
        if (foundMarker != null) {
            outMarkers.computeIfAbsent(key, k -> new HashMap<>()).put(pos, new Marker(SimplePointLandmark.TYPE, foundMarker.getLeft(), foundMarker.getRight(), pos.getCenterAtY(0), false, null));
        }

        if (startProviders.containsKey(key.getValue())) {
            StructureTileProvider provider = startProviders.get(key.getValue());
            provider.getTextures(world, summary.getBoundingBox(), debugPredicates).forEach((pos2, texture) -> {
                outTiles.put(pos2, texture);
                debugStructures.put(pos2, provider);
            });
        }

        if (startTypeProviders.containsKey(type.getValue())) {
            StructureTileProvider provider = startTypeProviders.get(key.getValue());
            provider.getTextures(world, summary.getBoundingBox(), debugPredicates).forEach((pos2, texture) -> {
                outTiles.put(pos2, texture);
                debugStructures.put(pos2, provider);
            });
        }

        tags.stream().filter(t -> startTagProviders.containsKey(t.id())).findFirst().ifPresent(tag -> {
            StructureTileProvider provider = startTagProviders.get(tag.id());
            provider.getTextures(world, summary.getBoundingBox(), debugPredicates).forEach((pos2, texture) -> {
                outTiles.put(pos2, texture);
                debugStructures.put(pos2, provider);
            });
        });

        summary.getChildren().forEach(p -> resolve(outTiles, debugStructures, debugPredicates, p, world));
    }

    private final Map<String, Map<Identifier, StructureTileProvider>> PROVIDER_MAPS = Map.of(
        "start/", startProviders,
        "tag/", startTagProviders,
        "type/", startTypeProviders,
        "piece/type/", pieceTypeProviders,
        "piece/jigsaw/single/", singleJigsawProviders,
        "piece/jigsaw/feature/", featureJigsawProviders
    );

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, TileTexture> textures = TileTextures.getInstance().getTextures();
        Set<TileTexture> unusedTextures = new HashSet<>(textures.values().stream().filter(t -> t.id().getPath().startsWith("structure")).toList());

        PROVIDER_MAPS.values().forEach(Map::clear);
        for (Map.Entry<Identifier, JsonElement> fileEntry : prepared.entrySet()) {
            Identifier fileId = fileEntry.getKey();
            PROVIDER_MAPS.forEach((prefix, providerMap) -> {
                if (fileId.getPath().startsWith(prefix)) {
                    Identifier id = new Identifier(fileId.getNamespace(), fileId.getPath().substring(prefix.length()));
                    try {
                        JsonObject fileJson = fileEntry.getValue().getAsJsonObject();
                        JsonElement textureJson = fileJson.get("textures");
                        List<TileTexture> defaultTextures = resolveTextureJson(textures, textureJson);
                        if (defaultTextures != null) {
                            StructureTileProvider provider = new StructureTileProvider(id, defaultTextures);
                            providerMap.put(provider.id(), provider);
                            unusedTextures.removeAll(provider.allTextures());
                        } else {
                            JsonObject textureObject = textureJson.getAsJsonObject();
                            Map<StructureTileProvider.ChunkMatcher, List<TileTexture>> matchers = new HashMap<>();
                            for (String matcherKey : textureObject.keySet()) {
                                Identifier matcherId = matcherKey.contains(":") ? new Identifier(matcherKey) : AntiqueAtlas.id(matcherKey);
                                StructureTileProvider.ChunkMatcher matcher = StructureTileProvider.getChunkMatcher(matcherId);
                                if (matcher == null) throw new IllegalStateException("Matcher %s does not exist!".formatted(matcherId.toString()));
                                List<TileTexture> matcherTextures = resolveTextureJson(textures, textureObject.get(matcherKey));
                                if (matcherTextures == null) throw new IllegalStateException("Malformed object %s in textures object!".formatted(matcherId.toString()));
                                matcherTextures.forEach(unusedTextures::remove);
                                matchers.put(matcher, matcherTextures);
                            }
                            if (matchers.isEmpty()) {
                                throw new IllegalStateException("No matcher keys were found in the textures object!");
                            }
                            StructureTileProvider provider = new StructureTileProvider(id, matchers);
                            providerMap.put(provider.id(), provider);
                            unusedTextures.removeAll(provider.allTextures());
                        }
                    } catch (Exception e) {
                        AntiqueAtlas.LOGGER.warn("[Antique Atlas] Error reading structure tile provider " + fileId + "!", e);
                    }
                }
            });
        }

        for (TileTexture texture : unusedTextures) {
            AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} isn't referenced by any structure tile provider!", texture.displayId());
        }
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(TileTextures.ID);
    }
}
