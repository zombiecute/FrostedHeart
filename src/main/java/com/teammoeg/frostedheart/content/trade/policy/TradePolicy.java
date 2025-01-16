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

package com.teammoeg.frostedheart.content.trade.policy;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teammoeg.frostedheart.content.trade.FHVillagerData;
import com.teammoeg.frostedheart.content.trade.policy.snapshot.PolicySnapshot;
import com.teammoeg.chorda.util.RegistryUtils;
import com.teammoeg.chorda.util.io.SerializeUtil;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes.TypeWithClass;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

public class TradePolicy extends IESerializableRecipe {
    public static class Serializer extends IERecipeSerializer<TradePolicy> {
        @Override
        public ItemStack getIcon() {
            return new ItemStack(Items.VILLAGER_SPAWN_EGG);
        }

        @Nullable
        @Override
        public TradePolicy fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ResourceLocation name = SerializeUtil.readOptional(buffer, FriendlyByteBuf::readResourceLocation).orElse(null);
            List<PolicyGroup> groups = SerializeUtil.readList(buffer, PolicyGroup::read);
            int root = buffer.readVarInt();
            VillagerProfession vp = buffer.readRegistryIdUnsafe(ForgeRegistries.VILLAGER_PROFESSIONS);
            return new TradePolicy(recipeId, name, groups, root, vp, buffer.readVarIntArray());
        }

        @Override
        public TradePolicy readFromJson(ResourceLocation recipeId, JsonObject json,IContext ctx) {
            ResourceLocation name = json.has("name") ? new ResourceLocation(json.get("name").getAsString()) : null;
            List<PolicyGroup> groups = SerializeUtil.parseJsonList(json.get("policies"), PolicyGroup::read);
            int root = json.has("weight") ? json.get("weight").getAsInt() : 0;
            int[] expBar;
            VillagerProfession vp = VillagerProfession.NONE;
            
            if (json.has("profession"))
                vp = RegistryUtils.getProfess(new ResourceLocation(json.get("profession").getAsString()));
            if (json.has("exps"))
                expBar = SerializeUtil.parseJsonElmList(json.get("exps"), JsonElement::getAsInt).stream().mapToInt(t -> t).toArray();
            else
                expBar = new int[0];
            return new TradePolicy(recipeId, name, groups, root, vp, expBar);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TradePolicy recipe) {
            SerializeUtil.writeOptional2(buffer, recipe.name, FriendlyByteBuf::writeResourceLocation);
            SerializeUtil.writeList(buffer, recipe.groups, PolicyGroup::write);
            buffer.writeVarInt(recipe.weight);
            buffer.writeRegistryIdUnsafe(ForgeRegistries.VILLAGER_PROFESSIONS, recipe.vp);
            buffer.writeVarIntArray(recipe.expBar);
        }
    }
    public static class Weighted implements WeightedEntry {
        TradePolicy policy;
        Weight weight;
        public Weighted(int itemWeightIn, TradePolicy policy) {
        	this.weight=Weight.of(itemWeightIn);
            this.policy = policy;
        }
		@Override
		public Weight getWeight() {

			return weight;
		}
    }
    public static RegistryObject<RecipeType<TradePolicy>> TYPE;
    public static Lazy<TypeWithClass<TradePolicy>> IEType=Lazy.of(()->new TypeWithClass<>(TYPE, TradePolicy.class));
    public static RegistryObject<IERecipeSerializer<TradePolicy>> SERIALIZER;

    public static Map<ResourceLocation, TradePolicy> policies;
    public static int totalW;
    public static List<Weighted> items;
    private ResourceLocation name;
    private VillagerProfession vp;
    List<PolicyGroup> groups;
    private int[] expBar;

    int weight = 0;

    public static TradePolicy random(RandomSource rnd) {
        return WeightedRandom.getRandomItem(rnd, items, totalW).map(t->t.policy).orElse(null);
    }

    public TradePolicy(ResourceLocation id, ResourceLocation name, List<PolicyGroup> groups, int weight, VillagerProfession vp, int[] expBar) {
        super(Lazy.of(()->ItemStack.EMPTY), IEType.get(), id);
        this.name = name;
        this.groups = groups;
        this.weight = weight;
        this.vp = vp;
        this.expBar = expBar;
    }

    public Weighted asWeight() {
        if (weight > 0)
            return new Weighted(weight, this);
        return null;
    }

    public void CollectPolicies(PolicySnapshot policy, FHVillagerData ve) {
        groups.forEach(t -> t.CollectPolicies(policy, ve));
    }

    public PolicySnapshot get(FHVillagerData ve) {
        PolicySnapshot ps = new PolicySnapshot();
        ps.maxExp = this.getExp(ve.getTradeLevel());
        this.CollectPolicies(ps, ve);
        return ps;
    }

    public int getExp(int level) {
        if (level >= expBar.length)
            return 0;
        return expBar[level];

    }

    @Override
    protected IERecipeSerializer<TradePolicy> getIESerializer() {
        return SERIALIZER.get();
    }

    public ResourceLocation getName() {
        return name == null ? super.id : name;
    }

    public VillagerProfession getProfession() {
        return vp == null ? VillagerProfession.NONE : vp;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess ra) {
        return ItemStack.EMPTY;
    }
}
