package folk.sisby.antique_atlas.client.api;

import folk.sisby.antique_atlas.api.TileAPI;
import folk.sisby.antique_atlas.client.gui.tiles.TileRenderIterator;
import folk.sisby.antique_atlas.util.Rect;
import net.minecraft.world.World;

public interface ClientTileAPI extends TileAPI {
    TileRenderIterator getTiles(World world, int atlasID, Rect scope, int step);
}
