package folk.sisby.antique_atlas.network.packet.s2c.play;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.gui.GuiAtlas;
import folk.sisby.antique_atlas.core.AtlasData;
import folk.sisby.antique_atlas.network.packet.s2c.S2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
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
    public static final Identifier ID = AntiqueAtlas.id("packet.s2c.map.data");

    public MapDataS2CPacket(int atlasID, NbtCompound data) {
        this.writeVarInt(atlasID);
        this.writeNbt(data);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        NbtCompound data = buf.readNbt();

        if (data == null) return;

        AtlasData atlasData = AntiqueAtlas.tileData.getData(atlasID, client.player.getEntityWorld());
        atlasData.updateFromNbt(data);

        if (AntiqueAtlas.CONFIG.Gameplay.doSaveBrowsingPos && MinecraftClient.getInstance().currentScreen instanceof GuiAtlas) {
            ((GuiAtlas) MinecraftClient.getInstance().currentScreen).loadSavedBrowsingPosition();
        }
    }
}
