package folk.sisby.antique_atlas;

import com.google.common.collect.Multimap;
import folk.sisby.antique_atlas.reloader.StructureTiles;
import folk.sisby.antique_atlas.terrain.SurveyorChunkUtil;
import folk.sisby.antique_atlas.util.Rect;
import folk.sisby.surveyor.SurveyorWorld;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.LandmarkType;
import folk.sisby.surveyor.landmark.NetherPortalLandmark;
import folk.sisby.surveyor.landmark.PlayerDeathLandmark;
import folk.sisby.surveyor.landmark.SimplePointLandmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.structure.WorldStructureSummary;
import folk.sisby.surveyor.terrain.WorldTerrainSummary;
import folk.sisby.surveyor.util.MapUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WorldAtlasData {
    private static final int CHUNK_TICK_LIMIT = AntiqueAtlas.CONFIG.performance.chunkTickLimit;
    private final Map<ChunkPos, TileTexture> biomeTiles = new HashMap<>();
    private final Map<ChunkPos, TileTexture> structureTiles = new HashMap<>();
    private final Rect tileScope = new Rect(0, 0, 0, 0);
    private final Deque<ChunkPos> terrainDeque = new ConcurrentLinkedDeque<>();
    private final Map<LandmarkType<?>, Map<BlockPos, Marker>> landmarkMarkers = new ConcurrentHashMap<>();
    private final Map<RegistryKey<Structure>, Map<ChunkPos, Marker>> structureMarkers = new ConcurrentHashMap<>();
    boolean isFinished = false;

    public WorldAtlasData(World world) {
        ((SurveyorWorld) world).surveyor$getWorldSummary().terrain().keySet().forEach(terrainDeque::addLast);
        ((SurveyorWorld) world).surveyor$getWorldSummary().structures().asMap().forEach((key, map) -> onStructuresAdded(world, ((SurveyorWorld) world).surveyor$getWorldSummary().structures(), MapUtil.hashMultiMapOf(Map.of(key, map.keySet()))));
        refreshLandmarkMarkers(world);
        AntiqueAtlas.LOGGER.info("[Antique Atlas] Beginning to load terrain for {} - {} chunks available.", world.getRegistryKey().getValue(), terrainDeque.size());
    }

    public void onTerrainUpdated(World world, WorldTerrainSummary ws, Collection<ChunkPos> chunks) {
        for (ChunkPos pos : chunks) {
            if (!terrainDeque.contains(pos)) terrainDeque.add(pos);
        }
    }

    public void onStructuresAdded(World world, WorldStructureSummary ws, Multimap<RegistryKey<Structure>, ChunkPos> summaries) {
        summaries.forEach((key, pos) -> StructureTiles.getInstance().resolve(structureTiles, structureMarkers, world, key, pos, ws.get(key, pos), ws.getType(key), ws.getTags(key)));
    }

    public void tick(World world) {
        for (int i = 0; i < CHUNK_TICK_LIMIT; i++) {
            ChunkPos pos = terrainDeque.pollFirst();
            if (pos == null) break;
            TileTexture tile = world.getRegistryKey() == World.NETHER ? SurveyorChunkUtil.terrainToTileNether(world, pos) : SurveyorChunkUtil.terrainToTile(world, pos);
            if (tile != null) {
                tileScope.extendTo(pos.x, pos.z);
                biomeTiles.put(pos, tile);
            }
        }
        if (!isFinished && terrainDeque.isEmpty()) {
            isFinished = true;
            AntiqueAtlas.LOGGER.info("[Antique Atlas] Finished loading terrain for {} - {} tiles.", world.getRegistryKey().getValue(), biomeTiles.size());
        }
    }

    public Rect getScope() {
        return tileScope;
    }

    public TileTexture getTile(int x, int z) {
        return getTile(new ChunkPos(x, z));
    }

    public TileTexture getTile(ChunkPos pos) {
        return structureTiles.containsKey(pos) ? structureTiles.get(pos) : biomeTiles.getOrDefault(pos, null);
    }

    public void refreshLandmarkMarkers(World world) {
        landmarkMarkers.clear();
        ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().keySet().forEach(((landmarkType, pos) -> {
            if (landmarkType == NetherPortalLandmark.TYPE) {
                NetherPortalLandmark landmark = (NetherPortalLandmark) ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().get(landmarkType, pos);
                landmarkMarkers.computeIfAbsent(landmarkType, t -> new HashMap<>()).put(landmark.pos(), new Marker(
                    landmarkType, AntiqueAtlas.id("nether_portal"), landmark.name(), landmark.pos(), true, landmark.owner()
                ));
            }
            if (landmarkType == PlayerDeathLandmark.TYPE) {
                PlayerDeathLandmark landmark = (PlayerDeathLandmark) ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().get(landmarkType, pos);

                AntiqueAtlasConfig.GraveStyle style = AntiqueAtlas.CONFIG.ui.graveStyle;
                if (landmark.name() == null && style == AntiqueAtlasConfig.GraveStyle.CAUSE) style = AntiqueAtlasConfig.GraveStyle.DIED;
                MutableText timeText = Text.literal(String.valueOf(1 + (landmark.created() / 24000))).formatted(Formatting.WHITE);
                MutableText text = switch (style) {
                    case CAUSE -> Text.translatable("gui.antique_atlas.marker.death.%s".formatted(style.toString().toLowerCase()), landmark.name().copy().formatted(Formatting.GRAY).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
                    case GRAVE, ITEMS, DIED -> Text.translatable("gui.antique_atlas.marker.death.%s".formatted(style.toString().toLowerCase()), Text.translatable("gui.antique_atlas.marker.death.%s.verb".formatted(style.toString().toLowerCase())).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
                    case EUPHEMISMS -> Text.translatable("gui.antique_atlas.marker.death.%s".formatted(style.toString().toLowerCase()), Text.translatable("gui.antique_atlas.marker.death.%s.verb.%s".formatted(style.toString().toLowerCase(), new Random(landmark.seed()).nextInt(11))).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
                };
                Identifier icon = switch (style) {
                    case CAUSE, GRAVE, DIED, EUPHEMISMS -> AntiqueAtlas.id("tomb");
                    case ITEMS -> AntiqueAtlas.id("bundle");
                };

                landmarkMarkers.computeIfAbsent(landmarkType, t -> new HashMap<>()).put(landmark.pos(), new Marker(
                    landmarkType, icon, text, landmark.pos(), true, landmark.owner()
                ));
            }
            if (landmarkType == SimplePointLandmark.TYPE) {
                SimplePointLandmark landmark = (SimplePointLandmark) ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().get(landmarkType, pos);
                landmarkMarkers.computeIfAbsent(landmarkType, t -> new HashMap<>()).put(landmark.pos(), new Marker(
                    landmarkType, landmark.texture(), landmark.name(), landmark.pos(), true, landmark.owner()
                ));
            }
        }));
    }

    public void onLandmarksAdded(World world, WorldLandmarks ws, Map<LandmarkType<?>, Map<BlockPos, Landmark<?>>> landmark) {
        refreshLandmarkMarkers(world);
    }

    public void onLandmarksRemoved(ClientWorld world, WorldLandmarks landmarks, Multimap<LandmarkType<?>, BlockPos> landmark) {
        refreshLandmarkMarkers(world);
    }

    public void addMarker(PlayerEntity player, World world, Marker marker) {
        ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().put(world, new SimplePointLandmark(
            new BlockPos(marker.pos().x(), 0, marker.pos().z()),
            player.getUuid(),
            DyeColor.BLUE,
            marker.label(),
            marker.type()
        ));
    }

    public boolean deleteMarker(World world, Marker marker) {
        if (marker.isGlobal()) return false;
        ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().remove(world, marker.landmarkType(), marker.blockPos());
        return true;
    }

    public Collection<Marker> getAllMarkers() {
        List<Marker> outList = new ArrayList<>();
        landmarkMarkers.values().forEach(m -> outList.addAll(m.values()));
        structureMarkers.values().forEach(m -> outList.addAll(m.values()));
        return outList;
    }
}
