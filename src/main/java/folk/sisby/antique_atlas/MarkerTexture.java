package folk.sisby.antique_atlas;

import net.minecraft.util.Identifier;

public record MarkerTexture(Identifier id, int offsetX, int offsetY, int textureWidth, int textureHeight, int mipLevels) {
    public static MarkerTexture ofId(Identifier id, int offsetX, int offsetY, int width, int height, int mipLevels) {
        return new MarkerTexture(new Identifier(id.getNamespace(), "textures/gui/markers/%s.png".formatted(id.getPath())), offsetX, offsetY, width, height, mipLevels);
    }

    public static MarkerTexture centered(Identifier id, int width, int height, int mipLevels) {
        return ofId(id, -width / 2, -height / 2, width, height, mipLevels);
    }

    public static final MarkerTexture DEFAULT = centered(AntiqueAtlas.id("unknown"), 32, 32, 0);

    public boolean withinTexture(int mouseX, int mouseY, int markerX, int markerY) {
        int x = mouseX - markerX;
        int z = mouseY - markerY;
        return x > 0 && x < textureWidth && z > 0 && z < textureHeight;
    }

    public Identifier keyId() {
        return new Identifier(id.getNamespace(), id.getPath().substring("textures/gui/markers/".length(), id.getPath().length() - 4));
    }

    public String displayId() {
        return id.getNamespace().equals(AntiqueAtlas.ID) ? keyId().getPath() : keyId().toString();
    }
}
