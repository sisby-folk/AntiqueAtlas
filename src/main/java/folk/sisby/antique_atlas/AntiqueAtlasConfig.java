package folk.sisby.antique_atlas;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;

import java.util.Map;

public class AntiqueAtlasConfig extends WrappedConfig {
    public enum GraveStyle {
        CAUSE,
        GRAVE,
        ITEMS,
        DIED,
        EUPHEMISMS
    }

    public enum FallbackHandling {
        TEST,
        MISSING,
        CRASH
    }

    @Comment("Whether to display the map in full-screen")
    @Comment("The background is slightly less stylish, but more tiles are shown at once")
    public final Boolean fullscreen = true;

    @Comment("How to depict player death locations.")
    public final GraveStyle graveStyle = GraveStyle.EUPHEMISMS;

    @Comment("The maximum number of chunks to represent as a tile, as a power of 2")
    @Comment("Effectively the 'minimum zoom'")
    @Comment("0: 1x1 chunk = 1 tile | 6: 64x64 chunks = 1 tile")
    @IntegerRange(min = 0, max = 6)
    public final Integer maxTileChunks = 5;

    @Comment("The maximum size to render a tile at, as a power of 2 multiplier")
    @Comment("Effectively the 'maximum zoom'")
    @Comment("0: 1 tile = 16x16 | 3: 1 tile = 128x128")
    @IntegerRange(min = 0, max = 3)
    public final Integer maxTilePixels = 1;

    @Comment("The effective GUI scale for tiles and markers - independent of the overall GUI scale.")
    @Comment("0 will match your GUI scale - pixels will be the same size as the background & buttons")
    @Comment("-1 will use half your GUI scale, rounding up.")
    @Comment("-2 will use half your GUI scale, rounding down.")
    @IntegerRange(min = -2, max = 10)
    public final Integer mapScale = 0;

    @Comment("The maximum number of chunks to load onto the map per tick after entering a world")
    public final Integer chunkTickLimit = 100;

    @Comment("How to handle biomes that aren't in any minecraft, conventional, or forge biome tags")
    public final FallbackHandling fallbackFailHandling = FallbackHandling.MISSING;

    @Comment("Whether to show debug information about hovered tiles and markers")
    public final Boolean debugRender = false;

    public final Map<String, Boolean> structureMarkers = ValueMap.builder(true)
        .put("minecraft:type/end_city", false)
        .build();
}
