package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.reloader.StructureTileProviders;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.LandmarkType;
import folk.sisby.surveyor.landmark.SimpleLandmarkType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record AtlasStructureLandmark(BlockPos pos, StructureTileProviders.ProviderType providerType, Identifier providedId) implements Landmark<AtlasStructureLandmark> {
	public static LandmarkType<AtlasStructureLandmark> TYPE = new SimpleLandmarkType<>(
		Identifier.of(AntiqueAtlas.ID, "structure"),
		null
	);

	public Identifier displayId() {
		return Identifier.of(providedId.getNamespace(), providerType.prefix() + providedId.getPath());
	}

	@Override
	public Text name() {
		return Text.translatable(providerType.translation(providedId));
	}

	@Override
	public LandmarkType<AtlasStructureLandmark> type() {
		return TYPE;
	}
}
