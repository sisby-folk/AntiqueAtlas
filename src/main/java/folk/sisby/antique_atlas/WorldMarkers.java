package folk.sisby.antique_atlas;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import folk.sisby.surveyor.SurveyorWorld;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.NetherPortalLandmark;
import folk.sisby.surveyor.landmark.SimplePointLandmark;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.World;

import java.util.Collection;

public class WorldMarkers {
    private final Multimap<ChunkPos, Marker> markers = HashMultimap.create();

    public WorldMarkers(World world) {
        refresh(world);
    }

    public void refresh(World world) {
        markers.clear();
        ((SurveyorWorld) world).surveyor$getWorldSummary().getLandmarks().forEach(((landmarkType, pos) -> {
            if (landmarkType == NetherPortalLandmark.TYPE) {
                Landmark<?> landmark = ((SurveyorWorld) world).surveyor$getWorldSummary().getLandmark(landmarkType, pos);
                markers.put(new ChunkPos(landmark.pos()), new Marker(
                    AntiqueAtlas.id("nether_portal"), landmark.name(), new ColumnPos(landmark.pos().getX(), landmark.pos().getZ()), true, true
                ));
            }
            if (landmarkType == SimplePointLandmark.TYPE) {
                Landmark<?> landmark = ((SurveyorWorld) world).surveyor$getWorldSummary().getLandmark(landmarkType, pos);
                markers.put(new ChunkPos(landmark.pos()), new Marker(
                    landmark.texture(), landmark.name(), new ColumnPos(landmark.pos().getX(), landmark.pos().getZ()), true, landmark.owner() == null
                ));
            }
        }));
    }

    public void addMarker(PlayerEntity player, World world, Marker marker) {
        ((SurveyorWorld) world).surveyor$getWorldSummary().putLandmark(new SimplePointLandmark(
            new BlockPos(marker.pos().x(), 0, marker.pos().z()),
            player.getUuid(),
            DyeColor.BLUE,
            marker.label(),
            marker.type()
        ));
        refresh(world);
    }

    public void deleteMarker(World world, Marker marker) {
        if (marker.isGlobal()) return;
        ((SurveyorWorld) world).surveyor$getWorldSummary().removeLandmark(SimplePointLandmark.TYPE, new BlockPos(marker.pos().x(), 0, marker.pos().z()));
        refresh(world);
    }

    public Collection<Marker> getAllMarkers() {
        return markers.values();
    }
}
