package folk.sisby.antique_atlas.marker;

import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains markers, mapped to dimensions, and then to their chunk coordinates.
 * <p>
 * On the server a separate instance of MarkersData contains all the global
 * markers, which are also copied to atlases, but not saved with them.
 * At runtime clients have both types of markers in the same collection..
 * </p>
 *
 * @author Hunternif
 */
public class MarkersData extends PersistentState {
    private static final int VERSION = 4;
    private static final String TAG_VERSION = "aaVersion";
    private static final String TAG_WORLD_MAP_LIST = "worldMap";
    private static final String TAG_WORLD_ID = "worldID";
    private static final String TAG_MARKERS = "markers";
    private static final String TAG_MARKER_ID = "id";
    private static final String TAG_MARKER_TYPE = "markerType";
    private static final String TAG_MARKER_LABEL = "label";
    private static final String TAG_MARKER_X = "x";
    private static final String TAG_MARKER_Y = "y";
    private static final String TAG_MARKER_VISIBLE_AHEAD = "visAh";

    /**
     * Markers are stored in lists within square areas this many MC chunks
     * across.
     */
    public static final int CHUNK_STEP = 8;

    private final AtomicInteger largestID = new AtomicInteger(0);

    private int getNewID() {
        return largestID.incrementAndGet();
    }

    private final Map<Integer /*marker ID*/, Marker> idMap = new ConcurrentHashMap<>(2, 0.75f, 2);
    /**
     * Maps a list of markers in a square to the square's coordinates, then to
     * dimension ID. It exists in case someone needs to quickly find markers
     * located in a square.
     * Within the list markers are ordered by the Z coordinate, so that markers
     * placed closer to the south will appear in front of those placed closer to
     * the north.
     * TODO: consider using Quad-tree. At small zoom levels iterating through
     * chunks to render markers gets very slow.
     */
    private final Map<RegistryKey<World>, DimensionMarkersData> worldMap =
        new ConcurrentHashMap<>(2, 0.75f, 2);

    public MarkersData() {
    }

    public static MarkersData fromNbt(NbtCompound compound) {
        MarkersData data = new MarkersData();
        doReadNbt(compound, data);
        return data;
    }

    protected static void doReadNbt(NbtCompound compound, MarkersData data) {

        int version = compound.getInt(TAG_VERSION);
        if (version < VERSION) {
            AntiqueAtlas.LOGGER.warn("Outdated atlas data format! Was {} but current is {}", version, VERSION);
            return;
        }

        NbtList dimensionMapList = compound.getList(TAG_WORLD_MAP_LIST, NbtElement.COMPOUND_TYPE);
        for (int d = 0; d < dimensionMapList.size(); d++) {
            NbtCompound tag = dimensionMapList.getCompound(d);
            RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, new Identifier(tag.getString(TAG_WORLD_ID)));

            NbtList tagList = tag.getList(TAG_MARKERS, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < tagList.size(); i++) {
                NbtCompound markerTag = tagList.getCompound(i);
                boolean visibleAhead = markerTag.getBoolean(TAG_MARKER_VISIBLE_AHEAD);

                int id = markerTag.getInt(TAG_MARKER_ID);
                if (data.getMarkerByID(id) != null) {
                    AntiqueAtlas.LOGGER.warn("Loading marker with duplicate id {}. Getting new id", id);
                    id = data.getNewID();
                }
                data.markDirty();
                if (data.largestID.intValue() < id) {
                    data.largestID.set(id);
                }

                Marker marker = new Marker(
                    id,
                    new Identifier(markerTag.getString(TAG_MARKER_TYPE)),
                    Text.Serializer.fromJson(markerTag.getString(TAG_MARKER_LABEL)),
                    world,
                    markerTag.getInt(TAG_MARKER_X),
                    markerTag.getInt(TAG_MARKER_Y),
                    visibleAhead);
                data.loadMarker(marker);
            }
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        AntiqueAtlas.LOGGER.info("Saving local markers data to NBT");
        compound.putInt(TAG_VERSION, VERSION);
        NbtList dimensionMapList = new NbtList();
        for (RegistryKey<World> world : worldMap.keySet()) {
            NbtCompound tag = new NbtCompound();
            tag.putString(TAG_WORLD_ID, world.getValue().toString());
            DimensionMarkersData data = getMarkersDataInWorld(world);
            NbtList tagList = new NbtList();
            for (Marker marker : data.getAllMarkers()) {
                NbtCompound markerTag = new NbtCompound();
                markerTag.putInt(TAG_MARKER_ID, marker.getId());
                markerTag.putString(TAG_MARKER_TYPE, marker.getType().toString());
                markerTag.putString(TAG_MARKER_LABEL, Text.Serializer.toJson(marker.getLabel()));
                markerTag.putInt(TAG_MARKER_X, marker.getX());
                markerTag.putInt(TAG_MARKER_Y, marker.getZ());
                markerTag.putBoolean(TAG_MARKER_VISIBLE_AHEAD, marker.isVisibleAhead());
                tagList.add(markerTag);
            }
            tag.put(TAG_MARKERS, tagList);
            dimensionMapList.add(tag);
        }
        compound.put(TAG_WORLD_MAP_LIST, dimensionMapList);

        return compound;
    }

    /**
     * Creates a new instance of {@link DimensionMarkersData}, if necessary.
     */
    public DimensionMarkersData getMarkersDataInWorld(RegistryKey<World> world) {
        return worldMap.computeIfAbsent(world, k -> new DimensionMarkersData(this));
    }

    private Marker getMarkerByID(int id) {
        return idMap.get(id);
    }

    public Marker removeMarker(int id) {
        Marker marker = getMarkerByID(id);
        if (marker == null) return null;
        if (idMap.remove(id) != null) {
            getMarkersDataInWorld(marker.getWorld()).removeMarker(marker);
            markDirty();
        }
        return marker;
    }

    /**
     * For internal use, when markers are loaded from NBT or sent from the
     * server. IF a marker's id is conflicting, the marker will not load!
     *
     * @return the marker instance that was added.
     */
    public Marker loadMarker(Marker marker) {
        if (!idMap.containsKey(marker.getId())) {
            idMap.put(marker.getId(), marker);
            getMarkersDataInWorld(marker.getWorld()).insertMarker(marker);
        }
        return marker;
    }

    public boolean isEmpty() {
        return idMap.isEmpty();
    }
}
