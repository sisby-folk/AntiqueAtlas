package folk.sisby.antique_atlas;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColumnPos;

public record Marker(Identifier type, Text label, ColumnPos pos, boolean visibleAhead, boolean isGlobal) {
}
