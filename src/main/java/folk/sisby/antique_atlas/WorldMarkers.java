package folk.sisby.antique_atlas;

import com.google.common.collect.Multimap;
import folk.sisby.surveyor.SurveyorWorld;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.LandmarkType;
import folk.sisby.surveyor.landmark.NetherPortalLandmark;
import folk.sisby.surveyor.landmark.PlayerDeathLandmark;
import folk.sisby.surveyor.landmark.SimplePointLandmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class WorldMarkers {
    private final Map<LandmarkType<?>, Map<BlockPos, Marker>> markers = new ConcurrentHashMap<>();

    public WorldMarkers(World world) {
        refresh(world);
    }

    public void refresh(World world) {
        markers.clear();
        ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().keySet().forEach(((landmarkType, pos) -> {
            if (landmarkType == NetherPortalLandmark.TYPE) {
                NetherPortalLandmark landmark = (NetherPortalLandmark) ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().get(landmarkType, pos);
                markers.computeIfAbsent(landmarkType, t -> new HashMap<>()).put(landmark.pos(), new Marker(
                    landmarkType, AntiqueAtlas.id("nether_portal"), landmark.name(), landmark.pos(), true, landmark.owner()
                ));
            }
            if (landmarkType == PlayerDeathLandmark.TYPE) {
                PlayerDeathLandmark landmark = (PlayerDeathLandmark) ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().get(landmarkType, pos);

                AntiqueAtlasConfig.GraveStyle style = AntiqueAtlas.CONFIG.ui.graveStyle;
                if (landmark.name() == null && style == AntiqueAtlasConfig.GraveStyle.CAUSE) style = AntiqueAtlasConfig.GraveStyle.DIED;
                MutableText timeText = Text.literal(String.valueOf(1 + (landmark.created() / 24000))).formatted(Formatting.WHITE);
                MutableText text = switch (style) {
                    case CAUSE -> Text.translatable("gui.antique_atlas.marker.death.%s".formatted(style.toString().toLowerCase()), landmark.name().copy().formatted(Formatting.GRAY).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
                    case GRAVE, ITEMS, DIED -> Text.translatable("gui.antique_atlas.marker.death.%s".formatted(style.toString().toLowerCase()), Text.translatable("gui.antique_atlas.marker.death.%s.verb".formatted(style.toString().toLowerCase())).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
                    case EUPHEMISMS -> Text.translatable("gui.antique_atlas.marker.death.%s".formatted(style.toString().toLowerCase()), Text.translatable("gui.antique_atlas.marker.death.%s.verb.%s".formatted(style.toString().toLowerCase(), new Random(landmark.seed()).nextInt(11))).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
                };
                Identifier icon = switch (style) {
                    case CAUSE, GRAVE, DIED, EUPHEMISMS -> AntiqueAtlas.id("tomb");
                    case ITEMS -> AntiqueAtlas.id("bundle");
                };

                markers.computeIfAbsent(landmarkType, t -> new HashMap<>()).put(landmark.pos(), new Marker(
                    landmarkType, icon, text, landmark.pos(), true, landmark.owner()
                ));
            }
            if (landmarkType == SimplePointLandmark.TYPE) {
                SimplePointLandmark landmark = (SimplePointLandmark) ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().get(landmarkType, pos);
                markers.computeIfAbsent(landmarkType, t -> new HashMap<>()).put(landmark.pos(), new Marker(
                    landmarkType, landmark.texture(), landmark.name(), landmark.pos(), true, landmark.owner()
                ));
            }
        }));
    }

    public void onLandmarksAdded(World world, WorldLandmarks ws, Collection<Landmark<?>> landmark) {
        refresh(world);
    }

    public void onLandmarksRemoved(ClientWorld world, WorldLandmarks landmarks, Multimap<LandmarkType<?>, BlockPos> landmark) {
        refresh(world);
    }

    public void addMarker(PlayerEntity player, World world, Marker marker) {
        ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().put(world, new SimplePointLandmark(
            new BlockPos(marker.pos().x(), 0, marker.pos().z()),
            player.getUuid(),
            DyeColor.BLUE,
            marker.label(),
            marker.type()
        ));
    }

    public boolean deleteMarker(World world, Marker marker) {
        if (marker.isGlobal()) return false;
        ((SurveyorWorld) world).surveyor$getWorldSummary().landmarks().remove(world, marker.landmarkType(), marker.blockPos());
        return true;
    }

    public Collection<Marker> getAllMarkers() {
        List<Marker> outList = new ArrayList<>();
        markers.values().forEach(m -> outList.addAll(m.values()));
        return outList;
    }
}
