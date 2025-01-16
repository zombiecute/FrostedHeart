/*
 * Copyright (c) 2021-2024 TeamMoeg
 *
 * This file is part of Frosted Heart.
 *
 * Frosted Heart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Frosted Heart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frosted Heart. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.teammoeg.chorda.util.client;

import java.util.Objects;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class UV extends Rect {
	public enum Transition{
		UP,
		DOWN,
		LEFT,
		RIGHT;
	}
	final int textureW,textureH;
    public static UV delta(int x1, int y1, int x2, int y2) {
        return new UV(Rect.delta(x1, y1, x2, y2));
    }
    public static UV deltaWH(int x1, int y1, int x2, int y2,int tw,int th) {
        return new UV(Rect.delta(x1, y1, x2, y2), tw, th);
    }
    public UV(int x, int y, int w, int h) {
        super(x, y, w, h);
        textureW=256;
        textureH=256;
    }

    public UV(int x, int y, int w, int h, int textureW, int textureH) {
		super(x, y, w, h);
		this.textureW = textureW;
		this.textureH = textureH;
	}

	public UV(Rect r) {
        super(r);
        textureW=256;
        textureH=256;
    }

    public UV(Rect r, int textureW, int textureH) {
		super(r);
		this.textureW = textureW;
		this.textureH = textureH;
	}

	public UV(UV uv) {
        this(uv.x, uv.y, uv.w, uv.h,uv.textureW,uv.textureH);
    }
	public void blitRotated(GuiGraphics graphics,ResourceLocation texture, int targetX, int targetY,int centerX,int centerY,float degrees) {
		PoseStack matrixStack=graphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(targetX + centerX, targetY + centerY, 0);//move to gauge center
		matrixStack.mulPose(new Quaternionf(new AxisAngle4f((float)(degrees/180*Math.PI),0f,0f,1f)));//rotate around Z
		graphics.blit(texture,-centerX,-centerY, w, h, x, y, w, h, textureW, textureH);//draw with center offset
		matrixStack.popPose();
	}
    //blit with width transition and  custom texture size
    public void blitRotated(GuiGraphics matrixStack,ResourceLocation texture, int targetX, int targetY,Point loc,int centerX,int centerY,float degrees) {
    	blitRotated(matrixStack,texture, targetX + loc.getX(), targetY + loc.getY(), centerX, centerY, degrees);
    }
    //blit with width transition and  custom texture size
    public void blit(GuiGraphics s,ResourceLocation texture, int targetX, int targetY, int sourceWidth, int sourceHeight) {
        s.blit(texture, targetX, targetY, x, y, sourceWidth, sourceHeight, textureW, textureH);
    }
    
    //blit with width transition and  custom texture size
    public void blit(GuiGraphics s,ResourceLocation texture, int targetX, int targetY, int sourceWidth) {
        s.blit(texture, targetX, targetY, x, y, sourceWidth, h, textureW, textureH);
    }

    //normal blit
    public void blit(GuiGraphics s,ResourceLocation texture, int targetX, int targetY) {
        s.blit(texture, targetX, targetY, x, y, w, h, textureW, textureH);
    }
    
    //normal blit
    public void blitCenter(GuiGraphics s,ResourceLocation texture, int centerX, int centerY) {
        s.blit(texture, centerX - w / 2, centerY - h / 2, x, y, w, h, textureW, textureH);
    }
    //blit add point
    public void blitCenter(GuiGraphics s,ResourceLocation texture, int centerX, int centerY, Point loc) {
    	blitCenter(s,texture, centerX + loc.getX(), centerY + loc.getY());
    }
    //blit with atlas
    public void blitAtlas(GuiGraphics s,ResourceLocation texture, int targetX, int targetY, int gridX, int gridY) {
    	s.blit(texture, targetX, targetY, x + gridX * w, y + gridY * h, w, h, textureW, textureH);
    }
    //blit add point
    public void blit(GuiGraphics s,ResourceLocation texture, int targetX, int targetY, Point loc) {
        blit(s,texture, targetX + loc.getX(), targetY + loc.getY());
    }
    //blit with width transition add point
    public void blit(GuiGraphics s,ResourceLocation texture,  int targetX, int targetY, Point loc, int sourceWidth) {
        blit(s,texture, targetX + loc.getX(), targetY + loc.getY(), sourceWidth);
    }

    //blit with width transition add point
    public void blit(GuiGraphics s,ResourceLocation texture, int targetX, int targetY, Point loc, Transition direction,double progress) {
    	blit(s, texture, targetX + loc.getX(), targetY + loc.getY(), direction, progress);
    }
    //blit with width transition add point
    public void blit(GuiGraphics s,ResourceLocation texture,  int targetX, int targetY, Transition direction,double progress) {
    	if(progress<0)
    		return;
    	if(progress>1)
    		progress=1;
    	switch(direction) {
    	case UP   :blit(s,texture, targetX, targetY +(int)(h*(1-progress)), w, (int)(h*progress));return;
    	case LEFT :blit(s,texture, targetX +(int)(w*(1-progress)), targetY, (int)(w*progress), h);return;
    	case DOWN :blit(s,texture, targetX, targetY, w, (int)(h*progress));return;
    	case RIGHT:blit(s,texture, targetX, targetY, (int)(w*progress), h);return;
    	}
    }
    //normal blit add point with custom texture size
    public void blitAt(GuiGraphics s,ResourceLocation texture, int targetX, int targetY, Point loc) {
        blit(s,texture, targetX + loc.getX(), targetY + loc.getY());
    }

    //blit with atlas and add point
    public void blitAtlas(GuiGraphics s,ResourceLocation texture, int targetX, int targetY, Point loc, int gridX, int gridY) {
        blitAtlas(s,texture, targetX + loc.getX(), targetY + loc.getY(), gridX, gridY);
    }
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(textureH, textureW);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		UV other = (UV) obj;
		return textureH == other.textureH && textureW == other.textureW;
	}
}
