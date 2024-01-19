package folk.sisby.antique_atlas.chunk;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public interface ChunkSummaryFactory<T extends ChunkSummary> {
    T fromChunk(World world, Chunk chunk);

    T fromNbt(NbtCompound nbt);
}
