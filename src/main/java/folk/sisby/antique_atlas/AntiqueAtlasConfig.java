package folk.sisby.antique_atlas;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.FloatRange;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;

public class AntiqueAtlasConfig extends WrappedConfig {
    public enum GraveVerb {
        DIED,
        TOTALLED,
        WIPED,
        FAINTED,
        LOST,
        LOST_GEAR
    }

    public final GameplaySettings Gameplay = new GameplaySettings();
    public final InterfaceSettings Interface = new InterfaceSettings();
    public final PerformanceSettings Performance = new PerformanceSettings();

    public static final class GameplaySettings implements Section {
        @Comment("Whether to add local marker for the spot where the player died.")
        public final Boolean autoDeathMarker = true;

        @Comment("Whether to add global markers for NPC villages.")
        public final Boolean autoVillageMarkers = false;

        @Comment("Whether to add global markers for Nether Portals.")
        public final Boolean autoNetherPortalMarkers = true;
    }

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

        @Comment("Which verb to use to describe player death locations.")
        public final GraveVerb graveVerb = GraveVerb.LOST_GEAR;
    }

    public static final class PerformanceSettings implements Section {
        @Comment("The radius of the area around the player which is scanned by the Atlas at regular intervals.")
        @Comment("Note that this will not force faraway chunks to load, unless forceChunkLoading is enabled.")
        @Comment("Lower value gives better performance.")
        public final Integer scanRadius = 11;

        @Comment("Force loading of chunks within scan radius even if it exceeds regular chunk loading distance.")
        @Comment("Enabling this may SEVERELY decrease performance!")
        public final Boolean forceChunkLoading = false;

        @Comment("Time in seconds between two scans of the area.")
        @Comment("Higher value gives better performance.")
        public final Float newScanInterval = 1f;

        @Comment("Whether to rescan chunks in the area that have been previously mapped")
        @Comment("This is useful in case of changes in coastline (including small ponds of water and lava), or if land disappears completely (for sky worlds).")
        @Comment("Disable for better performance.")
        public final Boolean doRescan = true;

        @Comment("The number of area scans between full rescans.")
        @Comment("Higher value gives better performance.")
        @IntegerRange(min = 1, max = 1000)
        public final Integer rescanRate = 4;

        @Comment("The maximum number of markers a particular atlas can hold.")
        @IntegerRange(min = 0, max = 2147483647)
        public final Integer markerLimit = 1024;

        @Comment("Whether to perform additional scanning to locate small ponds of water or lava.")
        @Comment("Disable for better performance.")
        public final Boolean doScanPonds = true;

        @Comment("Whether to perform additional scanning to locate ravines.")
        @Comment("Disable for better performance.")
        public final Boolean doScanRavines = true;

        @Comment("If true, map render time will be output.")
        public final Boolean debugRender = false;

        @Comment("If true, all resource pack loading information will be logged during start and reload.")
        public final Boolean resourcePackLogging = false;
    }
}
