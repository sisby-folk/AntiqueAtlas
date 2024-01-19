package folk.sisby.antique_atlas.structure;

import folk.sisby.antique_atlas.tile.TileTypes;
import folk.sisby.antique_atlas.data.StructureTiles;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;

public class Overworld {

    public static void registerPieces() {
        StructureTiles.getInstance().registerTile(StructurePieceType.RUINED_PORTAL, 10, TileTypes.RUINED_PORTAL.getId(), Overworld::aboveGround);
    }

    private static Collection<ChunkPos> aboveGround(World world, @SuppressWarnings("unused") StructurePoolElement structurePoolElement, BlockBox blockBox, StructurePiece piece) {
        BlockPos center = new BlockPos(blockBox.getCenter());
        if (world.getSeaLevel() - 4 <= center.getY()) {
            return Collections.singleton(new ChunkPos(center));
        }

        return Collections.emptyList();
    }
}
