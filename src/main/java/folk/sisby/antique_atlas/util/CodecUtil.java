package folk.sisby.antique_atlas.util;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CodecUtil {
	public static <T> Codec<Set<T>> set(Codec<T> codec) {
		return codec.listOf().xmap(HashSet::new, ArrayList::new);
	}

	public static <T extends Enum<T>> Codec<T> ofEnum(Class<T> enumClass) {
		return Codec.STRING.flatXmap(id -> {
			try {
				return DataResult.success(Enum.valueOf(enumClass, id.toUpperCase(Locale.ROOT)));
			} catch (Exception e) {
				return DataResult.error(() -> "Unknown type: " + id);
			}
		}, value -> DataResult.success(value.name()));
	}

	public record CodecResourceMetadataSerializer<T>(Codec<T> codec, Identifier id) implements ResourceMetadataReader<T> {
		@Override
		public @NotNull String getKey() {
			return id.toString();
		}

		@Override
		public @NotNull T fromJson(JsonObject json) {
			DataResult<T> result = codec.parse(JsonOps.INSTANCE, json);
			if (result.error().isPresent()) {
				throw new IllegalStateException("Failed to parse " + id + " metadata section: " + result.error().get());
			}
			if (result.result().isEmpty()) {
				throw new IllegalStateException("Failed to parse " + id + " metadata section: Empty result");
			}
			return result.result().get();
		}
	}
}
