package folk.sisby.antique_atlas.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class DrawBatcher implements AutoCloseable {
	private final Matrix4f matrix4f;
	private final BufferBuilder bufferBuilder;
	private final float textureWidth;
	private final float textureHeight;

	public DrawBatcher(DrawContext context, Identifier texture, int textureWidth, int textureHeight) {
		RenderSystem.setShaderTexture(0, texture);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		this.matrix4f = context.getMatrices().peek().getPositionMatrix();
		this.bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	public void add(int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight) {
		this.innerAdd(x, x + width, y, y + height, 0,
			(u + 0.0F) / textureWidth,
			(u + (float) regionWidth) / textureWidth,
			(v + 0.0F) / textureHeight,
			(v + (float) regionHeight) / textureHeight
		);
	}

	private void innerAdd(int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
		bufferBuilder.vertex(matrix4f, x1, y1, z).texture(u1, v1);
		bufferBuilder.vertex(matrix4f, x1, y2, z).texture(u1, v2);
		bufferBuilder.vertex(matrix4f, x2, y2, z).texture(u2, v2);
		bufferBuilder.vertex(matrix4f, x2, y1, z).texture(u2, v1);
	}

	@Override
	public void close() {
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}
}
