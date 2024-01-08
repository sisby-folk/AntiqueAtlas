package folk.sisby.antique_atlas.client.api.impl;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.api.MarkerAPI;
import folk.sisby.antique_atlas.marker.Marker;
import folk.sisby.antique_atlas.network.c2s.DeleteMarkerC2SPacket;
import folk.sisby.antique_atlas.network.c2s.PutMarkerC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class MarkerApiImplClient implements MarkerAPI {
    @Nullable
    @Override
    public Marker putMarker(@NotNull World world, boolean visibleAhead, int atlasID, Identifier marker, Text label, int x, int z) {
        new PutMarkerC2SPacket(atlasID, marker, x, z, visibleAhead, label).send();
        return null;
    }

    @Nullable
    @Override
    public Marker putGlobalMarker(@NotNull World world, boolean visibleAhead, Identifier marker, Text label, int x, int z) {
        AntiqueAtlas.LOG.warn("Client tried to add a global marker");

        return null;
    }

    @Override
    public void deleteMarker(@NotNull World world, int atlasID, int markerID) {
        new DeleteMarkerC2SPacket(atlasID, markerID).send();
    }

    @Override
    public void deleteGlobalMarker(@NotNull World world, int markerID) {
        AntiqueAtlas.LOG.warn("Client tried to delete a global marker");
    }
}
