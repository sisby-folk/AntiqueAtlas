package hunternif.mc.impl.atlas.core.watcher;

import hunternif.mc.api.AtlasAPI;
import hunternif.mc.impl.atlas.AntiqueAtlasMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Puts an skull marker to the player's death spot.
 *
 * @author Hunternif, Haven King
 */
public class DeathWatcher {
    public static void onPlayerDeath(PlayerEntity player) {
        if (AntiqueAtlasMod.CONFIG.autoDeathMarker) {
            int atlasID = AtlasAPI.getPlayerAtlasId(player);
            AtlasAPI.getMarkerAPI().putMarker(player.getEntityWorld(), true, atlasID, new Identifier("antiqueatlas:tomb"),
                    Text.translatable("gui.antiqueatlas.marker.tomb", player.getName()),
                    (int) player.getX(), (int) player.getZ());
        }
    }
}
