package folk.sisby.antique_atlas.client.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/**
 * An abstract base class, which implements the ITexture interface using
 * the DrawHelper.drawTexture method provided by minecraft code.
 */
public abstract class ATexture implements ITexture {
    final Identifier texture;
    final boolean autobind;

    public ATexture(Identifier texture) {
        this(texture, true);
    }

    public ATexture(Identifier texture, boolean autobind) {
        this.texture = texture;
        this.autobind = autobind;
    }

    @Override
    public Identifier getTexture() {
        return texture;
    }

    public void bind() {
        RenderSystem.setShaderTexture(0, texture);
    }

    @Override
    public void draw(DrawContext context, int x, int y) {
        draw(context, x, y, width(), height());
    }

    @Override
    public void draw(DrawContext context, int x, int y, int width, int height) {
        draw(context, x, y, width, height, 0, 0, this.width(), this.height());
    }

    @Override
    public void draw(DrawContext context, int x, int y, int u, int v, int regionWidth, int regionHeight) {
        draw(context, x, y, regionWidth, regionHeight, u, v, regionWidth, regionHeight);
    }

    @Override
    public void draw(DrawContext context, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
        if (autobind) {
            bind();
        }
        context.drawTexture(getTexture(), x, y, width, height, u, v, regionWidth, regionHeight, this.width(), this.height());
    }

    @Override
    public void drawCenteredWithRotation(DrawContext context, int x, int y, int width, int height, float rotation) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180 + rotation));
        context.getMatrices().translate(-width / 2f, -height / 2f, 0f);

        draw(context, 0, 0, width, height);

        context.getMatrices().pop();
    }
}
