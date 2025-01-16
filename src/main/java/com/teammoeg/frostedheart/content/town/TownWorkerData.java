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

package com.teammoeg.frostedheart.content.town;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teammoeg.chorda.util.io.CodecUtil;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;
import java.util.UUID;

/**
 * Data for a worker (town block) in the town.
 * <p>
 * A TownWorkerData is the basic data component in a TeamTownData.
 * It specifies the type of worker, the position of worker, the work data.
 * <p>
 * The work data is especially important, as it stores additional data that
 * should be synced with the entire town. It is an interface between the town
 * and the worker.
 * Work data consist of 2 parts: tileEntity and town. tileEntity stores the
 * data from the tile entity, and town stores the data from the town.
 * <p>
 * There can be multiple worker data with the same worker type.
 */
public class TownWorkerData {
	public static final Codec<TownWorkerData> CODEC=RecordCodecBuilder.create(t->
	t.group(CodecUtil.enumCodec(TownWorkerType.class).fieldOf("type").forGetter(o->o.type),
		CodecUtil.BLOCKPOS.fieldOf("pos").forGetter(o->o.pos),
		CompoundTag.CODEC.fieldOf("data").forGetter(o->o.workData),
		CodecUtil.defaultValue(Codec.INT, 0).fieldOf("priority").forGetter(o->o.priority)
		).apply(t,TownWorkerData::new));
    public static final String KEY_IS_OVERLAPPED = "isOverlapped";
    private TownWorkerType type;
    private BlockPos pos;
    private CompoundTag workData;
    private int priority;
    boolean loaded;

    public TownWorkerData(BlockPos pos) {
        super();
        this.pos = pos;
    }

    public TownWorkerData(TownWorkerType type, BlockPos pos, CompoundTag workData, int priority) {
		super();
		this.type = type;
		this.pos = pos;
		this.workData = workData;
		this.priority = priority;
	}

	public TownWorkerData(CompoundTag data) {
        super();
        this.pos = BlockPos.of(data.getLong("pos"));
        this.type = TownWorkerType.valueOf(data.getString("type"));
        this.workData = data.getCompound("data");
        this.priority = data.getInt("priority");
    }

    public boolean afterWork(Town resource) {
        return type.getWorker().afterWork(resource, workData);
    }

    public boolean beforeWork(Town resource) {
        return type.getWorker().beforeWork(resource, workData);
    }

    public boolean firstWork(Town resource) {
        return type.getWorker().firstWork(resource, workData);
    }

    public void fromTileEntity(TownBlockEntity te) {
        type = te.getWorkerType();
        workData = new CompoundTag();
        workData.put("tileEntity", te.getWorkData());
        priority = te.getPriority();
    }

    public void toTileEntity(TownBlockEntity te){
        te.setWorkData(workData.getCompound("town"));
    }

    public void updateFromTileEntity(ServerLevel world){
        if(loaded){
            TownBlockEntity te = (TownBlockEntity) world.getBlockEntity(pos);
            if(te != null){
                workData.put("tileEntity", te.getWorkData());
            }
        }
    }

    public void updateFromTileEntity(TownBlockEntity te){
        workData.put("tileEntity", te.getWorkData());
    }

    public void toTileEntity(ServerLevel world){
        if(loaded){
            TownBlockEntity te = (TownBlockEntity) world.getBlockEntity(pos);
            if(te != null){
                te.setWorkData(workData.getCompound("town"));
            }
        }
    }

    public void setDataFromTown(String key, Tag nbt){
        CompoundTag nbt0 = workData.getCompound("town");
        nbt0.put(key, nbt);
        workData.put("town", nbt0);
    }

    public void setOverlappingState(boolean b){
        this.setDataFromTown(KEY_IS_OVERLAPPED, b? ByteTag.ONE: ByteTag.ZERO);
    }

    public BlockPos getPos() {
        return pos;
    }

    public long getPriority() {
        return (long) (priority) << 32 + (type.getPriority());
    }

    public TownWorkerType getType() {
        return type;
    }

    public CompoundTag getWorkData() {
        return workData;
    }

    public boolean lastWork(Town resource) {
        return type.getWorker().lastWork(resource, workData);
    }

    public CompoundTag serialize() {
        CompoundTag data = new CompoundTag();
        data.putLong("pos", pos.asLong());
        data.putString("type", type.name());
        data.put("data", workData);
        data.putInt("priority", priority);
        return data;
    }

    @Deprecated
    public void setData(ServerLevel w) {
        if (loaded) {
            BlockEntity te = Utils.getExistingTileEntity(w, pos);
            if (te instanceof TownBlockEntity) {
                ((TownBlockEntity) te).setWorkData(workData);
            }
        }
    }

    public void setWorkData(CompoundTag workData) {
        this.workData = workData;
    }

    public boolean work(Town resource) {
        return type.getWorker().work(resource, workData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TownWorkerData)) return false;
        TownWorkerData that = (TownWorkerData) o;
        return priority == that.priority &&
                Objects.equals(type, that.type) &&
                Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, pos, priority);
    }


    /**
     * Get the residents of this worker.
     * Should ONLY be used if the worker holds residents, like house, mine, etc.
     */
    public ListTag getResidents(){
        return workData.getCompound("town").getList("residents", Tag.TAG_COMPOUND);
    }

    /**
     * Get the max resident of this worker.
     * Should ONLY be used if the worker holds residents, like house, mine, etc.
     */
    public int getMaxResident(){
        return workData.getCompound("tileEntity").getInt("maxResident");
    }

    /**
     * Add a resident to this worker.
     * Should ONLY be used if the worker holds residents, like house, mine, etc.
     */
    public void addResident(UUID uuid){
        CompoundTag dataFromTown = workData.getCompound("town");
        ListTag list = dataFromTown.getList("residents", Tag.TAG_STRING);
        list.add(StringTag.valueOf(uuid.toString()));
        dataFromTown.put("residents", list);
        workData.put("town", dataFromTown);
    }

    /**
     * Get priority when assigning work
     * Should ONLY be used if the worker holds residents, like house, mine, etc.
     */
    public double getResidentPriority(){
        return type.getResidentPriority(this);
    }

    public double getResidentPriority(int residentNum){
        return type.getResidentPriority(residentNum, this.getWorkData());
    }

    /**
     * @param nbt 完整的nbt，包含town和tileEntity部分
     * @return rating
     */
    public static Double getRating(CompoundTag nbt){
        return nbt.getCompound("tileEntity").getDouble("rating");
    }

}
