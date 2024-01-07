package folk.sisby.antique_atlas.api.client.impl;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.api.client.ClientTileAPI;
import folk.sisby.antique_atlas.client.TileRenderIterator;
import folk.sisby.antique_atlas.core.AtlasData;
import folk.sisby.antique_atlas.core.TileDataStorage;
import folk.sisby.antique_atlas.client.network.packet.PutTileC2SPacket;
import folk.sisby.antique_atlas.util.Log;
import folk.sisby.antique_atlas.util.Rect;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class TileApiImplClient implements ClientTileAPI {
    @Override
    public void putTile(World world, int atlasID, Identifier tile, int chunkX, int chunkZ) {
        new PutTileC2SPacket(atlasID, chunkX, chunkZ, tile).send();
    }

    @Override
    public Identifier getTile(World world, int atlasID, int chunkX, int chunkZ) {
        AtlasData data = AntiqueAtlas.tileData.getData(atlasID, world);
        return data.getWorldData(world.getRegistryKey()).getTile(chunkX, chunkZ);
    }

    @Override
    public void putGlobalTile(World world, Identifier tile, int chunkX, int chunkZ) {
        Log.warn("Client attempted to put global tile");
    }

    @Override
    public Identifier getGlobalTile(World world, int chunkX, int chunkZ) {
        TileDataStorage data = AntiqueAtlas.globalTileData.getData(world);
        return data.getTile(chunkX, chunkZ);
    }

    @Override
    public void deleteGlobalTile(World world, int chunkX, int chunkZ) {
        Log.warn("Client attempted to delete global tile");
    }

    @Override
    public TileRenderIterator getTiles(World world, int atlasID, Rect scope, int step) {
        TileRenderIterator iter = new TileRenderIterator(AntiqueAtlas.tileData
            .getData(atlasID, world)
            .getWorldData(world.getRegistryKey()));
        iter.setScope(scope);
        iter.setStep(step);
        return iter;
    }
}
