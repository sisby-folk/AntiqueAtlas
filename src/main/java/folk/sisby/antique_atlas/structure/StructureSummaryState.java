package folk.sisby.antique_atlas.structure;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;
import java.util.Set;

public class StructureSummaryState extends PersistentState {
    public static final String STATE_KEY = "antique_atlas_structure_summary";
    public static final String KEY_STRUCTURES = "structures";

    private final Set<StructureSummary> structures;

    public StructureSummaryState(Set<StructureSummary> structures) {
        this.structures = structures;
    }

    public void addStructure(World world, StructureStart start) {
        structures.add(StructureSummary.fromStart(start));
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList structureList = new NbtList();
        structures.forEach(summary -> structureList.add(summary.writeNbt(new NbtCompound())));
        nbt.put(KEY_STRUCTURES, structureList);
        return nbt;
    }

    public static StructureSummaryState readNbt(NbtCompound nbt) {
        Set<StructureSummary> structures = new HashSet<>();
        for (NbtElement structureElement : nbt.getList(KEY_STRUCTURES, NbtElement.COMPOUND_TYPE)) {
            NbtCompound structureCompound = ((NbtCompound) structureElement);
            structures.add(
                StructureSummary.fromNbt(structureCompound)
            );
        }
        return new StructureSummaryState(structures);
    }

    public static StructureSummaryState getOrCreate(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(StructureSummaryState::readNbt, () -> {
            StructureSummaryState state = new StructureSummaryState(new HashSet<>());
            state.markDirty();
            return state;
        }, STATE_KEY);
    }

    public static void onStructurePlace(ServerWorld world, StructureStart start) {
        StructureSummaryState state = StructureSummaryState.getOrCreate(world);
        state.addStructure(world, start);
    }

    public static void onChunkLoad(ServerWorld world, Chunk chunk) {
        StructureSummaryState state = StructureSummaryState.getOrCreate(world);
        chunk.getStructureStarts().forEach((structure, start) -> state.addStructure(world, start));
    }
}
