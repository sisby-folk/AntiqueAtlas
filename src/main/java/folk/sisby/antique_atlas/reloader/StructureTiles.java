package folk.sisby.antique_atlas.reloader;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.Marker;
import folk.sisby.antique_atlas.TileProvider;
import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.antique_atlas.structure.StructurePieceTile;
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
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StructureTiles extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final StructureTiles INSTANCE = new StructureTiles();

    public static final Identifier ID = AntiqueAtlas.id("structures");
    public static final int VERSION = 2;

    public static StructureTiles getInstance() {
        return INSTANCE;
    }

    public static Collection<ChunkPos> ALWAYS(World world, BlockBox box) {
        return Collections.singleton(new ChunkPos(box.getCenter()));
    }

    public interface PieceMatcher {
        Collection<ChunkPos> matches(World world, BlockBox box);
    }

    public static Optional<ChunkPos> chunkPosIfX(JigsawPieceSummary piece) {
        List<JigsawJunction> junctions = piece.getJunctions();
        if (junctions.size() == 2) {
            if (junctions.get(0).getSourceZ() == junctions.get(1).getSourceZ() || junctions.get(0).getSourceX() != junctions.get(1).getSourceX()) {
                return Optional.of(new ChunkPos(piece.getBoundingBox().getCenter()));
            }
        } else {
            return Optional.of(new ChunkPos(piece.getBoundingBox().getCenter()));
        }
        return Optional.empty();
    }

    public static Optional<ChunkPos> chunkPosIfZ(JigsawPieceSummary piece) {
        List<JigsawJunction> junctions = piece.getJunctions();
        if (junctions.size() == 2) {
            if (junctions.get(0).getSourceX() == junctions.get(1).getSourceX() || junctions.get(0).getSourceZ() != junctions.get(1).getSourceZ()) {
                return Optional.of(new ChunkPos(piece.getBoundingBox().getCenter()));
            }
        } else {
            return Optional.of(new ChunkPos(piece.getBoundingBox().getCenter()));
        }
        return Optional.empty();
    }

    private final Map<Identifier, Pair<Identifier, Text>> structureMarkers = new HashMap<>(); // Structure Start
    private final Map<TagKey<Structure>, Pair<Identifier, Text>> structureTagMarkers = new HashMap<>(); // Structure Start

    private final Multimap<Identifier, Pair<Identifier, PieceMatcher>> structurePieceTiles = HashMultimap.create();
    private final Multimap<Identifier, StructurePieceTile> jigsawTiles = HashMultimap.create();

    public StructureTiles() {
        super(new Gson(), "atlas/structures");
    }

    public void registerTile(StructurePieceType structurePieceType, Identifier textureId, PieceMatcher pieceMatcher) {
        Identifier id = Registries.STRUCTURE_PIECE.getId(structurePieceType);
        structurePieceTiles.put(id, Pair.of(textureId, pieceMatcher));
    }

    public void registerJigsawTile(Identifier id, StructurePieceTile tile) {
        jigsawTiles.put(id, tile);
    }

    public void registerMarker(StructureType<?> structureFeature, Identifier markerType, Text name) {
        structureMarkers.put(Registries.STRUCTURE_TYPE.getId(structureFeature), Pair.of(markerType, name));
    }

    public void registerMarker(TagKey<Structure> structureTag, Identifier markerType, Text name) {
        structureTagMarkers.put(structureTag, Pair.of(markerType, name));
    }

    public Map<ChunkPos, TileTexture> resolve(Map<ChunkPos, TileTexture> tiles, Map<ChunkPos, TileProvider> structureProviders, StructurePieceSummary piece, World world) {
        if (piece instanceof JigsawPieceSummary jigsawPiece) {
            for (StructurePieceTile pieceTile : jigsawTiles.get(jigsawPiece.getId())) {
                chunkPosIfX(jigsawPiece).ifPresent(pos -> {
                    TileProvider provider = BiomeTileProviders.getInstance().getTileProvider(pieceTile.tileX());
                    if (provider == TileProvider.DEFAULT) AntiqueAtlas.LOGGER.warn("[Antique Atlas] failed to find tile {} for structure {}", pieceTile.tileX(), jigsawPiece.getId());
                    TileTexture newTile = provider.getTexture(new ChunkPos(pos.x, pos.z), null);
                    structureProviders.put(pos, provider);
                    tiles.put(new ChunkPos(pos.x, pos.z), newTile);
                });
                chunkPosIfZ(jigsawPiece).ifPresent(pos -> {
                    TileProvider provider = BiomeTileProviders.getInstance().getTileProvider(pieceTile.tileZ());
                    if (provider == TileProvider.DEFAULT) AntiqueAtlas.LOGGER.warn("[Antique Atlas] failed to find tile {} for structure {}", pieceTile.tileX(), jigsawPiece.getId());
                    TileTexture newTile = provider.getTexture(new ChunkPos(pos.x, pos.z), null);
                    structureProviders.put(pos, provider);
                    tiles.put(new ChunkPos(pos.x, pos.z), newTile);
                });
            }
            return tiles;
        }

        Identifier structurePieceId = Registries.STRUCTURE_PIECE.getId(piece.getType());
        if (structurePieceTiles.containsKey(structurePieceId)) {
            for (Pair<Identifier, PieceMatcher> entry : structurePieceTiles.get(structurePieceId)) {
                for (ChunkPos pos : entry.getRight().matches(world, piece.getBoundingBox())) {
                    TileProvider provider = BiomeTileProviders.getInstance().getTileProvider(entry.getLeft());
                    if (provider == TileProvider.DEFAULT) AntiqueAtlas.LOGGER.warn("[Antique Atlas] failed to find tile {} for structure {}", entry.getLeft(), structurePieceId);
                    structureProviders.put(pos, provider);
                    tiles.put(pos, provider.getTexture(new ChunkPos(pos.x, pos.z), null));
                }
            }
        }
        return tiles;
    }

    public void resolve(Map<ChunkPos, TileTexture> outTiles, Map<ChunkPos, TileProvider> structureProviders, Map<RegistryKey<Structure>, Map<ChunkPos, Marker>> outMarkers, World world, RegistryKey<Structure> key, ChunkPos pos, StructureSummary summary, RegistryKey<StructureType<?>> type, Collection<TagKey<Structure>> tags) {
        Pair<Identifier, Text> foundMarker = structureMarkers.get(key.getValue());
        if (foundMarker == null) {
            foundMarker = structureTagMarkers.entrySet().stream().filter(entry -> tags.contains(entry.getKey())).findFirst().map(Map.Entry::getValue).orElse(null);
        }
        if (foundMarker != null) {
            outMarkers.computeIfAbsent(key, k -> new HashMap<>()).put(pos, new Marker(SimplePointLandmark.TYPE, foundMarker.getLeft(), foundMarker.getRight(), pos.getCenterAtY(0), false, null));
        }

        summary.getChildren().forEach(p -> resolve(outTiles, structureProviders, p, world));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, StructurePieceTile> outMap = new HashMap<>();
        for (Map.Entry<Identifier, JsonElement> fileEntry : prepared.entrySet()) {
            Identifier fileId = fileEntry.getKey();
            try {
                JsonObject fileJson = fileEntry.getValue().getAsJsonObject();

                StructurePieceTile structurePieceTile;
                int version = fileJson.getAsJsonPrimitive("version").getAsInt();
                if (version == 1) {
                    structurePieceTile = new StructurePieceTile(
                        Identifier.tryParse(fileJson.get("tile").getAsString()),
                        Identifier.tryParse(fileJson.get("tile").getAsString())
                    );
                } else if (version == VERSION) {
                    structurePieceTile = new StructurePieceTile(
                        Identifier.tryParse(fileJson.get("tile_x").getAsString()),
                        Identifier.tryParse(fileJson.get("tile_z").getAsString())
                    );
                } else {
                    throw new RuntimeException("Incompatible version (" + VERSION + " != " + version + ")");
                }

                outMap.put(fileId, structurePieceTile);
            } catch (Exception e) {
                AntiqueAtlas.LOGGER.warn("Error reading structure piece config from " + fileId + "!", e);
            }
        }

        outMap.forEach(this::registerJigsawTile);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
