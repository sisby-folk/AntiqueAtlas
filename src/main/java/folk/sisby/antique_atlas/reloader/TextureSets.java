package folk.sisby.antique_atlas.reloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.TextureSet;
import folk.sisby.antique_atlas.TileTexture;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps texture sets to their names.
 *
 * @author Hunternif
 */
public class TextureSets extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final TextureSets INSTANCE = new TextureSets();

    public static final Identifier ID = AntiqueAtlas.id("texture_sets");
    public static final int VERSION = 1;

    public static final Identifier DEFAULT = AntiqueAtlas.id("test");

    public static TextureSets getInstance() {
        return INSTANCE;
    }

    private final Map<Identifier, TextureSet> map = new HashMap<>();

    public TextureSets() {
        super(new Gson(), "atlas/texture_sets");
    }

    public void register(TextureSet set) {
        map.put(set.id, set);
    }

    public TextureSet get(Identifier id) {
        return map.get(id);
    }

    public boolean contains(Identifier id) {
        return map.containsKey(id);
    }

    public TextureSet getDefault() {
        return get(DEFAULT);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, TextureSet> outMap = new HashMap<>();

        Map<Identifier, TileTexture> tileTextures = new HashMap<>();
        manager.findResources("textures/gui/tiles", id -> id.getPath().endsWith(".png")).keySet().forEach(fileId -> {
            Identifier id = new Identifier(fileId.getNamespace(), fileId.getPath().substring("textures/gui/tiles/".length(), fileId.getPath().length() - ".png".length()));
            tileTextures.put(id, new TileTexture(fileId));
        });

        for (Map.Entry<Identifier, JsonElement> fileEntry : prepared.entrySet()) {
            Identifier fileId = fileEntry.getKey();
            try {
                JsonObject fileJson = fileEntry.getValue().getAsJsonObject();

                int version = fileJson.getAsJsonPrimitive("version").getAsInt();
                if (version != VERSION) {
                    throw new RuntimeException("The TextureSet " + fileId + " is in the wrong version!");
                }

                JsonObject data = fileJson.getAsJsonObject("data");

                List<Identifier> textures = new ArrayList<>();

                for (Map.Entry<String, JsonElement> entry : data.getAsJsonObject("textures").entrySet()) {
                    for (int i = 0; i < entry.getValue().getAsInt(); i++) {
                        textures.add(new Identifier(entry.getKey()));
                    }
                }

                Identifier[] textureArray = new Identifier[textures.size()];
                TextureSet set;

                if (!data.has("shore")) {
                    set = new TextureSet(fileId, textures.toArray(textureArray));
                } else {
                    JsonObject shore = data.getAsJsonObject("shore");

                    if (!shore.has("water")) {
                        throw new RuntimeException("The `shore` entry is missing a water entry.");
                    }

                    set = new TextureSet.TextureSetShore(fileId, new Identifier(shore.get("water").getAsString()), textures.toArray(textureArray));
                }

                if (data.has("stitch")) {
                    data.getAsJsonObject("stitch").entrySet().forEach(entry -> {
                        String to = entry.getValue().getAsString();

                        switch (to) {
                            case "both":
                                set.stitchTo(new Identifier(entry.getKey()));
                                break;
                            case "horizontal":
                                set.stitchToHorizontal(new Identifier(entry.getKey()));
                                break;
                            case "vertical":
                                set.stitchToVertical(new Identifier(entry.getKey()));
                                break;
                            default:
                                throw new RuntimeException("Invalid stitch value (" + to + ") for `" + entry.getKey() + "`");
                        }
                    });
                }
                outMap.put(fileId, set);
            } catch (Exception e) {
                AntiqueAtlas.LOGGER.warn("Error reading texture set " + fileId + "!", e);
            }
        }

        outMap.forEach((id, set) -> {
            try {
                set.loadTextures(tileTextures);
                register(set);
                if (AntiqueAtlas.CONFIG.Performance.resourcePackLogging) {
                    AntiqueAtlas.LOGGER.info("Loaded texture set {} with {} custom texture(s)", id, set.getTexturePaths().length);
                }
            } catch (Throwable e) {
                AntiqueAtlas.LOGGER.error("Failed to load the texture set `{}`:", id, e);
            }
        });

        outMap.forEach((id, set) -> {
            set.checkStitching();
            if (set instanceof TextureSet.TextureSetShore texture) {
                texture.loadWater();
                if (AntiqueAtlas.CONFIG.Performance.resourcePackLogging) {
                    AntiqueAtlas.LOGGER.info("Loaded water texture `{}` for shore texture `{}` texture", texture.waterName, texture.id);
                }
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
