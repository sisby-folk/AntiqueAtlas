package folk.sisby.antique_atlas.client.resource.reloader;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.client.MarkerType;
import folk.sisby.antique_atlas.client.resource.MarkerTypes;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MarkerMipsConfig implements ResourceReloader, IdentifiableResourceReloadListener {
    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.completedFuture(null).thenCompose(synchronizer::whenPrepared).thenCompose(t -> CompletableFuture.runAsync(() -> {
            for (MarkerType type : MarkerTypes.REGISTRY) {
                type.initMips();
            }
        }, applyExecutor));
    }

    @Override
    public Identifier getFabricId() {
        return AntiqueAtlas.id("marker_mips");
    }
}
