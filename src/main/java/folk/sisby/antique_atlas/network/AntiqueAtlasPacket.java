package folk.sisby.antique_atlas.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface AntiqueAtlasPacket {
    void writeBuf(PacketByteBuf buf);

    Identifier getId();

    default PacketByteBuf toBuf() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        writeBuf(buf);
        return buf;
    }
}
