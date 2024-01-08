package folk.sisby.antique_atlas.network.c2s;

import folk.sisby.antique_atlas.network.AntiqueAtlasPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public interface C2SPacket extends AntiqueAtlasPacket {
    default void send() {
        ClientPlayNetworking.send(getId(), toBuf());
    }

    int atlasID();
}
