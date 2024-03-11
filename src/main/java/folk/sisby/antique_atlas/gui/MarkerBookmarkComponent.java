package folk.sisby.antique_atlas.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.gui.core.ButtonComponent;
import folk.sisby.antique_atlas.reloader.MarkerTypes;
import folk.sisby.antique_atlas.Marker;
import folk.sisby.antique_atlas.MarkerType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;


/**
 * Bookmark-button in the journal. When a bookmark is selected, it will not
 * bulge on mouseover.
 */
public class MarkerBookmarkComponent extends ButtonComponent {
    public static final Identifier BOOKMARKS_LEFT = AntiqueAtlas.id("textures/gui/bookmarks_l.png");
    private static final int WIDTH = 21;
    private static final int HEIGHT = 18;

    private final int colorIndex;
    private Identifier iconTexture;
    private final Marker marker;

    MarkerBookmarkComponent(Marker marker) {
        this.colorIndex = 3;
        this.marker = marker;

        MarkerType type = MarkerTypes.getInstance().get(marker.type());
        setIconTexture(type.getTexture());

        setSize(WIDTH, HEIGHT);
    }

    void setIconTexture(Identifier iconTexture) {
        this.iconTexture = iconTexture;
    }

    public Text getTitle() {
        return marker.label();
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Render background:
        int u = colorIndex * WIDTH;
        int v = isMouseOver ? 0 : HEIGHT;
        context.drawTexture(BOOKMARKS_LEFT, getGuiX(), getGuiY(), u, v, WIDTH, HEIGHT, 84, 36);

        // Render the icon:
        context.drawTexture(iconTexture, getGuiX() - (isMouseOver ? 3 : 2), getGuiY() - 3, 0, 0, 24, 24, 24, 24);

        if (isMouseOver && !getTitle().getString().isEmpty()) {
            drawTooltip(Collections.singletonList(getTitle()), MinecraftClient.getInstance().textRenderer);
        }
    }
}
