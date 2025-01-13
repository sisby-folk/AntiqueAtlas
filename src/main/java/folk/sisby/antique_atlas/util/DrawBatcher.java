package folk.sisby.antique_atlas.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class DrawBatcher implements AutoCloseable {
	private final Matrix4f matrix4f;
	private final BufferBuilder bufferBuilder;
	private final VertexConsumer vertexConsumer;
	private final float textureWidth;
	private final float textureHeight;
	private final int light;

	public DrawBatcher(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier texture, int textureWidth, int textureHeight, int light) {
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);
		this.matrix4f = matrices.peek().getPositionMatrix();
		if (vertexConsumers == null) {
			this.bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
			this.vertexConsumer = bufferBuilder;
		} else {
			this.bufferBuilder = null;
			this.vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getText(texture));
		}
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.light = light;
	}

	public void add(int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight, int argb) {
		this.innerAdd(x, x + width, y, y + height, 0,
			(u + 0.0F) / textureWidth,
			(u + (float) regionWidth) / textureWidth,
			(v + 0.0F) / textureHeight,
			(v + (float) regionHeight) / textureHeight,
			argb
		);
	}

	private void innerAdd(int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, int argb) {
		vertexConsumer.vertex(matrix4f, x1, y1, z).color(argb).texture(u1, v1).light(light);
		vertexConsumer.vertex(matrix4f, x1, y2, z).color(argb).texture(u1, v2).light(light);
		vertexConsumer.vertex(matrix4f, x2, y2, z).color(argb).texture(u2, v2).light(light);
		vertexConsumer.vertex(matrix4f, x2, y1, z).color(argb).texture(u2, v1).light(light);
	}

	@Override
	public void close() {
		if (bufferBuilder != null) {
			BuiltBuffer bb = bufferBuilder.endNullable();
			if (bb != null) BufferRenderer.drawWithGlobalProgram(bb);
		}
	}
}
