package folk.sisby.antique_atlas;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.FloatRange;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;

public class AntiqueAtlasConfig extends WrappedConfig {
    public final InterfaceSettings Interface = new InterfaceSettings();
    public final PerformanceSettings Performance = new PerformanceSettings();

    public static final class InterfaceSettings implements Section {
        public final Boolean doScaleMarkers = false;

        @Comment("Default zoom level")
        @Comment("The number corresponds to the size of a block on the map relative to the size of a GUI pixel")
        @Comment("Preferrably a power of 2.")
        @FloatRange(min = 0.001953125, max = 16.0)
        public final Double defaultScale = 0.5;

        @Comment("Minimum zoom level")
        @Comment("The number corresponds to the size of a block on the map relative to the size of a GUI pixel")
        @Comment("Preferrably a power of 2")
        @Comment("Smaller values may decrease performance!")
        @FloatRange(min = 0.001953125, max = 16.0)
        public final Double minScale = 1.0 / 32.0;

        @Comment("Maximum zoom level")
        @Comment("The number corresponds to the size of a block on the map relative to the size of a GUI pixel")
        @Comment("Preferrably a power of 2.")
        @FloatRange(min = 0.001953125, max = 16.0)
        public final Double maxScale = 4.0;

        @Comment("If false (by default), then mousewheel up is zoom in, mousewheel down is zoom out.")
        @Comment("If true, then the direction is reversed.")
        public final Boolean doReverseWheelZoom = false;
    }

    public static final class PerformanceSettings implements Section {
        @Comment("If true, map render time will be output.")
        public final Boolean debugRender = false;

        @Comment("If true, all resource pack loading information will be logged during start and reload.")
        public final Boolean resourcePackLogging = false;
    }
}
