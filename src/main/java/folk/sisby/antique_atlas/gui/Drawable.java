package folk.sisby.antique_atlas.gui;

import net.minecraft.client.gui.DrawContext;

/**
 * A generic interface for textures. The texture know their own width and height.
 * All method parameters are provided in pixels.
 */
public interface Drawable {
    int width();

    int height();

    void draw(DrawContext context, int x, int y);

    void draw(DrawContext context, int x, int y, int width, int height);

    void drawCenteredWithRotation(DrawContext context, int x, int y, int width, int height, float rotation);

    void draw(DrawContext context, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight);

    void draw(DrawContext context, int x, int y, int u, int v, int regionWidth, int regionHeight);
}
