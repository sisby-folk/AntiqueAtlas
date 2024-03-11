package folk.sisby.antique_atlas.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.gui.core.ToggleButtonComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;


/**
 * Bookmark-button in the journal. When a bookmark is selected, it will not
 * bulge on mouseover.
 */
public class BookmarkComponent extends ToggleButtonComponent {
    public static final Identifier BOOKMARKS = AntiqueAtlas.id("textures/gui/bookmarks.png");
    private static final int WIDTH = 21;
    private static final int HEIGHT = 18;

    private final int colorIndex;
    private Identifier iconTexture;
    private Text title;

    /**
     * @param colorIndex  0=red, 1=blue, 2=yellow, 3=green
     * @param iconTexture the path to the 16x16 texture to be drawn on top of the bookmark.
     * @param title       hovering text.
     */
    BookmarkComponent(int colorIndex, Identifier iconTexture, Text title) {
        this.colorIndex = colorIndex;
        setIconTexture(iconTexture);
        setTitle(title);
        setSize(WIDTH, HEIGHT);
    }

    void setIconTexture(Identifier iconTexture) {
        this.iconTexture = iconTexture;
    }

    public Text getTitle() {
        return title;
    }

    void setTitle(Text title) {
        this.title = title;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Render background:
        int u = colorIndex * WIDTH;
        int v = isMouseOver || isSelected() ? 0 : HEIGHT;
        context.drawTexture(BOOKMARKS, getGuiX(), getGuiY(), u, v, WIDTH, HEIGHT, 84, 36);

        // Render the icon:
        context.drawTexture(iconTexture, getGuiX() + (isMouseOver || isSelected() ? 3 : 2), getGuiY() + 1, 0, 0, 16, 16, 16, 16);

        if (isMouseOver) {
            drawTooltip(Collections.singletonList(title), MinecraftClient.getInstance().textRenderer);
        }
    }
}
