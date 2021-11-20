/*
 * Copyright (c) 2021 TeamMoeg
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
 */

package com.teammoeg.frostedheart.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.Nullable;

import com.teammoeg.frostedheart.FHContent;
import com.teammoeg.frostedheart.FHContent.FHItems;
import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.compat.jei.category.ChargerCategory;
import com.teammoeg.frostedheart.compat.jei.category.ChargerCookingCategory;
import com.teammoeg.frostedheart.compat.jei.category.CuttingCategory;
import com.teammoeg.frostedheart.compat.jei.category.GeneratorFuelCategory;
import com.teammoeg.frostedheart.compat.jei.category.GeneratorSteamCategory;
import com.teammoeg.frostedheart.content.generator.GeneratorRecipe;
import com.teammoeg.frostedheart.content.generator.GeneratorSteamRecipe;
import com.teammoeg.frostedheart.content.generator.t1.T1GeneratorScreen;
import com.teammoeg.frostedheart.content.generator.t2.T2GeneratorScreen;
import com.teammoeg.frostedheart.content.steamenergy.charger.ChargerRecipe;
import com.teammoeg.frostedheart.util.FHNBT;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class JEICompat implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(FHMain.MODID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(FHContent.FHMultiblocks.generator), GeneratorFuelCategory.UID);
        registration.addRecipeCatalyst(new ItemStack(FHContent.FHMultiblocks.generator_t2), GeneratorFuelCategory.UID,GeneratorSteamCategory.UID);
        registration.addRecipeCatalyst(new ItemStack(FHContent.FHBlocks.charger), ChargerCategory.UID,ChargerCookingCategory.UID);
    }
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
    	 ClientWorld world = Minecraft.getInstance().world;
         checkNotNull(world, "minecraft world");
         RecipeManager recipeManager = world.getRecipeManager();
        registration.addRecipes(new ArrayList<>(GeneratorRecipe.recipeList.values()), GeneratorFuelCategory.UID);
        registration.addRecipes(new ArrayList<>(GeneratorSteamRecipe.recipeList.values()),GeneratorSteamCategory.UID);
        registration.addRecipes(new ArrayList<>(ChargerRecipe.recipeList.values()),ChargerCategory.UID);
        registration.addRecipes(recipeManager.getRecipesForType(IRecipeType.SMOKING),ChargerCookingCategory.UID);
        registration.addRecipes(Arrays.asList(new CuttingRecipe(FHNBT.Damage(new ItemStack(FHItems.red_mushroombed),0),new ItemStack(Items.RED_MUSHROOM,10)),
        		new CuttingRecipe(FHNBT.Damage(new ItemStack(FHItems.brown_mushroombed),0),new ItemStack(Items.BROWN_MUSHROOM,10))
        		),CuttingCategory.UID); 
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new GeneratorFuelCategory(guiHelper),
                new GeneratorSteamCategory(guiHelper),
                new ChargerCategory(guiHelper),
                new ChargerCookingCategory(guiHelper),
                new CuttingCategory(guiHelper)
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registry) {
        registry.addRecipeClickArea(T1GeneratorScreen.class,84, 35,9,12, GeneratorFuelCategory.UID);
        registry.addRecipeClickArea(T2GeneratorScreen.class,84, 35,9,12, GeneratorFuelCategory.UID,GeneratorSteamCategory.UID);
        //registry.addRecipeClickArea(CrucibleScreen.class, 80, 31, o2oarrowLoc[2], o2oarrowLoc[3], CrucibleCategory.UID);
    }

    public static <T> void checkNotNull(@Nullable T object, String name) {
        if (object == null) {
            throw new NullPointerException(name + " must not be null.");
        }
    }
}