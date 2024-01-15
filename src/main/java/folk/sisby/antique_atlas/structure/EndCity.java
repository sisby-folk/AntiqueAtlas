package folk.sisby.antique_atlas.structure;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.data.StructureTiles;
import net.minecraft.text.Text;
import net.minecraft.world.gen.structure.StructureType;

public class EndCity {

    public static void registerMarkers() {
        StructureTiles.getInstance().registerMarker(StructureType.END_CITY, AntiqueAtlas.id("end_city"), Text.literal(""));
    }

}
