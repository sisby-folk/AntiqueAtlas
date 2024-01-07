package folk.sisby.antique_atlas.core;

import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.util.Identifier;


/**
 * Just a collection of Identifiers used when tiles are referenced from code
 *
 * @author Hunternif
 */
public class TileIdMap {
    public static final Identifier
        // Village:
        TILE_VILLAGE_LIBRARY = AntiqueAtlas.id("npc_village_library"),
        TILE_VILLAGE_SMITHY = AntiqueAtlas.id("npc_village_smithy"),
        TILE_VILLAGE_L_HOUSE = AntiqueAtlas.id("npc_village_l_house"),
        TILE_VILLAGE_FARMLAND_SMALL = AntiqueAtlas.id("npc_village_farmland_small"),
        TILE_VILLAGE_FARMLAND_LARGE = AntiqueAtlas.id("npc_village_farmland_large"),
        TILE_VILLAGE_WELL = AntiqueAtlas.id("npc_village_well"),
        TILE_VILLAGE_TORCH = AntiqueAtlas.id("npc_village_torch"),
        TILE_VILLAGE_PATH_X = AntiqueAtlas.id("npc_village_path_x"),
        TILE_VILLAGE_PATH_Z = AntiqueAtlas.id("npc_village_path_z"),
        TILE_VILLAGE_HUT = AntiqueAtlas.id("npc_village_hut"),
        TILE_VILLAGE_SMALL_HOUSE = AntiqueAtlas.id("npc_village_small_house"),
        TILE_VILLAGE_BUTCHERS_SHOP = AntiqueAtlas.id("npc_village_butchers_shop"),
        TILE_VILLAGE_CHURCH = AntiqueAtlas.id("npc_village_church"),

    TILE_RAVINE = AntiqueAtlas.id("ravine"),
        SWAMP_WATER = AntiqueAtlas.id("swamp_water"),

    // Overworld stuff:
    RUINED_PORTAL = AntiqueAtlas.id("ruined_portal"),

    // Nether & Nether Fortress:
    TILE_LAVA = AntiqueAtlas.id("lava"),
        TILE_LAVA_SHORE = AntiqueAtlas.id("lava_shore"),
        NETHER_FORTRESS_BRIDGE_CROSSING = AntiqueAtlas.id("nether_bridge"),
        NETHER_BRIDGE_X = AntiqueAtlas.id("nether_bridge_x"),
        NETHER_BRIDGE_Z = AntiqueAtlas.id("nether_bridge_z"),
        NETHER_BRIDGE_END_X = AntiqueAtlas.id("nether_bridge_end_x"),
        NETHER_BRIDGE_END_Z = AntiqueAtlas.id("nether_bridge_end_z"),
        NETHER_FORTRESS_BRIDGE_SMALL_CROSSING = AntiqueAtlas.id("nether_bridge_gate"),
        NETHER_FORTRESS_BRIDGE_STAIRS = AntiqueAtlas.id("nether_tower"),
        NETHER_FORTRESS_WALL = AntiqueAtlas.id("nether_wall"),
        NETHER_FORTRESS_EXIT = AntiqueAtlas.id("nether_hall"),
        NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM = AntiqueAtlas.id("nether_fort_stairs"),
        NETHER_FORTRESS_BRIDGE_PLATFORM = AntiqueAtlas.id("nether_throne");
}
