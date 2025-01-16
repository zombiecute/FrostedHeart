package com.teammoeg.frostedheart.content.scenario.client.gui.layered;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL11;

import com.teammoeg.frostedheart.infrastructure.config.FHConfig;
import com.teammoeg.frostedheart.content.scenario.client.gui.layered.gl.TypedDynamicTexture;
import com.teammoeg.chorda.util.client.ClientUtils;
import com.teammoeg.chorda.util.client.Rect;

import net.minecraft.util.FastColor;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;

public class PrerenderParams {
	Graphics2D g2d;
	BufferedImage image;
	int width=1024*FHConfig.CLIENT.getScenarioScale();
	int height=576*FHConfig.CLIENT.getScenarioScale();
	double scale;
	public PrerenderParams() {
		image=new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		g2d=image.createGraphics();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		scale=ClientUtils.mc().getWindow().getGuiScale()/2;
	}
	public Graphics2D getG2d() {
		return g2d;
	}
	public BufferedImage getImage() {
		return image;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public static void freeTexture(int id) {
		GL11.glDeleteTextures(id);
	}
	public int calculateScaledX(int x) {
		/*if(x>width/2) {
			return (int) (width-((width-x)*scale));
		}
		return (int) (x*scale);*/
		return x*FHConfig.CLIENT.getScenarioScale()/2;
	}
	public int calculateScaledY(int y) {
		/*if(y>height/2) {
			return (int) (height-((height-y)*scale));
		}
		return (int) (y*scale);*/
		return y*FHConfig.CLIENT.getScenarioScale()/2;
	}
	public Rect calculateRect(int x,int y,int w,int h) {
		int nx=x*FHConfig.CLIENT.getScenarioScale()/2;
		int ny=y*FHConfig.CLIENT.getScenarioScale()/2;
		int nw=w*FHConfig.CLIENT.getScenarioScale()/2;
		int nh=h*FHConfig.CLIENT.getScenarioScale()/2;
		return new Rect(nx,ny,nw,nh);
	}
	public int calculateScaledSize(int s) {
		//return (int) (s*scale);
		return s*FHConfig.CLIENT.getScenarioScale()/2;
	}
	public TypedDynamicTexture loadTexture() {
		g2d.dispose();
		image.flush();
		BufferedImage cur=image;
		NativeImage texture=new NativeImage(Format.RGBA, cur.getWidth(), cur.getHeight(), false);//No need to close because dynamicTexture would handle this properly
		int[] pixels = new int[cur.getWidth() * cur.getHeight()];
		cur.getRGB(0, 0, cur.getWidth(), cur.getHeight(), pixels, 0, cur.getWidth());
		for (int y = 0; y < cur.getHeight(); y++) {
			for (int x = 0; x < cur.getWidth(); x++) {
				int pixel = pixels[y * cur.getWidth() + x];
				
				texture.setPixelRGBA(x, y,FastColor.ABGR32.color((pixel >> 24) & 0xFF,pixel & 0xFF,(pixel >> 8) & 0xFF, (pixel >> 16) & 0xFF));
			}
		}
		/*try {
			try (ByteArrayOutputStream baos=new ByteArrayOutputStream();ByteArrayInputStream bais=new ByteArrayInputStream(baos.toByteArray());){
				ImageIO.write(cur, "png", baos);
				texture = NativeImage.read(bais);
			} catch (IOException e) {
				e.printStackTrace();
				FHMain.LOGGER.error("Could not render scenario layer!");
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			FHMain.LOGGER.error("Error rendering scenario layer!");
		}*/
		
		return new TypedDynamicTexture(texture);
		/*int[] pixels = new int[cur.getWidth() * cur.getHeight()];
		cur.getRGB(0, 0, cur.getWidth(), cur.getHeight(), pixels, 0, cur.getWidth());

		ByteBuffer buffer = BufferUtils.createByteBuffer(cur.getWidth() * cur.getHeight() * 4+4);
		
		for (int y = 0; y < cur.getHeight(); y++) {
			for (int x = 0; x < cur.getWidth(); x++) {
				int pixel = pixels[y * cur.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
			}
		}
		buffer.flip(); // FOR THE LOVE OF GOD DO NOT FORGET THIS
		// You now have a ByteBuffer filled with the color data of each pixel.
		// Now just create a texture ID and bind it. Then you can load it using
		// whatever OpenGL method you want, for example:
		int textureID = GL11.glGenTextures(); // Generate texture ID
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); // Bind texture ID

		// Setup wrap mode
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		// Setup texture scaling filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		// Send texel data to OpenGL
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, cur.getWidth(), cur.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		// Return the texture ID so we can bind it later again
		return textureID;
		*/
	}
}
