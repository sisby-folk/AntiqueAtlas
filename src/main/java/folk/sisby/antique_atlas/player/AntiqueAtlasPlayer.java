package folk.sisby.antique_atlas.player;

import net.minecraft.util.math.ChunkPos;

import java.util.Set;

public interface AntiqueAtlasPlayer {
    String KEY_DATA = "antique_atlas";
    String KEY_EXPLORED_CHUNKS = "exploredChunks";

    Set<ChunkPos> antiqueAtlas$getExploredChunks();

    void antiqueAtlas$addExploredChunk(ChunkPos pos);
}
