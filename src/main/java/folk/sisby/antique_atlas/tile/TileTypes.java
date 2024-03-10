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
        NETHER_WASTES = TileType.of(BiomeKeys.NETHER_WASTES.getValue()),
        THE_VOID = TileType.of(BiomeKeys.THE_VOID.getValue()),
        END_VOID = TileType.of(AntiqueAtlas.id("feature/end_void")),
        RIVER = TileType.of(BiomeKeys.RIVER.getValue()),
        FROZEN_RIVER = TileType.of(BiomeKeys.FROZEN_RIVER.getValue()),
        TILE_RAVINE = TileType.of(AntiqueAtlas.id("feature/ravine")),
        SWAMP_WATER = TileType.of(AntiqueAtlas.id("feature/swamp_water")),
        SWAMP_HUT = TileType.of(AntiqueAtlas.id("structure/swamp_hut")),
        IGLOO = TileType.of(AntiqueAtlas.id("structure/igloo")),
        DESERT_PYRAMID = TileType.of(AntiqueAtlas.id("structure/desert_pyramid")),
        JUNGLE_TEMPLE = TileType.of(AntiqueAtlas.id("structure/jungle_temple")),
        SHIPWRECK_BEACHED = TileType.of(AntiqueAtlas.id("structure/shipwreck_beached")),
        RUINED_PORTAL = TileType.of(AntiqueAtlas.id("structure/ruined_portal")),
        TILE_LAVA = TileType.of(AntiqueAtlas.id("feature/lava")),
        TILE_LAVA_SHORE = TileType.of(AntiqueAtlas.id("feature/lava_shore")),
        NETHER_FORTRESS_BRIDGE_CROSSING = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_crossing")),
        NETHER_FORTRESS_BRIDGE_HORIZONTAL = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_horizontal")),
        NETHER_FORTRESS_BRIDGE_VERTICAL = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_vertical")),
        NETHER_FORTRESS_BRIDGE_END_HORIZONTAL = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_end_horizontal")),
        NETHER_FORTRESS_BRIDGE_END_VERTICAL = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_end_vertical")),
        NETHER_FORTRESS_BRIDGE_SMALL_CROSSING = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_small_crossing")),
        NETHER_FORTRESS_BRIDGE_STAIRS = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_stairs")),
        NETHER_FORTRESS_CORRIDOR = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor")),
        NETHER_FORTRESS_CORRIDOR_EXIT = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor_exit")),
        NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor_nether_warts_room")),
        NETHER_FORTRESS_BRIDGE_PLATFORM = TileType.of(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_platform"));
}
