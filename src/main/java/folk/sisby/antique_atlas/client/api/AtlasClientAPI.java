package folk.sisby.antique_atlas.client.api;

import folk.sisby.antique_atlas.api.MarkerAPI;
import folk.sisby.antique_atlas.client.api.impl.MarkerApiImplClient;
import folk.sisby.antique_atlas.client.api.impl.TileApiImplClient;

/**
 * Use this class to obtain a reference to the client-side APIs.
 *
 * @author Hunternif
 */
@SuppressWarnings("unused")
public class AtlasClientAPI {
    private static final ClientTileAPI tiles = new TileApiImplClient();
    private static final MarkerAPI markers = new MarkerApiImplClient();

    /**
     * API for biomes and custom tiles (i.e. dungeons, towns etc).
     */
    public static ClientTileAPI getTileAPI() {
        return tiles;
    }

    /**
     * API for custom markers.
     */
    public static MarkerAPI getMarkerAPI() {
        return markers;
    }
}
