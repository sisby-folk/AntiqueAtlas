package folk.sisby.antique_atlas.network.packet.c2s.play;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.api.AtlasAPI;
import folk.sisby.antique_atlas.network.packet.c2s.C2SPacket;
import folk.sisby.antique_atlas.util.Log;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Puts biome tile into one atlas. When sent to server, forwards it to every
 * client that has this atlas' data synced.
 *
 * @author Hunternif
 * @author Haven King
 */
public class PutTileC2SPacket extends C2SPacket {
    public static final Identifier ID = AntiqueAtlas.id("packet.c2s.tile.put");

    public PutTileC2SPacket(int atlasID, int x, int z, Identifier tile) {
        this.writeInt(atlasID);
        this.writeVarInt(x);
        this.writeVarInt(z);
        this.writeIdentifier(tile);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        int x = buf.readVarInt();
        int z = buf.readVarInt();
        Identifier tile = buf.readIdentifier();

        if (AtlasAPI.getPlayerAtlasId(player) != atlasID) {
            Log.warn("Player %s attempted to modify someone else's Atlas #%d",
                player.getName(), atlasID);
            return;
        }

        AtlasAPI.getTileAPI().putTile(player.getEntityWorld(), atlasID, tile, x, z);
    }
}
