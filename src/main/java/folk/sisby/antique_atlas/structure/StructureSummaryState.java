package folk.sisby.antique_atlas.structure;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.HashMap;
import java.util.Map;

public class StructureSummaryState extends PersistentState {
    public static final String STATE_KEY = "antique_atlas_structure_summary";
    public static final String KEY_STRUCTURES = "structures";
    public static final String KEY_X = "x";
    public static final String KEY_Z = "z";
    public static final String KEY_TYPE = "type";
    public static final String KEY_STARTS = "starts";
    public static final String KEY_SUMMARY = "summary";

    private final Map<ChunkPos, Map<StructureKey, StructureSummary>> structures;

    public StructureSummaryState(Map<ChunkPos, Map<StructureKey, StructureSummary>> structures) {
        this.structures = structures;
    }

    public void addStructure(ServerWorld world, StructureStart start) {
        RegistryKey<Structure> key = world.getRegistryManager().get(RegistryKeys.STRUCTURE).getKey(start.getStructure()).orElseThrow();
        structures.computeIfAbsent(start.getPos(), p -> new HashMap<>()).computeIfAbsent(new StructureKey(key, start.getStructure().getType()), k -> {
            markDirty();
            return StructureSummary.fromStart(start);
        });
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        Map<StructureKey, Map<ChunkPos, StructureSummary>> perStructure = new HashMap<>();
        structures.forEach((pos, map) -> map.forEach((structure, summary) -> {
            perStructure.computeIfAbsent(structure, p -> new HashMap<>()).put(pos, summary);
        }));
        NbtCompound structuresCompound = new NbtCompound();
        perStructure.forEach((structure, map) -> {
            NbtCompound structureCompound = new NbtCompound();
            structureCompound.putString(KEY_TYPE, Registries.STRUCTURE_TYPE.getId(structure.type).toString());
            NbtList startList = new NbtList();
            map.forEach((pos, summary) -> {
                NbtCompound startCompound = new NbtCompound();
                startCompound.putInt(KEY_X, pos.x);
                startCompound.putInt(KEY_Z, pos.z);
                startCompound.put(KEY_SUMMARY, summary.writeNbt(new NbtCompound()));
                startList.add(startCompound);
            });
            structureCompound.put(KEY_STARTS, startList);
            structuresCompound.put(structure.key.getValue().toString(), structureCompound);
        });
        nbt.put(KEY_STRUCTURES, structuresCompound);
        return nbt;
    }

    public static StructureSummaryState readNbt(NbtCompound nbt) {
        Map<ChunkPos, Map<StructureKey, StructureSummary>> structures = new HashMap<>();
        for (String structureId : nbt.getCompound(KEY_STRUCTURES).getKeys()) {
            RegistryKey<Structure> key = RegistryKey.of(RegistryKeys.STRUCTURE, new Identifier(structureId));
            StructureType<?> type = Registries.STRUCTURE_TYPE.get(new Identifier(nbt.getCompound(structureId).getString(KEY_TYPE)));
            NbtList startList = nbt.getCompound(KEY_STRUCTURES).getList(structureId, NbtElement.COMPOUND_TYPE);
            for (NbtElement startElement : startList) {
                NbtCompound startCompound = (NbtCompound) startElement;
                ChunkPos pos = new ChunkPos(startCompound.getInt(KEY_X), startCompound.getInt(KEY_Z));
                StructureSummary summary = StructureSummary.fromNbt(startCompound.getCompound(KEY_SUMMARY));
                structures.computeIfAbsent(pos, p -> new HashMap<>()).put(new StructureKey(key, type), summary);
            }
        }
        return new StructureSummaryState(structures);
    }

    public static StructureSummaryState getOrCreate(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(StructureSummaryState::readNbt, () -> {
            StructureSummaryState state = new StructureSummaryState(new HashMap<>());
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

    public record StructureKey(RegistryKey<Structure> key, StructureType<?> type) {

    }
}
