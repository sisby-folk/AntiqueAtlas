package folk.sisby.antique_atlas.mixin;

import folk.sisby.antique_atlas.core.PlayerEventHandler;
import folk.sisby.antique_atlas.core.watcher.DeathWatcher;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {
    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo info) {
        DeathWatcher.onPlayerDeath((PlayerEntity) (Object) this);
    }

    @Inject(at = @At("RETURN"), method = "tick")
    public void tick(CallbackInfo info) {
        PlayerEventHandler.onPlayerTick((PlayerEntity) (Object) this);
    }
}