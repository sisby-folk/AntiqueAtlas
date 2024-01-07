package folk.sisby.antique_atlas.core;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.api.AtlasAPI;
import folk.sisby.antique_atlas.marker.MarkersData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class PlayerEventHandler {
    public static void onPlayerLogin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();
        World world = player.world;
        int atlasID = AtlasAPI.getPlayerAtlasId(player);

        AtlasData data = AntiqueAtlas.tileData.getData(atlasID, world);
        // On the player join send the map from the server to the client:
        if (!data.isEmpty()) {
            data.syncToPlayer(atlasID, player);
        }

        // Same thing with the local markers:
        MarkersData markers = AntiqueAtlas.markersData.getMarkersData(atlasID, world);
        if (!markers.isEmpty()) {
            markers.syncToPlayer(atlasID, player);
        }
    }

    public static void onPlayerTick(PlayerEntity player) {
        // TODO Can we move world scanning to the server in this case as well?
        AtlasData data = AntiqueAtlas.tileData.getData(
            AtlasAPI.getPlayerAtlasId(player), player.world);

        AntiqueAtlas.worldScanner.updateAtlasAroundPlayer(data, player);
    }
}
