package folk.sisby.antique_atlas.network.c2s;

import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import folk.sisby.antique_atlas.network.AntiqueAtlasPacket;

public interface C2SPacket extends AntiqueAtlasPacket {
    default void send() {
        AntiqueAtlasNetworking.C2S_SENDER.accept(this);
    }

    int atlasID();
}
