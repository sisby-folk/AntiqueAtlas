package folk.sisby.antique_atlas.client.texture;

import folk.sisby.antique_atlas.client.gui.tiles.SubTile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * A specialized class for textures used as tiles in the atlas map.
 * It has a special method to draw SubTile instances given a size of the map.
 */
public class TileTexture extends Texture {
    public TileTexture(Identifier texture) {
        super(texture, 32, 48);
    }

    public void drawSubTile(DrawContext context, SubTile subtile, int tileHalfSize) {
        draw(context, subtile.x * tileHalfSize, subtile.y * tileHalfSize, tileHalfSize, tileHalfSize, subtile.getTextureU() * 8, subtile.getTextureV() * 8, 8, 8);
    }
}
