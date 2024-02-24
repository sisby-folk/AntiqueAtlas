package folk.sisby.antique_atlas.structure;

import folk.sisby.antique_atlas.tile.TileTypes;
import folk.sisby.antique_atlas.data.StructureTiles;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class NetherFortress {
    public static Collection<ChunkPos> bridgeX(World world, StructurePoolElement element, BlockBox box, StructurePiece piece) {
        HashSet<ChunkPos> matches = new HashSet<>();

        if (box.getBlockCountX() > 16) {
            int chunkZ = box.getCenter().getZ() >> 4;
            for (int x = box.getMinX(); x < box.getMaxX(); x += 16) {
                matches.add(new ChunkPos(x >> 4, chunkZ));
            }
        }

        return matches;
    }

    public static Collection<ChunkPos> bridgeZ(World world, StructurePoolElement element, BlockBox box, StructurePiece piece) {
        HashSet<ChunkPos> matches = new HashSet<>();

        if (box.getBlockCountZ() > 16) {
            int chunkX = box.getCenter().getX() >> 4;
            for (int z = box.getMinZ(); z < box.getMaxZ(); z += 16) {
                matches.add(new ChunkPos(chunkX, z >> 4));
            }
        }

        return matches;
    }

    public static Collection<ChunkPos> bridgeEndX(World world, StructurePoolElement element, BlockBox box, StructurePiece piece) {
        if (box.getBlockCountX() > box.getBlockCountZ()) {
            return Collections.singleton(new ChunkPos(box.getCenter().getX() >> 4, box.getCenter().getZ() >> 4));
        } else {
            return Collections.emptySet();
        }
    }

    public static Collection<ChunkPos> bridgeEndZ(World world, StructurePoolElement element, BlockBox box, StructurePiece piece) {
        if (box.getBlockCountZ() > box.getBlockCountX()) {
            return Collections.singleton(new ChunkPos(box.getCenter().getX() >> 4, box.getCenter().getZ() >> 4));
        } else {
            return Collections.emptySet();
        }
    }


    public static void registerPieces() {
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_PLATFORM, 40, TileTypes.NETHER_FORTRESS_BRIDGE_PLATFORM.getId());

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_STAIRS, 50, TileTypes.NETHER_FORTRESS_BRIDGE_STAIRS.getId());
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, 50, TileTypes.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM.getId());

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, 60, TileTypes.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING.getId());

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_BALCONY, 70, TileTypes.NETHER_FORTRESS_WALL.getId());
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_LEFT_TURN, 70, TileTypes.NETHER_FORTRESS_WALL.getId());
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_SMALL_CORRIDOR, 70, TileTypes.NETHER_FORTRESS_WALL.getId());
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_RIGHT_TURN, 70, TileTypes.NETHER_FORTRESS_WALL.getId());
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_START, 70, TileTypes.NETHER_FORTRESS_WALL.getId());
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_EXIT, 70, TileTypes.NETHER_FORTRESS_EXIT.getId());

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 80, TileTypes.NETHER_FORTRESS_BRIDGE_CROSSING.getId());

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, 90, TileTypes.NETHER_BRIDGE_END_X.getId(), NetherFortress::bridgeEndX);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, 90, TileTypes.NETHER_BRIDGE_END_Z.getId(), NetherFortress::bridgeEndZ);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE, 100, TileTypes.NETHER_BRIDGE_X.getId(), NetherFortress::bridgeX);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE, 100, TileTypes.NETHER_BRIDGE_Z.getId(), NetherFortress::bridgeZ);
    }
}
