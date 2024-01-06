package folk.sisby.antique_atlas.mixinhooks;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface NewPlayerConnectionCallback {
    Event<NewPlayerConnectionCallback> EVENT = EventFactory.createLoop();

    void onNewConnection(ServerPlayerEntity player);
}
