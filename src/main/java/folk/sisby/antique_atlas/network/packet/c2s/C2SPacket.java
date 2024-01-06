package folk.sisby.antique_atlas.network.packet.c2s;

import dev.architectury.networking.NetworkManager;
import folk.sisby.antique_atlas.network.packet.AntiqueAtlasPacket;

public abstract class C2SPacket extends AntiqueAtlasPacket {
	public void send() {
		NetworkManager.sendToServer(this.getId(), this);
	}
}
