package folk.sisby.antique_atlas.network.s2c;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import folk.sisby.antique_atlas.marker.MarkerData;
import folk.sisby.antique_atlas.marker.Marker;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

/**
 * Sends markers set via API from server to client.
 * Only one dimension per packet.
 * The markers in one packet are either all global or all local.
 *
 * @author Hunternif
 * @author Haven King
 */
public record PutMarkersS2CPacket(int atlasID, RegistryKey<World> world, ListMultimap<Identifier, MarkerData> markers) implements S2CPacket {
    public PutMarkersS2CPacket(int atlasID, RegistryKey<World> world, Collection<Marker> markers) {
        this(atlasID, world, getMarkersWithIDs(markers));
    }

    private static ListMultimap<Identifier, MarkerData> getMarkersWithIDs(Collection<Marker> markers) {
        ListMultimap<Identifier, MarkerData> markersByType = ArrayListMultimap.create();
        for (Marker marker : markers) {
            markersByType.put(marker.getType(), marker);
        }
        return markersByType;
    }

    public PutMarkersS2CPacket(PacketByteBuf buf) {
        this(buf.readVarInt(), RegistryKey.of(Registry.WORLD_KEY, buf.readIdentifier()), readMarkers(buf));
    }

    private static ListMultimap<Identifier, MarkerData> readMarkers(PacketByteBuf buf) {
        int typesLength = buf.readVarInt();

        ListMultimap<Identifier, MarkerData> markersByType = ArrayListMultimap.create();
        for (int i = 0; i < typesLength; ++i) {
            Identifier type = buf.readIdentifier();
            int markersLength = buf.readVarInt();
            for (int j = 0; j < markersLength; ++j) {
                markersByType.put(type, new MarkerData(buf));
            }
        }

        return markersByType;
    }

    @Override
    public void writeBuf(PacketByteBuf buf) {
        buf.writeVarInt(atlasID);
        buf.writeIdentifier(world.getValue());
        buf.writeVarInt(markers.keySet().size());

        for (Identifier type : markers.keySet()) {
            buf.writeIdentifier(type);
            List<MarkerData> markerList = markers.get(type);
            buf.writeVarInt(markerList.size());
            for (MarkerData marker : markerList) {
                marker.write(buf);
            }
        }
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_PUT_MARKERS;
    }
}
