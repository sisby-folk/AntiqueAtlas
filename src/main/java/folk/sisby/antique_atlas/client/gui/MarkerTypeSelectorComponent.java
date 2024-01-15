package folk.sisby.antique_atlas.client.gui;

import folk.sisby.antique_atlas.client.AntiqueAtlasTextures;
import folk.sisby.antique_atlas.client.gui.core.ToggleButtonComponent;
import folk.sisby.antique_atlas.client.texture.Drawable;
import folk.sisby.antique_atlas.client.MarkerType;
import net.minecraft.client.util.math.MatrixStack;


public class MarkerTypeSelectorComponent extends ToggleButtonComponent {
    public static final int FRAME_SIZE = 34;

    private final MarkerType markerType;

    public MarkerTypeSelectorComponent(MarkerType markerType) {
        this.markerType = markerType;
        setSize(FRAME_SIZE, FRAME_SIZE);
    }

    public MarkerType getMarkerType() {
        return markerType;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTick) {
        Drawable frameTexture = isSelected() ? AntiqueAtlasTextures.MARKER_FRAME_ON : AntiqueAtlasTextures.MARKER_FRAME_OFF;
        frameTexture.draw(matrices, getGuiX() + 1, getGuiY() + 1);

        Drawable texture = markerType.getTexture();
        if (texture != null) {
            texture.draw(matrices, getGuiX() + 1, getGuiY() + 1);
        }

        super.render(matrices, mouseX, mouseY, partialTick);
    }
}
