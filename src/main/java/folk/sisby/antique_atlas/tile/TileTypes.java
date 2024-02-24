package folk.sisby.antique_atlas.tile;


import folk.sisby.antique_atlas.AntiqueAtlas;

/**
 * Just a collection of Identifiers used when tiles are referenced from code
 *
 * @author Hunternif
 */
public class TileTypes {
    public static final TileType
        TILE_RAVINE = TileType.of(AntiqueAtlas.id("ravine")),
        SWAMP_WATER = TileType.of(AntiqueAtlas.id("swamp_water")),
        SWAMP_HUT = TileType.of(AntiqueAtlas.id("swamp_hut")),
        IGLOO = TileType.of(AntiqueAtlas.id("igloo")),
        DESERT_TEMPLE = TileType.of(AntiqueAtlas.id("desert_pyramid")),
        JUNGLE_TEMPLE = TileType.of(AntiqueAtlas.id("jungle_pyramid")),
        SHIPWRECK_BEACHED = TileType.of(AntiqueAtlas.id("shipwreck_beached")),
        RUINED_PORTAL = TileType.of(AntiqueAtlas.id("ruined_portal")),
        TILE_LAVA = TileType.of(AntiqueAtlas.id("lava")),
        TILE_LAVA_SHORE = TileType.of(AntiqueAtlas.id("lava_shore")),
        NETHER_FORTRESS_BRIDGE_CROSSING = TileType.of(AntiqueAtlas.id("nether_bridge")),
        NETHER_BRIDGE_X = TileType.of(AntiqueAtlas.id("nether_bridge_x")),
        NETHER_BRIDGE_Z = TileType.of(AntiqueAtlas.id("nether_bridge_z")),
        NETHER_BRIDGE_END_X = TileType.of(AntiqueAtlas.id("nether_bridge_end_x")),
        NETHER_BRIDGE_END_Z = TileType.of(AntiqueAtlas.id("nether_bridge_end_z")),
        NETHER_FORTRESS_BRIDGE_SMALL_CROSSING = TileType.of(AntiqueAtlas.id("nether_bridge_gate")),
        NETHER_FORTRESS_BRIDGE_STAIRS = TileType.of(AntiqueAtlas.id("nether_tower")),
        NETHER_FORTRESS_WALL = TileType.of(AntiqueAtlas.id("nether_wall")),
        NETHER_FORTRESS_EXIT = TileType.of(AntiqueAtlas.id("nether_hall")),
        NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM = TileType.of(AntiqueAtlas.id("nether_fort_stairs")),
        NETHER_FORTRESS_BRIDGE_PLATFORM = TileType.of(AntiqueAtlas.id("nether_throne"));
}
