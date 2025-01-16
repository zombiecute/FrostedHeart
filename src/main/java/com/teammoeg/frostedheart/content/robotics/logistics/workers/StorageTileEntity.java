package com.teammoeg.frostedheart.content.robotics.logistics.workers;

import com.teammoeg.chorda.blockentity.CBlockEntity;
import com.teammoeg.frostedheart.content.robotics.logistics.FilterSlot;
import com.teammoeg.frostedheart.content.robotics.logistics.ItemChangeListener;
import com.teammoeg.frostedheart.content.robotics.logistics.ItemHandlerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class StorageTileEntity extends CBlockEntity implements ILogisticsStorage,ItemChangeListener{
	ItemStackHandler container=new ItemStackHandler(27);
	ItemHandlerListener handler=new ItemHandlerListener(container,this);
	FilterSlot filter;
	public StorageTileEntity(BlockEntityType<? extends BlockEntity> type,BlockPos pos,BlockState bs) {
		super(type,pos,bs);
		filter=new FilterSlot();
	}

	@Override
	public ItemStackHandler getInventory() {
		return container;
	}

	@Override
	public boolean isValidFor(ItemStack stack) {
		return filter.isValidFor(stack);
	}


	@Override
	public void onSlotChange(int slot, ItemStack after) {
	}

	@Override
	public void onSlotClear(int slot) {
	}

	@Override
	public void onCountChange(int slot, int before, int after) {
	}

	@Override
	public void readCustomNBT(CompoundTag arg0, boolean arg1) {
		container.deserializeNBT(arg0.getCompound("container"));
	}

	@Override
	public void writeCustomNBT(CompoundTag arg0, boolean arg1) {
		arg0.put("container", container.serializeNBT());
	}

}
