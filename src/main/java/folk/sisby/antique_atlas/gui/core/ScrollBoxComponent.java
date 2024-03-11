package folk.sisby.antique_atlas.gui.core;

import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.util.Identifier;

public class ScrollBoxComponent extends Component {
    public static final Identifier SCROLLBAR_HOR = AntiqueAtlas.id("textures/gui/scrollbar_hor.png");
    public static final Identifier SCROLLBAR_VER = AntiqueAtlas.id("textures/gui/scrollbar_ver.png");
    private final ViewportComponent viewport;
    private final HScrollbarComponent scrollbarHor;
    private final VScrollbarComponent scrollbarVer;

    public ScrollBoxComponent() {
        viewport = new ViewportComponent();
        scrollbarHor = new HScrollbarComponent(viewport);
        scrollbarHor.setTexture(SCROLLBAR_HOR, 8, 7, 2);
        scrollbarVer = new VScrollbarComponent(viewport);
        scrollbarVer.setTexture(SCROLLBAR_VER, 7, 8, 2);
        setWheelScrollsVertically();
        this.addChild(viewport);
        this.addChild(scrollbarHor);
        this.addChild(scrollbarVer);
    }

    /**
     * Add scrolling content. Use removeContent to remove it.
     *
     * @return the child added
     */
    public Component addContent(Component child) {
        return viewport.addContent(child);
    }

    public void removeAllContent() {
        viewport.removeAllContent();
    }

    public void setViewportSize(int width, int height) {
        viewport.setSize(width, height);
        scrollbarHor.setRelativeCoords(0, height);
        scrollbarHor.setSize(width, scrollbarHor.getHeight());
        scrollbarVer.setRelativeCoords(width, 0);
        scrollbarVer.setSize(scrollbarVer.getWidth(), height);
    }

    @Override
    protected void validateSize() {
        super.validateSize();
        scrollbarHor.updateContent();
        scrollbarVer.updateContent();
    }

    /**
     * Mouse wheel will affect <b>horizontal</b> scrolling and not vertical.
     * This is the default behavior.
     */
    public void setWheelScrollsHorizontally() {
        scrollbarHor.setUsesWheel(true);
        scrollbarVer.setUsesWheel(false);
    }

    /**
     * Mouse wheel will affect <b>vertical</b> scrolling and not horizontal.
     */
    public void setWheelScrollsVertically() {
        scrollbarHor.setUsesWheel(false);
        scrollbarVer.setUsesWheel(true);
    }

    /**
     * Scroll to the specified point relative to the content's top left corner.
     * The container attempts to place the specified point at the top left
     * corner of the viewport as well.
     */
    public void scrollTo(int x, int y) {
        scrollbarHor.setScrollPos(x);
        scrollbarVer.setScrollPos(y);
    }

    @Override
    public int getWidth() {
        return super.getWidth() - (scrollbarVer.visible ? 0 : scrollbarVer.getWidth());
    }

    @Override
    public int getHeight() {
        return super.getHeight() - (scrollbarHor.visible ? 0 : scrollbarHor.getHeight());
    }
}
