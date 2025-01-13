package folk.sisby.antique_atlas.util;

import net.minecraft.client.util.math.Rect2i;
import org.joml.Vector2d;

public class MathUtil {
	/**
	 * Returns the nearest number to a multiple of a given number.
	 */
	public static int roundToBase(int a, int base) {
		return a - a % base;
	}

	/**
	 * Returns the nearest number to a multiple of a given number.
	 */
	public static int roundToBase(double a, double base) {
		return (int) (a - a % base);
	}

	public static double innerDistanceToEdge(Rect2i rect, Vector2d point) {
		var dx = Math.min(point.x - rect.getX(), rect.getX() + rect.getWidth() - point.x);
		var dy = Math.min(point.y - rect.getY(), rect.getY() + rect.getHeight() - point.y);
		return Math.min(dx, dy);
	}
}
