package folk.sisby.antique_atlas.client.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Lifecycle;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.texture.ITexture;
import folk.sisby.antique_atlas.client.texture.Texture;
import folk.sisby.antique_atlas.util.BitMatrix;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleDefaultedRegistry;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MarkerType {
    public static final RegistryKey<Registry<MarkerType>> KEY = RegistryKey.ofRegistry(AntiqueAtlas.id("marker"));
    public static final SimpleDefaultedRegistry<MarkerType> REGISTRY = new SimpleDefaultedRegistry<>(AntiqueAtlas.id("red_x_small").toString(),
            KEY,
            Lifecycle.experimental(),
            false);

    private Identifier[] icons;
    private BitMatrix[] iconPixels;
    private int[] iconSizes = null;

    private int viewSize = 2;
    private int clipMin = -1000;
    private int clipMax = 1000;

    private boolean alwaysShow = false;
    private boolean isTile = false;
    private boolean isTechnical = false;

    private double centerX = 0.5;
    private double centerY = 0.5;

    private final JSONData data = new JSONData(this);

    public MarkerType(Identifier... icons) {
        this.icons = icons;
    }

    public static void register(Identifier location, MarkerType type) {
        type.initMips();
        if (REGISTRY.containsId(location)) {
            int id = REGISTRY.getRawId(REGISTRY.get(location));
            REGISTRY.set(id, RegistryKey.of(KEY, location), type, Lifecycle.stable());
        } else {
            REGISTRY.add(RegistryKey.of(KEY, location), type, Lifecycle.stable());
        }
    }

    public boolean isTechnical() {
        return isTechnical;
    }

    /**
     * Whether the marker should be hidden
     */
    public boolean shouldHide(boolean isHidingMarkers, int scaleIndex) {
        return shouldClip(scaleIndex) || (!alwaysShow && isHidingMarkers);
    }

    /**
     * Whether the marker should hide due to the scale clipping
     */
    private boolean shouldClip(int scaleIndex) {
        return !(scaleIndex >= clipMin && scaleIndex <= clipMax);
    }

    /**
     * If the cursor is currently hovering over the marker
     *
     * @param x The X position in the marker (0-1 is the bounding box of the
     *          render, though it may be outside that range)
     * @param y The Y position in the marker (0-1 is the bounding box of the
     *          render, though it may be outside that range)
     */
    public boolean shouldHover(double x, double y) {
        if (isTechnical() || x > 1 || x < 0 || y > 1 || y < 0)
            return false;
        if (iconPixels == null || iconPixels.length == 0 || iconIndex < 0)
            return true;
        int iconX = (int) (iconPixels[iconIndex].getWidth() * x);
        int iconY = (int) (iconPixels[iconIndex].getHeight() * y);

        return iconPixels[iconIndex].get(iconX, iconY);
    }

    /**
     * The size of the icon, in chunks
     */
    private int viewSize() {
        return viewSize;
    }

    /**
     * Whether the marker is a tile, and as such should scale with the map
     */
    private boolean isTile() {
        return isTile;
    }

    /**
     * The X position (0-1) of the icon that should be at the marker location
     */
    private double getCenterX() {
        return centerX;
    }

    /**
     * The Y position (0-1) of the icon that should be at the marker location
     */
    private double getCenterY() {
        return centerY;
    }

    /**
     * Get the icon for the marker
     */
    public Identifier getIcon() {
        return icons.length == 0 || iconIndex < 0 ? TextureManager.MISSING_IDENTIFIER : icons[iconIndex];
    }

    public ITexture getTexture() {
        if (icons.length == 0 || iconIndex < 0) return null;
        return new Texture(getIcon(), iconSizes[iconIndex], iconSizes[iconIndex]);
    }

    private int iconIndex = 0;

    public void calculateMip(double scale, double mapScale) {
        int size = (int) (16 * scale * viewSize());
        if (isTile) {
            size *= (int) mapScale;
        }

        if (icons.length > 1) {
            int closestValue = Integer.MAX_VALUE;
            int closestIndex = -1;
            for (int i = 0; i < iconSizes.length; i++) {
                if (iconSizes[i] < closestValue && iconSizes[i] >= size) {
                    closestValue = iconSizes[i];
                    closestIndex = i;
                }
            }
            if (closestIndex > 0) {
                iconIndex = closestIndex;
            }
        }
    }

    public void resetMip() {
        iconIndex = 0;
    }

    public MarkerRenderInfo getRenderInfo(double scale, double mapScale) {
        boolean isTile = isTile();

        int size = (int) (16 * scale * viewSize());
        if (isTile) {
            size *= (int) mapScale;
        }
        int x = -(int) (size * getCenterX());
        int y = -(int) (size * getCenterY());

        return new MarkerRenderInfo(getTexture(), x, y, size, size);
    }

    public void initMips() {
        iconSizes = new int[icons.length];
        iconPixels = new BitMatrix[icons.length];
        int ALPHA_THRESHOLD = 8;
        for (int i = 0; i < icons.length; i++) {
            iconSizes[i] = -1;
            if (icons[i] == null) {
                AntiqueAtlas.LOG.warn("Marker {} -- Texture location is null at index {}!", MarkerType.REGISTRY.getId(this).toString(), i);
            }

            NativeImage bufferedimage = null;

            try {
                Resource iresource = MinecraftClient.getInstance().getResourceManager().getResource(icons[i]).orElseThrow(IOException::new);
                bufferedimage = NativeImage.read(iresource.getInputStream());
                iconSizes[i] = Math.min(bufferedimage.getWidth(), bufferedimage.getHeight());
                BitMatrix matrix = new BitMatrix(bufferedimage.getWidth(), bufferedimage.getHeight(), false);

                for (int x = 0; x < bufferedimage.getWidth(); x++) {
                    for (int y = 0; y < bufferedimage.getHeight(); y++) {

                        int color = bufferedimage.getColor(x, y);
                        int alpha = (color >> 24) & 0xff;

                        if (alpha >= ALPHA_THRESHOLD) {
                            matrix.set(x, y, true);

                            // sides
                            matrix.set(x - 1, y, true);
                            matrix.set(x + 1, y, true);
                            matrix.set(x, y - 1, true);
                            matrix.set(x, y + 1, true);

                            // corners
                            matrix.set(x + 1, y + 1, true);
                            matrix.set(x - 1, y - 1, true);
                            matrix.set(x + 1, y - 1, true);
                            matrix.set(x - 1, y + 1, true);
                        }
                    }
                }

                iconPixels[i] = matrix;
            } catch (IOException e) {
                AntiqueAtlas.LOG.warn("Marker {} -- Error getting texture size data for index {} - {}",
                    MarkerType.REGISTRY.getId(this).toString(), i, icons[i].toString(), e);
            } finally {
                if (bufferedimage != null) {
                    bufferedimage.close();
                }
            }
        }
    }

    public JSONData getJSONData() {
        return data;
    }

    public static class JSONData {
        static final String
            ICONS = "textures",
            SIZE = "size",
            CLIP_MIN = "clipMin",
            CLIP_MAX = "clipMax",
            ALWAYS_SHOW = "alwaysShow",
            IS_TILE = "isTile",
            IS_TECH = "isTechnical",
            CENTER_X = "centerX",
            CENTER_Y = "centerY",

        NONE = "NONE";


        private final MarkerType type;

        Identifier[] icons;
        Integer viewSize = null, clipMin = null, clipMax = null;
        Boolean alwaysShow = null, isTile = null, isTechnical = null;
        Double centerX = null, centerY = null;

        JSONData(MarkerType type) {
            this.type = type;
        }

        public void readFrom(JsonObject object) {
            if (object.entrySet().isEmpty())
                return;

            Identifier typeName = MarkerType.REGISTRY.getId(type);
            String workingOn = NONE;
            try {
                if (object.has(ICONS) && object.get(ICONS).isJsonArray()) {
                    workingOn = ICONS;
                    List<Identifier> list = new ArrayList<>();
                    int i = 0;
                    for (JsonElement elem : object.get(ICONS).getAsJsonArray()) {
                        if (elem.isJsonPrimitive()) {
                            list.add(AntiqueAtlas.id(elem.getAsString()));
                        } else {
                            AntiqueAtlas.LOG.warn("Loading marker {} from JSON: Texture item {} isn't a primitive", typeName, i);
                        }
                        i++;
                    }
                    icons = list.toArray(new Identifier[0]);
                    workingOn = NONE;
                }

                if (object.has(SIZE) && object.get(SIZE).isJsonPrimitive()) {
                    workingOn = SIZE;
                    viewSize = object.get(SIZE).getAsInt();
                    workingOn = NONE;
                }

                if (object.has(CLIP_MIN) && object.get(CLIP_MIN).isJsonPrimitive()) {
                    workingOn = CLIP_MIN;
                    clipMin = object.get(CLIP_MIN).getAsInt();
                    workingOn = NONE;
                }

                if (object.has(CLIP_MAX) && object.get(CLIP_MAX).isJsonPrimitive()) {
                    workingOn = CLIP_MAX;
                    clipMax = object.get(CLIP_MAX).getAsInt();
                    workingOn = NONE;
                }

                if (object.has(ALWAYS_SHOW) && object.get(ALWAYS_SHOW).isJsonPrimitive()) {
                    workingOn = ALWAYS_SHOW;
                    alwaysShow = object.get(ALWAYS_SHOW).getAsBoolean();
                    workingOn = NONE;
                }

                if (object.has(IS_TILE) && object.get(IS_TILE).isJsonPrimitive()) {
                    workingOn = IS_TILE;
                    isTile = object.get(IS_TILE).getAsBoolean();
                    workingOn = NONE;
                }

                if (object.has(IS_TECH) && object.get(IS_TECH).isJsonPrimitive()) {
                    workingOn = IS_TECH;
                    isTechnical = object.get(IS_TECH).getAsBoolean();
                    workingOn = NONE;
                }

                if (object.has(CENTER_X) && object.get(CENTER_X).isJsonPrimitive()) {
                    workingOn = CENTER_X;
                    centerX = object.get(CENTER_X).getAsDouble();
                    workingOn = NONE;
                }

                if (object.has(CENTER_Y) && object.get(CENTER_Y).isJsonPrimitive()) {
                    workingOn = CENTER_Y;
                    centerY = object.get(CENTER_Y).getAsDouble();
                    workingOn = NONE;
                }
            } catch (ClassCastException e) {
                AntiqueAtlas.LOG.warn("Loading marker {} from JSON: Parsing element {}: element was wrong type!", typeName, workingOn, e);
            } catch (NumberFormatException e) {
                AntiqueAtlas.LOG.warn("Loading marker {} from JSON: Parsing element {}: element was an invalid number!", typeName, workingOn, e);
            }

            if (icons != null)
                type.icons = icons;

            if (viewSize != null)
                type.viewSize = viewSize;
            if (clipMin != null)
                type.clipMin = clipMin;
            if (clipMax != null)
                type.clipMax = clipMax;

            if (alwaysShow != null)
                type.alwaysShow = alwaysShow;
            if (isTile != null)
                type.isTile = isTile;
            if (isTechnical != null)
                type.isTechnical = isTechnical;

            if (centerX != null)
                type.centerX = centerX;
            if (centerY != null)
                type.centerY = centerY;
        }

    }
}
