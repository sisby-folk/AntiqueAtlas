package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.MarkerTexture;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public class MarkerBookmarkButton extends BookmarkButton {
	protected final MarkerTexture markerTexture;

	protected MarkerBookmarkButton(Text title, MarkerTexture markerTexture, DyeColor accent, boolean left) {
		super(title, markerTexture.id(), accent, accent, markerTexture.textureWidth(), markerTexture.textureHeight(), left);
		this.markerTexture = markerTexture;
	}

	@Override
	public void drawIcon(DrawContext context, int x, int y) {
		markerTexture.drawIcon(context, x, y, iconTint);
	}
}
