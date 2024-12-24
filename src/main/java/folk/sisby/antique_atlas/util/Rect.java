package folk.sisby.antique_atlas.util;

public class Rect {
	public int minX, minY, maxX, maxY;

	public Rect() {
		this(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public Rect(int minX, int minY, int maxX, int maxY) {
		this.set(minX, minY, maxX, maxY);
	}

	public Rect set(int minX, int minY, int maxX, int maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		return this;
	}

	public Rect set(Rect r) {
		this.set(r.minX, r.minY, r.maxX, r.maxY);
		return this;
	}

	public void extendTo(int x, int y) {
		if (x < minX) minX = x;
		if (x > maxX) maxX = x;
		if (y < minY) minY = y;
		if (y > maxY) maxY = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Rect r)) return false;
		return minX == r.minX && minY == r.minY && maxX == r.maxX && maxY == r.maxY;
	}

	@Override
	public String toString() {
		return String.format("Rect{%d, %d, %d, %d}", minX, minY, maxX, maxY);
	}
}
