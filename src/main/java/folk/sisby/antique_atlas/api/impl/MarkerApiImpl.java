package folk.sisby.antique_atlas.api.impl;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.api.MarkerAPI;
import folk.sisby.antique_atlas.marker.Marker;
import folk.sisby.antique_atlas.marker.MarkersData;
import folk.sisby.antique_atlas.network.s2c.DeleteMarkerS2CPacket;
import folk.sisby.antique_atlas.network.s2c.PutMarkersS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

@SuppressWarnings("unused")
public class MarkerApiImpl implements MarkerAPI {
    /**
     * Used in place of atlasID to signify that the marker is global.
     */
    private static final int GLOBAL = -1;

    @Nullable
    @Override
    public Marker putMarker(@NotNull World world, boolean visibleAhead, int atlasID, Identifier marker, Text label, int x, int z) {
        return doPutMarker(world, visibleAhead, atlasID, marker, label, x, z);
    }

    @Nullable
    @Override
    public Marker putGlobalMarker(@NotNull World world, boolean visibleAhead, Identifier marker, Text label, int x, int z) {
        return doPutMarker(world, visibleAhead, GLOBAL, marker, label, x, z);
    }

    private Marker doPutMarker(World world, boolean visibleAhead, int atlasID, Identifier markerId, Text label, int x, int z) {
        Marker marker = null;
        if (!world.isClient && world.getServer() != null) {
            MarkersData data = atlasID == GLOBAL
                ? AntiqueAtlas.globalMarkersData.getData()
                : AntiqueAtlas.markersData.getMarkersData(atlasID, world);

            marker = data.createAndSaveMarker(markerId, world.getRegistryKey(), x, z, visibleAhead, label);
            new PutMarkersS2CPacket(atlasID, world.getRegistryKey(), Collections.singleton(marker)).send((ServerWorld) world);
        }

        return marker;
    }

    @Override
    public void deleteMarker(@NotNull World world, int atlasID, int markerID) {
        doDeleteMarker(world, atlasID, markerID);
    }

    @Override
    public void deleteGlobalMarker(@NotNull World world, int markerID) {
        doDeleteMarker(world, GLOBAL, markerID);
    }

    private void doDeleteMarker(World world, int atlasID, int markerID) {
        MarkersData data = atlasID == GLOBAL ?
            AntiqueAtlas.globalMarkersData.getData() :
            AntiqueAtlas.markersData.getMarkersData(atlasID, world);
        data.removeMarker(markerID);

        new DeleteMarkerS2CPacket(atlasID, markerID).send(((ServerWorld) world).getServer());
    }
}
