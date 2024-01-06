package folk.sisby.antique_atlas.structure;

import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.text.Text;
import net.minecraft.world.gen.structure.StructureType;

public class EndCity {

    public static void registerMarkers() {
        StructureHandler.registerMarker(StructureType.END_CITY, AntiqueAtlas.id("end_city"), Text.literal(""));
    }

}
