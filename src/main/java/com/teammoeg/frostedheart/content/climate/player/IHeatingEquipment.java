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

package com.teammoeg.frostedheart.content.climate.player;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.ISlotType;

/**
 * Interface IHeatingEquipment.
 * Interface for Dynamic Heating Equipment Item
 *
 * @author khjxiaogu
 * file: IHeatingEquipment.java
 * Date: 2021/9/14
 */
public interface IHeatingEquipment {

    /**
     * Compute added effective temperature.<br>
     *
     * @param slot            current slot, null for display<br>
     * @param stack           the stack<br>
     * @param effectiveTemp   the effective temp<br>
     * @param bodyTemp        the body temperature, normalize to 0<br>
     * @return returns body temperature change
     */
    float getEffectiveTempAdded(@Nullable Either<ISlotType,EquipmentSlotType> slot,ItemStack stack, float effectiveTemp, float bodyTemp);
}
