package folk.sisby.antique_atlas.client.gui.core;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.client.texture.ITexture;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

/**
 * Displays a texture that changes alpha at regular intervals.
 * By default the texture file is assumed to be full image, but that behavior
 * can be altered by overriding the method {@link #drawImage(MatrixStack)}.
 *
 * @author Hunternif
 */
public class GuiBlinkingImage extends GuiComponent {
    private ITexture texture;
    /**
     * The number of milliseconds the icon spends visible or invisible.
     */
    private final long blinkTime;
    private final float visibleAlpha;
    private final float invisibleAlpha;

    private long lastTickTime;
    /**
     * The flag that switches value every "blink".
     */
    private boolean isVisible;

    public GuiBlinkingImage(long blinkTime, float visibleAlpha, float invisibleAlpha) {
        this.blinkTime = blinkTime;
        this.visibleAlpha = visibleAlpha;
        this.invisibleAlpha = invisibleAlpha;
    }

    public GuiBlinkingImage() {
        this(500, 1, 0.25f);
    }

    public void setTexture(ITexture texture, int width, int height) {
        this.texture = texture;
        setSize(width, height);
        // Set up the timer so that the image appears visible at the first moment:
        lastTickTime = 0;
        isVisible = false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTick) {
        long currentTime = System.currentTimeMillis();
        if (lastTickTime + blinkTime < currentTime) {
            lastTickTime = currentTime;
            isVisible = !isVisible;
        }
        RenderSystem.setShaderColor(1, 1, 1, isVisible ? visibleAlpha : invisibleAlpha);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        drawImage(matrices);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private void drawImage(MatrixStack matrices) {
        texture.draw(matrices, getGuiX(), getGuiY(), getWidth(), getHeight());
    }
}
