package folk.sisby.antique_atlas.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import folk.sisby.antique_atlas.marker.Marker;
import net.minecraft.entity.player.PlayerEntity;

@FunctionalInterface
public interface MarkerHoveredCallback {
    Event<MarkerHoveredCallback> EVENT = EventFactory.createLoop();

    void onHovered(PlayerEntity player, Marker marker);
}
