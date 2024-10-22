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

package com.teammoeg.frostedheart.content.climate.heatdevice.generator;

import com.teammoeg.frostedheart.FHBaseContianer;
import com.teammoeg.frostedheart.FHBlockEntityContainer;
import com.teammoeg.frostedheart.FHContainer;
import com.teammoeg.frostedheart.util.FHMultiblockHelper;
import com.teammoeg.frostedheart.util.client.Point;

import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu.MultiblockMenuContext;
import blusunrize.immersiveengineering.common.gui.IESlot.NewOutput;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public abstract class MasterGeneratorContainer<R extends MasterGeneratorState,T extends MasterGeneratorTileEntity<T,R>> extends FHBaseContianer {
    public ContainerData data;
    public GetterAndSetter<Boolean> isWorking;
    public GetterAndSetter<Boolean> isOverdrive;
    public BlockPos masterPos;
    public MasterGeneratorContainer(MenuType<?> type, int id, Inventory inventoryPlayer, MultiblockMenuContext<R> ctx) {
        super(type,id,2);
        Point in=getSlotIn();
        R state=ctx.mbContext().getState();
        isWorking=new GetterAndSetter<>(()->state.isWorking,t->state.isWorking=t);
        isOverdrive=new GetterAndSetter<>(()->state.isOverdrive,t->state.isOverdrive=t);
        masterPos=FHMultiblockHelper.getAbsoluteMaster(ctx.mbContext().getLevel());
        IItemHandler handler=state.getData(FHMultiblockHelper.getAbsoluteMaster(ctx.mbContext().getLevel())).get().inventory;
        this.addSlot(new SlotItemHandler(handler, 0, in.getX(), in.getY()) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return GeneratorData.isStackValid(inventoryPlayer.player.level(), 0, itemStack);
            }
        });
        Point out=getSlotOut();
        this.addSlot(new NewOutput(handler, 1, out.getX(), out.getY()));
        super.addPlayerInventory(inventoryPlayer, 8, 140, 198);
        data = state.guiData;
       /* super.addGenericData(GenericContainerData.bool(isWorking, isWorking));
        super.addGenericData(GenericContainerData.bool(isOverdrive, isOverdrive));
        super.addGenericData(GenericContainerData);*/
        addDataSlots(data);
    }
    public abstract Point getSlotIn();
    public abstract Point getSlotOut();
    public abstract int getTier();
    public abstract FluidTank getTank();
	public void receiveMessage(short btn,int state) {
        switch(btn) {
        case 1:isWorking.set(state>0);break;
        case 2:isOverdrive.set(state>0);break;
        }
       /* if (message.contains("temperatureLevel", Tag.TAG_INT))
            setTemperatureLevel(message.getInt("temperatureLevel"));
        if (message.contains("rangeLevel", Tag.TAG_INT))
            setRangeLevel(message.getInt("rangeLevel"));*/
    }
}

