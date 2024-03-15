package folk.sisby.antique_atlas.client.assets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Lifecycle;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.MarkerType;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.RegistryKey;

import java.util.HashMap;
import java.util.Map;

public class MarkerTypes extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final MarkerTypes INSTANCE = new MarkerTypes();

    public static final Identifier ID = AntiqueAtlas.id("markers");
    public static final int VERSION = 1;

    public static MarkerTypes getInstance() {
        return INSTANCE;
    }

    private final DefaultedRegistry<MarkerType> registry = new DefaultedRegistry<>(AntiqueAtlas.id("red_x_small").toString(), RegistryKey.ofRegistry(AntiqueAtlas.id("marker")), Lifecycle.experimental(), null);

    public MarkerTypes() {
        super(new Gson(), "atlas/markers");
    }

    public MarkerType get(Identifier id) {
        return registry.get(id);
    }

    public Identifier getId(MarkerType type) {
        return registry.getId(type);
    }

    public MarkerType getDefault() {
        return registry.get(registry.getDefaultId());
    }

    public IndexedIterable<MarkerType> iterator() {
        return registry;
    }

    private void register(Identifier location, MarkerType type) {
        if (registry.containsId(location)) {
            int id = registry.getRawId(registry.get(location));
            registry.set(id, RegistryKey.of(registry.getKey(), location), type, Lifecycle.stable());
        } else {
            registry.add(RegistryKey.of(registry.getKey(), location), type, Lifecycle.stable());
        }
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, MarkerType> outMap = new HashMap<>();
        for (Map.Entry<Identifier, JsonElement> fileEntry : prepared.entrySet()) {
            Identifier fileId = fileEntry.getKey();
            try {
                JsonObject fileJson = fileEntry.getValue().getAsJsonObject();

                int version = fileJson.getAsJsonPrimitive("version").getAsInt();

                if (version != VERSION) {
                    throw new RuntimeException("Incompatible version (" + VERSION + " != " + version + ")");
                }

                MarkerType markerType = new MarkerType(fileId);
                markerType.getJSONData().readFrom(fileJson);
                outMap.put(fileId, markerType);
            } catch (Exception e) {
                AntiqueAtlas.LOGGER.warn("Error reading marker " + fileId + "!", e);
            }
        }

        outMap.forEach(this::register);
        registry.forEach(MarkerType::initMips);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
