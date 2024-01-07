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
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Packet used to save the last browsing position for a dimension in an atlas.
 *
 * @author Hunternif
 * @author Haven King
 */
public class PutBrowsingPositionC2SPacket extends C2SPacket {
    public static final Identifier ID = AntiqueAtlas.id("packet.c2s.browsing_position.put");

    public PutBrowsingPositionC2SPacket(int atlasID, RegistryKey<World> world, int x, int y, double zoom) {
        this.writeVarInt(atlasID);
        this.writeIdentifier(world.getValue());
        this.writeVarInt(x);
        this.writeVarInt(y);
        this.writeDouble(zoom);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static void apply(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        RegistryKey<World> world = RegistryKey.of(Registry.WORLD_KEY, buf.readIdentifier());
        int x = buf.readVarInt();
        int y = buf.readVarInt();
        double zoom = buf.readDouble();

        if (AtlasAPI.getPlayerAtlasId(player) != atlasID) {
            Log.warn("Player %s attempted to put position marker into someone else's Atlas #%d", player.getCommandSource().getName(), atlasID);
            return;
        }

        AntiqueAtlas.tileData.getData(atlasID, player.getEntityWorld()).getWorldData(world).setBrowsingPosition(x, y, zoom);
    }
}
