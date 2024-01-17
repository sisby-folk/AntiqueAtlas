package folk.sisby.antique_atlas.core;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.network.s2c.TileGroupsS2CPacket;
import folk.sisby.antique_atlas.util.Rect;
import folk.sisby.antique_atlas.util.Streams;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All tiles seen in dimension. Thread-safe (probably)
 */
public class WorldData implements ITileStorage {
    public final AtlasData parent;
    public final RegistryKey<World> world;

    /**
     * a map of chunks the player has seen. This map is thread-safe. CAREFUL!
     * Don't modify chunk coordinates that are already put in the map!
     * <p>
     * Key is a ChunkPos representing the tilegroup's position in units of TileGroup.CHUNK_STEP
     */
    private final Map<ChunkPos, TileGroup> tileGroups = new ConcurrentHashMap<>(2, 0.75f, 2);

    /**
     * Limits of explored area, in chunks.
     */
    private final Rect scope = new Rect();

    public WorldData(AtlasData parent, RegistryKey<World> world) {
        this.parent = parent;
        this.world = world;
    }

    public void setTile(int x, int y, Identifier tile) {
        ChunkPos groupPos = new ChunkPos((int) Math.floor(x / (float) TileGroup.CHUNK_STEP),
            (int) Math.floor(y / (float) TileGroup.CHUNK_STEP));
        TileGroup tg = tileGroups.get(groupPos);
        if (tg == null) {
            tg = new TileGroup(groupPos.x * TileGroup.CHUNK_STEP, groupPos.z * TileGroup.CHUNK_STEP);
            tileGroups.put(groupPos, tg);
        }
        tg.setTile(x, y, tile);
        scope.extendTo(x, y);
        parent.markDirty();
    }

    /**
     * Puts a tileGroup into this dimensionData, overwriting any previous stuff.
     */
    public void putTileGroup(TileGroup t) {
        ChunkPos key = new ChunkPos(Math.floorDiv(t.scope.minX, TileGroup.CHUNK_STEP), Math.floorDiv(t.scope.minY, TileGroup.CHUNK_STEP));
        tileGroups.put(key, t);
        extendToTileGroup(t);
    }

    public Identifier removeTile(int x, int y) {
        //TODO
        // since scope is not modified, I assume this was never really used
        // Tile oldTile = tileGroups.remove(getKey().set(x, y));
        // if (oldTile != null) parent.markDirty();
        // return oldTile;
        return getTile(x, y);
    }

    @Override
    public Identifier getTile(int x, int y) {
        ChunkPos groupPos = new ChunkPos((int) Math.floor(x / (float) TileGroup.CHUNK_STEP),
            (int) Math.floor(y / (float) TileGroup.CHUNK_STEP));
        TileGroup tg = tileGroups.get(groupPos);
        if (tg == null) {
            return null;
        }
        return tg.getTile(x, y);
    }

    public boolean hasTileAt(int x, int y) {
        return getTile(x, y) != null;
    }

    @Override
    public Rect getScope() {
        return scope;
    }

    public NbtList writeToNBT() {
        NbtList tileGroupList = new NbtList();
        for (Entry<ChunkPos, TileGroup> entry : tileGroups.entrySet()) {
            NbtCompound newbie = new NbtCompound();
            entry.getValue().writeToNBT(newbie);
            tileGroupList.add(newbie);
        }
        return tileGroupList;
    }

    private void extendToTileGroup(TileGroup tg) {
        for (int x = tg.scope.minX; x <= tg.scope.maxX; x++) {
            for (int y = tg.scope.minY; y <= tg.scope.maxY; y++) {
                if (tg.hasTileAt(x, y)) {
                    scope.extendTo(x, y);
                }
            }
        }
    }

    public void readFromNBT(NbtList me) {
        if (me == null) {
            return;
        }
        for (int d = 0; d < me.size(); d++) {
            NbtCompound tgTag = me.getCompound(d);
            TileGroup tg = new TileGroup(0, 0);
            tg.readFromNBT(tgTag);
            putTileGroup(tg);
        }
    }

    public void syncToPlayer(int atlasID, ServerPlayerEntity player) {
        AntiqueAtlas.LOG.info("Sending dimension #{}", this.world.toString());

        Streams.chunked(this.tileGroups.values().stream(), TileGroupsS2CPacket.TILE_GROUPS_PER_PACKET).forEach(
            chunk -> new TileGroupsS2CPacket(atlasID, this.world, chunk).send(player)
        );

        AntiqueAtlas.LOG.info("Sent dimension #{} ({} groups)", this.world.toString(), this.tileGroups.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WorldData other)) return false;
        if (other.tileGroups.size() != tileGroups.size()) return false;
        for (ChunkPos entry : tileGroups.keySet()) {
            if (!this.tileGroups.get(entry).equals(other.tileGroups.get(entry))) return false;
        }
        return true;
    }
}
