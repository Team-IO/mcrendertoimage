package net.teamio.mcrendertoimage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Created by oliver on 2018-06-20.
 */
public class RenderTools {

	private final Minecraft mc;
	private final FontRenderer fontRenderer;
	private final TextureManager renderEngine;
	private final TileEntityRendererDispatcher tERD;

	private int width = 100;
	private int height = 100;

	private File file;

	public RenderTools() {
		this.mc = Minecraft.getMinecraft();
		this.fontRenderer = mc.fontRendererObj;

		tERD = TileEntityRendererDispatcher.instance;
		TextureManager renderEngine = tERD.renderEngine;

		if (renderEngine == null) {
			renderEngine = mc.renderEngine;

			Iterator iterator = tERD.mapSpecialRenderers.values().iterator();

			while (iterator.hasNext()) {
				TileEntitySpecialRenderer tileentityspecialrenderer = (TileEntitySpecialRenderer) iterator.next();
				tileentityspecialrenderer.setRendererDispatcher(tERD);
			}
		}
		this.renderEngine = renderEngine;
	}

	public void setUpGL() {

	}


	public void prepareRendering(String filename, float camX, float camY, float camZ) {
		File dir = new File(Minecraft.getMinecraft().mcDataDir, "renders/");
		if (!dir.exists() && !dir.mkdirs()) {
			throw new RuntimeException("The recipes directory could not be created: " + dir);
		}

		file = new File(Minecraft.getMinecraft().mcDataDir, "renders/" + filename + ".png");

		GL11.glPushMatrix();
		//GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		//GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);

		GlStateManager.enableAlpha();
		GlStateManager.clearColor(1, 1, 1, 0);

		float ratio = (float) width / height;

		GL11.glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		//Project.gluPerspective(90, (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, 3000 * MathHelper.SQRT_2);

		//GL11.glFrustum(-ratio, ratio, -1, 1, 1, 100);
		GLU.gluPerspective(60.0f, ratio, .1f, 3000.0f);

		//GL11.glOrtho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();


		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();


		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GLU.gluLookAt(camX, camY, camZ, .5f, .5f, .5f, 0, 1f, 0);

		GL11.glPushMatrix();
	}

	public void render() {
	}

	public void endRendering() {

		ByteBuffer fb = ByteBuffer.allocateDirect(width * height * 4);

		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, fb);
		GL11.glPopMatrix();
		GL11.glPopMatrix();
		//GL11.glPopAttrib();
		//GL11.glPopClientAttrib();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		//try {
			Display.update();
			//Display.swapBuffers();
		//} catch (LWJGLException e1) {
		//	e1.printStackTrace();
		//}

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int i = (x + (width * y)) * 4;
				int r = fb.get(i) & 0xFF;
				int g = fb.get(i + 1) & 0xFF;
				int b = fb.get(i + 2) & 0xFF;
				int a = fb.get(i + 3) & 0xFF;

				int rgba = new Color(r, g, b, a).getRGB();
				image.setRGB(x, height - (y + 1), rgba);
			}
		}

		try {
			ImageIO.write(image, "png", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void RenderAt(IBlockAccess world, BlockPos pos, String filename, float camX, float camY, float camZ) {
		RenderTools tools = new RenderTools();

		tools.prepareRendering(filename, camX, camY, camZ);
		tools.render();

		TileEntity te = world.getTileEntity(pos);
		if (te != null) {
			GL11.glPushMatrix();

			tools.tERD.renderTileEntityAt(te, 0, 0, 0, 0, -1);

			GL11.glPopMatrix();
		}
		GL11.glPushMatrix();

		//GlStateManager.Profile.DEFAULT.apply();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

		tools.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableTexture2D();
		GlStateManager.enableColorMaterial();
		GlStateManager.disableFog();
		GlStateManager.colorMaterial(1028, 4608);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		//GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableCull();
		GlStateManager.alphaFunc(516, 0.5F);
		GlStateManager.glNormal3f(0.0F, -1.0F, 0.0F);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		//GlStateManager.depthMask(false);
		//GlStateManager.shadeModel(7425);

		VertexBuffer vertexBuffer = new VertexBuffer(2097152);
		vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);

		BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		GL11.glTranslatef(-pos.getX(), -pos.getY(), -pos.getZ());
		//blockrendererdispatcher.renderBlock(world.getBlockState(pos), pos, world, vertexBuffer);
		renderBlock(blockrendererdispatcher, world.getBlockState(pos), pos, world, vertexBuffer);

		vertexBuffer.finishDrawing();


		WorldVertexBufferUploader worldVertexUploader = new WorldVertexBufferUploader();
		worldVertexUploader.draw(vertexBuffer);
		vertexBuffer.reset();

		GL11.glPopMatrix();

		tools.endRendering();
	}

	private static boolean renderBlock(BlockRendererDispatcher blockrendererdispatcher, IBlockState state, BlockPos pos, IBlockAccess blockAccess, VertexBuffer worldRendererIn) {
		try {
			EnumBlockRenderType enumblockrendertype = state.getRenderType();

			if (enumblockrendertype == EnumBlockRenderType.INVISIBLE) {
				return false;
			} else {
				if (blockAccess.getWorldType() != WorldType.DEBUG_WORLD) {
					try {
						state = state.getActualState(blockAccess, pos);
					} catch (Exception var8) {
						;
					}
				}

				switch (enumblockrendertype) {
					case MODEL:
						IBakedModel model = blockrendererdispatcher.getModelForState(state);
						state = state.getBlock().getExtendedState(state, blockAccess, pos);
						return blockrendererdispatcher.getBlockModelRenderer().renderModel(blockAccess, model, state, pos, worldRendererIn, false);
					case ENTITYBLOCK_ANIMATED:
						return false;
					//case LIQUID:
					//return blockrendererdispatcher.fluidRenderer.renderFluid(blockAccess, state, pos, worldRendererIn);
					default:
						return false;
				}
			}
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
			CrashReportCategory.addBlockInfo(crashreportcategory, pos, state.getBlock(), state.getBlock().getMetaFromState(state));
			throw new ReportedException(crashreport);
		}
	}


	public static void RenderAt(IBlockAccess world, BlockPos pos) {

		Vec3d positionEyes = Minecraft.getMinecraft().thePlayer.getPositionEyes(0);
		positionEyes = positionEyes.addVector(0.5f, 0.5f, 0.5f).subtract(pos.getX(), pos.getY(), pos.getZ());
		RenderAt(world, pos, "0", (float)positionEyes.xCoord, (float)positionEyes.yCoord, (float)positionEyes.zCoord);

		float distance = 1;
		RenderAt(world, pos, "1", 0.5f + distance, 1.5f, 0.5f + distance);
		RenderAt(world, pos, "2", 0.5f - distance, 1.5f, 0.5f + distance);
		RenderAt(world, pos, "3", 0.5f - distance, 1.5f, 0.5f - distance);
		RenderAt(world, pos, "4", 0.5f + distance, 1.5f, 0.5f - distance);

	}

}
