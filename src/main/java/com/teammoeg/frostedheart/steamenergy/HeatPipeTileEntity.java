package com.teammoeg.frostedheart.steamenergy;

import java.util.EnumMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.util.Strings;

import com.teammoeg.frostedheart.content.FHTileTypes;
import com.teammoeg.frostedheart.state.FHBlockInterfaces;

import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import it.unimi.dsi.fastutil.Arrays;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class HeatPipeTileEntity extends IEBaseTileEntity implements EnergyNetworkProvider,ITickableTileEntity,FHBlockInterfaces.IActiveState{
	protected Direction dMaster;
	private SteamEnergyNetwork network;
	private int length=Integer.MAX_VALUE;
	private boolean networkinit;
	private boolean isPathFinding;
	private boolean requireRP;
	public HeatPipeTileEntity() {
		super(FHTileTypes.HEATPIPE.get());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket) {
		if(descPacket)return;
		if(nbt.contains("dm"))
		dMaster=Direction.values()[nbt.getInt("dm")];
		length=nbt.getInt("length");
		requireRP=nbt.getBoolean("rep");
	}
	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket) {
		if(descPacket)return;
		if(dMaster!=null)
			nbt.putInt("dm",dMaster.ordinal());
		nbt.putInt("length",length);
		nbt.putBoolean("rep",requireRP);
	}
	public SteamEnergyNetwork getNetwork() {
		if(networkinit)return null;
		try {
			networkinit=true;//avoid recursive calling
			if(network==null&&dMaster!=null) {
				TileEntity te = Utils.getExistingTileEntity(this.getWorld(),this.getPos().offset(dMaster));
				if (te instanceof EnergyNetworkProvider) {
					SteamEnergyNetwork tnetwork=((EnergyNetworkProvider) te).getNetwork();
					if(tnetwork!=null) {
						network=tnetwork;
					}
				}
			}
		}finally {
			networkinit=false;
		}
		return network;
	}
	
	protected void propagate(Direction from,SteamEnergyNetwork newNetwork,int lengthx) {
		if(isPathFinding)return;
		try {
			isPathFinding=true;
			final SteamEnergyNetwork network=getNetwork();
			if(network!=null&&newNetwork!=null&&network!=newNetwork) {//network conflict
				return;//disconnect
			}
			if(newNetwork==null) {
				unpropagate(from);
				return;
			}
			//setActive(true);
			if(length<=lengthx)return;
			length=lengthx;
			dMaster=from;
			if(network!=newNetwork) {
				this.network=newNetwork;
				for(Direction d:Direction.values()) {
					if(dMaster==d)continue;
					BlockPos n=this.getPos().offset(d);
					TileEntity te = Utils.getExistingTileEntity(this.getWorld(),n);
					if (te instanceof HeatPipeTileEntity) {
						((HeatPipeTileEntity) te).propagate(d.getOpposite(),this.network,length+1);
					}
				}
			}
			return;
		}finally {
			isPathFinding=false;
		}
	}
	protected void unpropagate(Direction from) {
		doUnpropagate(from);
	}
	protected void doUnpropagate(Direction from) {
		if(dMaster==null)return;
		if(dMaster==from) {
			network=null;
			dMaster=null;
			length=Integer.MAX_VALUE;
			for(Direction d:Direction.values()) {
				if(d==from)continue;
				BlockPos n=this.getPos().offset(d);
				TileEntity te = Utils.getExistingTileEntity(this.getWorld(),n);
				if (te instanceof HeatPipeTileEntity) {
					((HeatPipeTileEntity) te).unpropagate(d.getOpposite());
				}
			}
			//setActive(false);
		}else {
			requireRP=true;
		}
	}
	/*
	protected int findPathToMaster(Direction from) {
		if(isVisiting)return -1;
		try {
			isVisiting=true;
			int result=-1;
			dMaster.remove(from);//avoid cycle detection
			if(!dMaster.isEmpty()) {
				for(int i:dMaster.values())
					result=Math.min(result,i);
				return result;
			}
			network=null;//assume no network
			for(Direction d:Direction.values()) {
				if(d==from)continue;
				BlockPos n=this.getPos().add(d.getDirectionVec());
				TileEntity te = Utils.getExistingTileEntity(this.getWorld(),n);
				if (te instanceof HeatPipeTileEntity) {
					int rs=((HeatPipeTileEntity) te).findPathToMaster(d.getOpposite())+1;
					if(rs>0){
						if(result==-1)
							result=rs;
						else
							result=Math.min(rs,result);
						dMaster.put(d,rs);
						SteamEnergyNetwork newNetwork=((HeatPipeTileEntity) te).getNetwork();
						if(network!=null&&network!=newNetwork) {//network conflict
							dMaster.clear();
							return -1;//disconnect
						}
						network=newNetwork;
					}
				}else if(te instanceof HeatProvider) {
					SteamEnergyNetwork newNetwork=((HeatProvider) te).getNetwork();
					if(network!=null&&network!=newNetwork) {//network conflict
						dMaster.clear();
						return -1;//disconnect
					}
					network=newNetwork;
					result=1;
					dMaster.put(d,result);
				}
			}
				
			return result;
		}finally {
			isVisiting=false;
		}
	}
	*/
	public void disconnectAt(Direction to) {
		if(network==null)return;
		unpropagate(to);//try find new path
	}
	public void connectAt(Direction to) {
		TileEntity te = Utils.getExistingTileEntity(this.getWorld(),this.getPos().offset(to));
		final SteamEnergyNetwork network=getNetwork();
		if(te instanceof HeatProvider){
			SteamEnergyNetwork newNetwork=((HeatProvider) te).getNetwork();
			if(network==null) {
				this.propagate(to, newNetwork,1);
			}
			return;
		}
		if(network==null)return;
		if (te instanceof HeatPipeTileEntity) {
			if(dMaster!=to)
				((HeatPipeTileEntity) te).propagate(to.getOpposite(),network, length);
		}else {
			disconnectAt(to);
		}
	}

	@Override
	public void tick() {
		if(requireRP) {
			requireRP=false;
			if(dMaster!=null) {
				for(Direction d:Direction.values()) {
					if(d==dMaster)continue;
					BlockPos n=this.getPos().offset(d);
					TileEntity te = Utils.getExistingTileEntity(this.getWorld(),n);
					if (te instanceof HeatPipeTileEntity) {
						((HeatPipeTileEntity) te).propagate(d.getOpposite(),network,length+1);
					}
				}
			}
		}
	}
}