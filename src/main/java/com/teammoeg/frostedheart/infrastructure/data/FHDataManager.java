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

package com.teammoeg.frostedheart.infrastructure.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.teammoeg.chorda.util.CRegistries;
import com.teammoeg.frostedheart.content.climate.WorldTemperature;
import com.teammoeg.frostedheart.content.climate.data.*;
import com.teammoeg.frostedheart.content.climate.player.ITempAdjustFood;
import com.teammoeg.chorda.util.io.CodecUtil;
import com.teammoeg.chorda.util.io.IdDataPair;
import com.teammoeg.chorda.util.mixin.StructureUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FHDataManager implements ResourceManagerReloadListener {
	public static class ResourceMap<T> extends HashMap<ResourceLocation, T> {
		private static final long serialVersionUID = 1564047056157250446L;
		public ResourceMap() {
			super();
		}

		public ResourceMap(int initialCapacity) {
			super(initialCapacity);
		}

		public ResourceMap(int initialCapacity, float loadFactor) {
			super(initialCapacity, loadFactor);
		}

		public ResourceMap(Map<? extends ResourceLocation, ? extends T> m) {
			super(m);
		}
	}

	public static class DataType<T> {
		public static final List<DataType<?>> types = new ArrayList<>();
		final int id;
		final Class<T> dataCls;
		final String location;
		final String domain;
		final Codec<IdDataPair<T>> codec;

		public DataType(Class<T> dataCls, String domain, String location, MapCodec<T> codec) {
			this.location = location;
			this.dataCls = dataCls;
			this.domain = domain;
			this.codec = IdDataPair.createCodec(codec).codec();
			this.id = types.size();
			types.add(this);
		}

		public IdDataPair<T> create(JsonElement jo) {
			return codec.decode(JsonOps.INSTANCE, jo).result().map(t -> t.getFirst()).orElse(null);
		}

		public void write(IdDataPair<T> obj, FriendlyByteBuf pb) {

			CodecUtil.writeCodec(pb, codec, obj);
		};

		public IdDataPair<T> read(FriendlyByteBuf pb) {
			return CodecUtil.readCodec(pb, codec);
		};

		public String getLocation() {
			return domain + "/" + location;
		}

		public int getId() {
			return id;
		}
	}

	public static final DataType<ArmorTempData> Armor = (new DataType<>(ArmorTempData.class, "temperature", "armor", ArmorTempData.CODEC));
	public static final DataType<BiomeTempData> Biome = (new DataType<>(BiomeTempData.class, "temperature", "biome", BiomeTempData.CODEC));
	public static final DataType<PlantTempData> Plant = (new DataType<>(PlantTempData.class, "temperature", "plant", PlantTempData.CODEC));
	public static final DataType<FoodTempData> Food = (new DataType<>(FoodTempData.class, "temperature", "food", FoodTempData.CODEC));
	public static final DataType<BlockTempData> Block = (new DataType<>(BlockTempData.class, "temperature", "block", BlockTempData.CODEC));
	public static final DataType<DrinkTempData> Drink = (new DataType<>(DrinkTempData.class, "temperature", "drink", DrinkTempData.CODEC));
	public static final DataType<CupData> Cup = (new DataType<>(CupData.class, "temperature", "cup", CupData.CODEC));
	public static final DataType<WorldTempData> World = (new DataType<>(WorldTempData.class, "temperature", "world", WorldTempData.CODEC));

	public static void main(String[] args) {
		System.out.println(ArmorTempData.CODEC.decode(JsonOps.INSTANCE, JsonOps.INSTANCE.getMap(new JsonParser().parse("{\"factor\":12}")).result().get()).result().orElse(null));
		// Object
		// nbt=FHDataType.Armor.type.codec.encodeStart(DataOps.COMPRESSED,(DataReference<Object>)FHDataType.Armor.type.create(new
		// JsonParser().parse("{\"id\":\"abc:def\",\"factor\":12}"))).result().orElse(null);
		ByteBuf bb = ByteBufAllocator.DEFAULT.buffer(256);
		FriendlyByteBuf pb = new FriendlyByteBuf(bb);
		Armor.write(Armor.create(new JsonParser().parse("{\"id\":\"abc:def\",\"factor\":12}")), pb);
		System.out.println(bb.writerIndex());
		bb.resetReaderIndex();
		for (int i = 0; i < bb.writerIndex(); i++)
			System.out.print(String.format("%2x", bb.readByte()) + " ");
		bb.resetReaderIndex();
		System.out.println();
		for (int i = 0; i < bb.writerIndex(); i++) {
			byte data = bb.readByte();
			if (data != '\r' && data != '\n')
				System.out.print(String.format(" %c", data) + " ");
			else
				System.out.print("   ");
		}
		System.out.println();
		bb.resetReaderIndex();
		System.out.println(Armor.read(pb));
	}

	public static final Map<DataType<?>, ResourceMap<Object>> ALL_DATA = new HashMap<>();

	public static boolean synched = false;
	static {
		for (DataType<?> dt : DataType.types) {
			ALL_DATA.put(dt, new ResourceMap<>());
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> ResourceMap<T> get(DataType<T> dt) {
		return (ResourceMap<T>) ALL_DATA.get(dt);
	}

	public static ArmorTempData getArmor(ItemStack is) {
		return FHDataManager.<ArmorTempData>get(Armor).get(CRegistries.getRegistryName(is.getItem()));
	}

	public static ArmorTempData getArmor(String is) {
		return FHDataManager.<ArmorTempData>get(Armor).get(new ResourceLocation(is));
	}

	@Nonnull
	public static Float getBiomeTemp(Biome b) {
		if (b == null) return 0f;
		BiomeTempData data = FHDataManager.get(Biome).get(CRegistries.getRegistryName(b));
		if (data != null)
			return data.getTemp();
		return 0F;
	}

	@Nullable
	public static PlantTempData getPlantData(Block b) {
		return FHDataManager.get(Plant).get(CRegistries.getRegistryName(b));
	}

	public static BlockTempData getBlockData(Block b) {
		return FHDataManager.get(Block).get(CRegistries.getRegistryName(b));
	}

	public static BlockTempData getBlockData(ItemStack b) {
		return FHDataManager.get(Block).get(CRegistries.getRegistryName(b.getItem()));
	}

	public static float getDrinkHeat(FluidStack f) {
		DrinkTempData dtd = FHDataManager.get(Drink).get(CRegistries.getRegistryName(f.getFluid()));
		if (dtd != null)
			return dtd.getHeat();
		return -0.3f;
	}

	/**
	 * Get the temperature adjuster for the food item.
	 * If stack is a cup, return the cup's temperature adjuster.
	 * If stack has food temp data, return the food's temperature adjuster.
	 * Otherwise, return null.
	 *
	 * @param stack the item stack
	 * @return the temperature adjuster
	 */
	public static @Nullable ITempAdjustFood getTempAdjustFood(ItemStack stack) {
		return getTempAdjustFood(stack.getItem());
	}

	public static @Nullable ITempAdjustFood getTempAdjustFood(Item item) {
		if (item instanceof ITempAdjustFood) {
			return (ITempAdjustFood) item;
		}
		CupData data = FHDataManager.get(Cup).get(CRegistries.getRegistryName(item));
		ResourceMap<FoodTempData> foodData = FHDataManager.get(Food);
		if (data != null) {
			return new CupTempAdjustProxy(data.getEfficiency(), foodData.get(CRegistries.getRegistryName(item)));
		}
		return foodData.get(CRegistries.getRegistryName(item));
	}

	public static @Nullable FoodTempData getFoodTemp(Item item) {
		return FHDataManager.get(Food).get(CRegistries.getRegistryName(item));
	}

	@Nonnull
	public static Float getWorldTemp(Level w) {
		WorldTempData data = FHDataManager.get(World).get(w.dimension().location());
		if (data != null)
			return data.getTemp();
		return -10F;
	}

	public static <T> void load(DataType<T> type, List<IdDataPair<?>> entries) {
		ResourceMap<Object> map = (ALL_DATA.get(type));
		map.clear();
		for (IdDataPair<?> de : entries) {
			map.put(de.getId(), de.getObj());
		}
	}

	public static <T> void register(DataType<T> dt, JsonObject data) {
		IdDataPair<T> jdh = dt.create(data);
		((ResourceMap<Object>) ALL_DATA.get(dt)).put(jdh.getId(), jdh.getObj());
		synched = false;
	}

	public static void reset() {
		synched = false;
		for (ResourceMap<?> rm : ALL_DATA.values())
			rm.clear();
	}

	public static <T> List<IdDataPair<?>> save(DataType<T> type) {
		List<IdDataPair<?>> entries = new ArrayList<>();
		for (Entry<ResourceLocation, ?> jdh : ALL_DATA.get(type).entrySet()) {
			entries.add(new IdDataPair<>(jdh.getKey(), jdh.getValue()));
		}
		return entries;
	}

	private FHDataManager() {}

	public static final FHDataManager INSTANCE = new FHDataManager();

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		FHDataManager.reset();
		StructureUtils.addBanedBlocks();
		WorldTemperature.clear();
		for (DataType<?> dat : DataType.types) {
			for (Entry<ResourceLocation, Resource> rl : manager.listResources(dat.getLocation(), (s) -> s.getPath().endsWith(".json")).entrySet()) {
				Resource rc = rl.getValue();
				try (
					
					BufferedReader reader = rc.openAsReader();){
					
					JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
					FHDataManager.register(dat, object);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
