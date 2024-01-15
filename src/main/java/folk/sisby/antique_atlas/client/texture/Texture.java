package folk.sisby.antique_atlas.client.texture;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class Texture implements Drawable {
    protected final Identifier texture;
    protected final int width;
    protected final int height;

    public Texture(Identifier texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    public Identifier getTexture() {
        return texture;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void draw(DrawContext context, int x, int y) {
        draw(context, x, y, width(), height());
    }

    @Override
    public void draw(DrawContext context, int x, int y, int width, int height) {
        draw(context, x, y, width, height, 0, 0, width(), height());
    }

    @Override
    public void draw(DrawContext context, int x, int y, int u, int v, int textureWidth, int textureHeight) {
        draw(context, x, y, textureWidth, textureHeight, u, v, textureWidth, textureHeight);
    }

    @Override
    public void draw(DrawContext context, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
        context.drawTexture(getTexture(), x, y, width, height, u, v, regionWidth, regionHeight, width(), height());
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
