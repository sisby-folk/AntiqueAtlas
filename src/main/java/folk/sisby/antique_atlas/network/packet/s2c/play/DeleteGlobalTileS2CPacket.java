package folk.sisby.antique_atlas.network.packet.s2c.play;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.core.TileDataStorage;
import folk.sisby.antique_atlas.network.packet.s2c.S2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Sent from server to client to remove a custom global tile.
 *
 * @author Hunternif
 * @author Haven King
 */
public class DeleteGlobalTileS2CPacket extends S2CPacket {
    public static final Identifier ID = AntiqueAtlas.id("packet.c2s.global_tile.delete");

    public DeleteGlobalTileS2CPacket(RegistryKey<World> world, int chunkX, int chunkZ) {
        this.writeIdentifier(world.getValue());
        this.writeVarInt(chunkX);
        this.writeVarInt(chunkZ);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public static void apply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        RegistryKey<World> world = RegistryKey.of(Registry.WORLD_KEY, buf.readIdentifier());
        int chunkX = buf.readVarInt();
        int chunkZ = buf.readVarInt();

        TileDataStorage data = AntiqueAtlas.globalTileData.getData(world);
        data.removeTile(chunkX, chunkZ);
    }
}
