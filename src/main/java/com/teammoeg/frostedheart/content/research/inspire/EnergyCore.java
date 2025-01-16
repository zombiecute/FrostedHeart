/*
 * Copyright (c) 2022-2024 TeamMoeg
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

package com.teammoeg.frostedheart.content.research.inspire;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.teammoeg.chorda.util.lang.Components;
import com.teammoeg.frostedheart.FHNetwork;
import com.teammoeg.chorda.team.TeamDataClosure;
import com.teammoeg.frostedheart.bootstrap.common.FHCapabilities;
import com.teammoeg.frostedheart.bootstrap.common.FHMobEffects;
import com.teammoeg.frostedheart.content.climate.player.PlayerTemperatureData;
import com.teammoeg.frostedheart.content.research.api.ResearchDataAPI;
import com.teammoeg.frostedheart.content.research.data.ResearchVariant;
import com.teammoeg.frostedheart.content.research.data.TeamResearchData;
import com.teammoeg.frostedheart.content.research.network.FHEnergyDataSyncPacket;
import com.teammoeg.chorda.util.io.CodecUtil;
import com.teammoeg.chorda.util.io.NBTSerializable;
import com.teammoeg.chorda.util.utility.LeveledValue;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

public class EnergyCore implements NBTSerializable {
    /*long energy;
    long cenergy;
    long penergy;*/
	private static final Codec<LeveledValue> LEVELED_CODEC=LeveledValue.createCodec(l->(Math.min(60f, l)*25+2000));
	LeveledValue level;
	private static final Codec<LeveledValue> PERSIST_LEVELED_CODEC=LeveledValue.createCodec(l->(l*100f+2000));
	LeveledValue persistLevel;
	int maxLevel;
	int maxPesistLevel;
	int researchPoint;
    long lastsleepdate;
    long lastsleep;
    double utbody;
    public void update(int level,int exp,int plevel,int pexp,int researchPoint) {
    	this.level.setValue(level, exp);
    	this.persistLevel.setValue(plevel, pexp);
    	this.researchPoint=researchPoint;
    }
    public EnergyCore() {
    	level=CodecUtil.initEmpty(LEVELED_CODEC);
    	persistLevel=CodecUtil.initEmpty(PERSIST_LEVELED_CODEC);
    	//System.out.println("init===============");
		//System.out.println(level);
		//System.out.println(persistLevel);
    }
    protected void addPersistExp(ServerPlayer player, float value) {
    	persistLevel.addValue(value*getModifier(player));
    	int pLvl=persistLevel.getLevel();
    	if(pLvl>=maxPesistLevel) {
			researchPoint+=pLvl-maxPesistLevel;
			maxPesistLevel=pLvl;
		}
    }
    protected void addTemperalExp(ServerPlayer player, float value) {
    	level.addValue(value*getModifier(player));
    	int lvl=level.getLevel();
		if(lvl>=maxLevel) {
			researchPoint+=lvl-maxLevel;
			maxLevel=lvl;
		}
    }
    public static void addEnergy(ServerPlayer player, int val) {
    	getCapability(player).ifPresent(t->t.addTemperalExp(player,val));
        FHNetwork.send(PacketDistributor.PLAYER.with(() -> player), new FHEnergyDataSyncPacket(player));
    }

    public static void addPersistentEnergy(ServerPlayer player, int val) {
    	getCapability(player).ifPresent(t->t.addPersistExp(player,val));
        FHNetwork.send(PacketDistributor.PLAYER.with(() -> player), new FHEnergyDataSyncPacket(player));
    }
    public static void addPoint(ServerPlayer player, int val) {
    	getCapability(player).ifPresent(t->t.researchPoint+=val);
        FHNetwork.send(PacketDistributor.PLAYER.with(() -> player), new FHEnergyDataSyncPacket(player));
    }
    public static void applySleep(ServerPlayer player) {
        EnergyCore data=getCapability(player).orElse(null);
        long lsd = data.lastsleepdate;
        long csd = (player.level().getDayTime() + 12000L) / 24000L;
        //System.out.println("slept");
        if (csd == lsd) return;
        //System.out.println("sleptx");
        float tbody = PlayerTemperatureData.getCapability(player).map(PlayerTemperatureData::getFeelTemp).orElse(0f);
        //System.out.println(out);
        data.utbody= Math.pow(10, 4 - Math.abs(tbody - 40) / 10);
        data.lastsleep=0;
        data.lastsleepdate=csd;
    }

    public float getModifier(ServerPlayer player) {
    	boolean isBodyNotWell = player.getEffect(FHMobEffects.HYPERTHERMIA.get()) != null || player.getEffect(FHMobEffects.HYPOTHERMIA.get()) != null;
    	if(isBodyNotWell)return 0;
    	TeamDataClosure<TeamResearchData> trd = ResearchDataAPI.getData(player);
    	double initValue=(1 + trd.get().getVariants().getDouble(ResearchVariant.MAX_ENERGY_MULT.getToken()));
        if ( utbody != 0) {
            double t = Mth.clamp(((int)lastsleep), 1, Integer.MAX_VALUE) / 1200d;
            initValue *= ( utbody / (t * t * t * t * t * t +  utbody * 2) + 0.5);
        } else {
        	initValue *= 0.5;
        }
        double dietValue = 0;
        //TODO implement diet
        //IDietTracker idt = DietCapability.get(player).orElse(null);
        /*if (idt != null) {
            int tdv = 0;
            for (Entry<String, Float> vs : idt.getValues().entrySet())
                if (DietGroupCodec.getGroup(vs.getKey()).isBeneficial()) {
                    dietValue += vs.getValue();
                    tdv++;
                }

            if (tdv != 0)
                dietValue /= tdv;
        }*/
        initValue+=(dietValue - 0.4);
        initValue/=1+(trd.team().getOnlineMembers().size()-1)*0.8f;
        return (float) initValue;
    }
    public static void dT(ServerPlayer player) {
        //System.out.println("dt");
    	EnergyCore data=getCapability(player).orElse(null);
        data.lastsleep++;
       // FHNetwork.send(PacketDistributor.PLAYER.with(() -> player), new FHEnergyDataSyncPacket(player));
    }

    public static int getEnergy(Player player) {
        return getCapability(player).map(t->t.researchPoint).orElse(0);
    }
    public static void costEnergy(Player player,int val) {
        getCapability(player).ifPresent(t->t.researchPoint-=val);
    }

    public static void reportEnergy(Player player) {
    	getCapability(player).ifPresent(data->player.sendSystemMessage(Components.str("Energy:" + data.level + ",Persist Energy: " + data.persistLevel)));
    }
 


    public static LazyOptional<EnergyCore> getCapability(@Nullable Player player) {
    	return FHCapabilities.ENERGY.getCapability(player);
    }



	public void onrespawn() {
        utbody=0;
        lastsleep=0;
        lastsleepdate=0;
        level.minPercent(0.1f);
        
	}
	public void sendUpdate(ServerPlayer player) {
		FHNetwork.send(PacketDistributor.PLAYER.with(() -> player), new FHEnergyDataSyncPacket(player));
	}

	@Override
	public void save(CompoundTag saved, boolean isPacket) {
	    CodecUtil.encodeNBT(LEVELED_CODEC, saved, "lvl", level);
	    CodecUtil.encodeNBT(PERSIST_LEVELED_CODEC, saved, "plvl", persistLevel);
	    saved.putInt("lastLvl", maxLevel);
	    saved.putInt("lastPlvl", maxPesistLevel);
	    saved.putInt("rp", researchPoint);
	    if(!isPacket) {
		    saved.putLong("lastsleepdate", lastsleepdate);
		    saved.putLong("lastsleep", lastsleep);
		    saved.putDouble("utbody", utbody);
	    }
	}

	@Override
	public void load(CompoundTag saved, boolean isPacket) {
		level=CodecUtil.decodeNBT(LEVELED_CODEC, saved, "lvl");
		persistLevel=CodecUtil.decodeNBT(PERSIST_LEVELED_CODEC, saved, "plvl");
		//System.out.println("load===============");
		//System.out.println(level);
		//System.out.println(persistLevel);
		maxLevel=saved.getInt("lastLvl");
		maxPesistLevel=saved.getInt("lastPlvl");
		researchPoint=saved.getInt("rp");
		if(!isPacket) {
		    lastsleepdate=saved.getLong("lastsleepdate");
		    lastsleep=saved.getLong("lastsleep");
		    utbody=saved.getDouble("utbody");
		}
	}
}
