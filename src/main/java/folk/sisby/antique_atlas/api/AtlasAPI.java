package folk.sisby.antique_atlas.api;

import folk.sisby.antique_atlas.api.impl.MarkerApiImpl;
import folk.sisby.antique_atlas.api.impl.TileApiImpl;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Use this class to obtain a reference to the APIs.
 *
 * @author Hunternif
 */
@SuppressWarnings("unused")
public class AtlasAPI {
    private static final TileAPI tiles = new TileApiImpl();
    private static final MarkerAPI markers = new MarkerApiImpl();

    /**
     * API for biomes and custom tiles (i.e. dungeons, towns etc).
     */
    public static TileAPI getTileAPI() {
        return tiles;
    }

    /**
     * API for custom markers.
     */
    public static MarkerAPI getMarkerAPI() {
        return markers;
    }

    public static int getPlayerAtlasId(PlayerEntity player) {
        return player.getUuid().hashCode();
    }
}
