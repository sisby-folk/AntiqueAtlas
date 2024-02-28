package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.StructureTiles;
import folk.sisby.antique_atlas.terrain.SurveyorChunkUtil;
import folk.sisby.antique_atlas.tile.TileType;
import folk.sisby.antique_atlas.util.Rect;
import folk.sisby.surveyor.SurveyorWorld;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.chunk.ChunkSummary;
import folk.sisby.surveyor.structure.StructureSummary;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WorldTiles {
    private static final int CHUNK_TICK_LIMIT = 100;
    private final Map<ChunkPos, TileType> biomeTiles = new HashMap<>();
    private final Map<ChunkPos, TileType> structureTiles = new HashMap<>();
    private final Rect tileScope = new Rect(0, 0, 0, 0);
    private final Deque<ChunkPos> terrainDeque = new ConcurrentLinkedDeque<>();
    boolean isFinished = false;

    public WorldTiles(PlayerEntity player, World world) {
        ((SurveyorWorld) world).surveyor$getWorldSummary().getChunks().stream().sorted(Comparator.comparingInt(p -> player == null ? 0 : player.getChunkPos().getChebyshevDistance(p))).forEach(terrainDeque::addLast);
        for (StructureSummary summary : ((SurveyorWorld) world).surveyor$getWorldSummary().getStructures()) {
            StructureTiles.getInstance().resolve(structureTiles, summary, world);
        }
        AntiqueAtlas.LOGGER.info("[Antique Atlas] Beginning to load terrain for {} - {} chunks available.", world.getRegistryKey().getValue(), terrainDeque.size());
    }

    public void onChunkAdded(World world, WorldSummary ws, ChunkPos pos, ChunkSummary chunk) {
        if (!terrainDeque.contains(pos)) terrainDeque.add(pos);
    }

    public void onStructureAdded(World world, WorldSummary ws, StructureSummary structure) {
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
