package folk.sisby.antique_atlas.player;

import net.minecraft.util.math.ChunkPos;

import java.util.Set;

public interface AntiqueAtlasPlayer {
    String KEY_DATA = "antique_atlas";
    String KEY_EXPLORED_CHUNKS = "exploredChunks";
    String KEY_X = "x";
    String KEY_Z = "z";

    Set<ChunkPos> antiqueAtlas$getExploredChunks();

    void antiqueAtlas$addExploredChunk(ChunkPos pos);
}
