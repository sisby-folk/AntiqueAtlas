package folk.sisby.antique_atlas;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2d;

public record MarkerTexture(Identifier id, int offsetX, int offsetY, int textureWidth, int textureHeight, int mipLevels) {
    public static MarkerTexture ofId(Identifier id, int offsetX, int offsetY, int width, int height, int mipLevels) {
        return new MarkerTexture(new Identifier(id.getNamespace(), "textures/atlas/marker/%s.png".formatted(id.getPath())), offsetX, offsetY, width, height, mipLevels);
    }

    public static MarkerTexture centered(Identifier id, int width, int height, int mipLevels) {
        return ofId(id, -width / 2, -height / 2, width, height, mipLevels);
    }

    public static final MarkerTexture DEFAULT = centered(AntiqueAtlas.id("unknown"), 32, 32, 0);

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
        return new Vector2d(((double) offsetX() + (double) textureWidth() / 2.0) / (double) (1 << mipLevel), ((double) offsetY() + (double) textureHeight() / 2.0) / (double) (1 << mipLevel));
    }

    public double getSquaredSize(int tileChunks) {
        int mipLevel = MathHelper.clamp(MathHelper.ceilLog2(tileChunks), 0, mipLevels);
        return textureWidth * textureWidth / (double) (1 << mipLevel);
    }

    public void draw(DrawContext context, double markerX, double markerY, float markerScale, int tileChunks) {
        context.getMatrices().push();
        context.getMatrices().translate(markerX, markerY, 0.0);
        context.getMatrices().scale(markerScale, markerScale, 1.0F);
        if (tileChunks > 1 && mipLevels > 0) {
            int mipLevel = MathHelper.clamp(MathHelper.ceilLog2(tileChunks), 0, mipLevels);
            context.drawTexture(id(), offsetX() / (1 << mipLevel), offsetY() / (1 << mipLevel), getU(mipLevel), 0, textureWidth / (1 << mipLevel), textureHeight / (1 << mipLevel), fullTextureWidth(), textureHeight());
        } else {
            context.drawTexture(id(), offsetX(), offsetY(), 0, 0, textureWidth(), textureHeight(), fullTextureWidth(), textureHeight());
        }
        context.getMatrices().pop();
    }
}
