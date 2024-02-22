package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.terrain.SurveyorChunkUtil;
import folk.sisby.antique_atlas.tile.TileType;
import folk.sisby.antique_atlas.util.Rect;
import folk.sisby.surveyor.SurveyorWorld;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class WorldTiles {
    private final Map<ChunkPos, TileType> biomeTiles = new HashMap<>();
    private final Rect tileScope = new Rect(0, 0, 0, 0);

    public WorldTiles(ClientWorld world) {
        for (ChunkPos pos : ((SurveyorWorld) world).surveyor$getWorldSummary().getChunks()) {
            TileType tile = world.getRegistryKey() == World.NETHER ? SurveyorChunkUtil.terrainToTileNether(world, pos) : SurveyorChunkUtil.terrainToTile(world, pos);
            if (tile != null) {
                tileScope.extendTo(pos.x, pos.z);
                biomeTiles.put(pos, tile);
            }
        }
    }

    public Rect getScope() {
        return tileScope;
    }

    public Identifier getTile(int x, int z) {
        TileType tile = biomeTiles.get(new ChunkPos(x, z));
        return tile == null ? null : tile.getId();
    }
}
