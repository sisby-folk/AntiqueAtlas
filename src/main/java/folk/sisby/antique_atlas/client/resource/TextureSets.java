package folk.sisby.antique_atlas.client.resource;

import folk.sisby.antique_atlas.client.TextureSet;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps texture sets to their names.
 *
 * @author Hunternif
 */
public class TextureSets {
    private static final TextureSets INSTANCE = new TextureSets();

    public static TextureSets getInstance() {
        return INSTANCE;
    }

    private final Map<Identifier, TextureSet> map = new HashMap<>();

    public void register(TextureSet set) {
        map.put(set.name, set);
    }

    public TextureSet getByName(Identifier name) {
        return map.get(name);
    }

    static public boolean isRegistered(Identifier name) {
        return INSTANCE.map.containsKey(name);
    }
}
