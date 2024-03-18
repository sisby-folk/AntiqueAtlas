package folk.sisby.antique_atlas;

import net.minecraft.structure.JigsawJunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record StructureTileProvider(Identifier id, Map<ChunkMatcher, List<TileTexture>> matchers) {
    public StructureTileProvider(Identifier id, List<TileTexture> textures) {
        this(id, Map.of(ChunkMatcher::center, textures));
    }

    public StructureTileProvider(Identifier id, ChunkMatcher matcher, List<TileTexture> textures) {
        this(id, Map.of(matcher, textures));
    }

    public Map<ChunkPos, TileTexture> getTextures(World world, BlockBox box) {
        Map<ChunkPos, TileTexture> outMap = new HashMap<>();
        matchers.forEach((matcher, textures) -> {
            for (ChunkPos pos : matcher.matches(world, box)) {
                if (!outMap.containsKey(pos)) {
                    int variation = (int) (MathHelper.hashCode(pos.x, pos.z, pos.x * pos.z) & 0x7FFFFFFF);
                    outMap.put(pos, textures.get(variation % textures.size()));
                }
            }
        });
        return outMap;
    }

    public Map<ChunkPos, TileTexture> getTextures(World world, BlockBox box, List<JigsawJunction> junctions) {
        Map<ChunkPos, TileTexture> outMap = new HashMap<>();
        matchers.forEach((matcher, textures) -> {
            for (ChunkPos pos : matcher.matches(world, box, junctions)) {
                if (!outMap.containsKey(pos)) {
                    int variation = (int) (MathHelper.hashCode(pos.x, pos.z, pos.x * pos.z) & 0x7FFFFFFF);
                    outMap.put(pos, textures.get(variation % textures.size()));
                }
            }
        });
        return outMap;
    }

    public Set<TileTexture> allTextures() {
        Set<TileTexture> outSet = new HashSet<>();
        matchers.values().forEach(outSet::addAll);
        return outSet;
    }

    public interface ChunkMatcher extends ChunkJunctionMatcher {
        Collection<ChunkPos> matches(World world, BlockBox box);

        @Override
        default Collection<ChunkPos> matches(World world, BlockBox box, List<JigsawJunction> junctions) {
            return matches(world, box);
        }

        static Collection<ChunkPos> center(World world, BlockBox box) {
            return Collections.singleton(new ChunkPos(box.getCenter()));
        }

        static Collection<ChunkPos> topAboveGround(World world, BlockBox box) {
            if (world.getSeaLevel() <= box.getMaxY()) {
                return Collections.singleton(new ChunkPos(box.getCenter()));
            }

            return Collections.emptyList();
        }

        static Collection<ChunkPos> aboveGround(World world, BlockBox box) {
            BlockPos center = new BlockPos(box.getCenter());
            if (world.getSeaLevel() - 4 <= center.getY()) {
                return Collections.singleton(new ChunkPos(center));
            }

            return Collections.emptyList();
        }

        static Collection<ChunkPos> bridgeX(World world, BlockBox box) {
            HashSet<ChunkPos> matches = new HashSet<>();

            if (box.getBlockCountX() > 16) {
                int chunkZ = box.getCenter().getZ() >> 4;
                for (int x = box.getMinX(); x < box.getMaxX(); x += 16) {
                    matches.add(new ChunkPos(x >> 4, chunkZ));
                }
            }

            return matches;
        }

        static Collection<ChunkPos> bridgeZ(World world, BlockBox box) {
            HashSet<ChunkPos> matches = new HashSet<>();

            if (box.getBlockCountZ() > 16) {
                int chunkX = box.getCenter().getX() >> 4;
                for (int z = box.getMinZ(); z < box.getMaxZ(); z += 16) {
                    matches.add(new ChunkPos(chunkX, z >> 4));
                }
            }

            return matches;
        }

        static Collection<ChunkPos> bridgeEndX(World world, BlockBox box) {
            if (box.getBlockCountX() > box.getBlockCountZ()) {
                return Collections.singleton(new ChunkPos(box.getCenter().getX() >> 4, box.getCenter().getZ() >> 4));
            } else {
                return Collections.emptySet();
            }
        }

        static Collection<ChunkPos> bridgeEndZ(World world, BlockBox box) {
            if (box.getBlockCountZ() > box.getBlockCountX()) {
                return Collections.singleton(new ChunkPos(box.getCenter().getX() >> 4, box.getCenter().getZ() >> 4));
            } else {
                return Collections.emptySet();
            }
        }
    }

    public interface ChunkJunctionMatcher {
        Collection<ChunkPos> matches(World world, BlockBox box, List<JigsawJunction> junctions);

        static Collection<ChunkPos> junction(World world, BlockBox box, List<JigsawJunction> junctions) {
            if (junctions.size() > 2) {
                return List.of(new ChunkPos(box.getCenter()));
            }
            return List.of();
        }

        static Collection<ChunkPos> horizontal(World world, BlockBox box, List<JigsawJunction> junctions) {
            if (junctions.size() == 2 && (junctions.get(0).getSourceZ() == junctions.get(1).getSourceZ() || junctions.get(0).getSourceX() != junctions.get(1).getSourceX())) {
                return List.of(new ChunkPos(box.getCenter()));
            }
            return List.of();
        }

        static Collection<ChunkPos> vertical(World world, BlockBox box, List<JigsawJunction> junctions) {
            if (junctions.size() == 2 && (junctions.get(0).getSourceX() == junctions.get(1).getSourceX() || junctions.get(0).getSourceZ() != junctions.get(1).getSourceZ())) {
                return List.of(new ChunkPos(box.getCenter()));
            }
            return List.of();
        }

    }
}

