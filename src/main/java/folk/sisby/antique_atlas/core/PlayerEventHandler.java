package folk.sisby.antique_atlas.core;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.AntiqueAtlasConfig;
import folk.sisby.antique_atlas.api.AtlasAPI;
import folk.sisby.antique_atlas.marker.MarkersData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.Locale;

public class PlayerEventHandler {
    public static void onPlayerLogin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();
        World world = player.getWorld();
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
        AtlasData data = AntiqueAtlas.tileData.getData(AtlasAPI.getPlayerAtlasId(player), player.getWorld());

        AntiqueAtlas.worldScanner.updateAtlasAroundPlayer(data, player);
    }

    public static void onPlayerDeath(PlayerEntity player) {
        if (AntiqueAtlas.CONFIG.Gameplay.autoDeathMarker) {
            int atlasID = AtlasAPI.getPlayerAtlasId(player);
            AtlasAPI.getMarkerAPI().putMarker(player.getEntityWorld(), true, atlasID, AntiqueAtlas.CONFIG.Interface.graveVerb == AntiqueAtlasConfig.GraveVerb.DIED ? AntiqueAtlas.id("tomb") : AntiqueAtlas.id("bundle"),
                Text.translatable("gui.antique_atlas.marker.tomb", Text.translatable("gui.antique_atlas.marker.tomb.%s".formatted(AntiqueAtlas.CONFIG.Interface.graveVerb.toString().toLowerCase(Locale.ROOT))).formatted(Formatting.RED), Text.literal(String.valueOf(1 + (player.getEntityWorld().getTimeOfDay()  / 24000))).formatted(Formatting.WHITE)).formatted(Formatting.GRAY),
                (int) player.getX(), (int) player.getZ());
        }
    }
}
