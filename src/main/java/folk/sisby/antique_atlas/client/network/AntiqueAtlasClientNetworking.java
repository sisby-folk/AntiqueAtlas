package folk.sisby.antique_atlas.client.network;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.AntiqueAtlasClient;
import folk.sisby.antique_atlas.client.MarkerType;
import folk.sisby.antique_atlas.client.gui.GuiAtlas;
import folk.sisby.antique_atlas.core.AtlasData;
import folk.sisby.antique_atlas.core.TileDataStorage;
import folk.sisby.antique_atlas.core.TileGroup;
import folk.sisby.antique_atlas.core.TileInfo;
import folk.sisby.antique_atlas.core.WorldData;
import folk.sisby.antique_atlas.marker.Marker;
import folk.sisby.antique_atlas.marker.MarkerData;
import folk.sisby.antique_atlas.marker.MarkersData;
import folk.sisby.antique_atlas.network.AntiqueAtlasNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class AntiqueAtlasClientNetworking {
    private static final int GLOBAL = -1;

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_DELETE_GLOBAL_TILE, AntiqueAtlasClientNetworking::handleDeleteGlobalTile);
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_DELETE_MARKER, AntiqueAtlasClientNetworking::handleDeleteMarker);
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_DIMENSION_UPDATE, AntiqueAtlasClientNetworking::handleDimensionUpdate);
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_MAP_DATA, AntiqueAtlasClientNetworking::handleMapData);
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_PUT_GLOBAL_TILE, AntiqueAtlasClientNetworking::handlePutGlobalTile);
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_PUT_MARKERS, AntiqueAtlasClientNetworking::handlePutMarkers);
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_PUT_TILE, AntiqueAtlasClientNetworking::handlePutTile);
        ClientPlayNetworking.registerGlobalReceiver(AntiqueAtlasNetworking.S2C_TILE_GROUPS, AntiqueAtlasClientNetworking::handleTileGroups);
    }

    public static void handleDeleteGlobalTile(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier());
        int chunkX = buf.readVarInt();
        int chunkZ = buf.readVarInt();

        TileDataStorage data = AntiqueAtlas.globalTileData.getData(world);
        data.removeTile(chunkX, chunkZ);
    }

    public static void handleDeleteMarker(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        int markerID = buf.readVarInt();

        MarkersData data = atlasID == GLOBAL ?
            AntiqueAtlas.globalMarkersData.getData() :
            AntiqueAtlas.markersData.getMarkersData(atlasID, client.player.getEntityWorld());
        data.removeMarker(markerID);
        AntiqueAtlasClient.getAtlasGUI().updateBookmarkerList();
    }

    public static void handleDimensionUpdate(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier());
        int tileCount = buf.readVarInt();

        if (world == null) return;

        List<TileInfo> tiles = new ArrayList<>();
        for (int i = 0; i < tileCount; ++i) {
            tiles.add(new TileInfo(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readIdentifier())
            );
        }

        AtlasData data = AntiqueAtlas.tileData.getData(atlasID, client.player.getEntityWorld());

        for (TileInfo info : tiles) {
            data.getWorldData(world).setTile(info.x, info.z, info.id);
        }
    }

    public static void handleMapData(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        NbtCompound data = buf.readNbt();

        if (data == null) return;

        AtlasData atlasData = AntiqueAtlas.tileData.getData(atlasID, client.player.getEntityWorld());
        atlasData.updateFromNbt(data);

        if (AntiqueAtlas.CONFIG.Gameplay.doSaveBrowsingPos && MinecraftClient.getInstance().currentScreen instanceof GuiAtlas) {
            ((GuiAtlas) MinecraftClient.getInstance().currentScreen).loadSavedBrowsingPosition();
        }
    }

    public static void handlePutGlobalTile(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier());
        int tileCount = buf.readVarInt();

        TileDataStorage data = AntiqueAtlas.globalTileData.getData(world);
        for (int i = 0; i < tileCount; ++i) {
            data.setTile(buf.readVarInt(), buf.readVarInt(), buf.readIdentifier());
        }
    }

    public static void handlePutMarkers(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier());
        int typesLength = buf.readVarInt();

        ListMultimap<Identifier, MarkerData> markersByType = ArrayListMultimap.create();
        for (int i = 0; i < typesLength; ++i) {
            Identifier type = buf.readIdentifier();
            int markersLength = buf.readVarInt();
            for (int j = 0; j < markersLength; ++j) {
                markersByType.put(type, new MarkerData(buf));
            }
        }

        MarkersData markersData = atlasID == GLOBAL
            ? AntiqueAtlas.globalMarkersData.getData()
            : AntiqueAtlas.markersData.getMarkersDataCached(atlasID, world);

        for (Identifier type : markersByType.keys()) {
            MarkerType markerType = MarkerType.REGISTRY.get(type);
            for (MarkerData precursor : markersByType.get(type)) {
                markersData.loadMarker(new Marker(MarkerType.REGISTRY.getId(markerType), world, precursor));
            }
        }

        AntiqueAtlasClient.getAtlasGUI().updateBookmarkerList();
    }

    public static void handlePutTile(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier());
        int x = buf.readVarInt();
        int z = buf.readVarInt();
        Identifier tile = buf.readIdentifier();

        AtlasData data = AntiqueAtlas.tileData.getData(atlasID, client.player.getEntityWorld());
        data.setTile(world, x, z, tile);
    }

    public static void handleTileGroups(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int atlasID = buf.readVarInt();
        RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier());
        int length = buf.readVarInt();
        List<TileGroup> tileGroups = new ArrayList<>(length);

        for (int i = 0; i < length; ++i) {
            NbtCompound tag = buf.readNbt();

            if (tag != null) {
                tileGroups.add(TileGroup.fromNBT(tag));
            }
        }

        AtlasData atlasData = AntiqueAtlas.tileData.getData(atlasID, client.player.getEntityWorld());
        WorldData worldData = atlasData.getWorldData(world);
        for (TileGroup t : tileGroups) {
            worldData.putTileGroup(t);
        }
    }
}
