package folk.sisby.antique_atlas.mixin;

import folk.sisby.antique_atlas.mixinhooks.EntityHooksAA;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class MixinEntity implements EntityHooksAA {
    @Shadow
    protected boolean inNetherPortal;

    @Override
    public boolean antiqueAtlas_isInPortal() {
        return inNetherPortal;
    }
}
