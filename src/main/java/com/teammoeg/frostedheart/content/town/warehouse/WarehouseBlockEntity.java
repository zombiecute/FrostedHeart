package com.teammoeg.frostedheart.content.town.warehouse;

import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.chorda.scheduler.SchedulerQueue;
import com.teammoeg.frostedheart.base.team.FHTeamDataManager;
import com.teammoeg.frostedheart.base.team.SpecialDataTypes;
import com.teammoeg.frostedheart.bootstrap.common.FHBlockEntityTypes;
import com.teammoeg.frostedheart.content.town.*;
import com.teammoeg.frostedheart.content.town.blockscanner.BlockScanner;
import com.teammoeg.frostedheart.content.town.blockscanner.FloorBlockScanner;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.UUID;

public class WarehouseBlockEntity extends AbstractTownWorkerBlockEntity {
    private int volume;//有效体积
    private int area;//占地面积
    private double capacity;//最大容量
    private boolean addedToSchedulerQueue = false;
    @Getter
    private UUID teamID;//frostedheartID

    public WarehouseBlockEntity(BlockPos pos, BlockState state) {
        super(FHBlockEntityTypes.WAREHOUSE.get(),pos,state);
    }

    public boolean isStructureValid(){
        BlockPos warehousePos = this.getBlockPos();
        BlockPos doorPos = BlockScanner.getDoorAdjacent(level, warehousePos);
        if (doorPos == null) return false;
        BlockPos floorBelowDoor = BlockScanner.getBlockBelow((pos)->!(Objects.requireNonNull(level).getBlockState(pos).is(BlockTags.DOORS)), doorPos);//找到门下面垫的的那个方块
        for (Direction direction : BlockScanner.PLANE_DIRECTIONS) {
            assert floorBelowDoor != null;
            BlockPos startPos = floorBelowDoor.relative(direction);//找到门下方块旁边的方块
            if (!FloorBlockScanner.isValidFloorOrLadder(Objects.requireNonNull(level), startPos)) {//如果门下方块旁边的方块不是合法的地板，找一下它下面的方块
                if (!FloorBlockScanner.isValidFloorOrLadder(Objects.requireNonNull(level), startPos.below()) || FloorBlockScanner.isHouseBlock(level, startPos.above(2))) {//如果它下面的方块也不是合法地板（或者梯子），或者门的上半部分堵了方块，就不找了。我们默认村民不能从两格以上的高度跳下来，也不能从一格高的空间爬过去
                    continue;
                }
                startPos = startPos.below();
            }
            WarehouseBlockScanner scanner = new WarehouseBlockScanner(level, startPos);
            if(scanner.scan()){
                this.area = scanner.getArea();
                this.volume = scanner.getVolume();
                //容量与体积相似，但是在随着房间高度增高略有衰减
                this.capacity = area*Math.pow((volume*0.02/area), 0.9)*37;
                this.occupiedArea = scanner.getOccupiedArea();
                return true;
            }
        }
        return false;
    }

    public void refresh(){
        if(this.isOccupiedAreaOverlapped()){
            this.isStructureValid();
        }
        else {
            this.setWorkerState(this.isStructureValid()?TownWorkerState.VALID: TownWorkerState.NOT_VALID);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public TownWorkerType getWorkerType() {
        return TownWorkerType.WAREHOUSE;
    }


    @Override
    public CompoundTag getWorkData() {
        CompoundTag nbt = getBasicWorkData();
        if(this.isValid()){
            nbt.putDouble("capacity", this.capacity);
        }
        return nbt;
    }

    @Override
    public void setWorkData(CompoundTag data) {
        this.setBasicWorkData(data);
    }

    public int getVolume(){
        return this.isWorkValid()?this.volume:0;
    }
    public int getArea(){
        return this.isWorkValid()?this.area:0;
    }
    public double getCapacity(){
        return this.isWorkValid()?this.capacity:0;
    }

    @Override
    public void tick() {
        if(!this.addedToSchedulerQueue){
            SchedulerQueue.add(this);
            this.addedToSchedulerQueue = true;
        }
    }

    public TeamTown getTown(){
        if(this.teamID == null){
            FHMain.LOGGER.error("WareHouseBlockEntity.getTown(): TeamName is null!");
            return null;
        }
        TeamTownData townData = Objects.requireNonNull(FHTeamDataManager.getDataByResearchID(this.teamID)).getData(SpecialDataTypes.TOWN_DATA);
        return new TeamTown(townData);
    }

    void setTeamID(UUID teamID){
        this.teamID = teamID;
    }

    @Override
    public void readCustomNBT(CompoundTag compoundNBT, boolean isPacket){
        this.teamID = compoundNBT.getUUID("teamID");
    }

    @Override
    public void writeCustomNBT(CompoundTag compoundNBT, boolean isPacket){
        compoundNBT.putUUID("teamID", this.teamID);
    }

}
