package folk.sisby.antique_atlas.marker;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class MarkerData {
    final int id;
    final Text label;
    final int x, z;
    final boolean visibleAhead;

    public MarkerData(int id, Text label, int x, int z, boolean visibleAhead) {
        this.id = id;
        this.label = label;
        this.x = x;
        this.z = z;
        this.visibleAhead = visibleAhead;
    }

    public MarkerData(PacketByteBuf buf) {
        this.id = buf.readVarInt();
        this.label = buf.readText();
        this.x = buf.readVarInt();
        this.z = buf.readVarInt();
        this.visibleAhead = buf.readBoolean();
    }

    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.id);
        buf.writeText(this.label);
        buf.writeVarInt(this.x);
        buf.writeVarInt(this.z);
        buf.writeBoolean(this.visibleAhead);
    }
}
