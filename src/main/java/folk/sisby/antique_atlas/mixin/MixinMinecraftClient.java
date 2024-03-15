package folk.sisby.antique_atlas.mixin;

import folk.sisby.antique_atlas.reloader.BiomeTileProviders;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Inject(method = "joinWorld", at = @At("TAIL"))
    void AntiqueAtlas_joinWorld(ClientWorld world, CallbackInfo info) {
        BiomeTileProviders.getInstance().registerFallbacks(world.getRegistryManager().get(RegistryKeys.BIOME));
    }
}
