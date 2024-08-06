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

package com.teammoeg.frostedheart;

import com.teammoeg.frostedheart.content.climate.heatdevice.generator.t1.T1GeneratorContainer;
import com.teammoeg.frostedheart.content.climate.heatdevice.generator.t1.T1GeneratorTileEntity;
import com.teammoeg.frostedheart.content.climate.heatdevice.generator.t2.T2GeneratorContainer;
import com.teammoeg.frostedheart.content.climate.heatdevice.generator.t2.T2GeneratorTileEntity;
import com.teammoeg.frostedheart.content.decoration.RelicChestContainer;
import com.teammoeg.frostedheart.content.decoration.RelicChestTileEntity;
import com.teammoeg.frostedheart.content.incubator.HeatIncubatorTileEntity;
import com.teammoeg.frostedheart.content.incubator.IncubatorT1Container;
import com.teammoeg.frostedheart.content.incubator.IncubatorT2Container;
import com.teammoeg.frostedheart.content.incubator.IncubatorTileEntity;
import com.teammoeg.frostedheart.content.research.blocks.DrawingDeskTileEntity;
import com.teammoeg.frostedheart.content.research.gui.drawdesk.DrawDeskContainer;
import com.teammoeg.frostedheart.content.steamenergy.HeatStatContainer;
import com.teammoeg.frostedheart.content.steamenergy.sauna.SaunaContainer;
import com.teammoeg.frostedheart.content.steamenergy.sauna.SaunaTileEntity;
import com.teammoeg.frostedheart.content.trade.gui.TradeContainer;
import com.teammoeg.frostedheart.util.utility.ReferenceSupplier;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu.MultiblockMenuContext;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainerConstructor;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.MultiblockContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class FHContainer {
    @FunctionalInterface
    public interface BEMenuFactory<T extends AbstractContainerMenu,BE extends BlockEntity>{
    	T get(int id, Inventory inventoryPlayer, BE tile);
    } 
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
            FHMain.MODID);
    public static final RegistryObject<MenuType<TradeContainer>> TRADE_GUI = CONTAINERS.register("trade", () -> IForgeMenuType.create(TradeContainer::new));
    public static final RegistryObject<MenuType<HeatStatContainer>> HEAT_STAT = CONTAINERS.register("heat_stat", () -> IForgeMenuType.create(HeatStatContainer::new));

    
    public static final RegistryObject<MenuType<T1GeneratorContainer>> GENERATOR_T1 =  register(T1GeneratorTileEntity.class, new ResourceLocation(FHMain.MODID, "generator"), T1GeneratorContainer::new);
    public static final RegistryObject<MenuType<T2GeneratorContainer>> GENERATOR_T2 = register(T2GeneratorTileEntity.class, new ResourceLocation(FHMain.MODID, "generator_t2"), T2GeneratorContainer::new);

    public static final RegistryObject<MenuType<RelicChestContainer>> RELIC_CHEST = register(RelicChestTileEntity.class, new ResourceLocation(FHMain.MODID, "relic_chest"), RelicChestContainer::new);
    public static final RegistryObject<MenuType<DrawingDeskContainer>> DRAW_DESK = register(DrawingDeskTileEntity.class, new ResourceLocation(FHMain.MODID, "draw_desk"), DrawDeskContainer::new);
    public static final RegistryObject<MenuType<SaunaContainer>> SAUNA = register(SaunaTileEntity.class, new ResourceLocation(FHMain.MODID, "sauna_vent"), SaunaContainer::new);
    public static final RegistryObject<MenuType<IncubatorContainer>> INCUBATOR_T1 = register(IncubatorTileEntity.class, new ResourceLocation(FHMain.MODID, "incubator"), IncubatorT1Container::new);
    public static final RegistryObject<MenuType<HeatIncubatorContainer>> INCUBATOR_T2 = register(HeatIncubatorTileEntity.class, new ResourceLocation(FHMain.MODID, "heat_incubator"), IncubatorT2Container::new);
    public <T extends AbstractContainerMenu,BE extends BlockEntity> RegistryObject<MenuType<T>> register(Class<BE> BEClass,String name,BEMenuFactory<T,BE> factory) {
    	return CONTAINERS.register("trade", () -> IForgeMenuType.create((id,inv,pb)->{
    		BlockEntity be=inv.player.level().getBlockEntity(pb.readBlockPos());
    		if(BEClass.isInstance(be))
    			return factory.get(id, inv, (BE)be);
    		return null;
    	}));
    }
}
