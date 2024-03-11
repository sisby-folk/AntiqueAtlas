package folk.sisby.antique_atlas.gui;


import folk.sisby.antique_atlas.MarkerType;
import net.minecraft.util.Identifier;

public class MarkerRenderInfo {
    public final Identifier tex;
    public int x, y;
    public int width, height;

    public MarkerRenderInfo(Identifier tex, int x, int y, int width, int height) {
        this.tex = tex;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void scale(double factor) {
        x = (int) ((1 - factor) / 2f * width + x);
        y = (int) ((1 - factor) / 2f * height + y);
        width = (int) (factor * width);
        height = (int) (factor * height);
    }

    public static MarkerRenderInfo ofType(MarkerType type, double scale, double mapScale) {
        int size = (int) (16 * scale * type.viewSize());
        if (type.isTile()) {
            size *= (int) mapScale;
        }
        int x = -(int) (size * type.getCenterX());
        int y = -(int) (size * type.getCenterY());

        return new MarkerRenderInfo(type.getTexture(), x, y, size, size);
    }
}
