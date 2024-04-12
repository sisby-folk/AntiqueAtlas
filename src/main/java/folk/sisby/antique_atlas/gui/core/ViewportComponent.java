package folk.sisby.antique_atlas.gui.core;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * The children of this component are rendered and process input only inside
 * the viewport frame. Use {@link #setSize(int, int)} to set its bounds.
 *
 * @author Hunternif
 */
public class ViewportComponent extends Component {
    /**
     * The container component for content.
     */
    final Component content = new Component();

    private boolean hidden;

    public ViewportComponent() {
        this.addChild(content);
    }

    /**
     * Add scrolling content. Use removeContent to remove it.
     *
     * @return the child added
     */
    public Component addContent(Component child) {
        return content.addChild(child);
    }

    public void removeAllContent() {
        content.removeAllChildren();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float par3) {
        if (hidden) return;
        double guiScale = client.getWindow().getScaleFactor();
        RenderSystem.enableScissor(
            (int) (guiScale * getGuiX()),
            (int) (MinecraftClient.getInstance().getWindow().getFramebufferHeight() - (getGuiY() + properHeight) * guiScale),
            (int) (guiScale * (properWidth + 1)),
            (int) (guiScale * (properHeight + 1))
        );

        // Draw the content (child GUIs):
        super.render(context, mouseX, mouseY, par3);

        RenderSystem.disableScissor();
    }

    @Override
    public int getWidth() {
        return properWidth;
    }

    @Override
    public int getHeight() {
        return properHeight;
    }

    @Override
    protected void validateSize() {
        super.validateSize();
        // Update the clipping flag on content's child components:
        for (Component child : this.getChildren()) {
            child.setClipped(child.getGuiY() > getGuiY() + properHeight ||
                    child.getGuiY() + child.getHeight() < getGuiY() ||
                    child.getGuiX() > getGuiX() + properWidth ||
                    child.getGuiX() + child.getWidth() < getGuiX()
            );
        }
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
