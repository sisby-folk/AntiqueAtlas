package folk.sisby.antique_atlas.tile;


import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.world.biome.BiomeKeys;

/**
 * Just a collection of Identifiers used when tiles are referenced from code
 *
 * @author Hunternif
 */
public class TileTypes {
    public static final TileType
        NETHER_WASTES = new TileType(BiomeKeys.NETHER_WASTES.getValue()),
        THE_VOID = new TileType(BiomeKeys.THE_VOID.getValue()),
        RIVER = new TileType(BiomeKeys.RIVER.getValue()),
        TILE_RAVINE = new TileType(AntiqueAtlas.id("ravine")),
        SWAMP_WATER = new TileType(AntiqueAtlas.id("swamp_water")),
        SWAMP_HUT = new TileType(AntiqueAtlas.id("swamp_hut")),
        IGLOO = new TileType(AntiqueAtlas.id("igloo")),
        DESERT_TEMPLE = new TileType(AntiqueAtlas.id("desert_pyramid")),
        JUNGLE_TEMPLE = new TileType(AntiqueAtlas.id("jungle_pyramid")),
        SHIPWRECK_BEACHED = new TileType(AntiqueAtlas.id("shipwreck_beached")),
        RUINED_PORTAL = new TileType(AntiqueAtlas.id("ruined_portal")),
        TILE_LAVA = new TileType(AntiqueAtlas.id("lava")),
        TILE_LAVA_SHORE = new TileType(AntiqueAtlas.id("lava_shore")),
        NETHER_FORTRESS_BRIDGE_CROSSING = new TileType(AntiqueAtlas.id("nether_bridge")),
        NETHER_BRIDGE_X = new TileType(AntiqueAtlas.id("nether_bridge_x")),
        NETHER_BRIDGE_Z = new TileType(AntiqueAtlas.id("nether_bridge_z")),
        NETHER_BRIDGE_END_X = new TileType(AntiqueAtlas.id("nether_bridge_end_x")),
        NETHER_BRIDGE_END_Z = new TileType(AntiqueAtlas.id("nether_bridge_end_z")),
        NETHER_FORTRESS_BRIDGE_SMALL_CROSSING = new TileType(AntiqueAtlas.id("nether_bridge_gate")),
        NETHER_FORTRESS_BRIDGE_STAIRS = new TileType(AntiqueAtlas.id("nether_tower")),
        NETHER_FORTRESS_WALL = new TileType(AntiqueAtlas.id("nether_wall")),
        NETHER_FORTRESS_EXIT = new TileType(AntiqueAtlas.id("nether_hall")),
        NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM = new TileType(AntiqueAtlas.id("nether_fort_stairs")),
        NETHER_FORTRESS_BRIDGE_PLATFORM = new TileType(AntiqueAtlas.id("nether_throne"));
}
