package folk.sisby.antique_atlas.gui.core;

import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.util.Identifier;

public class ScrollBoxComponent extends Component {
    public static final Identifier ARROW = AntiqueAtlas.id("textures/gui/arrow.png");

    private final int scrollStep;
    private final boolean vertical;
    private final ViewportComponent viewport;

    /**
     * How much the content of the viewport is displaced.
     */
    int scrollPos = 0;


    public ScrollBoxComponent(boolean vertical, int scrollStep) {
        this.vertical = vertical;
        this.scrollStep = scrollStep;
        this.viewport = new ViewportComponent();
        this.addChild(viewport);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double wheelMove) {
        if (isMouseOver) {
            if (wheelMove != 0) {
                wheelMove = wheelMove > 0 ? -1 : 1;
                doSetScrollPos((int) (scrollPos + wheelMove * scrollStep));
                return true;
            }
        }

        return super.mouseScrolled(mx, my, wheelMove);
    }

    /**
     * Offset of the viewport's content in pixels. This method forces
     * validation of the viewport and its content in order to work correctly
     * during initGui().
     */
    public void setScrollPos(int scrollPos) {
        viewport.content.validateSize();
        viewport.validateSize();
        doSetScrollPos(scrollPos);
    }

    /**
     * Offset of the viewport's content in pixels. This will only work
     * correctly after the viewport's size has been validated.
     */
    private void doSetScrollPos(int scrollPos) {
        scrollPos = Math.max(0, Math.min(scrollPos, getContentSize() - getViewportSize()));
        this.scrollPos = scrollPos;
        updateContentPos();
    }

    protected void updateContentPos() {
        viewport.content.setRelativeCoords(vertical ? viewport.content.getRelativeX() : -scrollPos, vertical ? -scrollPos: viewport.content.getRelativeY());
    }

    public int getContentSize() {
        return vertical ? viewport.contentHeight : viewport.contentWidth;
    }

    public int getViewportSize() {
        return vertical ? viewport.getHeight() : viewport.getWidth();
    }

    public Component addContent(Component child) {
        return viewport.addContent(child);
    }

    public void removeAllContent() {
        viewport.removeAllContent();
    }

    public void setViewportSize(int width, int height) {
        viewport.setSize(width, height);
    }
}
