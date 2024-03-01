package folk.sisby.antique_atlas;

import folk.sisby.surveyor.landmark.LandmarkType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;

import java.util.UUID;

public record Marker(LandmarkType<?> landmarkType, Identifier type, Text label, BlockPos blockPos, boolean visibleAhead, UUID owner) {
    public boolean isGlobal() {
        return MinecraftClient.getInstance().isIntegratedServerRunning() ? owner == null : !Uuids.getUuidFromProfile(MinecraftClient.getInstance().getSession().getProfile()).equals(owner);
    }

    public ColumnPos pos() {
        return new ColumnPos(blockPos.getX(), blockPos.getZ());
    }
}
