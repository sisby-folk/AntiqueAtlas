package folk.sisby.antique_atlas.marker;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Marker on the map in an atlas. Has a type and a text label.
 *
 * @author Hunternif
 */
public class Marker extends MarkerData {
    /**
     * Id is unique only within a MarkersData instance, i.e. within one atlas
     * or among global markers in a world.
     */
    private final Identifier type;
    private final RegistryKey<World> world;
    private boolean isGlobal;

    //TODO make an option for the marker to disappear at a certain scale.

    public Marker(int id, Identifier type, Text label, RegistryKey<World> world, int x, int z, boolean visibleAhead) {
        super(id, label, x, z, visibleAhead);
        this.type = type;
        this.world = world;
    }

    public Marker(Identifier type, RegistryKey<World> world, MarkerData data) {
        this(data.id, type, data.label, world, data.x, data.z, data.visibleAhead);
    }

    public int getId() {
        return id;
    }

    public Identifier getType() {
        return type;
    }

    /**
     * The label "as is", it might be a placeholder in the format
     * "gui.antique_atlas.marker.*" that has to be translated.
     */
    public Text getLabel() {
        return label;
    }

    public RegistryKey<World> getWorld() {
        return this.world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    /**
     * X coordinate of the chunk.
     */
    public int getChunkX() {
        return x >> 4;
    }

    /**
     * Z coordinate of the chunk.
     */
    public int getChunkZ() {
        return z >> 4;
    }

    /**
     * Whether the marker is visible regardless of the player having seen the location.
     */
    public boolean isVisibleAhead() {
        return visibleAhead;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    Marker setGlobal(boolean value) {
        this.isGlobal = value;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Marker marker)) return false;
        return this.id == marker.id;
    }

    @Override
    public String toString() {
        return "#" + id + "\"" + label.getString() + "\"" + "@(" + x + ", " + z + ")";
    }
}
