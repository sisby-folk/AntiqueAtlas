package folk.sisby.antique_atlas.network.packet.s2c;

import folk.sisby.antique_atlas.network.packet.AntiqueAtlasPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public abstract class S2CPacket extends AntiqueAtlasPacket {
    public void send(ServerPlayerEntity playerEntity) {
        ServerPlayNetworking.send(playerEntity, this.getId(), this);
    }

    public void send(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, this.getId(), this);
        }
    }

    public void send(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, this.getId(), this);
        }
    }
}
