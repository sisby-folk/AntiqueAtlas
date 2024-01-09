package folk.sisby.antique_atlas.client.gui;

import folk.sisby.antique_atlas.client.gui.core.BlinkingTextureComponent;
import folk.sisby.antique_atlas.client.resource.MarkerType;

public class BlinkingMarkerComponent extends BlinkingTextureComponent implements MarkerModalComponent.IMarkerTypeSelectListener {
    public void onSelectMarkerType(MarkerType markerType) {
        setTexture(markerType.getTexture(), AtlasScreen.MARKER_SIZE, AtlasScreen.MARKER_SIZE);
    }
}
