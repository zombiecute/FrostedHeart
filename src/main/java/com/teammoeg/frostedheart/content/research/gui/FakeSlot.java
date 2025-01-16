/*
 * Copyright (c) 2022-2024 TeamMoeg
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

package com.teammoeg.frostedheart.content.research.gui;

import java.util.function.Consumer;

import com.teammoeg.chorda.util.CGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import com.teammoeg.chorda.util.client.ClientUtils;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetType;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class FakeSlot extends Widget {
    ItemStack[] i;
    int cnt;
    int ovlw, ovlh;
    Icon overlay;
    Consumer<TooltipList> tooltip;

    public FakeSlot(Panel panel) {
        super(panel);
        this.setSize(16, 16);
    }

    public FakeSlot(Panel panel, Ingredient is) {
        super(panel);
        this.i = is.getItems();
        this.setSize(16, 16);
    }

    public FakeSlot(Panel panel, ItemStack iss) {
        super(panel);
        this.i = new ItemStack[]{iss};
        this.setSize(16, 16);
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (i == null) return;
        ItemStack cur = i[(int) ((System.currentTimeMillis() / 1000) % i.length)];
        //list.add(cur.getDisplayName());
        cur.getTooltipLines(ClientUtils.getPlayer(), TooltipFlag.Default.NORMAL).forEach(list::add);
        if (tooltip != null)
            tooltip.accept(list);
    }

    public void clear() {
        i = null;
        cnt = 0;
    }

    @Override
    public void draw(GuiGraphics matrixStack, Theme theme, int x, int y, int w, int h) {
        if (i == null) return;
        ItemStack cur = i[(int) ((System.currentTimeMillis() / 1000) % i.length)];
        dev.ftb.mods.ftblibrary.ui.GuiHelper.setupDrawing();
        
        CGuiHelper.drawItem(matrixStack, cur, x, y,100, w / 16F, h / 16F, true, cnt==0?null:String.valueOf(cnt));
        
        matrixStack.pose().pushPose();
        matrixStack.pose().translate(0, 0, 200);
        if (overlay != null)
            overlay.draw(matrixStack, x, y, ovlw, ovlh);

        matrixStack.pose().popPose();
    }

    public Icon getOverlay() {
        return overlay;
    }

    public Consumer<TooltipList> getTooltip() {
        return tooltip;
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (isMouseOver()) {
            if (i == null) return false;
            if (getWidgetType() != WidgetType.DISABLED) {
                onClick(button);
            }

            return true;
        }

        return false;
    }

    public void onClick(MouseButton btn) {
    }

    public void resetOverlay() {
        this.overlay = null;
    }

    public void setCount(int store) {
        cnt = store;
    }

    public void setOverlay(Icon overlay, int height, int width) {
        this.overlay = overlay;
        this.ovlh = height;
        this.ovlw = width;
    }

    public void setSlot(Ingredient is) {
        this.i = is.getItems();
    }

    public void setSlot(ItemStack iss) {
        this.i = new ItemStack[]{iss};
    }

    public void setTooltip(Consumer<TooltipList> tooltip) {
        this.tooltip = tooltip;
    }

}
