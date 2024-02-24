package folk.sisby.antique_atlas.tile;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TileType {
    public final Identifier biome;
    public final TileElevation height;
    public final Identifier compositeId;

    public TileType(Identifier biome, @Nullable TileElevation height) {
        this.biome = biome;
        this.height = height;
        this.compositeId = height != null ? new Identifier(biome.getNamespace(), "%s_%s".formatted(biome.getPath(), height)) : biome;
    }

    public static TileType of(Identifier biome) {
        return new TileType(biome, null);
    }

    public static TileType of(RegistryKey<Biome> biome) {
        return new TileType(biome.getValue(), null);
    }

    public Identifier getId() {
        return compositeId;
    }

    @Override
    public int hashCode() {
        return compositeId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileType tileType = (TileType) o;
        return Objects.equals(compositeId, tileType.compositeId);
    }
}
