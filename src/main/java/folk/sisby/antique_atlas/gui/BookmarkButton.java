package folk.sisby.antique_atlas.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.gui.core.ToggleButtonComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class BookmarkButton extends ToggleButtonComponent {
    public static final Identifier TEXTURE_LEFT = AntiqueAtlas.id("textures/gui/bookmark_left.png");
    public static final Identifier TEXTURE_RIGHT = AntiqueAtlas.id("textures/gui/bookmark_right.png");
    public static final int WIDTH = 24;
    public static final int HEIGHT = 18;

    private Text title;
    private Identifier iconTexture;
    private final float[] backgroundTint;
    private final float[] iconTint;
    private final int iconWidth;
    private final int iconHeight;
    private final boolean left;
    private final Identifier backgroundTexture;

    protected BookmarkButton(Identifier backgroundTexture, Text title, Identifier iconTexture, DyeColor backgroundTint, @Nullable DyeColor iconTint, int iconWidth, int iconHeight, boolean left) {
        super(false);
        this.backgroundTexture = backgroundTexture;
        this.title = title;
        this.iconTexture = iconTexture;
        this.backgroundTint = backgroundTint == null ? null : backgroundTint.getColorComponents();
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.iconTint = iconTint == null ? null : iconTint.getColorComponents();
        this.left = left;
        setTitle(title);
        setSize(WIDTH, HEIGHT);
    }

    BookmarkButton(Text title, Identifier iconTexture, DyeColor backgroundTint, @Nullable DyeColor iconTint, int iconWidth, int iconHeight, boolean left) {
        this(left ? TEXTURE_LEFT : TEXTURE_RIGHT, title, iconTexture, backgroundTint, iconTint, iconWidth, iconHeight, left);
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
        boolean mouseOver = isMouseOver(mouseX, mouseY);
        boolean isExtended = mouseOver || isSelected();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (backgroundTint != null) RenderSystem.setShaderColor(backgroundTint[0], backgroundTint[1], backgroundTint[2], 1.0F);
        context.drawTexture(backgroundTexture, getGuiX(), getGuiY(), 0, isExtended ? 0 : HEIGHT, WIDTH, HEIGHT, WIDTH, HEIGHT * 2);

        if (iconTexture != null) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (iconTint != null) RenderSystem.setShaderColor(iconTint[0], iconTint[1], iconTint[2], 1.0F);
            int iconX = getGuiX() + 10 - iconWidth / 2 + (isExtended ? (left ? 3 : 1) : (left ? 4 : 0));
            int iconY = getGuiY() + 9 - iconHeight / 2;
            context.drawTexture(iconTexture, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        renderTooltip(context, mouseX, mouseY, partialTick, mouseOver);
    }

    public void renderTooltip(DrawContext context, int mouseX, int mouseY, float partialTick, boolean mouseOver) {
        if (mouseOver && !title.getString().isEmpty()) {
            drawTooltip(Collections.singletonList(title), MinecraftClient.getInstance().textRenderer);
        }
    }
}
