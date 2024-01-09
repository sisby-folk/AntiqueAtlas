package folk.sisby.antique_atlas.resource.reloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.structure.StructureHandler;
import folk.sisby.antique_atlas.structure.StructurePieceTile;
import folk.sisby.antique_atlas.structure.StructurePieceTileXZ;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;

public class JigsawConfig extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = AntiqueAtlas.id("structures");
    private static final int VERSION = 2;

    public JigsawConfig() {
        super(new Gson(), "atlas/structures");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, StructurePieceTile> outMap = new HashMap<>();
        for (Map.Entry<Identifier, JsonElement> fileEntry : prepared.entrySet()) {
            Identifier fileId = fileEntry.getKey();
            try {
                JsonObject fileJson = fileEntry.getValue().getAsJsonObject();

                StructurePieceTile structurePieceTile;
                int version = fileJson.getAsJsonPrimitive("version").getAsInt();
                if (version == 1) {
                    structurePieceTile = new StructurePieceTile(
                        Identifier.tryParse(fileJson.get("tile").getAsString()),
                        fileJson.get("priority").getAsInt()
                    );
                } else if (version == VERSION) {
                    structurePieceTile = new StructurePieceTileXZ(
                        Identifier.tryParse(fileJson.get("tile_x").getAsString()),
                        Identifier.tryParse(fileJson.get("tile_z").getAsString()),
                        fileJson.get("priority").getAsInt()
                    );
                } else {
                    throw new RuntimeException("Incompatible version (" + VERSION + " != " + version + ")");
                }

                outMap.put(fileId, structurePieceTile);
            } catch (Exception e) {
                AntiqueAtlas.LOG.warn("Error reading structure piece config from " + fileId + "!", e);
            }
        }

        outMap.forEach((id, piece) -> {
            if (AntiqueAtlas.CONFIG.Performance.resourcePackLogging) {
                AntiqueAtlas.LOG.info("Apply structure piece config: " + id);
            }
            if (piece instanceof StructurePieceTileXZ) {
                StructureHandler.registerJigsawTile(id, piece.getPriority(), piece.getTileX(), StructureHandler::IF_X_DIRECTION);
                StructureHandler.registerJigsawTile(id, piece.getPriority(), piece.getTileZ(), StructureHandler::IF_Z_DIRECTION);
            } else {
                StructureHandler.registerJigsawTile(id, piece.getPriority(), piece.getTile());
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}
