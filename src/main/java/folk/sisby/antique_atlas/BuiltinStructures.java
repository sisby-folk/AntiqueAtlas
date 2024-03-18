package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.StructureTileProviders;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;

public class BuiltinStructures {
    public static void init() {
        if (AntiqueAtlas.CONFIG.markVillages) StructureTileProviders.getInstance().registerMarker(StructureTags.VILLAGE, new Identifier("antique_atlas:village"), Text.translatable("gui.antique_atlas.marker.village"));
        StructureTileProviders.getInstance().registerMarker(StructureType.END_CITY, new Identifier("antique_atlas:end_city"), Text.literal(""));
    }
}
