package folk.sisby.antique_atlas;

import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record TileProvider(Map<TileElevation, List<TileTexture>> textures) {
    public static final TileProvider DEFAULT = new TileProvider(List.of(TileTexture.DEFAULT));

    public TileProvider(List<TileTexture> textures) {
        this(Arrays.stream(TileElevation.values()).collect(Collectors.toMap(e -> e, e -> textures)));
    }

    public TileTexture getTexture(ChunkPos pos, @Nullable TileElevation elevation) {
        int variation = (Objects.hash(pos.x, pos.z, pos.x * pos.z) & 0x7FFFFFFF);
        TileElevation usedElevation = elevation == null ? TileElevation.VALLEY : elevation;
        return textures.get(usedElevation).get(variation % textures.get(usedElevation).size());
    }
}
