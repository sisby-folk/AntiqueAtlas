package folk.sisby.antique_atlas.client.resource;

import com.mojang.serialization.Lifecycle;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.MarkerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class MarkerTypes {
    public static final RegistryKey<Registry<MarkerType>> KEY = RegistryKey.ofRegistry(AntiqueAtlas.id("marker"));
    public static final DefaultedRegistry<MarkerType> REGISTRY = new DefaultedRegistry<>(AntiqueAtlas.id("red_x_small").toString(), KEY, Lifecycle.experimental(), null);

    public static void register(Identifier location, MarkerType type) {
        type.initMips();
        if (REGISTRY.containsId(location)) {
            int id = REGISTRY.getRawId(REGISTRY.get(location));
            REGISTRY.set(id, RegistryKey.of(KEY, location), type, Lifecycle.stable());
        } else {
            REGISTRY.add(RegistryKey.of(KEY, location), type, Lifecycle.stable());
        }
    }
}
