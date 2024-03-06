package folk.sisby.antique_atlas.mixin;

import folk.sisby.antique_atlas.AntiqueAtlasWorld;
import folk.sisby.antique_atlas.WorldAtlasData;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientWorld.class)
public class MixinClientWorld implements AntiqueAtlasWorld {
    private @Unique WorldAtlasData antiqueAtlas$worldAtlasData;

    @Override
    public WorldAtlasData antiqueAtlas$getData() {
        if (antiqueAtlas$worldAtlasData == null) {
            antiqueAtlas$worldAtlasData = new WorldAtlasData((ClientWorld) (Object) this);
        }
        return antiqueAtlas$worldAtlasData;
    }
}
