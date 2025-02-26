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

package com.teammoeg.frostedheart.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.item.ItemStack;

@Mixin(Gui.class)
public interface IngameGuiAccess {
    @Accessor("lastToolHighlight")
    ItemStack getHighlightingItemStack();

    @Accessor("toolHighlightTimer")
    int getRemainingHighlightTicks();

    @Accessor("screenWidth")
    int getScreenWidth();

    @Accessor("screenHeight")
    int getScreenHeight();
}
