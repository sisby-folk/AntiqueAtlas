package folk.sisby.antique_atlas.client.gui;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import folk.sisby.antique_atlas.client.AntiqueAtlasTextures;
import folk.sisby.antique_atlas.client.gui.core.GuiComponent;
import folk.sisby.antique_atlas.client.texture.ITexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.Map;


/**
 * A scale bar that displays pixel-to-block ratio. To fit into the overall
 * Atlas style it is rendered at half-scale.
 */
public class GuiScaleBar extends GuiComponent {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 8;

    private static final Map<Double, ITexture> textureMap;

    static {
        Builder<Double, ITexture> builder = ImmutableMap.builder();
        builder.put(0.0625, AntiqueAtlasTextures.SCALEBAR_512);
        builder.put(0.125, AntiqueAtlasTextures.SCALEBAR_256);
        builder.put(0.25, AntiqueAtlasTextures.SCALEBAR_128);
        builder.put(0.5, AntiqueAtlasTextures.SCALEBAR_64);
        builder.put(1.0, AntiqueAtlasTextures.SCALEBAR_32);
        builder.put(2.0, AntiqueAtlasTextures.SCALEBAR_16);
        builder.put(4.0, AntiqueAtlasTextures.SCALEBAR_8);
        builder.put(8.0, AntiqueAtlasTextures.SCALEBAR_4);
        textureMap = builder.build();
    }

    /**
     * Pixel-to-block ratio.
     */
    private double mapScale = 1;

    GuiScaleBar() {
        setSize(WIDTH, HEIGHT);
    }

    void setMapScale(double scale) {
        this.mapScale = scale;
    }

    /**
     * Returns the background texture depending on the scale.
     */
    private ITexture getTexture() {
        return textureMap.get(mapScale);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        ITexture texture = getTexture();
        if (texture == null) return;

        texture.draw(context, getGuiX(), getGuiY());

        if (isMouseOver) {
            drawTooltip(Collections.singletonList(Text.translatable("gui.antique_atlas.scalebar")), MinecraftClient.getInstance().textRenderer);
        }
    }
}