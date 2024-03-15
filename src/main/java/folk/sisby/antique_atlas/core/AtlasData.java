package folk.sisby.antique_atlas.core;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.network.s2c.MapDataS2CPacket;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class stores all the data
 */
public class AtlasData extends PersistentState {
    public static final int VERSION = 4;
    public static final String TAG_VERSION = "aaVersion";
    public static final String TAG_WORLD_MAP_LIST = "qWorldMap";
    public static final String TAG_WORLD_ID = "qWorldID";
    public static final String TAG_VISITED_CHUNKS = "qVisitedChunks";

    /**
     * This map contains, for each dimension, a map of chunks the player
     * has seen. This map is thread-safe.
     * CAREFUL! Don't modify chunk coordinates that are already put in the map!
     */
    private final Map<RegistryKey<World>, WorldData> worldMap =
        new ConcurrentHashMap<>(2, 0.75f, 2);

    /**
     * Set of players this Atlas data has been sent to.
     */
    private final Set<ServerPlayerEntity> playersSentTo = new HashSet<>();

    public AtlasData() {
    }

    public static AtlasData fromNbt(NbtCompound compound) {
        AtlasData data = new AtlasData();
        data.updateFromNbt(compound);
        return data;
    }

    public void updateFromNbt(NbtCompound compound) {
        int version = compound.getInt(TAG_VERSION);
        if (version < VERSION) {
            AntiqueAtlas.LOGGER.warn("Outdated atlas data format! Was {} but current is {}.", version, VERSION);
            return;
        }

        NbtList worldMapList = compound.getList(TAG_WORLD_MAP_LIST, NbtElement.COMPOUND_TYPE);
        for (int d = 0; d < worldMapList.size(); d++) {
            NbtCompound worldTag = worldMapList.getCompound(d);
            RegistryKey<World> worldID;
            worldID = RegistryKey.of(Registry.WORLD_KEY, new Identifier(worldTag.getString(TAG_WORLD_ID)));
            NbtList dimensionTag = (NbtList) worldTag.get(TAG_VISITED_CHUNKS);
            WorldData dimData = this.getWorldData(worldID);
            dimData.readFromNBT(dimensionTag);
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        return writeToNBT(compound, true);
    }

    public NbtCompound writeToNBT(NbtCompound compound, boolean includeTileData) {
        NbtList dimensionMapList = new NbtList();
        compound.putInt(TAG_VERSION, VERSION);
        for (Entry<RegistryKey<World>, WorldData> dimensionEntry : worldMap.entrySet()) {
            NbtCompound dimTag = new NbtCompound();
            dimTag.putString(TAG_WORLD_ID, dimensionEntry.getKey().getValue().toString());
            WorldData dimData = dimensionEntry.getValue();
            if (includeTileData) {
                dimTag.put(TAG_VISITED_CHUNKS, dimData.writeToNBT());
            }
            dimensionMapList.add(dimTag);
        }
        compound.put(TAG_WORLD_MAP_LIST, dimensionMapList);

        return compound;
    }

    /**
     * Puts a given tile into given map at specified coordinates and,
     * if tileStitcher is present, sets appropriate sectors on adjacent tiles.
     */
    public void setTile(RegistryKey<World> world, int x, int y, Identifier tile) {
        WorldData worldData = getWorldData(world);
        worldData.setTile(x, y, tile);
    }

    /**
     * Returns the Tile previously set at given coordinates.
     */
    public Identifier removeTile(RegistryKey<World> world, int x, int y) {
        WorldData dimData = getWorldData(world);
        return dimData.removeTile(x, y);
    }

    /**
     * If this dimension is not yet visited, empty DimensionData will be created.
     */
    public WorldData getWorldData(RegistryKey<World> world) {
        return worldMap.computeIfAbsent(world, k -> new WorldData(this, world));
    }

    /**
     * The set of players this AtlasData has already been sent to.
     */
    public Collection<ServerPlayerEntity> getSyncedPlayers() {
        return Collections.unmodifiableCollection(playersSentTo);
    }

    /**
     * Send all data to the player in several zipped packets. Called once on login.
     */
    public void syncToPlayer(int atlasID, ServerPlayerEntity player) {
        NbtCompound data = new NbtCompound();

        // Before syncing make sure the changes are written to the nbt.
        // Do not include dimension tile data.  This will happen later.
        writeToNBT(data, false);
        new MapDataS2CPacket(atlasID, data).send(player);

        for (RegistryKey<World> world : worldMap.keySet()) {
            worldMap.get(world).syncToPlayer(atlasID, player);
        }

        AntiqueAtlas.LOGGER.info("Sent Atlas #{} data to player {}", atlasID, player.getCommandSource().getName());
        playersSentTo.add(player);
    }

    public boolean isEmpty() {
        return worldMap.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AtlasData other)) return false;
        // TODO: This doesn't handle disjoint DimensionType keysets of equal size
        if (other.worldMap.size() != worldMap.size()) return false;
        for (RegistryKey<World> key : worldMap.keySet()) {
            if (!worldMap.get(key).equals(other.worldMap.get(key))) return false;
        }
        return true;
    }
}
