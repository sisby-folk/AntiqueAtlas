package folk.sisby.antique_atlas.tile;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public record TileType(Identifier id) {
    public TileType(Identifier baseId, TileElevation elevation) {
        this(new Identifier(baseId.getNamespace(), baseId.getPath() + "_" + elevation));
    }

    public TileType(Registry<Biome> biomeRegistry, Biome biome) {
        this(biomeRegistry.getId(biome));
    }

    @SuppressWarnings("DataFlowIssue")
    public TileType(Registry<Biome> biomeRegistry, Biome biome, TileElevation elevation) {
        this(biomeRegistry.getId(biome), elevation);
    }
}
