package folk.sisby.antique_atlas.structure;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
import net.minecraft.structure.pool.FeaturePoolElement;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record StructureSummary(Collection<StructurePieceSummary> pieces) {
    public static final String KEY_BOX = "BB";
    public static final String KEY_PIECES = "pieces";

    public static StructureSummary fromStart(StructureStart start) {
        List<StructurePieceSummary> pieces = new ArrayList<>();
        for (StructurePiece piece : start.getChildren()) {
            pieces.add(JigsawSummary.tryFromPiece(piece).orElseGet(() -> StructurePieceSummary.fromPiece(piece)));
        }
        return new StructureSummary(pieces);
    }

    public static StructureSummary fromNbt(NbtCompound nbt) {
        Collection<StructurePieceSummary> pieces = new ArrayList<>();
        for (NbtElement pieceElement : nbt.getList(KEY_PIECES, NbtElement.STRING_TYPE)) {
            if (((NbtCompound) pieceElement).getString(StructurePieceSummary.KEY_TYPE).equals(Registries.STRUCTURE_PIECE.getId(StructurePieceType.JIGSAW).toString())) {
                pieces.add(JigsawSummary.fromNbt((NbtCompound) pieceElement));
            } else {
                pieces.add(StructurePieceSummary.fromNbt((NbtCompound) pieceElement));
            }
        }
        return new StructureSummary(pieces);
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList pieceList = new NbtList();
        for (StructurePieceSummary piece : pieces) {
            pieceList.add(piece.writeNbt(new NbtCompound()));
        }
        nbt.put(KEY_PIECES, pieceList);
        return nbt;
    }

    public static class StructurePieceSummary {
        public static final String KEY_TYPE = "type";
        public final StructurePieceType type;
        public final BlockBox boundingBox;

        public StructurePieceSummary(StructurePieceType type, BlockBox boundingBox) {
            this.type = type;
            this.boundingBox = boundingBox;
        }

        public static StructurePieceSummary fromPiece(StructurePiece piece) {
            return new StructurePieceSummary(piece.getType(), piece.getBoundingBox());
        }

        public static StructurePieceSummary fromNbt(NbtCompound nbt) {
            return new StructurePieceSummary(
                Registries.STRUCTURE_PIECE.get(new Identifier(nbt.getString(KEY_TYPE))),
                BlockBox.CODEC
                    .parse(NbtOps.INSTANCE, nbt.get("BB"))
                    .resultOrPartial(AntiqueAtlas.LOG::error)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid boundingbox"))
            );
        }

        public NbtCompound writeNbt(NbtCompound nbt) {
            nbt.putString(KEY_TYPE, Registries.STRUCTURE_PIECE.getId(type).toString());
            BlockBox.CODEC.encodeStart(NbtOps.INSTANCE, this.boundingBox).resultOrPartial(AntiqueAtlas.LOG::error).ifPresent(element -> nbt.put(KEY_BOX, element));
            return nbt;
        }
    }

    public static class JigsawSummary extends StructurePieceSummary {
        public static final BiMap<String, StructurePoolElementType<?>> TYPE_KEYS = HashBiMap.create(Map.of(
            "single", StructurePoolElementType.SINGLE_POOL_ELEMENT,
            "feature", StructurePoolElementType.FEATURE_POOL_ELEMENT
        ));
        public static final String KEY_JUNCTIONS = "junctions";
        public final StructurePoolElementType<?> jigsawType;
        public final Identifier id;
        public final BlockBox boundingBox;
        public final List<JigsawJunction> junctions;

        public JigsawSummary(StructurePoolElementType<?> jigsawType, Identifier id, BlockBox boundingBox, List<JigsawJunction> junctions) {
            super(StructurePieceType.JIGSAW, boundingBox);
            this.jigsawType = jigsawType;
            this.id = id;
            this.boundingBox = boundingBox;
            this.junctions = junctions;
        }

        public static Optional<StructurePieceSummary> tryFromPiece(StructurePiece piece) {
            if (piece instanceof PoolStructurePiece poolPiece) {
                if (poolPiece.getPoolElement() instanceof SinglePoolElement poolElement && poolElement.location.left().isPresent()) {
                    return Optional.of(new JigsawSummary(StructurePoolElementType.SINGLE_POOL_ELEMENT, poolElement.location.left().orElseThrow(), piece.getBoundingBox(), poolPiece.getJunctions()));
                } else if (poolPiece.getPoolElement() instanceof FeaturePoolElement poolElement && poolElement.feature.getKey().isPresent()) {
                    return Optional.of(new JigsawSummary(StructurePoolElementType.FEATURE_POOL_ELEMENT, poolElement.feature.getKey().orElseThrow().getValue(), piece.getBoundingBox(), poolPiece.getJunctions()));
                }
            }
            return Optional.empty();
        }

        public static JigsawSummary fromNbt(NbtCompound nbt) {
            StructurePieceSummary baseSummary = StructurePieceSummary.fromNbt(nbt);
            List<JigsawJunction> junctions = new ArrayList<>();
            nbt.getList(KEY_JUNCTIONS, NbtElement.COMPOUND_TYPE).forEach(junctionElement -> junctions.add(JigsawJunction.deserialize(new Dynamic<>(NbtOps.INSTANCE, junctionElement))));

            String idKey = TYPE_KEYS.keySet().stream().filter(nbt::contains).findFirst().orElseThrow();

            return new JigsawSummary(
                TYPE_KEYS.get(idKey),
                new Identifier(nbt.getString(idKey)),
                baseSummary.boundingBox,
                junctions
            );
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            super.writeNbt(nbt);
            String idKey = TYPE_KEYS.inverse().get(jigsawType);
            nbt.putString(idKey, id.toString());
            NbtList junctionList = new NbtList();
            for (JigsawJunction junction : junctions) {
                junctionList.add(junction.serialize(NbtOps.INSTANCE).getValue());
            }
            if (!junctionList.isEmpty()) nbt.put(KEY_JUNCTIONS, junctionList);
            return nbt;
        }
    }
}
