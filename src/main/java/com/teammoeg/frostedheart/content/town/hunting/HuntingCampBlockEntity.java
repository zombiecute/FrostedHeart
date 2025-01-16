package com.teammoeg.frostedheart.content.town.hunting;

import com.teammoeg.frostedheart.bootstrap.common.FHBlockEntityTypes;
import com.teammoeg.frostedheart.content.town.AbstractTownWorkerBlockEntity;
import com.teammoeg.frostedheart.content.town.TownWorkerState;
import com.teammoeg.frostedheart.content.town.TownWorkerType;
import com.teammoeg.frostedheart.content.town.blockscanner.BlockScanner;
import com.teammoeg.frostedheart.content.town.blockscanner.ConfinedSpaceScanner;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class HuntingCampBlockEntity extends AbstractTownWorkerBlockEntity {
    public HuntingCampBlockEntity(BlockPos pos, BlockState state) {
        super(FHBlockEntityTypes.HUNTING_CAMP.get(),pos,state);
    }

    private boolean isStructureValid(){
        ConfinedSpaceScanner confinedSpaceScanner = new ConfinedSpaceScanner(this.level, worldPosition.above());
        return !confinedSpaceScanner.scan(256);
    }

    
    @Override
    public void refresh() {
        this.occupiedArea.add(BlockScanner.toColumnPos(worldPosition));
        if(this.workerState == TownWorkerState.OCCUPIED_AREA_OVERLAPPED) return;
        this.workerState = isStructureValid()?TownWorkerState.VALID:TownWorkerState.NOT_VALID;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public TownWorkerType getWorkerType() {
        return TownWorkerType.HUNTING_CAMP;
    }

    @Override
    public CompoundTag getWorkData() {
        return getBasicWorkData();
    }

    @Override
    public void setWorkData(CompoundTag data) {
        setBasicWorkData(data);
    }
}
