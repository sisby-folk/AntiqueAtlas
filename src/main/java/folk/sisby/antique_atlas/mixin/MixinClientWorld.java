package folk.sisby.antique_atlas.mixin;

import folk.sisby.antique_atlas.AntiqueAtlasWorld;
import folk.sisby.antique_atlas.WorldAtlasData;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientWorld.class)
public class MixinClientWorld implements AntiqueAtlasWorld {
    @Override
    public WorldAtlasData antiqueAtlas$getData() {
        ClientWorld self = (ClientWorld) (Object) this;
        return WorldAtlasData.WORLDS.computeIfAbsent(self.getRegistryKey(), k -> new WorldAtlasData(self));
    }
}
