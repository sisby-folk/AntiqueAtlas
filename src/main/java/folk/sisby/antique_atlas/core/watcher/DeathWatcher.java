package folk.sisby.antique_atlas.core.watcher;

import folk.sisby.antique_atlas.api.AtlasAPI;
import folk.sisby.antique_atlas.AntiqueAtlas;
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
        if (AntiqueAtlas.CONFIG.autoDeathMarker) {
            int atlasID = AtlasAPI.getPlayerAtlasId(player);
            AtlasAPI.getMarkerAPI().putMarker(player.getEntityWorld(), true, atlasID, new Identifier("antique_atlas:tomb"),
                    Text.translatable("gui.antique_atlas.marker.tomb", player.getName()),
                    (int) player.getX(), (int) player.getZ());
        }
    }
}
