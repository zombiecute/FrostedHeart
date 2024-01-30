package com.teammoeg.frostedheart.scenario.client.gui.layered;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.teammoeg.frostedheart.client.util.Rect;

import blusunrize.immersiveengineering.client.ClientUtils;

public class PrerenderParams {
	Graphics2D g2d;
	BufferedImage image;
	int width=2048;
	int height=1152;
	double scale;
	public PrerenderParams() {
		image=new BufferedImage(width,width, BufferedImage.TYPE_INT_ARGB);
		g2d=image.createGraphics();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		scale=ClientUtils.mc().getMainWindow().getGuiScaleFactor()/2;
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
		return x;
	}
	public int calculateScaledY(int y) {
		/*if(y>height/2) {
			return (int) (height-((height-y)*scale));
		}
		return (int) (y*scale);*/
		return y;
	}
	public Rect calculateRect(int x,int y,int w,int h) {
		/*int nx=calculateScaledX(x);
		int ny=calculateScaledY(y);
		int nw=calculateScaledX(x+w)-nx;
		int nh=calculateScaledY(y+h)-ny;*/
		return new Rect(x,y,w,h);
	}
	public int calculateScaledSize(int s) {
		//return (int) (s*scale);
		return s;
	}
	public int loadTexture() {
		g2d.dispose();
		image.flush();
		BufferedImage cur=image;
		int[] pixels = new int[cur.getWidth() * cur.getHeight()];
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
/*
		// Setup wrap mode
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		// Setup texture scaling filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
*/
		// Send texel data to OpenGL
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, cur.getWidth(), cur.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		
		// Return the texture ID so we can bind it later again
		return textureID;
	}
}