package folk.sisby.antique_atlas;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2d;

public record MarkerTexture(Identifier id, Identifier accentId, int offsetX, int offsetY, int textureWidth, int textureHeight, int mipLevels) {
    private static Identifier idToTexture(Identifier id) {
        return id.withPrefixedPath("textures/atlas/marker/").withSuffixedPath(".png");
    }

    public static MarkerTexture ofId(Identifier id, int offsetX, int offsetY, int width, int height, int mipLevels, boolean accent) {
        return new MarkerTexture(idToTexture(id), accent ? idToTexture(id.withSuffixedPath("_accent")) : null, offsetX, offsetY, width, height, mipLevels);
    }

    public static MarkerTexture centered(Identifier id, int width, int height, int mipLevels, boolean accent) {
        return ofId(id, -width / 2, -height / 2, width, height, mipLevels, accent);
    }

    public static final MarkerTexture DEFAULT = centered(AntiqueAtlas.id("custom/point"), 32, 32, 0, true);

    public Identifier keyId() {
        return new Identifier(id.getNamespace(), id.getPath().substring("textures/atlas/marker/".length(), id.getPath().length() - 4));
    }

    public String displayId() {
        return id.getNamespace().equals(AntiqueAtlas.ID) ? keyId().getPath() : keyId().toString();
    }

    public int fullTextureWidth() {
        int width = textureWidth;
        for (int i = 0; i < mipLevels; i++) {
            width += textureWidth >> (i + 1);
        }
        return width;
    }

    public int getU(int mipLevel) {
        int currentMipLevel = mipLevel - 1;
        int u = 0;
        while (currentMipLevel >= 0) {
            u += textureWidth / (1 << currentMipLevel);
            currentMipLevel--;
        }
        return u;
    }

    public Vector2d getCenter(int tileChunks) {
        int mipLevel = MathHelper.clamp(MathHelper.ceilLog2(tileChunks), 0, mipLevels);
        return new Vector2d(((double) offsetX + (double) textureWidth / 2.0) / (double) (1 << mipLevel), ((double) offsetY + (double) textureHeight / 2.0) / (double) (1 << mipLevel));
    }

    public double getSquaredSize(int tileChunks) {
        int mipLevel = MathHelper.clamp(MathHelper.ceilLog2(tileChunks), 0, mipLevels);
        return textureWidth * textureHeight / (double) (1 << mipLevel);
    }

    public void draw(DrawContext context, double markerX, double markerY, float markerScale, int tileChunks, DyeColor accent, float tint, float alpha) {
        context.getMatrices().push();
        context.getMatrices().translate(markerX, markerY, 0.0);
        context.getMatrices().scale(markerScale, markerScale, 1.0F);
        if (tileChunks > 1 && mipLevels > 0) {
            int mipLevel = MathHelper.clamp(MathHelper.ceilLog2(tileChunks), 0, mipLevels);
            context.drawTexture(id, offsetX / (1 << mipLevel), offsetY / (1 << mipLevel), getU(mipLevel), 0, textureWidth / (1 << mipLevel), textureHeight / (1 << mipLevel), fullTextureWidth(), textureHeight);
            if (accentId != null && accent != null) {
                float[] rgb = accent.getColorComponents();
                RenderSystem.setShaderColor(tint * rgb[0], tint * rgb[1], tint * rgb[2], alpha);
                context.drawTexture(accentId, offsetX / (1 << mipLevel), offsetY / (1 << mipLevel), getU(mipLevel), 0, textureWidth / (1 << mipLevel), textureHeight / (1 << mipLevel), fullTextureWidth(), textureHeight);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        } else {
            context.drawTexture(id, offsetX, offsetY, 0, 0, textureWidth, textureHeight, fullTextureWidth(), textureHeight);
            if (accentId != null && accent != null) {
                float[] rgb = accent.getColorComponents();
                RenderSystem.setShaderColor(tint * rgb[0], tint * rgb[1], tint * rgb[2], alpha);
                context.drawTexture(accentId, offsetX, offsetY, 0, 0, textureWidth, textureHeight, fullTextureWidth(), textureHeight);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
        context.getMatrices().pop();
    }
}
