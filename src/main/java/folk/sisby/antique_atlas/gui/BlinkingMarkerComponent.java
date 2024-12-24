package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.gui.core.BlinkingTextureComponent;

public class BlinkingMarkerComponent extends BlinkingTextureComponent implements MarkerModal.IMarkerTypeSelectListener {
	public void onSelectMarkerType(MarkerTexture markerTexture) {
		setTexture(markerTexture.id(), AtlasScreen.MARKER_SIZE, AtlasScreen.MARKER_SIZE);
	}
}
