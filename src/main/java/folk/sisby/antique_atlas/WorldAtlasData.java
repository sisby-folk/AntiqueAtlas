package folk.sisby.antique_atlas;

import com.google.common.collect.Multimap;
import folk.sisby.antique_atlas.reloader.MarkerTextures;
import folk.sisby.antique_atlas.reloader.StructureTileProviders;
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
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WorldAtlasData {
    public static final Map<RegistryKey<World>, WorldAtlasData> WORLDS = new HashMap<>();

    public static WorldAtlasData get(World world) {
        return WorldAtlasData.WORLDS.computeIfAbsent(world.getRegistryKey(), k -> new WorldAtlasData(world));
    }

    private final Map<ChunkPos, TileTexture> biomeTiles = new HashMap<>();
    private final Map<ChunkPos, TileTexture> structureTiles = new HashMap<>();
    private final Map<Landmark<?>, MarkerTexture> landmarkMarkers = new ConcurrentHashMap<>();
    private final Map<Landmark<?>, MarkerTexture> structureMarkers = new ConcurrentHashMap<>();

    private final Rect tileScope = new Rect(0, 0, 0, 0);
    private final Deque<ChunkPos> terrainDeque = new ConcurrentLinkedDeque<>();
    boolean isFinished = false;

    // Debug Display Info
    private final Map<ChunkPos, String> debugBiomePredicates = new HashMap<>();
    private final Map<ChunkPos, String> debugStructurePredicates = new HashMap<>();
    private final Map<ChunkPos, TerrainTileProvider> debugBiomes = new HashMap<>();
    private final Map<ChunkPos, StructureTileProvider> debugStructures = new HashMap<>();

    public WorldAtlasData(World world) {
        ((SurveyorWorld) world).surveyor$getWorldSummary().terrain().keySet().forEach(terrainDeque::addLast);
        ((SurveyorWorld) world).surveyor$getWorldSummary().structures().asMap().forEach((key, map) -> onStructuresAdded(world, ((SurveyorWorld) world).surveyor$getWorldSummary().structures(), MapUtil.hashMultiMapOf(Map.of(key, map.keySet()))));
        refreshLandmarkMarkers(world);
        AntiqueAtlas.LOGGER.info("[Antique Atlas] Beginning to load terrain for {} - {} chunks available.", world.getRegistryKey().getValue(), terrainDeque.size());
    }

    public void onTerrainUpdated(World ignored, WorldTerrainSummary ignored2, Collection<ChunkPos> chunks) {
        for (ChunkPos pos : chunks) {
            if (!terrainDeque.contains(pos)) terrainDeque.add(pos);
        }
    }

    public void onStructuresAdded(World world, WorldStructureSummary ws, Multimap<RegistryKey<Structure>, ChunkPos> summaries) {
        summaries.forEach((key, pos) -> StructureTileProviders.getInstance().resolve(structureTiles, debugStructures, debugStructurePredicates, structureMarkers, world, key, pos, ws.get(key, pos), ws.getType(key), ws.getTags(key)));
    }

    public void tick(World world) {
        for (int i = 0; i < AntiqueAtlas.CONFIG.performance.chunkTickLimit; i++) {
            ChunkPos pos = terrainDeque.pollFirst();
            if (pos == null) break;
            Pair<TerrainTileProvider, TileElevation> tile = world.getRegistryKey() == World.NETHER ? TerrainTiling.terrainToTileNether(world, pos) : TerrainTiling.terrainToTile(world, pos);
            if (tile != null) {
                tileScope.extendTo(pos.x, pos.z);
                biomeTiles.put(pos, tile.left().getTexture(pos, tile.right()));
                debugBiomes.put(pos, tile.left());
                debugBiomePredicates.put(pos, tile.right() == null ? null : tile.right().getName());
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

    public Identifier getProvider(ChunkPos pos) {
        if (structureTiles.containsKey(pos)) {
            return debugStructures.get(pos).id();
        } else {
            return debugBiomes.containsKey(pos) ? debugBiomes.get(pos).id() : null;
        }
    }

    public String getTilePredicate(ChunkPos pos) {
        if (structureTiles.containsKey(pos)) {
            return debugStructurePredicates.get(pos);
        } else {
            return debugBiomePredicates.get(pos);
        }
    }

    public void refreshLandmarkMarkers(World world) {
        landmarkMarkers.clear();
        ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().keySet().forEach(((landmarkType, pos) -> {
            if (landmarkType == NetherPortalLandmark.TYPE) {
                NetherPortalLandmark landmark = (NetherPortalLandmark) ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().get(landmarkType, pos);
                landmarkMarkers.put(landmark, MarkerTextures.getInstance().get(AntiqueAtlas.id("custom/nether_portal")));
            } else if (landmarkType == PlayerDeathLandmark.TYPE) {
                PlayerDeathLandmark landmark = (PlayerDeathLandmark) ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().get(landmarkType, pos);

                AntiqueAtlasConfig.GraveStyle style = AntiqueAtlas.CONFIG.ui.graveStyle;
                if (landmark.name() == null && style == AntiqueAtlasConfig.GraveStyle.CAUSE) style = AntiqueAtlasConfig.GraveStyle.DIED;
                MutableText timeText = Text.literal(String.valueOf(1 + (landmark.created() / 24000))).formatted(Formatting.WHITE);
                String key = "gui.antique_atlas.marker.death.%s".formatted(style.toString().toLowerCase());
                MutableText text = switch (style) {
                    case CAUSE -> Text.translatable(key, landmark.name().copy().formatted(Formatting.GRAY).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
                    case GRAVE, ITEMS, DIED -> Text.translatable(key, Text.translatable("gui.antique_atlas.marker.death.%s.verb".formatted(style.toString().toLowerCase())).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
                    case EUPHEMISMS -> Text.translatable(key, Text.translatable("gui.antique_atlas.marker.death.%s.verb.%s".formatted(style.toString().toLowerCase(), new Random(landmark.seed()).nextInt(11))).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
                };
                Identifier icon = switch (style) {
                    case CAUSE, GRAVE, DIED, EUPHEMISMS -> AntiqueAtlas.id("landmark/tomb");
                    case ITEMS -> AntiqueAtlas.id("landmark/bundle");
                };

                landmarkMarkers.put(new PlayerDeathLandmark(landmark.pos(), landmark.owner(), text, landmark.created(), landmark.seed()), MarkerTextures.getInstance().get(icon));
            } else {
                Landmark<?> landmark = ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().get(landmarkType, pos);
                landmarkMarkers.put(landmark, MarkerTextures.getInstance().getTextures().getOrDefault(landmark.texture(), MarkerTexture.DEFAULT));
            }
        }));
    }

    public void onLandmarksAdded(World world, WorldLandmarks ignored, Map<LandmarkType<?>, Map<BlockPos, Landmark<?>>> ignored2) {
        refreshLandmarkMarkers(world);
    }

    public void onLandmarksRemoved(ClientWorld world, WorldLandmarks ignored, Multimap<LandmarkType<?>, BlockPos> ignored2) {
        refreshLandmarkMarkers(world);
    }

    public static boolean landmarkIsEditable(Landmark<?> landmark) {
        return !(MinecraftClient.getInstance().isIntegratedServerRunning() ? landmark.owner() == null : !Uuids.getUuidFromProfile(MinecraftClient.getInstance().getSession().getProfile()).equals(landmark.owner()));
    }

    public boolean deleteLandmark(World world, Landmark<?> landmark) {
        ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().remove(world, landmark.type(), landmark.pos());
        return true;
    }

    public Map<Landmark<?>, MarkerTexture> getEditableLandmarks() {
        Map<Landmark<?>, MarkerTexture> map = new HashMap<>();
        landmarkMarkers.forEach((landmark, texture) -> { if (landmarkIsEditable(landmark)) map.put(landmark, texture); });
        return map;
    }

    public Map<Landmark<?>, MarkerTexture> getAllMarkers() {
        Map<Landmark<?>, MarkerTexture> map = new HashMap<>();
        map.putAll(landmarkMarkers);
        map.putAll(structureMarkers);
        return map;
    }

    public MarkerTexture getMarkerTexture(Landmark<?> landmark) {
        return landmarkMarkers.getOrDefault(landmark, structureMarkers.get(landmark));
    }

    public void placeCustomMarker(World world, MarkerTexture selectedTexture, MutableText label, BlockPos blockPos) {
        ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().put(world, new SimplePointLandmark(
            blockPos,
            Uuids.getUuidFromProfile(MinecraftClient.getInstance().getSession().getProfile()),
            DyeColor.BLUE,
            label,
            selectedTexture.keyId()
        ));
    }
}
