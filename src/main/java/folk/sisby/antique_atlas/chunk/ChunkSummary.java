package folk.sisby.antique_atlas.chunk;

import folk.sisby.antique_atlas.tile.TileType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public interface ChunkSummary {
    NbtCompound writeNbt(NbtCompound nbt);

    TileType toTileType(World world);

    default int priorityForBiome(RegistryEntry<Biome> biomeEntry) {
        if (biomeEntry.isIn(BiomeTags.IS_OCEAN) || biomeEntry.isIn(BiomeTags.IS_RIVER) || biomeEntry.isIn(BiomeTags.IS_DEEP_OCEAN)) {
            return 4;
        } else if (biomeEntry.isIn(BiomeTags.IS_BEACH)) {
            return 3;
        } else {
            return 1;
        }
    }

}
