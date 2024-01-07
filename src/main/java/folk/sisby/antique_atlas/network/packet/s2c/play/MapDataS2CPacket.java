package folk.sisby.antique_atlas.network.packet.s2c.play;

import dev.architectury.networking.NetworkManager;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.gui.GuiAtlas;
import folk.sisby.antique_atlas.core.AtlasData;
import folk.sisby.antique_atlas.network.packet.s2c.S2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Used to sync atlas data from server to client.
 *
 * @author Hunternif
 * @author Haven King
 */
public class MapDataS2CPacket extends S2CPacket {
    public static final Identifier ID = AntiqueAtlas.id("packet", "s2c", "map", "data");

    public MapDataS2CPacket(int atlasID, NbtCompound data) {
        this.writeVarInt(atlasID);
        this.writeNbt(data);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public static void apply(PacketByteBuf buf, NetworkManager.PacketContext context) {
        int atlasID = buf.readVarInt();
        NbtCompound data = buf.readNbt();

        if (data == null) return;

        context.queue(() -> {
            AtlasData atlasData = AntiqueAtlas.tileData.getData(atlasID, context.getPlayer().getEntityWorld());
            atlasData.updateFromNbt(data);

            if (AntiqueAtlas.CONFIG.doSaveBrowsingPos && MinecraftClient.getInstance().currentScreen instanceof GuiAtlas) {
                ((GuiAtlas) MinecraftClient.getInstance().currentScreen).loadSavedBrowsingPosition();
            }
        });
    }
}
