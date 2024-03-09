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
        END_VOID = new TileType(AntiqueAtlas.id("feature/end_void")),
        RIVER = new TileType(BiomeKeys.RIVER.getValue()),
        TILE_RAVINE = new TileType(AntiqueAtlas.id("feature/ravine")),
        SWAMP_WATER = new TileType(AntiqueAtlas.id("feature/swamp_water")),
        SWAMP_HUT = new TileType(AntiqueAtlas.id("structure/swamp_hut")),
        IGLOO = new TileType(AntiqueAtlas.id("structure/igloo")),
        DESERT_PYRAMID = new TileType(AntiqueAtlas.id("structure/desert_pyramid")),
        JUNGLE_TEMPLE = new TileType(AntiqueAtlas.id("structure/jungle_temple")),
        SHIPWRECK_BEACHED = new TileType(AntiqueAtlas.id("structure/shipwreck_beached")),
        RUINED_PORTAL = new TileType(AntiqueAtlas.id("structure/ruined_portal")),
        TILE_LAVA = new TileType(AntiqueAtlas.id("feature/lava")),
        TILE_LAVA_SHORE = new TileType(AntiqueAtlas.id("feature/lava_shore")),
        NETHER_FORTRESS_BRIDGE_CROSSING = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_crossing")),
        NETHER_FORTRESS_BRIDGE_HORIZONTAL = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_horizontal")),
        NETHER_FORTRESS_BRIDGE_VERTICAL = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_vertical")),
        NETHER_FORTRESS_BRIDGE_END_HORIZONTAL = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_end_horizontal")),
        NETHER_FORTRESS_BRIDGE_END_VERTICAL = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_end_vertical")),
        NETHER_FORTRESS_BRIDGE_SMALL_CROSSING = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_small_crossing")),
        NETHER_FORTRESS_BRIDGE_STAIRS = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_stairs")),
        NETHER_FORTRESS_CORRIDOR = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor")),
        NETHER_FORTRESS_CORRIDOR_EXIT = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor_exit")),
        NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor_nether_warts_room")),
        NETHER_FORTRESS_BRIDGE_PLATFORM = new TileType(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_platform"));
}
