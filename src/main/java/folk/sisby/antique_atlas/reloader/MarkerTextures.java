package folk.sisby.antique_atlas.reloader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.util.CodecUtil;
import folk.sisby.surveyor.landmark.LandmarkType;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MarkerTextures extends SinglePreparationResourceReloader<Map<Identifier, MarkerTextures.MarkerTextureMeta>> implements IdentifiableResourceReloadListener {
    private static final MarkerTextures INSTANCE = new MarkerTextures();
    public static final Identifier ID = AntiqueAtlas.id("marker_textures");

    public static MarkerTextures getInstance() {
        return INSTANCE;
    }

    private final Map<Identifier, MarkerTexture> textures = new HashMap<>();

    public MarkerTexture get(Identifier id) {
        return textures.get(id);
    }

    public MarkerTexture getOrDefault(Identifier id) {
        return getOrDefault(id, MarkerTexture.DEFAULT);
    }

    public MarkerTexture getOrDefault(Identifier id, MarkerTexture defaultTexture) {
        return textures.getOrDefault(id, defaultTexture);
    }

    public MarkerTexture getLandmarkType(LandmarkType<?> type) {
        return getOrDefault(new Identifier(type.id().getNamespace(), "landmark/type/" + type.id().getPath()));
    }

    public MarkerTexture getLandmarkType(LandmarkType<?> type, String variant) {
        return getOrDefault(new Identifier(type.id().getNamespace(), "landmark/type/" + type.id().getPath() + (variant == null ? "" : "/" + variant)));
    }

    public Map<Identifier, MarkerTexture> asMap() {
        return new HashMap<>(textures);
    }

    @Override
    protected Map<Identifier, MarkerTextureMeta> prepare(ResourceManager manager, Profiler profiler) {
        Map<Identifier, MarkerTextures.MarkerTextureMeta> textureMeta = new HashMap<>();
        for (Map.Entry<Identifier, Resource> e : manager.findResources("textures/atlas/marker", id -> id.getPath().endsWith(".png")).entrySet()) {
            Identifier id = new Identifier(e.getKey().getNamespace(), e.getKey().getPath().substring("textures/atlas/marker/".length(), e.getKey().getPath().length() - ".png".length()));
            try {
                ResourceMetadata metadata = e.getValue().getMetadata();
                textureMeta.put(id, metadata.decode(MarkerTextures.MarkerTextureMeta.METADATA).orElse(MarkerTextureMeta.DEFAULT));
            } catch (IOException ex) {
                AntiqueAtlas.LOGGER.error("[Antique Atlas] Failed to access marker texture metadata for {}", e.getKey(), ex);
                textureMeta.put(id, MarkerTextures.MarkerTextureMeta.DEFAULT);
            }
        }
        return textureMeta;
    }

    @Override
    protected void apply(Map<Identifier, MarkerTextureMeta> prepared, ResourceManager manager, Profiler profiler) {
        AntiqueAtlas.LOGGER.info("[Antique Atlas] Reloading Marker Textures...");
        textures.clear();
        prepared.forEach((id, meta) -> textures.put(id, meta.build(id)));
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public record MarkerTextureMeta(Optional<Integer> textureWidth, Optional<Integer> textureHeight, Optional<Integer> mipLevels, Optional<Integer> offsetX, Optional<Integer> offsetY) {
        public static final MarkerTextureMeta DEFAULT = new MarkerTextureMeta(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        public static final Codec<MarkerTextureMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("textureWidth").forGetter(MarkerTextureMeta::textureWidth),
            Codec.INT.optionalFieldOf("textureHeight").forGetter(MarkerTextureMeta::textureHeight),
            Codec.INT.optionalFieldOf("mipLevels").forGetter(MarkerTextureMeta::mipLevels),
            Codec.INT.optionalFieldOf("offsetX").forGetter(MarkerTextureMeta::offsetX),
            Codec.INT.optionalFieldOf("offsetY").forGetter(MarkerTextureMeta::offsetY)
        ).apply(instance, MarkerTextureMeta::new));

        public static final ResourceMetadataReader<MarkerTextureMeta> METADATA = new CodecUtil.CodecResourceMetadataSerializer<>(CODEC, AntiqueAtlas.id("marker"));

        public MarkerTexture build(Identifier id) {
            int textureWidth = this.textureWidth.orElse(32);
            int textureHeight = this.textureHeight.orElse(32);
            int mipLevels = this.mipLevels.orElse(0);
            int offsetX = this.offsetX.orElse(-textureWidth / 2);
            int offsetY = this.offsetY.orElse(-textureHeight / 2);
            return MarkerTexture.ofId(id, offsetX, offsetY, textureWidth, textureHeight, mipLevels);
        }
    }
}
