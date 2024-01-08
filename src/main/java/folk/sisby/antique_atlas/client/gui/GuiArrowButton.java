package folk.sisby.antique_atlas.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.client.AntiqueAtlasTextures;
import folk.sisby.antique_atlas.client.gui.core.GuiComponentButton;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.opengl.GL11;

public class GuiArrowButton extends GuiComponentButton {
    private static final int WIDTH = 12;
    private static final int HEIGHT = 12;

    public enum ArrowDirection {
        UP(12, 0),
        DOWN(12, 12),
        LEFT(0, 0),
        RIGHT(0, 12);

        public final int u;
        public final int v;

        ArrowDirection(int u, int v) {
            this.u = u;
            this.v = v;
        }
    }

    private final ArrowDirection direction;

    private GuiArrowButton(ArrowDirection direction) {
        setSize(WIDTH, HEIGHT);
        this.direction = direction;
    }

    static GuiArrowButton up() {
        return new GuiArrowButton(ArrowDirection.UP);
    }

    static GuiArrowButton down() {
        return new GuiArrowButton(ArrowDirection.DOWN);
    }

    static GuiArrowButton left() {
        return new GuiArrowButton(ArrowDirection.LEFT);
    }

    static GuiArrowButton right() {
        return new GuiArrowButton(ArrowDirection.RIGHT);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int x = getGuiX(), y = getGuiY();
        if (isMouseOver) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            // Fade out when the mouse is far from them:
            int distanceSq = (mouseX - x - getWidth() / 2) * (mouseX - x - getWidth() / 2) +
                (mouseY - y - getHeight() / 2) * (mouseY - y - getHeight() / 2);
            double alpha = distanceSq < 400 ? 0.5 : Math.pow(distanceSq, -0.28);
            RenderSystem.setShaderColor(1, 1, 1, (float) alpha);
        }

        AntiqueAtlasTextures.BTN_ARROWS.draw(context, x, y, direction.u, direction.v, WIDTH, HEIGHT);

        RenderSystem.disableBlend();
    }
}
