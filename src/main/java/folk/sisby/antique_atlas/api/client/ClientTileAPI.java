package folk.sisby.antique_atlas.api.client;

import folk.sisby.antique_atlas.api.TileAPI;
import folk.sisby.antique_atlas.client.TileRenderIterator;
import folk.sisby.antique_atlas.util.Rect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public interface ClientTileAPI extends TileAPI {
    TileRenderIterator getTiles(World world, int atlasID, Rect scope, int step);
}
