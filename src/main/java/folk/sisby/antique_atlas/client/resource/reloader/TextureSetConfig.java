package folk.sisby.antique_atlas.client.resource.reloader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.TextureSet;
import folk.sisby.antique_atlas.client.resource.TextureSets;
import folk.sisby.antique_atlas.resource.reloader.ResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Saves texture set names with the lists of texture variations.
 */
public class TextureSetConfig implements ResourceReloadListener<Collection<TextureSet>> {
    public static final Identifier ID = AntiqueAtlas.id("texture_sets");
    private static final int VERSION = 1;
    private static final JsonParser PARSER = new JsonParser();

    @Override
    public CompletableFuture<Collection<TextureSet>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, TextureSet> sets = new HashMap<>();

            try {
                for (Identifier id : manager.findResources("atlas/texture_sets", id -> id.toString().endsWith(".json")).keySet()) {
                    Identifier texture_id = new Identifier(
                        id.getNamespace(),
                        id.getPath().replace("atlas/texture_sets/", "").replace(".json", "")
                    );

                    try {
                        Resource resource = manager.getResource(id).orElseThrow(IOException::new);
                        try (
                            InputStream stream = resource.getInputStream();
                            InputStreamReader reader = new InputStreamReader(stream)
                        ) {
                            JsonObject object = PARSER.parse(reader).getAsJsonObject();

                            int version = object.getAsJsonPrimitive("version").getAsInt();
                            if (version != VERSION) {
                                AntiqueAtlas.LOG.warn("The TextureSet " + texture_id + " is in the wrong version! Skipping.");
                                continue;
                            }

                            JsonObject data = object.getAsJsonObject("data");

                            List<Identifier> textures = new ArrayList<>();

                            for (Entry<String, JsonElement> entry : data.getAsJsonObject("textures").entrySet()) {
                                for (int i = 0; i < entry.getValue().getAsInt(); i++) {
                                    textures.add(new Identifier(entry.getKey()));
                                }
                            }

                            Identifier[] textureArray = new Identifier[textures.size()];
                            TextureSet set;

                            if (!data.has("shore")) {
                                set = new TextureSet(texture_id, textures.toArray(textureArray));
                            } else {
                                JsonObject shore = data.getAsJsonObject("shore");

                                if (!shore.has("water")) {
                                    throw new RuntimeException("The `shore` entry is missing a water entry.");
                                }

                                set = new TextureSet.TextureSetShore(texture_id, new Identifier(shore.get("water").getAsString()), textures.toArray(textureArray));
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


                            sets.put(texture_id, set);
                        }
                    } catch (Exception e) {
                        AntiqueAtlas.LOG.warn("Error reading TextureSet " + texture_id + "!", e);
                    }
                }
            } catch (Throwable e) {
                AntiqueAtlas.LOG.warn("Failed to read texture sets!", e);
            }

            return sets.values();
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(Collection<TextureSet> sets, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            for (TextureSet set : sets) {
                try {
                    set.loadTextures();
                    TextureSets.getInstance().register(set);
                    if (AntiqueAtlas.CONFIG.Performance.resourcePackLogging)
                        AntiqueAtlas.LOG.info("Loaded texture set {} with {} custom texture(s)", set.name, set.getTexturePaths().length);
                } catch (Throwable e) {
                    AntiqueAtlas.LOG.error("Failed to load the texture set `{}`:", set.name, e);
                }

            }

            for (TextureSet set : sets) {
                set.checkStitching();

                if (set instanceof TextureSet.TextureSetShore texture) {
                    texture.loadWater();
                    if (AntiqueAtlas.CONFIG.Performance.resourcePackLogging)
                        AntiqueAtlas.LOG.info("Loaded water texture `{}` for shore texture `{}` texture", texture.waterName, texture.name);
                }
            }
        }, executor);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Collection<Identifier> getDependencies() {
        return Collections.singleton(TextureConfig.ID);
    }
}
