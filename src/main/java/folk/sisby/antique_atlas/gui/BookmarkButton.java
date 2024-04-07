package folk.sisby.antique_atlas.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.gui.core.ToggleButtonComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.Collections;

public class BookmarkButton extends ToggleButtonComponent {
    public static final Identifier TEXTURE_LEFT = AntiqueAtlas.id("textures/gui/bookmark_left.png");
    public static final Identifier TEXTURE_RIGHT = AntiqueAtlas.id("textures/gui/bookmark_right.png");
    public static final int WIDTH = 24;
    public static final int HEIGHT = 18;

    private Text title;
    private Identifier iconTexture;
    private final int backgroundTint;
    private final int iconTint;
    private final int iconSize;
    private final boolean left;

    BookmarkButton(Text title, Identifier iconTexture, int backgroundTint, int iconTint, int iconSize, boolean left) {
        this.title = title;
        this.iconTexture = iconTexture;
        this.backgroundTint = backgroundTint;
        this.iconSize = iconSize;
        this.iconTint = iconTint;
        this.left = left;
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
        boolean isExtended = isMouseOver || (!left && isSelected());

        RenderSystem.setShaderColor(ColorHelper.Argb.getRed(backgroundTint) / 255.0F, ColorHelper.Argb.getGreen(backgroundTint) / 255.0F, ColorHelper.Argb.getBlue(backgroundTint) / 255.0F, 1.0F);
        context.drawTexture(left ? TEXTURE_LEFT : TEXTURE_RIGHT, getGuiX(), getGuiY(), 0, isExtended ? 0 : HEIGHT, WIDTH, HEIGHT, WIDTH, HEIGHT * 2);

        RenderSystem.setShaderColor(ColorHelper.Argb.getRed(iconTint) / 255.0F, ColorHelper.Argb.getGreen(iconTint) / 255.0F, ColorHelper.Argb.getBlue(iconTint) / 255.0F, 1.0F);

        int iconX = getGuiX() + 10 - iconSize / 2 + (isExtended ? (left ? 2 : 1) : (left ? 3 : 0));
        int iconY = getGuiY() + 9 - iconSize / 2;
        context.drawTexture(iconTexture, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (isMouseOver && !title.getString().isEmpty()) {
            drawTooltip(Collections.singletonList(title), MinecraftClient.getInstance().textRenderer);
        }
    }
}
