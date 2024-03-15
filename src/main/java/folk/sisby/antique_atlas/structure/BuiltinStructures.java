package folk.sisby.antique_atlas.structure;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.TileTypes;
import folk.sisby.antique_atlas.reloader.StructureTiles;
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
        StructureTiles.getInstance().registerTile(StructurePieceType.RUINED_PORTAL, TileTypes.RUINED_PORTAL, BuiltinStructures::aboveGround);
        if (AntiqueAtlas.CONFIG.markVillages) StructureTiles.getInstance().registerMarker(StructureTags.VILLAGE, AntiqueAtlas.id("village"), Text.translatable("gui.antique_atlas.marker.village"));
        StructureTiles.getInstance().registerTile(StructurePieceType.SWAMP_HUT, TileTypes.SWAMP_HUT, BuiltinStructures::aboveGround);
        StructureTiles.getInstance().registerTile(StructurePieceType.IGLOO, TileTypes.IGLOO, BuiltinStructures::topAboveGround);
        StructureTiles.getInstance().registerTile(StructurePieceType.DESERT_TEMPLE, TileTypes.DESERT_PYRAMID, BuiltinStructures::aboveGround);
        StructureTiles.getInstance().registerTile(StructurePieceType.JUNGLE_TEMPLE, TileTypes.JUNGLE_TEMPLE, BuiltinStructures::aboveGround);
        StructureTiles.getInstance().registerTile(StructurePieceType.SHIPWRECK, TileTypes.SHIPWRECK_BEACHED, BuiltinStructures::aboveGround);

        // Nether
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_PLATFORM, TileTypes.NETHER_FORTRESS_BRIDGE_PLATFORM, StructureTiles::ALWAYS);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_STAIRS, TileTypes.NETHER_FORTRESS_BRIDGE_STAIRS, StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, TileTypes.NETHER_FORTRESS_CORRIDOR_NETHER_WARTS_ROOM, StructureTiles::ALWAYS);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, TileTypes.NETHER_FORTRESS_BRIDGE_SMALL_CROSSING, StructureTiles::ALWAYS);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_BALCONY, TileTypes.NETHER_FORTRESS_CORRIDOR, StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_LEFT_TURN, TileTypes.NETHER_FORTRESS_CORRIDOR, StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_SMALL_CORRIDOR, TileTypes.NETHER_FORTRESS_CORRIDOR, StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_RIGHT_TURN, TileTypes.NETHER_FORTRESS_CORRIDOR, StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_START, TileTypes.NETHER_FORTRESS_CORRIDOR, StructureTiles::ALWAYS);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_CORRIDOR_EXIT, TileTypes.NETHER_FORTRESS_CORRIDOR_EXIT, StructureTiles::ALWAYS);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, TileTypes.NETHER_FORTRESS_BRIDGE_CROSSING, StructureTiles::ALWAYS);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, TileTypes.NETHER_FORTRESS_BRIDGE_END_HORIZONTAL, BuiltinStructures::bridgeEndX);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE_END, TileTypes.NETHER_FORTRESS_BRIDGE_END_VERTICAL, BuiltinStructures::bridgeEndZ);

        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE, TileTypes.NETHER_FORTRESS_BRIDGE_HORIZONTAL, BuiltinStructures::bridgeX);
        StructureTiles.getInstance().registerTile(StructurePieceType.NETHER_FORTRESS_BRIDGE, TileTypes.NETHER_FORTRESS_BRIDGE_VERTICAL, BuiltinStructures::bridgeZ);

        // End
        StructureTiles.getInstance().registerMarker(StructureType.END_CITY, AntiqueAtlas.id("end_city"), Text.literal(""));
    }

    private static Collection<ChunkPos> topAboveGround(World world, BlockBox box) {
        if (world.getSeaLevel() <= box.getMaxY()) {
            return Collections.singleton(new ChunkPos(box.getCenter()));
        }

        return Collections.emptyList();
    }

    private static Collection<ChunkPos> aboveGround(World world, BlockBox box) {
        BlockPos center = new BlockPos(box.getCenter());
        if (world.getSeaLevel() - 4 <= center.getY()) {
            return Collections.singleton(new ChunkPos(center));
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
