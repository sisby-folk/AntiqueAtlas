package folk.sisby.antique_atlas.client.resource;

import com.mojang.serialization.Lifecycle;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.MarkerType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleDefaultedRegistry;
import net.minecraft.util.Identifier;

public class MarkerTypes {
    private static final RegistryKey<Registry<MarkerType>> KEY = RegistryKey.ofRegistry(AntiqueAtlas.id("marker"));
    public static final SimpleDefaultedRegistry<MarkerType> REGISTRY = new SimpleDefaultedRegistry<>(AntiqueAtlas.id("red_x_small").toString(), KEY, Lifecycle.experimental(), false);

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
