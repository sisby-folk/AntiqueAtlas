package folk.sisby.antique_atlas.client.network;

import folk.sisby.antique_atlas.network.AntiqueAtlasPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public abstract class C2SPacket extends AntiqueAtlasPacket {
    public void send() {
        ClientPlayNetworking.send(this.getId(), this);
    }
}
