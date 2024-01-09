package folk.sisby.antique_atlas.client.resource.reloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.MarkerType;
import folk.sisby.antique_atlas.client.resource.MarkerTypes;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps marker type to texture.
 *
 * @author Hunternif
 */
public class MarkerTextureConfig extends JsonDataLoader implements IdentifiableResourceReloadListener {
    public static final Identifier ID = AntiqueAtlas.id("markers");
    private static final int VERSION = 1;

    public MarkerTextureConfig() {
        super(new Gson(), "atlas/markers");
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
                AntiqueAtlas.LOG.warn("Error reading marker " + fileId + "!", e);
            }
        }

        outMap.forEach(MarkerTypes::register);
        MarkerTypes.REGISTRY.forEach(MarkerType::initMips);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
