package folk.sisby.antique_atlas.client.gui;

import folk.sisby.antique_atlas.client.gui.core.GuiBlinkingImage;
import folk.sisby.antique_atlas.client.resource.MarkerType;

public class GuiBlinkingMarker extends GuiBlinkingImage implements GuiMarkerFinalizer.IMarkerTypeSelectListener {
    public void onSelectMarkerType(MarkerType markerType) {
        setTexture(markerType.getTexture(), GuiAtlas.MARKER_SIZE, GuiAtlas.MARKER_SIZE);
    }
}
