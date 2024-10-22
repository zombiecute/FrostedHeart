package com.teammoeg.frostedheart.content.climate.heatdevice.generator;

import java.util.function.Supplier;

import com.teammoeg.frostedheart.base.network.FHMessage;
import com.teammoeg.frostedheart.util.FHMultiblockHelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class GeneratorModifyPacket implements FHMessage{
	public GeneratorModifyPacket() {
	}
	public GeneratorModifyPacket(FriendlyByteBuf buffer) {
	}
	@Override
	public void encode(FriendlyByteBuf buffer) {
	}
	@Override
	public void handle(Supplier<Context> context) {
		
		context.get().enqueueWork(() -> {
			ServerPlayer spe=context.get().getSender();
			AbstractContainerMenu container=spe.containerMenu;
			if(container instanceof MasterGeneratorContainer crncontainer) {
				FHMultiblockHelper.getBEHelper(spe.level(), crncontainer.masterPos).ifPresent(helper->{
					if (helper.getMultiblock().logic()instanceof MasterGeneratorTileEntity mas) {
						mas.onUpgradeMaintainClicked(helper.getContext(), spe);
					}
				});
				
			}
		});
		context.get().setPacketHandled(true);
	}

}
