package folk.sisby.antique_atlas.mixin;

import folk.sisby.antique_atlas.AntiqueAtlasWorld;
import folk.sisby.antique_atlas.WorldMarkers;
import folk.sisby.antique_atlas.WorldTiles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientWorld.class)
public class MixinClientWorld implements AntiqueAtlasWorld {
    private @Unique WorldTiles antiqueAtlas$worldTiles;
    private @Unique WorldMarkers antiqueAtlas$worldMarkers;

    @Override
    public WorldTiles antiqueAtlas$getWorldTiles() {
        if (antiqueAtlas$worldTiles == null) {
            antiqueAtlas$worldTiles = new WorldTiles(MinecraftClient.getInstance().player, (ClientWorld) (Object) this);
        }
        return antiqueAtlas$worldTiles;
    }

    @Override
    public WorldMarkers antiqueAtlas$getWorldMarkers() {
        if (antiqueAtlas$worldMarkers == null) {
            antiqueAtlas$worldMarkers = new WorldMarkers((ClientWorld) (Object) this);
        }
        return antiqueAtlas$worldMarkers;
    }
}
