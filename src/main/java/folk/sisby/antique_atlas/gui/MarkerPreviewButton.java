package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.MarkerTexture;
import net.minecraft.client.gui.DrawContext;

public class MarkerPreviewButton extends TexturePreviewButton<MarkerTexture> {
	public MarkerPreviewButton(MarkerTexture markerTexture, float[] tint) {
		super(markerTexture, markerTexture.id(), markerTexture.textureWidth(), markerTexture.textureHeight(), 0, tint);
	}

	@Override
	protected void drawTexture(DrawContext context, int x, int y) {
		getValue().drawIcon(context, x, y, tint);
	}
}
