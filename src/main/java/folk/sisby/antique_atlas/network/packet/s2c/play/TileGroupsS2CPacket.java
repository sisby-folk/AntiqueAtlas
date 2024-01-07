package folk.sisby.antique_atlas.network.packet.s2c.play;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.core.AtlasData;
import folk.sisby.antique_atlas.core.TileGroup;
import folk.sisby.antique_atlas.core.WorldData;
import folk.sisby.antique_atlas.network.packet.s2c.S2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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


/**
 * Syncs tile groups to the client.
 *
 * @author Hunternif
 * @author Haven King
 */
public class TileGroupsS2CPacket extends S2CPacket {
    public static final int TILE_GROUPS_PER_PACKET = 100;
    public static final Identifier ID = AntiqueAtlas.id("packet.s2c.tile.groups");

    public TileGroupsS2CPacket(int atlasID, RegistryKey<World> world, List<TileGroup> tileGroups) {
        this.writeVarInt(atlasID);
        this.writeIdentifier(world.getValue());
        this.writeVarInt(tileGroups.size());

        for (TileGroup tileGroup : tileGroups) {
            this.writeNbt(tileGroup.writeToNBT(new NbtCompound()));
        }
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
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
