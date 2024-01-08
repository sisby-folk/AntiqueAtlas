package folk.sisby.antique_atlas.core;

import folk.sisby.antique_atlas.util.Rect;
import net.minecraft.util.Identifier;

public interface ITileStorage {

    /**
     * Retrieves the tile set a given position
     * @return either the tile stored for the coords or null
     */
    Identifier getTile(int x, int y);

    Rect getScope();
}
