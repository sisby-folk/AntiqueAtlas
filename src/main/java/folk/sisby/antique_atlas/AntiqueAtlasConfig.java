package folk.sisby.antique_atlas;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.FloatRange;

public class AntiqueAtlasConfig extends WrappedConfig {
    public enum GraveStyle {
        CAUSE,
        GRAVE,
        ITEMS,
        DIED,
        EUPHEMISMS
    }

    public final UISection ui = new UISection();
    public final DebugSection debug = new DebugSection();
    public final PerformanceSection performance = new PerformanceSection();

    public static final class UISection implements Section {
        @Comment("Default zoom level")
        @Comment("The number corresponds to the size of a block on the map relative to the size of a GUI pixel")
        @Comment("Preferably a power of 2.")
        @FloatRange(min = 0.001953125, max = 16.0)
        public final Double defaultScale = 0.5;

        @Comment("Minimum zoom level")
        @Comment("The number corresponds to the size of a block on the map relative to the size of a GUI pixel")
        @Comment("Preferably a power of 2")
        @Comment("Smaller values may decrease performance!")
        @FloatRange(min = 0.001953125, max = 16.0)
        public final Double minScale = 1.0 / 32.0;

        @Comment("Maximum zoom level")
        @Comment("The number corresponds to the size of a block on the map relative to the size of a GUI pixel")
        @Comment("Preferably a power of 2.")
        @FloatRange(min = 0.001953125, max = 16.0)
        public final Double maxScale = 4.0;

        @Comment("If false (by default), then mousewheel up is zoom in, mousewheel down is zoom out.")
        @Comment("If true, then the direction is reversed.")
        public final Boolean reverseZoom = false;

        @Comment("How to depict player death locations.")
        public final GraveStyle graveStyle = GraveStyle.EUPHEMISMS;

        @Comment("Whether to display the map in full-screen")
        @Comment("Full screen mode uses a simplistic background texture, and is more performance intensive")
        public final Boolean fullscreen = false;
    }

    public static final class PerformanceSection implements Section {
        @Comment("The maximum number of chunks to load onto the map per tick after entering a world.")
        public final Integer chunkTickLimit = 100;
    }

    public static final class DebugSection implements Section {
        @Comment("If true, map render time will be output.")
        public final Boolean debugRender = false;
    }
}
