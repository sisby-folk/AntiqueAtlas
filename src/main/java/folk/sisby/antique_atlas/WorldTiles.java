package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.StructureTiles;
import folk.sisby.antique_atlas.terrain.SurveyorChunkUtil;
import folk.sisby.antique_atlas.tile.TileType;
import folk.sisby.antique_atlas.util.Rect;
import folk.sisby.surveyor.SurveyorWorld;
import folk.sisby.surveyor.structure.StructureSummary;
import folk.sisby.surveyor.structure.WorldStructureSummary;
import folk.sisby.surveyor.terrain.ChunkSummary;
import folk.sisby.surveyor.terrain.WorldTerrainSummary;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WorldTiles {
    private static final int CHUNK_TICK_LIMIT = AntiqueAtlas.CONFIG.performance.chunkTickLimit;
    private final Map<ChunkPos, TileType> biomeTiles = new HashMap<>();
    private final Map<ChunkPos, TileType> structureTiles = new HashMap<>();
    private final Rect tileScope = new Rect(0, 0, 0, 0);
    private final Deque<ChunkPos> terrainDeque = new ConcurrentLinkedDeque<>();
    boolean isFinished = false;

    public WorldTiles(World world) {
        ((SurveyorWorld) world).surveyor$getWorldSummary().terrain().keySet().forEach(terrainDeque::addLast);
        for (StructureSummary summary : ((SurveyorWorld) world).surveyor$getWorldSummary().structures().values()) {
            StructureTiles.getInstance().resolve(structureTiles, summary, world);
        }
        AntiqueAtlas.LOGGER.info("[Antique Atlas] Beginning to load terrain for {} - {} chunks available.", world.getRegistryKey().getValue(), terrainDeque.size());
    }

    public void onChunkAdded(World world, WorldTerrainSummary ws, ChunkPos pos, ChunkSummary chunk) {
        if (!terrainDeque.contains(pos)) terrainDeque.add(pos);
    }

    public void onStructureAdded(World world, WorldStructureSummary ws, StructureSummary structure) {
        StructureTiles.getInstance().resolve(structureTiles, structure, world);
    }

    public void tick(World world) {
        for (int i = 0; i < CHUNK_TICK_LIMIT; i++) {
            ChunkPos pos = terrainDeque.pollFirst();
            if (pos == null) break;
            TileType tile = world.getRegistryKey() == World.NETHER ? SurveyorChunkUtil.terrainToTileNether(world, pos) : SurveyorChunkUtil.terrainToTile(world, pos);
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

    public Identifier getTile(int x, int z) {
        return getTile(new ChunkPos(x, z));
    }

    public Identifier getTile(ChunkPos pos) {
        return structureTiles.containsKey(pos) ? structureTiles.get(pos).id() : biomeTiles.containsKey(pos) ? biomeTiles.get(pos).id() : null;
    }
}
