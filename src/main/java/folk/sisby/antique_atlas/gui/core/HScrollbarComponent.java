package folk.sisby.antique_atlas.gui.core;

import net.minecraft.client.gui.DrawContext;

public class HScrollbarComponent extends AbstractScrollbarComponent {

    public HScrollbarComponent(ViewportComponent viewport) {
        super(viewport);
    }

    @Override
    protected void drawAnchor(DrawContext context) {
        // Draw left cap:
        context.drawTexture(texture, getGuiX() + anchorPos, getGuiY(), 0, 0, capLength, textureHeight, capLength, textureHeight);

        // Draw body:
        context.drawTexture(texture, getGuiX() + anchorPos + capLength, getGuiY(), capLength, 0, anchorSize, textureHeight, textureBodyLength, textureHeight);

        // Draw right cap:
        context.drawTexture(texture, getGuiX() + anchorPos + capLength + anchorSize, getGuiY(), textureWidth - capLength, 0, capLength, textureHeight, capLength, textureHeight);
    }

    @Override
    protected int getTextureLength() {
        return textureWidth;
    }

    @Override
    protected int getScrollbarLength() {
        return getWidth();
    }

    @Override
    protected int getViewportSize() {
        return viewport.getWidth();
    }

    @Override
    protected int getContentSize() {
        return viewport.contentWidth;
    }

    @Override
    protected int getMousePos(int mouseX, int mouseY) {
        return mouseX - getGuiX();
    }

    @Override
    protected void updateContentPos() {
        viewport.content.setRelativeCoords(-scrollPos, viewport.content.getRelativeY());
    }

    @Override
    protected void setScrollbarWidth(int textureWidth, int textureHeight) {
        setSize(getWidth(), textureHeight);
    }

}
