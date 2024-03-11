package folk.sisby.antique_atlas.gui;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.gui.core.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;


/**
 * A scale bar that displays pixel-to-block ratio. To fit into the overall
 * Atlas style it is rendered at half-scale.
 */
public class BarScaleComponent extends Component {
    public static final Identifier SCALEBAR_4 = AntiqueAtlas.id("textures/gui/scalebar/scalebar_4.png");
    public static final Identifier SCALEBAR_8 = AntiqueAtlas.id("textures/gui/scalebar/scalebar_8.png");
    public static final Identifier SCALEBAR_16 = AntiqueAtlas.id("textures/gui/scalebar/scalebar_16.png");
    public static final Identifier SCALEBAR_32 = AntiqueAtlas.id("textures/gui/scalebar/scalebar_32.png");
    public static final Identifier SCALEBAR_64 = AntiqueAtlas.id("textures/gui/scalebar/scalebar_64.png");
    public static final Identifier SCALEBAR_128 = AntiqueAtlas.id("textures/gui/scalebar/scalebar_128.png");
    public static final Identifier SCALEBAR_256 = AntiqueAtlas.id("textures/gui/scalebar/scalebar_256.png");
    public static final Identifier SCALEBAR_512 = AntiqueAtlas.id("textures/gui/scalebar/scalebar_512.png");
    public static final int WIDTH = 20;
    public static final int HEIGHT = 8;

    private static final Map<Double, Identifier> textureMap;

    static {
        Builder<Double, Identifier> builder = ImmutableMap.builder();
        builder.put(0.0625, SCALEBAR_512);
        builder.put(0.125, SCALEBAR_256);
        builder.put(0.25, SCALEBAR_128);
        builder.put(0.5, SCALEBAR_64);
        builder.put(1.0, SCALEBAR_32);
        builder.put(2.0, SCALEBAR_16);
        builder.put(4.0, SCALEBAR_8);
        builder.put(8.0, SCALEBAR_4);
        textureMap = builder.build();
    }

    /**
     * Pixel-to-block ratio.
     */
    private double mapScale = 1;

    BarScaleComponent() {
        setSize(WIDTH, HEIGHT);
    }

    void setMapScale(double scale) {
        this.mapScale = scale;
    }

    /**
     * Returns the background texture depending on the scale.
     */
    private Identifier getTexture() {
        return textureMap.get(mapScale);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        Identifier texture = getTexture();
        if (texture == null) return;

        context.drawTexture(texture, getGuiX(), getGuiY(), 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);

        if (isMouseOver) {
            drawTooltip(Collections.singletonList(Text.translatable("gui.antique_atlas.scalebar")), MinecraftClient.getInstance().textRenderer);
        }
    }
}
