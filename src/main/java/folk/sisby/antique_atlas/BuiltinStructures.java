package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.StructureTileProviders;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltinStructures {
    public static void init() {
        if (AntiqueAtlas.CONFIG.markVillages) StructureTileProviders.getInstance().registerMarker(StructureTags.VILLAGE, AntiqueAtlas.id("village"), Text.translatable("gui.antique_atlas.marker.village"));
        StructureTileProviders.getInstance().registerMarker(StructureType.END_CITY, AntiqueAtlas.id("end_city"), Text.literal(""));
    }

    public static Map<StructurePieceType, StructureTileProvider> reload(Map<Identifier, TileTexture> textures) {
        Map<StructurePieceType, StructureTileProvider> map = new HashMap<>();
        map.put(StructurePieceType.RUINED_PORTAL, new StructureTileProvider(AntiqueAtlas.id("piece/ruined_portal"), StructureTileProvider.ChunkMatcher::aboveGround, List.of(textures.get(AntiqueAtlas.id("structure/ruined_portal/ruined_portal")))));
        map.put(StructurePieceType.SWAMP_HUT, new StructureTileProvider(AntiqueAtlas.id("piece/swamp_hut"), StructureTileProvider.ChunkMatcher::aboveGround, List.of(textures.get(AntiqueAtlas.id("structure/witch_hut/swamp/swamp_hut")))));
        map.put(StructurePieceType.IGLOO, new StructureTileProvider(AntiqueAtlas.id("piece/igloo"), StructureTileProvider.ChunkMatcher::topAboveGround, List.of(textures.get(AntiqueAtlas.id("structure/igloo/igloo")))));
        map.put(StructurePieceType.DESERT_TEMPLE, new StructureTileProvider(AntiqueAtlas.id("piece/desert_pyramid"), StructureTileProvider.ChunkMatcher::aboveGround, List.of(textures.get(AntiqueAtlas.id("structure/pyramid/desert/desert_pyramid")))));
        map.put(StructurePieceType.JUNGLE_TEMPLE, new StructureTileProvider(AntiqueAtlas.id("piece/jungle_temple"), StructureTileProvider.ChunkMatcher::aboveGround, List.of(textures.get(AntiqueAtlas.id("structure/temple/jungle/jungle_temple")))));
        map.put(StructurePieceType.SHIPWRECK, new StructureTileProvider(AntiqueAtlas.id("piece/shipwreck"), StructureTileProvider.ChunkMatcher::aboveGround, List.of(textures.get(AntiqueAtlas.id("structure/shipwreck/beached/shipwreck_beached")))));
        map.put(StructurePieceType.NETHER_FORTRESS_BRIDGE_PLATFORM, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_bridge_platform"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_platform")))));
        map.put(StructurePieceType.NETHER_FORTRESS_BRIDGE_STAIRS, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_bridge_stairs"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_stairs")))));
        map.put(StructurePieceType.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_corridor_nether_warts_room"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor_nether_warts_room")))));
        map.put(StructurePieceType.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_bridge_small_crossing"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_small_crossing")))));
        map.put(StructurePieceType.NETHER_FORTRESS_CORRIDOR_BALCONY, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_corridor"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor")))));
        map.put(StructurePieceType.NETHER_FORTRESS_CORRIDOR_LEFT_TURN, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_corridor"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor")))));
        map.put(StructurePieceType.NETHER_FORTRESS_SMALL_CORRIDOR, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_corridor"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor")))));
        map.put(StructurePieceType.NETHER_FORTRESS_CORRIDOR_RIGHT_TURN, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_corridor"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor")))));
        map.put(StructurePieceType.NETHER_FORTRESS_START, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_corridor"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor")))));
        map.put(StructurePieceType.NETHER_FORTRESS_CORRIDOR_EXIT, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_corridor_exit"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_corridor_exit")))));
        map.put(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_bridge_crossing"), StructureTileProvider.ChunkMatcher::center, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_crossing")))));
        map.put(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_bridge_end_horizontal"), Map.of(
            StructureTileProvider.ChunkMatcher::centerIfHorizontal, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_end_horizontal"))),
            StructureTileProvider.ChunkMatcher::centerIfVertical, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_end_vertical")))
        )));
        map.put(StructurePieceType.NETHER_FORTRESS_BRIDGE, new StructureTileProvider(AntiqueAtlas.id("piece/nether_fortress_bridge_horizontal"), Map.of(
            StructureTileProvider.ChunkMatcher::bridgeHorizontal, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_horizontal"))),
            StructureTileProvider.ChunkMatcher::bridgeVertical, List.of(textures.get(AntiqueAtlas.id("structure/fortress/nether/nether_fortress_bridge_vertical")))
        )));
        return map;
    }
}
