package folk.sisby.antique_atlas.structure;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.reloader.StructureTiles;
import folk.sisby.antique_atlas.tile.TileTypes;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class BuiltinStructures {
    public static void init () {
        // Overworld
        StructureTiles.getInstance().registerTile(StructurePieceType.RUINED_PORTAL, 10, TileTypes.RUINED_PORTAL.getId(), BuiltinStructures::aboveGround);
        StructureTiles.getInstance().registerMarker(StructureTags.VILLAGE, AntiqueAtlas.id("village"), Text.translatable("gui.antique_atlas.marker.village"));
        StructureTiles.getInstance().registerTile(StructurePieceType.SWAMP_HUT, 10, TileTypes.SWAMP_HUT.getId(), BuiltinStructures::aboveGround);
        StructureTiles.getInstance().registerTile(StructurePieceType.IGLOO, 10, TileTypes.IGLOO.getId(), BuiltinStructures::aboveGround);
        StructureTiles.getInstance().registerTile(StructurePieceType.DESERT_TEMPLE, 10, TileTypes.DESERT_TEMPLE.getId(), BuiltinStructures::aboveGround);
        StructureTiles.getInstance().registerTile(StructurePieceType.JUNGLE_TEMPLE, 10, TileTypes.JUNGLE_TEMPLE.getId(), BuiltinStructures::aboveGround);
        StructureTiles.getInstance().registerTile(StructurePieceType.SHIPWRECK, 10, TileTypes.SHIPWRECK_BEACHED.getId(), BuiltinStructures::aboveGround);

        // Nether
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_PLATFORM, 40, TileTypes.NETHER_FORTRESS_BRIDGE_PLATFORM.getId(), StructureTiles::ALWAYS);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_STAIRS, 50, TileTypes.NETHER_FORTRESS_BRIDGE_STAIRS.getId(), StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, 50, TileTypes.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM.getId(), StructureTiles::ALWAYS);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, 60, TileTypes.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING.getId(), StructureTiles::ALWAYS);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_BALCONY, 70, TileTypes.NETHER_FORTRESS_WALL.getId(), StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_LEFT_TURN, 70, TileTypes.NETHER_FORTRESS_WALL.getId(), StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_SMALL_CORRIDOR, 70, TileTypes.NETHER_FORTRESS_WALL.getId(), StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_RIGHT_TURN, 70, TileTypes.NETHER_FORTRESS_WALL.getId(), StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_START, 70, TileTypes.NETHER_FORTRESS_WALL.getId(), StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_EXIT, 70, TileTypes.NETHER_FORTRESS_EXIT.getId(), StructureTiles::ALWAYS);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 80, TileTypes.NETHER_FORTRESS_BRIDGE_CROSSING.getId(), StructureTiles::ALWAYS);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, 90, TileTypes.NETHER_BRIDGE_END_X.getId(), BuiltinStructures::bridgeEndX);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, 90, TileTypes.NETHER_BRIDGE_END_Z.getId(), BuiltinStructures::bridgeEndZ);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE, 100, TileTypes.NETHER_BRIDGE_X.getId(), BuiltinStructures::bridgeX);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE, 100, TileTypes.NETHER_BRIDGE_Z.getId(), BuiltinStructures::bridgeZ);

        // End
        StructureTiles.getInstance().registerMarker(StructureType.END_CITY, AntiqueAtlas.id("end_city"), Text.literal(""));
    }

    private static Collection<ChunkPos> aboveGround(World world, BlockBox box) {
        if (world.getSeaLevel() <= box.getMaxY()) {
            return Collections.singleton(new ChunkPos(box.getCenter()));
        }

        return Collections.emptyList();
    }

    public static Collection<ChunkPos> bridgeX(World world, BlockBox box) {
        HashSet<ChunkPos> matches = new HashSet<>();

        if (box.getBlockCountX() > 16) {
            int chunkZ = box.getCenter().getZ() >> 4;
            for (int x = box.getMinX(); x < box.getMaxX(); x += 16) {
                matches.add(new ChunkPos(x >> 4, chunkZ));
            }
        }

        return matches;
    }

    public static Collection<ChunkPos> bridgeZ(World world, BlockBox box) {
        HashSet<ChunkPos> matches = new HashSet<>();

        if (box.getBlockCountZ() > 16) {
            int chunkX = box.getCenter().getX() >> 4;
            for (int z = box.getMinZ(); z < box.getMaxZ(); z += 16) {
                matches.add(new ChunkPos(chunkX, z >> 4));
            }
        }

        return matches;
    }

    public static Collection<ChunkPos> bridgeEndX(World world, BlockBox box) {
        if (box.getBlockCountX() > box.getBlockCountZ()) {
            return Collections.singleton(new ChunkPos(box.getCenter().getX() >> 4, box.getCenter().getZ() >> 4));
        } else {
            return Collections.emptySet();
        }
    }

    public static Collection<ChunkPos> bridgeEndZ(World world, BlockBox box) {
        if (box.getBlockCountZ() > box.getBlockCountX()) {
            return Collections.singleton(new ChunkPos(box.getCenter().getX() >> 4, box.getCenter().getZ() >> 4));
        } else {
            return Collections.emptySet();
        }
    }
}
