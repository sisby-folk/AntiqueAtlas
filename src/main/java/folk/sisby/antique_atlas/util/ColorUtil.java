package folk.sisby.antique_atlas.util;

import net.minecraft.util.math.ColorHelper;

public class ColorUtil {
    public static float[] getColorFromArgb(int color) {
        return new float[] { ColorHelper.getRed(color) / 255f, ColorHelper.getGreen(color) / 255f, ColorHelper.getBlue(color) / 255f };
    }
}
