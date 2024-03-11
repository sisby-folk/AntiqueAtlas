package folk.sisby.antique_atlas.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.gui.core.ButtonComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.Collections;

public class FollowButtonComponent extends ButtonComponent {
    public static final Identifier BTN_POSITION = AntiqueAtlas.id("textures/gui/position.png");
    public static final int WIDTH = 11;
    public static final int HEIGHT = 11;

    public FollowButtonComponent() {
        setSize(WIDTH, HEIGHT);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        if (isEnabled()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            int x = getGuiX(), y = getGuiY();
            if (isMouseOver) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
            }

            context.drawTexture(BTN_POSITION, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);

            RenderSystem.disableBlend();

            if (isMouseOver) {
                drawTooltip(Collections.singletonList(Text.translatable("gui.antique_atlas.followPlayer")), MinecraftClient.getInstance().textRenderer);
            }
        }
    }
}
