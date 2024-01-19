package folk.sisby.antique_atlas.mixin;

import folk.sisby.antique_atlas.player.PlayerEventHandler;
import folk.sisby.antique_atlas.player.AntiqueAtlasPlayer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
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
        NbtList chunkList = new NbtList();
        for (ChunkPos pos : antiqueAtlas$exploredChunks) {
            NbtCompound chunkCompound = new NbtCompound();
            chunkCompound.putInt(KEY_X, pos.x);
            chunkCompound.putInt(KEY_Z, pos.z);
            chunkList.add(chunkCompound);
        }
        modCompound.put(KEY_EXPLORED_CHUNKS, chunkList);
        nbt.put(KEY_DATA, modCompound);
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    public void readExploredChunks(NbtCompound nbt, CallbackInfo ci) {
        antiqueAtlas$exploredChunks.clear();
        for (NbtElement posElement : nbt.getCompound(KEY_DATA).getList(KEY_EXPLORED_CHUNKS, NbtElement.COMPOUND_TYPE)) {
            antiqueAtlas$exploredChunks.add(new ChunkPos(((NbtCompound) posElement).getInt(KEY_X), ((NbtCompound) posElement).getInt(KEY_Z)));
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