package folk.sisby.antique_atlas;


import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;

/**
 * Just a collection of Identifiers used when tiles are referenced from code
 *
 * @author Hunternif
 */
public class TileTypes {
    public static final Identifier
        NETHER_WASTES = BiomeKeys.NETHER_WASTES.getValue(),
        THE_VOID = BiomeKeys.THE_VOID.getValue(),
        END_VOID = AntiqueAtlas.id("feature/end_void"),
        RIVER = BiomeKeys.RIVER.getValue(),
        FROZEN_RIVER = BiomeKeys.FROZEN_RIVER.getValue(),
        TILE_RAVINE = AntiqueAtlas.id("feature/ravine"),
        SWAMP_WATER = AntiqueAtlas.id("feature/swamp_water"),
        SWAMP_HUT = AntiqueAtlas.id("structure/swamp_hut"),
        IGLOO = AntiqueAtlas.id("structure/igloo"),
        DESERT_PYRAMID = AntiqueAtlas.id("structure/desert_pyramid"),
        JUNGLE_TEMPLE = AntiqueAtlas.id("structure/jungle_temple"),
        SHIPWRECK_BEACHED = AntiqueAtlas.id("structure/shipwreck_beached"),
        RUINED_PORTAL = AntiqueAtlas.id("structure/ruined_portal"),
        TILE_LAVA = AntiqueAtlas.id("feature/lava"),
        TILE_LAVA_SHORE = AntiqueAtlas.id("feature/lava_shore"),
        NETHER_FORTRESS_BRIDGE_CROSSING = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_crossing"),
        NETHER_FORTRESS_BRIDGE_HORIZONTAL = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_horizontal"),
        NETHER_FORTRESS_BRIDGE_VERTICAL = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_vertical"),
        NETHER_FORTRESS_BRIDGE_END_HORIZONTAL = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_end_horizontal"),
        NETHER_FORTRESS_BRIDGE_END_VERTICAL = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_end_vertical"),
        NETHER_FORTRESS_BRIDGE_SMALL_CROSSING = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_small_crossing"),
        NETHER_FORTRESS_BRIDGE_STAIRS = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_stairs"),
        NETHER_FORTRESS_CORRIDOR = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor"),
        NETHER_FORTRESS_CORRIDOR_EXIT = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor_exit"),
        NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor_nether_warts_room"),
        NETHER_FORTRESS_BRIDGE_PLATFORM = AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_platform");
}
