package folk.sisby.antique_atlas.chunk;

import folk.sisby.antique_atlas.tile.TileType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.Map;

public class ChunkSummaryState<T extends ChunkSummary> extends PersistentState {
    public static final String STATE_KEY = "antique_atlas_chunk_summary";
    public static final String KEY_CHUNKS = "chunks";
    public static final String KEY_SUMMARY = "summary";
    public static final String KEY_X = "x";
    public static final String KEY_Z = "z";

    public static final Map<RegistryKey<World>, ChunkSummaryFactory<?>> FACTORIES = Map.of(
        World.OVERWORLD, OverworldChunkSummary.FACTORY,
        World.NETHER, NetherChunkSummary.FACTORY,
        World.END, EndChunkSummary.FACTORY
    );

    private final Map<ChunkPos, T> chunks;
    private final ChunkSummaryFactory<T> factory;

    private final Map<ChunkPos, TileType> tileCache = new HashMap<>();

    public ChunkSummaryState(Map<ChunkPos, T> chunks, ChunkSummaryFactory<T> factory) {
        this.chunks = chunks;
        this.factory = factory;
    }

    public boolean contains(Chunk chunk) {
        return chunks.containsKey(chunk.getPos());
    }

    public void putChunk(World world, Chunk chunk) {
        chunks.put(chunk.getPos(), factory.fromChunk(world, chunk));
        markDirty();
    }

    public TileType getTileType(World world, ChunkPos pos) {
        if (tileCache.containsKey(pos)) return tileCache.get(pos);
        if (chunks.containsKey(pos)) {
            TileType type = chunks.get(pos).toTileType(world);
            tileCache.put(pos, type);
            return type;
        }
        return null;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList chunkList = new NbtList();
        chunks.forEach((pos, summary) -> {
            NbtCompound chunkCompound = new NbtCompound();
            chunkCompound.putInt(KEY_X, pos.x);
            chunkCompound.putInt(KEY_Z, pos.z);
            chunkCompound.put(KEY_SUMMARY, summary.writeNbt(new NbtCompound()));
            chunkList.add(chunkCompound);
        });
        nbt.put(KEY_CHUNKS, chunkList);
        return nbt;
    }

    public static <T extends ChunkSummary> ChunkSummaryState<T> readNbt(NbtCompound nbt, ChunkSummaryFactory<T> factory) {
        Map<ChunkPos, T> chunks = new HashMap<>();
        for (NbtElement chunkElement : nbt.getList(KEY_CHUNKS, NbtElement.COMPOUND_TYPE)) {
            NbtCompound chunkCompound = ((NbtCompound) chunkElement);
            chunks.put(
                new ChunkPos(chunkCompound.getInt(KEY_X), chunkCompound.getInt(KEY_Z)),
                factory.fromNbt(chunkCompound.getCompound(KEY_SUMMARY))
            );
        }
        return new ChunkSummaryState<>(chunks, factory);
    }

    public static ChunkSummaryState<?> getOrCreate(ServerWorld world) {
        RegistryKey<World> worldKey = FACTORIES.containsKey(world.getRegistryKey()) ? world.getRegistryKey() : World.OVERWORLD;
        return world.getPersistentStateManager().getOrCreate(nbt -> ChunkSummaryState.readNbt(nbt, FACTORIES.get(worldKey)), () -> {
            ChunkSummaryState<?> state = new ChunkSummaryState<>(new HashMap<>(), FACTORIES.get(worldKey));
            state.markDirty();
            return state;
        }, STATE_KEY);
    }

    public static void onChunkLoad(ServerWorld world, Chunk chunk) {
        ChunkSummaryState<?> state = ChunkSummaryState.getOrCreate(world);
        if (!state.contains(chunk)) state.putChunk(world, chunk);
    }

    public static void onChunkUnload(ServerWorld world, WorldChunk chunk) {
        ChunkSummaryState<?> state = ChunkSummaryState.getOrCreate(world);
        if (chunk.needsSaving()) state.putChunk(world, chunk);
    }
}
