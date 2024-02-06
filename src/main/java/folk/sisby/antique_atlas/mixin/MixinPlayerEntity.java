package folk.sisby.antique_atlas.mixin;

import folk.sisby.antique_atlas.player.AntiqueAtlasPlayer;
import folk.sisby.antique_atlas.player.PlayerEventHandler;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public class MixinPlayerEntity implements AntiqueAtlasPlayer {
    @Unique private final Set<ChunkPos> antiqueAtlas$exploredChunks = new HashSet<>();

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo info) {
        PlayerEventHandler.onPlayerDeath((PlayerEntity) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    public void writeExploredChunks(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound modCompound = new NbtCompound();
        List<Integer> coordList = new ArrayList<>();
        antiqueAtlas$exploredChunks.forEach(pos -> {
            coordList.add(pos.x);
            coordList.add(pos.z);
        });
        NbtIntArray coordArray = new NbtIntArray(coordList);
        modCompound.put(KEY_EXPLORED_CHUNKS, coordArray);
        nbt.put(KEY_DATA, modCompound);
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    public void readExploredChunks(NbtCompound nbt, CallbackInfo ci) {
        antiqueAtlas$exploredChunks.clear();
        int[] coordArray = nbt.getCompound(KEY_DATA).getIntArray(KEY_EXPLORED_CHUNKS);
        for (int i = 1; i < coordArray.length; i += 2) {
            antiqueAtlas$exploredChunks.add(new ChunkPos(coordArray[i - 1], coordArray[i]));
        }
    }

    @Inject(at = @At("TAIL"), method = "copyFrom")
    public void copyExploredChunks(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (oldPlayer instanceof AntiqueAtlasPlayer them) {
            antiqueAtlas$exploredChunks.clear();
            antiqueAtlas$exploredChunks.addAll(them.antiqueAtlas$getExploredChunks());
        }
    }

    @Override
    public Set<ChunkPos> antiqueAtlas$getExploredChunks() {
        return antiqueAtlas$exploredChunks;
    }

    @Override
    public void antiqueAtlas$addExploredChunk(ChunkPos pos) {
        antiqueAtlas$exploredChunks.add(pos);
    }
}