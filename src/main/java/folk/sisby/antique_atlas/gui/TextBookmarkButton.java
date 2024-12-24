package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TextBookmarkButton extends BookmarkButton {
	public static final Identifier RULER_TEXTURE_RIGHT = AntiqueAtlas.id("textures/gui/ruler_right.png");
	public static final int SHORT_WIDTH = 20;
	private Text label;

	TextBookmarkButton(Text title, Text label) {
		super(RULER_TEXTURE_RIGHT, title, null, null, null, 32, 32, false);
		this.clickSound = null;
		this.label = label;
	}

	public void setLabel(Text label) {
		this.label = label;
	}

	@Override
	public void renderTooltip(DrawContext context, int mouseX, int mouseY, float partialTick, boolean mouseOver) {
		boolean isExtended = mouseOver || isSelected();
		int centerOffsetX = (SHORT_WIDTH - textRenderer.getWidth(label)) / 2;
		context.drawText(textRenderer, label, getGuiX() + centerOffsetX + (isExtended ? 3 : 0), getGuiY() + 5, 0xFF000000, false);
		if (!label.getString().equals("1c")) super.renderTooltip(context, mouseX, mouseY, partialTick, mouseOver);
	}
}
