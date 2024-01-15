package folk.sisby.antique_atlas.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.client.AntiqueAtlasTextures;
import folk.sisby.antique_atlas.client.gui.core.ButtonComponent;
import folk.sisby.antique_atlas.client.assets.MarkerTypes;
import folk.sisby.antique_atlas.client.texture.Drawable;
import folk.sisby.antique_atlas.marker.Marker;
import folk.sisby.antique_atlas.client.MarkerType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Collections;


/**
 * Bookmark-button in the journal. When a bookmark is selected, it will not
 * bulge on mouseover.
 */
public class MarkerBookmarkComponent extends ButtonComponent {
    private static final int WIDTH = 21;
    private static final int HEIGHT = 18;

    private final int colorIndex;
    private Drawable iconTexture;
    private final Marker marker;

    MarkerBookmarkComponent(Marker marker) {
        this.colorIndex = 3;
        this.marker = marker;

        MarkerType type = MarkerTypes.getInstance().get(marker.getType());
        setIconTexture(type.getTexture());

        setSize(WIDTH, HEIGHT);
    }

    void setIconTexture(Drawable iconTexture) {
        this.iconTexture = iconTexture;
    }

    public Text getTitle() {
        return marker.getLabel();
    }


    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Render background:
        int u = colorIndex * WIDTH;
        int v = isMouseOver ? 0 : HEIGHT;
        AntiqueAtlasTextures.BOOKMARKS_LEFT.draw(matrices, getGuiX(), getGuiY(), u, v, WIDTH, HEIGHT);

        // Render the icon:
        iconTexture.draw(matrices, getGuiX() - (isMouseOver ? 3 : 2), getGuiY() - 3, 24, 24);

        if (isMouseOver && !getTitle().getString().isEmpty()) {
            drawTooltip(Collections.singletonList(getTitle()), MinecraftClient.getInstance().textRenderer);
        }
    }
}
