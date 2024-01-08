package folk.sisby.antique_atlas.api.impl;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.api.TileAPI;
import folk.sisby.antique_atlas.core.AtlasData;
import folk.sisby.antique_atlas.core.TileDataStorage;
import folk.sisby.antique_atlas.network.s2c.DeleteGlobalTileS2CPacket;
import folk.sisby.antique_atlas.network.s2c.PutGlobalTileS2CPacket;
import folk.sisby.antique_atlas.network.s2c.PutTileS2CPacket;
import folk.sisby.antique_atlas.util.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;


public class TileApiImpl implements TileAPI {
    public TileApiImpl() {
    }

    public void putTile(World world, int atlasID, Identifier tile, int chunkX, int chunkZ) {
        if (tile == null) {
            Log.error("Attempted to put custom tile with null name");
            return;
        }

        RegistryKey<World> dimension = world.getRegistryKey();
        AtlasData data = AntiqueAtlas.tileData.getData(atlasID, world);
        data.setTile(dimension, chunkX, chunkZ, tile);
        for (PlayerEntity syncedPlayer : data.getSyncedPlayers()) {
            new PutTileS2CPacket(atlasID, dimension, chunkX, chunkZ, tile).send((ServerPlayerEntity) syncedPlayer);
        }
    }

    @Override
    public Identifier getTile(World world, int atlasID, int chunkX, int chunkZ) {
        AtlasData data = AntiqueAtlas.tileData.getData(atlasID, world);

        return data.getWorldData(world.getRegistryKey()).getTile(chunkX, chunkZ);
    }

    @Override
    public void putGlobalTile(World world, Identifier tileId, int chunkX, int chunkZ) {
        if (tileId == null) {
            Log.error("Attempted to put global tile with null name");
            return;
        }

        if (world.isClient) {
            Log.warn("Client attempted to put global tile");
            return;
        }

        TileDataStorage data = AntiqueAtlas.globalTileData.getData(world);
        data.setTile(chunkX, chunkZ, tileId);

        // Send tile packet:
        new PutGlobalTileS2CPacket(world.getRegistryKey(), chunkX, chunkZ, tileId).send((ServerWorld) world);
    }

    @Override
    public Identifier getGlobalTile(World world, int chunkX, int chunkZ) {
        TileDataStorage data = AntiqueAtlas.globalTileData.getData(world);
        return data.getTile(chunkX, chunkZ);
    }

    @Override
    public void deleteGlobalTile(World world, int chunkX, int chunkZ) {
        TileDataStorage data = AntiqueAtlas.globalTileData.getData(world);
        if (data.getTile(chunkX, chunkZ) != null) {
            data.removeTile(chunkX, chunkZ);
            new DeleteGlobalTileS2CPacket(world.getRegistryKey(), chunkX, chunkZ).send((ServerWorld) world);
        }
    }
}
