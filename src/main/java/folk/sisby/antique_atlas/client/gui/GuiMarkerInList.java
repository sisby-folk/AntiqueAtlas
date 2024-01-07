package folk.sisby.antique_atlas.client.gui;

import folk.sisby.antique_atlas.client.Textures;
import folk.sisby.antique_atlas.client.gui.core.GuiToggleButton;
import folk.sisby.antique_atlas.client.texture.ITexture;
import folk.sisby.antique_atlas.client.MarkerType;
import net.minecraft.client.gui.DrawContext;


public class GuiMarkerInList extends GuiToggleButton {
    public static final int FRAME_SIZE = 34;

    private final MarkerType markerType;

    public GuiMarkerInList(MarkerType markerType) {
        this.markerType = markerType;
        setSize(FRAME_SIZE, FRAME_SIZE);
    }

    public MarkerType getMarkerType() {
        return markerType;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTick) {
        ITexture frame_texture = isSelected() ? Textures.MARKER_FRAME_ON : Textures.MARKER_FRAME_OFF;
        frame_texture.draw(context, getGuiX() + 1, getGuiY() + 1);

        ITexture texture = markerType.getTexture();
        if (texture != null) {
            texture.draw(context, getGuiX() + 1, getGuiY() + 1);
        }

        super.render(context, mouseX, mouseY, partialTick);
    }
}
