package folk.sisby.antique_atlas.structure;

import com.mojang.serialization.Dynamic;
import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.StructureType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record StructureSummary(ChunkPos pos, StructureType<?> type, Collection<StructurePieceSummary> pieces, Collection<JigsawSummary> jigsaws) {
    public static final String KEY_X = "x";
    public static final String KEY_Z = "z";
    public static final String KEY_ID = "id";
    public static final String KEY_BOX = "BB";
    public static final String KEY_PIECES = "pieces";
    public static final String KEY_JIGSAWS = "jigsaws";

    public static StructureSummary fromStart(StructureStart start) {
        return new StructureSummary(
            start.getPos(),
            start.getStructure().getType(),
            start.getChildren().stream().map(StructurePieceSummary::fromPiece).toList(),
            start.getChildren().stream().map(JigsawSummary::fromPiece).filter(Optional::isPresent).map(Optional::get).toList()
        );
    }

    public static StructureSummary fromNbt(NbtCompound nbt) {
        ChunkPos pos = new ChunkPos(nbt.getInt(KEY_X), nbt.getInt(KEY_Z));
        StructureType<?> type = nbt.contains(KEY_ID) ? Registries.STRUCTURE_TYPE.get(new Identifier(nbt.getString(KEY_ID))) : null;
        Collection<StructurePieceSummary> pieces = new ArrayList<>();
        for (NbtElement pieceElement : nbt.getList(KEY_PIECES, NbtElement.STRING_TYPE)) {
            pieces.add(StructurePieceSummary.fromNbt((NbtCompound) pieceElement));
        }
        Collection<JigsawSummary> jigsaws = new ArrayList<>();
        for (NbtElement jigsawElement : nbt.getList(KEY_JIGSAWS, NbtElement.STRING_TYPE)) {
            jigsaws.add(JigsawSummary.fromNbt((NbtCompound) jigsawElement));
        }
        return new StructureSummary(pos, type, pieces, jigsaws);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt(KEY_X, pos.x);
        nbt.putInt(KEY_Z, pos.z);
        if (type != null) nbt.putString(KEY_ID, Registries.STRUCTURE_TYPE.getId(type).toString());
        NbtList pieceList = new NbtList();
        for (StructurePieceSummary piece : pieces) {
            pieceList.add(piece.writeNbt(new NbtCompound()));
        }
        nbt.put(KEY_PIECES, pieceList);
        NbtList jigsawList = new NbtList();
        for (JigsawSummary jigsaw : jigsaws) {
            jigsawList.add(jigsaw.writeNbt(new NbtCompound()));
        }
        nbt.put(KEY_JIGSAWS, jigsawList);
        return nbt;
    }

    public record StructurePieceSummary(StructurePieceType type, BlockBox boundingBox) {
        public static StructurePieceSummary fromPiece(StructurePiece piece) {
            return new StructurePieceSummary(piece.getType(), piece.getBoundingBox());
        }

        public static StructurePieceSummary fromNbt(NbtCompound nbt) {
            return new StructurePieceSummary(
                Registries.STRUCTURE_PIECE.get(new Identifier(nbt.getString(KEY_ID))),
                BlockBox.CODEC
                    .parse(NbtOps.INSTANCE, nbt.get("BB"))
                    .resultOrPartial(AntiqueAtlas.LOG::error)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid boundingbox"))
            );
        }

        public NbtCompound writeNbt(NbtCompound nbt) {
            nbt.putString(KEY_ID, Registries.STRUCTURE_PIECE.getId(type).toString());
            BlockBox.CODEC.encodeStart(NbtOps.INSTANCE, this.boundingBox).resultOrPartial(AntiqueAtlas.LOG::error).ifPresent(element -> nbt.put(KEY_BOX, element));
            return nbt;
        }
    }

    public record JigsawSummary(Identifier id, BlockBox boundingBox, List<JigsawJunction> junctions) {
        public static final String KEY_JUNCTIONS = "junctions";

        public static Optional<JigsawSummary> fromPiece(StructurePiece piece) {
            if (piece instanceof PoolStructurePiece poolPiece && poolPiece.getPoolElement() instanceof SinglePoolElement poolElement && poolElement.location.left().isPresent()) {
                return Optional.of(new JigsawSummary(poolElement.location.left().orElseThrow(), piece.getBoundingBox(), poolPiece.getJunctions()));
            }
            return Optional.empty();
        }

        public static JigsawSummary fromNbt(NbtCompound nbt) {
            List<JigsawJunction> junctions = new ArrayList<>();
            nbt.getList(KEY_JUNCTIONS, NbtElement.COMPOUND_TYPE).forEach(junctionElement -> junctions.add(JigsawJunction.deserialize(new Dynamic<>(NbtOps.INSTANCE, junctionElement))));

            return new JigsawSummary(
                new Identifier(nbt.getString(KEY_ID)),
                BlockBox.CODEC
                    .parse(NbtOps.INSTANCE, nbt.get("BB"))
                    .resultOrPartial(AntiqueAtlas.LOG::error)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid boundingbox")),
                junctions
            );
        }

        public NbtCompound writeNbt(NbtCompound nbt) {
            nbt.putString(KEY_ID, id.toString());
            BlockBox.CODEC.encodeStart(NbtOps.INSTANCE, this.boundingBox).resultOrPartial(AntiqueAtlas.LOG::error).ifPresent(element -> nbt.put(KEY_BOX, element));
            NbtList junctionList = new NbtList();
            for (JigsawJunction junction : junctions) {
                junctionList.add(junction.serialize(NbtOps.INSTANCE).getValue());
            }
            nbt.put(KEY_JUNCTIONS, junctionList);
            return nbt;
        }
    }
}
