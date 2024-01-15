package folk.sisby.antique_atlas.client.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

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
    public void draw(MatrixStack matrices, int x, int y) {
        draw(matrices, x, y, width(), height());
    }

    @Override
    public void draw(MatrixStack matrices, int x, int y, int width, int height) {
        draw(matrices, x, y, width, height, 0, 0, width(), height());
    }

    @Override
    public void draw(MatrixStack matrices, int x, int y, int u, int v, int textureWidth, int textureHeight) {
        draw(matrices, x, y, textureWidth, textureHeight, u, v, textureWidth, textureHeight);
    }

    @Override
    public void draw(MatrixStack matrices, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
        RenderSystem.setShaderTexture(0, texture);
        DrawableHelper.drawTexture(matrices, x, y, width, height, u, v, regionWidth, regionHeight, width(), height());
    }

    @Override
    public void drawCenteredWithRotation(MatrixStack matrices, int x, int y, int width, int height, float rotation) {
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180 + rotation));
        matrices.translate(-width / 2f, -height / 2f, 0f);

        draw(matrices, 0, 0, width, height);

        matrices.pop();
    }
}
