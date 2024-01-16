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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

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
        AtlasData data = AntiqueAtlas.tileData.getData(AtlasAPI.getPlayerAtlasId(player), player.world);

        AntiqueAtlas.worldScanner.updateAtlasAroundPlayer(data, player);
    }

    public static void onPlayerDeath(PlayerEntity player) {
        if (AntiqueAtlas.CONFIG.Gameplay.autoDeathMarker) {
            int atlasID = AtlasAPI.getPlayerAtlasId(player);
            AntiqueAtlasConfig.GraveStyle style = AntiqueAtlas.CONFIG.Interface.graveStyle;
            MutableText timeText = Text.literal(String.valueOf(1 + (player.getEntityWorld().getTimeOfDay()  / 24000))).formatted(Formatting.WHITE);
            MutableText text = switch (style) {
                case GRAVE, ITEMS, DIED -> Text.translatable("gui.antique_atlas.marker.death.%s".formatted(style.toString().toLowerCase()), Text.translatable("gui.antique_atlas.marker.death.%s.verb".formatted(style.toString().toLowerCase())).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
                case EUPHEMISMS -> Text.translatable("gui.antique_atlas.marker.death.%s".formatted(style.toString().toLowerCase()), Text.translatable("gui.antique_atlas.marker.death.%s.verb.%s".formatted(style.toString().toLowerCase(), player.getRandom().nextInt(11))).formatted(Formatting.RED), timeText).formatted(Formatting.GRAY);
            };
            Identifier icon = switch (style) {
                case GRAVE, DIED, EUPHEMISMS -> AntiqueAtlas.id("tomb");
                case ITEMS ->  AntiqueAtlas.id("bundle");
            };

            AtlasAPI.getMarkerAPI().putMarker(player.getEntityWorld(), true, atlasID, icon, text, (int) player.getX(), (int) player.getZ());
        }
    }
}
