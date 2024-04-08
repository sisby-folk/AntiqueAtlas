package folk.sisby.antique_atlas.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class DrawUtil {
    public static void drawCenteredWithRotation(DrawContext context, Identifier texture, double x, double y, float scale, int textureWidth, int textureHeight, float rotation) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 0);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180 + rotation));
        context.getMatrices().translate(-textureWidth / 2f, -textureWidth / 2f, 0f);
        context.drawTexture(texture, 0, 0, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        context.getMatrices().pop();
    }
}
