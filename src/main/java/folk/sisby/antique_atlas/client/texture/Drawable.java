package folk.sisby.antique_atlas.client.texture;

import net.minecraft.client.util.math.MatrixStack;

/**
 * A generic interface for textures. The texture know their own width and height.
 * All method parameters are provided in pixels.
 */
public interface Drawable {
    int width();

    int height();

    void draw(MatrixStack matrices, int x, int y);

    void draw(MatrixStack matrices, int x, int y, int width, int height);

    void drawCenteredWithRotation(MatrixStack matrices, int x, int y, int width, int height, float rotation);

    void draw(MatrixStack matrices, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight);

    void draw(MatrixStack matrices, int x, int y, int u, int v, int regionWidth, int regionHeight);
}
