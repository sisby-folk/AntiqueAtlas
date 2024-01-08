package folk.sisby.antique_atlas.client.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

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

    public Identifier getTexture() {
        return texture;
    }

    public void bind() {
        RenderSystem.setShaderTexture(0, texture);
    }

    public void draw(MatrixStack matrices, int x, int y) {
        draw(matrices, x, y, width(), height());
    }

    public void draw(MatrixStack matrices, int x, int y, int width, int height) {
        draw(matrices, x, y, width, height, 0, 0, this.width(), this.height());
    }

    public void draw(MatrixStack matrices, int x, int y, int u, int v, int regionWidth, int regionHeight) {
        draw(matrices, x, y, regionWidth, regionHeight, u, v, regionWidth, regionHeight);
    }

    public void draw(MatrixStack matrices, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
        if (autobind) {
            bind();
        }
        DrawableHelper.drawTexture(matrices, x, y, width, height, u, v, regionWidth, regionHeight, this.width(), this.height());
    }

    public void drawCenteredWithRotation(MatrixStack matrices, int x, int y, int width, int height, float rotation) {
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180 + rotation));
        matrices.translate(-width / 2f, -height / 2f, 0f);

        draw(matrices, 0, 0, width, height);

        matrices.pop();
    }
}
