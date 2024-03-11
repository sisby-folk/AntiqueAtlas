package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.TextureSets;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TextureSet implements Comparable<TextureSet> {
    /**
     * Name of the texture pack to write in the config file.
     */
    public final Identifier id;

    /**
     * The actual textures in this set.
     */
    public final Identifier[] textures;

    /**
     * Texture sets that a tile rendered with this set can be stitched to,
     * excluding itself.
     */
    private final Set<Identifier> stitchTo = new HashSet<>();
    private final Set<Identifier> stitchToHorizontal = new HashSet<>();
    private final Set<Identifier> stitchToVertical = new HashSet<>();
    private final Identifier[] texturePaths;
    private final boolean stitchesToNull = false;
    private boolean anisotropicStitching = false;

    /**
     * Name has to be unique, it is used for equals() tests.
     */
    public TextureSet(Identifier id, Identifier... textures) {
        this.id = id;
        this.texturePaths = textures;
        this.textures = new Identifier[textures.length];
    }

    /**
     * Add other texture sets that this texture set will be stitched to
     * (but the opposite may be false, in case of asymmetric stitching.)
     */
    @SuppressWarnings("UnusedReturnValue")
    public TextureSet stitchTo(Identifier... textureSets) {
        Collections.addAll(stitchTo, textureSets);
        Collections.addAll(stitchToHorizontal, textureSets);
        Collections.addAll(stitchToVertical, textureSets);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextureSet stitchToHorizontal(Identifier... textureSets) {
        this.anisotropicStitching = true;
        Collections.addAll(stitchToHorizontal, textureSets);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public TextureSet stitchToVertical(Identifier... textureSets) {
        this.anisotropicStitching = true;
        Collections.addAll(stitchToVertical, textureSets);
        return this;
    }

    /**
     * Actually used when stitching along the diagonal.
     */
    public boolean shouldStitchTo(TextureSet toSet) {
        return toSet == this || stitchesToNull && toSet == null || stitchTo.contains(toSet.id);
    }

    public boolean shouldStitchToHorizontally(TextureSet toSet) {
        if (toSet == this || stitchesToNull && toSet == null) return true;
        if (anisotropicStitching) return stitchToHorizontal.contains(toSet.id);
        else return stitchTo.contains(toSet.id);
    }

    public boolean shouldStitchToVertically(TextureSet toSet) {
        if (toSet == this || stitchesToNull && toSet == null) return true;
        if (anisotropicStitching) return stitchToVertical.contains(toSet.id);
        else return stitchTo.contains(toSet.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TextureSet set)) return false;
        return this.id.equals(set.id);
    }

    @Override
    public int compareTo(TextureSet textureSet) {
        return id.toString().compareTo(textureSet.id.toString());
    }

    public Identifier getTexture(int variationNumber) {
        return textures[variationNumber % textures.length];
    }

    public Identifier[] getTexturePaths() {
        return texturePaths;
    }

    public void loadTextures(Map<Identifier, Identifier> tileTextures) {
        for (int i = 0; i < texturePaths.length; i++) {
            if (!tileTextures.containsKey(texturePaths[i])) {
                throw new RuntimeException("Couldn't find the specified texture: " + texturePaths[i].toString());
            }
            textures[i] = tileTextures.get(texturePaths[i]);
        }
    }

    /**
     * This method goes through the list of all TextureSets this should stitch to and assert that these TextureSet exist
     */
    public void checkStitching() {
        stitchTo.stream().filter(identifier -> !TextureSets.getInstance().contains(identifier)).forEach(identifier ->
            AntiqueAtlas.LOGGER.error("The texture set {} tries to stitch to {}, which does not exist.", id, identifier));
        stitchToVertical.stream().filter(identifier -> !TextureSets.getInstance().contains(identifier)).forEach(identifier ->
            AntiqueAtlas.LOGGER.error("The texture set {} tries to stitch vertically to {}, which does not exist.", id, identifier));
        stitchToHorizontal.stream().filter(identifier -> !TextureSets.getInstance().contains(identifier)).forEach(identifier ->
            AntiqueAtlas.LOGGER.error("The texture set {} tries to stitch horizontally to {}, which does not exist.", id, identifier));
    }

    /**
     * A special texture set that is stitched to everything except water.
     */
    public static class TextureSetShore extends TextureSet {
        public final Identifier waterName;
        private TextureSet water;

        public TextureSetShore(Identifier id, Identifier water, Identifier... textures) {
            super(id, textures);
            this.waterName = water;
        }

        public void loadWater() {
            water = TextureSets.getInstance().get(waterName);
        }

        @Override
        public boolean shouldStitchToHorizontally(TextureSet otherSet) {
            return otherSet == this || !water.shouldStitchToHorizontally(otherSet);
        }

        @Override
        public boolean shouldStitchToVertically(TextureSet otherSet) {
            return otherSet == this || !water.shouldStitchToVertically(otherSet);
        }
    }
}
