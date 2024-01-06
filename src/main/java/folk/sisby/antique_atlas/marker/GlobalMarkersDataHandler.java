package folk.sisby.antique_atlas.marker;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Handles the world-saved data with global markers.
 * <p>
 * When in single player, this instance is shared between the client and the
 * server, so the packet-based synchronization becomes redundant.
 * </p>
 * <p>
 * When connecting to a remote server, data has to be reset, see
 * {@link #onClientConnectedToServer(ClientPlayNetworkHandler, PacketSender, MinecraftClient)}
 * </p>
 * @author Hunternif
 */
public class GlobalMarkersDataHandler {
	private static final String DATA_KEY = "aAtlasGlobalMarkers";

	private GlobalMarkersData data;

	public void onWorldLoad(MinecraftServer server, ServerWorld world) {
		if (world.getRegistryKey() == World.OVERWORLD) {
			data = world.getPersistentStateManager().getOrCreate(GlobalMarkersData::readNbt, () -> {
				GlobalMarkersData data = new GlobalMarkersData();
				data.markDirty();
				return data;
			}, DATA_KEY);
		}
	}

	/**
	 * This method sets {@link #data} to null when the client connects to a
	 * remote server. It is required in order that global markers data is not
	 * transferred from a previous world the client visited.
	 * <p>
	 * Using a "connect" event instead of "disconnect" because according to a
	 * form post, the latter event isn't actually fired on the client.
	 * </p>
	 */
	public void onClientConnectedToServer(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
		if (!client.isIntegratedServerRunning()) {
			data = null;
		}
	}

	public GlobalMarkersData getData() {
		if (data == null) { // This will happen on the client
			data = new GlobalMarkersData();
		}
		return data;
	}

	/** Synchronizes global markers with the connecting client. */
	public void onPlayerLogin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		data.syncOnPlayer(handler.getPlayer());
	}
}
