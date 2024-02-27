package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.StructureTiles;
import folk.sisby.antique_atlas.terrain.SurveyorChunkUtil;
import folk.sisby.antique_atlas.tile.TileType;
import folk.sisby.antique_atlas.util.Rect;
import folk.sisby.surveyor.SurveyorWorld;
import folk.sisby.surveyor.structure.StructureSummary;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class WorldTiles {
    private final Map<ChunkPos, TileType> biomeTiles = new HashMap<>();
    private final Map<ChunkPos, TileType> structureTiles = new HashMap<>();
    private final Rect tileScope = new Rect(0, 0, 0, 0);

    public WorldTiles(ClientWorld world) {
        for (ChunkPos pos : ((SurveyorWorld) world).surveyor$getWorldSummary().getChunks()) {
            TileType tile = world.getRegistryKey() == World.NETHER ? SurveyorChunkUtil.terrainToTileNether(world, pos) : SurveyorChunkUtil.terrainToTile(world, pos);
            if (tile != null) {
                tileScope.extendTo(pos.x, pos.z);
                biomeTiles.put(pos, tile);
            }
        }

        for (StructureSummary summary : ((SurveyorWorld) world).surveyor$getWorldSummary().getStructures()) {
            StructureTiles.getInstance().resolve(structureTiles, summary, world);
        }
    }

    public Rect getScope() {
        return tileScope;
    }

    public Identifier getTile(int x, int z) {
        return getTile(new ChunkPos(x, z));
    }

    public Identifier getTile(ChunkPos pos) {
        return structureTiles.containsKey(pos) ? structureTiles.get(pos).getId() : biomeTiles.containsKey(pos) ? biomeTiles.get(pos).getId() : null;
    }
}
