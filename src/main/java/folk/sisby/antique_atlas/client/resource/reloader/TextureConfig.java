package folk.sisby.antique_atlas.client.resource.reloader;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.AntiqueAtlasTextures;
import folk.sisby.antique_atlas.client.texture.ITexture;
import folk.sisby.antique_atlas.client.texture.TileTexture;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;

/**
 * Reads all png files available under assets/(?modid)/textures/gui/tiles/(?tex).png as Textures that
 * are referenced by the TextureSets.
 * <p>
 * Note that each texture is represented by TWO Identifiers:
 * - The identifier of the physical location in modid:texture/gui/tiles/tex.png
 * - The logical identifier modid:tex referenced by TextureSets
 */
public class TextureConfig extends SinglePreparationResourceReloader<Map<Identifier, ITexture>> implements IdentifiableResourceReloadListener {
    public static final Identifier ID = AntiqueAtlas.id("textures");

    @Override
    protected Map<Identifier, ITexture> prepare(ResourceManager manager, Profiler profiler) {
        Map<Identifier, ITexture> outMap = new HashMap<>();

        for (Identifier id : manager.findResources("textures/gui/tiles", id -> id.toString().endsWith(".png")).keySet()) {
            try {
                Identifier texture_id = new Identifier(
                    id.getNamespace(),
                    id.getPath().replace("textures/gui/tiles/", "").replace(".png", "")
                );

                outMap.put(texture_id, new TileTexture(id));
            } catch (InvalidIdentifierException e) {
                AntiqueAtlas.LOG.warn("Failed to read texture!", e);
            }
        }

        return outMap;
    }

    @Override
    protected void apply(Map<Identifier, ITexture> prepared, ResourceManager manager, Profiler profiler) {
        AntiqueAtlasTextures.TILE_TEXTURES_MAP.clear();
        prepared.forEach((key, value) -> {
            AntiqueAtlasTextures.TILE_TEXTURES_MAP.put(key, value);
            if (AntiqueAtlas.CONFIG.Performance.resourcePackLogging) {
                AntiqueAtlas.LOG.info("Loaded texture {} with path {}", key, value.getTexture());
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
