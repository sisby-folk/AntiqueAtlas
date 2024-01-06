package folk.sisby.antique_atlas.network.packet.c2s.play;

import dev.architectury.networking.NetworkManager;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.api.AtlasAPI;
import folk.sisby.antique_atlas.network.packet.c2s.C2SPacket;
import folk.sisby.antique_atlas.util.Log;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * Packet used to save the last browsing position for a dimension in an atlas.
 * @author Hunternif
 * @author Haven King
 */
public class PutBrowsingPositionC2SPacket extends C2SPacket {
	public static final Identifier ID = AntiqueAtlas.id("packet", "c2s", "browsing_position", "put");

	public PutBrowsingPositionC2SPacket(int atlasID, RegistryKey<World> world, int x, int y, double zoom) {
		this.writeVarInt(atlasID);
		this.writeIdentifier(world.getValue());
		this.writeVarInt(x);
		this.writeVarInt(y);
		this.writeDouble(zoom);
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	public static void apply(PacketByteBuf buf, NetworkManager.PacketContext context) {
		int atlasID = buf.readVarInt();
		RegistryKey<World> world = RegistryKey.of(Registry.WORLD_KEY, buf.readIdentifier());
		int x = buf.readVarInt();
		int y = buf.readVarInt();
		double zoom = buf.readDouble();

		context.queue(() -> {
			if (AtlasAPI.getPlayerAtlasId(context.getPlayer()) != atlasID) {
				Log.warn("Player %s attempted to put position marker into someone else's Atlas #%d",
						context.getPlayer().getCommandSource().getName(), atlasID);
				return;
			}

			AntiqueAtlas.tileData.getData(atlasID, context.getPlayer().getEntityWorld())
					.getWorldData(world).setBrowsingPosition(x, y, zoom);
		});
	}
}
