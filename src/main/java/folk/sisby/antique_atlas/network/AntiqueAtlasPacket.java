package folk.sisby.antique_atlas.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public abstract class AntiqueAtlasPacket extends PacketByteBuf {
    public AntiqueAtlasPacket() {
        super(Unpooled.buffer());
    }

    public abstract Identifier getId();
}
