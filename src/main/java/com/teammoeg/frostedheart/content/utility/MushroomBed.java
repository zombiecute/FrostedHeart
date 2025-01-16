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

package com.teammoeg.frostedheart.content.utility;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.chorda.creativeTab.CreativeTabItemHelper;
import com.teammoeg.frostedheart.item.FHBaseItem;
import com.teammoeg.frostedheart.bootstrap.client.FHTabs;
import com.teammoeg.frostedheart.content.climate.player.IHeatingEquipment;
import com.teammoeg.frostedheart.util.client.Lang;
import com.teammoeg.frostedheart.content.climate.player.EquipmentSlotType;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.type.ISlotType;

public class MushroomBed extends FHBaseItem implements IHeatingEquipment {
    public static final TagKey<Item> ktag = ItemTags.create(new ResourceLocation(FHMain.MODID, "knife"));

    Item resultType;

    public MushroomBed( Item resultType, Properties properties) {
        super(properties);
        this.resultType = resultType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        if (stack.getDamageValue() > 0)
            list.add(Lang.translateTooltip("meme.mushroom").withStyle(ChatFormatting.GRAY));
        else
            list.add(Lang.translateTooltip("mushroom").withStyle(ChatFormatting.GRAY));
    }



    @Override
    public void fillItemCategory(CreativeTabItemHelper helper) {
        if (helper.isType(FHTabs.itemGroup)) {
            ItemStack is = new ItemStack(this);
            helper.accept(is);
            is.setDamageValue(is.getMaxDamage());
            helper.accept(is);
        }
    }
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        InteractionResultHolder<ItemStack> FAIL = new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        if (stack.getDamageValue() > 0)
            return FAIL;


        InteractionHand otherHand = handIn == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (playerIn.getItemInHand(otherHand).is(ktag)) {
            playerIn.startUsingItem(handIn);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return FAIL;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {

        if (stack.getDamageValue() == 0) {
            InteractionHand otherHand = entityLiving.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            entityLiving.getItemInHand(otherHand).hurtAndBreak(1, entityLiving, (player2) -> player2.broadcastBreakEvent(otherHand));
            return new ItemStack(resultType, 10);

        }
        return stack;
    }

	@Override
	public float getEffectiveTempAdded(Either<ISlotType,EquipmentSlotType> slot, ItemStack stack, float effectiveTemp, float bodyTemp) {
		if(slot==null)
			return 0.5f;
        if (stack.getDamageValue() > 0) {
            if (bodyTemp > -1) {
                this.setDamage(stack, this.getDamage(stack) - 1);
                return 0.5f;
            }
        }
		return 0;
	}

}
