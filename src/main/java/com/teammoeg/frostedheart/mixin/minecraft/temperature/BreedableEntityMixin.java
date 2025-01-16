/*
 * Copyright (c) 2024 TeamMoeg
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

package com.teammoeg.frostedheart.mixin.minecraft.temperature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.teammoeg.chorda.util.mixin.BreedUtil;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
/**
 * Add more breeding cooldown and set breading item to tag items
 * For removal in 1.20.4+
 * */
@Mixin({Pig.class, Chicken.class, Fox.class, Rabbit.class, Cat.class, Llama.class, AbstractHorse.class})
public abstract class BreedableEntityMixin extends Animal {

    protected BreedableEntityMixin(EntityType<? extends Animal> type, Level worldIn) {
        super(type, worldIn);
    }

    @Inject(at = @At("HEAD"), method = "isFood", cancellable = true)
    public void isFood(ItemStack itemStack, CallbackInfoReturnable<Boolean> cbi) {
        EntityType<?> type = getType();
        boolean f = BreedUtil.isBreedingItem(type, itemStack);
        if (f)
            cbi.setReturnValue(true);
    }
}
