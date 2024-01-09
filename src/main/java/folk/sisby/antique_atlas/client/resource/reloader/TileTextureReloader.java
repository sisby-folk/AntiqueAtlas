package folk.sisby.antique_atlas.client.resource.reloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.TextureSet;
import folk.sisby.antique_atlas.client.resource.TextureSets;
import folk.sisby.antique_atlas.client.resource.TileTextures;
import folk.sisby.antique_atlas.core.scanning.TileHeightType;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client-only config mapping biome IDs to texture sets.
 * <p>Must be loaded after {@link TextureSetConfig}!</p>
 *
 * @author Hunternif
 */
public class TileTextureReloader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    public static final Identifier ID = AntiqueAtlas.id("tile_textures");
    private static final int VERSION = 2;

    public TileTextureReloader() {
        super(new Gson(), "atlas/tiles");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, Identifier> outMap = new HashMap<>();
        for (Map.Entry<Identifier, JsonElement> fileEntry : prepared.entrySet()) {
            Identifier fileId = fileEntry.getKey();
            try {
                JsonObject fileJson = fileEntry.getValue().getAsJsonObject();

                int version = fileJson.getAsJsonPrimitive("version").getAsInt();
                if (version == 1) {
                    Identifier texture_set = new Identifier(fileJson.get("texture_set").getAsString());

                    outMap.put(fileId, texture_set);

                    for (TileHeightType layer : TileHeightType.values()) {
                        outMap.put(Identifier.tryParse(fileId + "_" + layer.getName()), texture_set);
                    }
                } else if (version == VERSION) {
                    Identifier default_entry = TileTextures.DEFAULT_TEXTURE;

                    try {
                        default_entry = new Identifier(fileJson.getAsJsonObject("texture_sets").get("default").getAsString());
                    } catch (Exception ignored) {
                    }

                    // insert the old-style texture set with the default one
                    outMap.put(fileId, default_entry);

                    for (TileHeightType layer : TileHeightType.values()) {
                        Identifier texture_set = default_entry;

                        try {
                            texture_set = new Identifier(fileJson.getAsJsonObject("texture_sets").get(layer.getName()).getAsString());
                        } catch (Exception ignored) {
                        }

                        outMap.put(Identifier.tryParse(fileId + "_" + layer), texture_set);
                    }
                } else {
                    throw new RuntimeException("Incompatible version (" + VERSION + " != " + version + ")");
                }
            } catch (Exception e) {
                AntiqueAtlas.LOG.warn("Error reading biome tile " + fileId + "!", e);
            }
        }

        outMap.forEach((id, setName) -> {
            TextureSet set = TextureSets.getInstance().getByName(setName);

            if (set == null) {
                AntiqueAtlas.LOG.error("Missing texture set `{}` for tile `{}`. Using default.", setName, id);
                set = TileTextures.getInstance().getDefaultTexture();
            }

            TileTextures.getInstance().setTexture(id, set);
            if (AntiqueAtlas.CONFIG.Performance.resourcePackLogging) {
                AntiqueAtlas.LOG.info("Loaded tile {} with texture set {}", id, set.name);
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Collections.singleton(TextureSetConfig.ID);
    }
}
