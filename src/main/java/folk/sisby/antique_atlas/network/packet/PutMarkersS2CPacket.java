package folk.sisby.antique_atlas.network.packet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import folk.sisby.antique_atlas.network.S2CPacket;
import folk.sisby.antique_atlas.marker.Marker;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.minecraft.util.Identifier;
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
public class PutMarkersS2CPacket extends S2CPacket {
    public PutMarkersS2CPacket(int atlasID, RegistryKey<World> world, Collection<Marker> markers) {
        ListMultimap<Identifier, Marker> markersByType = ArrayListMultimap.create();
        for (Marker marker : markers) {
            markersByType.put(marker.getType(), marker);
        }

        this.writeVarInt(atlasID);
        this.writeIdentifier(world.getValue());
        this.writeVarInt(markersByType.keySet().size());

        for (Identifier type : markersByType.keySet()) {
            this.writeIdentifier(type);
            List<Marker> markerList = markersByType.get(type);
            this.writeVarInt(markerList.size());
            for (Marker marker : markerList) {
                marker.write(this);
            }
        }
    }

    @Override
    public Identifier getId() {
        return AntiqueAtlasNetworking.S2C_PUT_MARKERS;
    }
}
