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

package com.teammoeg.frostedheart.content.town.house;

import com.teammoeg.frostedheart.bootstrap.common.FHBlockEntityTypes;
import com.teammoeg.frostedheart.bootstrap.common.FHCapabilities;
import com.teammoeg.frostedheart.content.steamenergy.HeatEndpoint;
import com.teammoeg.frostedheart.content.town.AbstractTownWorkerBlockEntity;
import com.teammoeg.frostedheart.content.town.TownWorkerState;
import com.teammoeg.frostedheart.content.town.TownWorkerType;
import com.teammoeg.frostedheart.util.client.FHClientUtils;
import com.teammoeg.frostedheart.content.town.blockscanner.BlockScanner;
import com.teammoeg.frostedheart.content.town.blockscanner.FloorBlockScanner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * A house in the town.
 * <p>
 * Functionality:
 * - Provide a place for residents to live
 * - (Optional) Consume heat to add temperature based on the heat level
 * - Consume resources to maintain the house
 * - Check if the house structure is valid
 * - Compute comfort rating based on the house structure
 */
public class HouseBlockEntity extends AbstractTownWorkerBlockEntity {

    /** The temperature at which the house is comfortable. */
    public static final double COMFORTABLE_TEMP_HOUSE = 24;
    public static final int MAX_TEMP_HOUSE = 50;
    public static final int MIN_TEMP_HOUSE = 0;

    /** Work data, stored in town. */
    private int maxResident = -1; // how many resident can live here
    //public List<UUID> residents = new ArrayList<>();
    private int volume = -1;
    //private int decoration = -1;
    private int area = -1;
    private double temperature = -1;
    private Map<String, Integer> decorations = new HashMap<>();
    private double rating = -1;
    private double temperatureModifier = 0;

    /** Tile data, stored in tile entity. */
    HeatEndpoint endpoint = new HeatEndpoint(99, 10, 0, 1);

    public HouseBlockEntity(BlockPos pos, BlockState state) {
        super(FHBlockEntityTypes.HOUSE.get(),pos,state);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public TownWorkerType getWorkerType() {
        return TownWorkerType.HOUSE;
    }

    /**
     * Check if work environment is valid.
     * <p>
     * For the house, this implies whether the house would accommodate the residents,
     * consume resources, and other.
     * <p>
     * Room structure should be valid.
     * Temperature should be within a reasonable range.
     */
    public void refresh(){
        if(this.isOccupiedAreaOverlapped()){
            this.isStructureValid();
            this.isTemperatureValid();
        } else{
            this.workerState = this.isStructureValid() && this.isTemperatureValid() ? TownWorkerState.VALID : TownWorkerState.NOT_VALID;
            this.rating = this.computeRating();
            this.maxResident = this.calculateMaxResidents();
        }
    }


    @Override
    public CompoundTag getWorkData() {
        CompoundTag data = getBasicWorkData();
        if(this.isValid()) {
            ListTag residentList = new ListTag();
            data.putInt("maxResident", maxResident);
            data.putDouble("rating", rating);
        }
        return data;
    }

    @Override
    public void setWorkData(CompoundTag data) {
        setBasicWorkData(data);
    }

    public int getMaxResident() {
        return isWorkValid() ? this.maxResident : 0;
    }

    public int getVolume() {
        return isWorkValid() ? this.volume : 0;
    }

    public int getArea() {
        return isWorkValid() ? this.area : 0;
    }

    public double getTemperature() {
        return isWorkValid() ? this.temperature : 0;
    }

    public double getRating() {
        if(this.isWorkValid()){
            if(this.rating == -1){
                return rating = this.computeRating();
            }
            return this.rating;
        }
        return 0;
    }

    public double getTemperatureModifier() {
        return isWorkValid() ? this.temperatureModifier : 0;
    }


    /**
     * Determine whether the house structure is well-defined.
     * <p>
     * Check room insulation
     * Check minimum volume
     * Check within generator range (or just check steam connection instead?)
     * <p>
     *
     * @return whether the house structure is valid
     */
    public boolean isStructureValid() {
        BlockPos housePos = this.getBlockPos();
        List<BlockPos> doorPosSet = BlockScanner.getBlocksAdjacent(housePos, (pos) -> Objects.requireNonNull(level).getBlockState(pos).is(BlockTags.DOORS));
        if (doorPosSet.isEmpty()) return false;
        for (BlockPos doorPos : doorPosSet) {
            BlockPos floorBelowDoor = BlockScanner.getBlockBelow((pos)->!(Objects.requireNonNull(level).getBlockState(pos).is(BlockTags.DOORS)), doorPos);//找到门下面垫的的那个方块
            for (Direction direction : BlockScanner.PLANE_DIRECTIONS) {
                //FHMain.LOGGER.debug("HouseScanner: creating new HouseBlockScanner");
                assert floorBelowDoor != null;
                BlockPos startPos = floorBelowDoor.relative(direction);//找到门下方块旁边的方块
                //FHMain.LOGGER.debug("HouseScanner: start pos 1" + startPos);
                if (!HouseBlockScanner.isValidFloorOrLadder(Objects.requireNonNull(level), startPos)) {//如果门下方块旁边的方块不是合法的地板，找一下它下面的方块
                    if(!HouseBlockScanner.isValidFloorOrLadder(Objects.requireNonNull(level), startPos.below()) || FloorBlockScanner.isHouseBlock(level, startPos.above(2))){//如果它下面的方块也不是合法地板（或者梯子），或者门的上半部分堵了方块，就不找了。我们默认村民不能从两格以上的高度跳下来，也不能从一格高的空间爬过去
                        continue;
                    }
                    startPos = startPos.below();
                    //FHMain.LOGGER.debug("HouseScanner: start pos 2" + startPos);
                }
                HouseBlockScanner scanner = new HouseBlockScanner(this.level, startPos);
                if (scanner.scan()) {
                    //FHMain.LOGGER.debug("HouseScanner: scan successful");
                    this.volume = scanner.getVolume();
                    this.area = scanner.getArea();
                    this.decorations = scanner.getDecorations();
                    this.temperature = scanner.getTemperature();
                    this.occupiedArea = scanner.getOccupiedArea();
                    this.rating = computeRating();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine whether the house temperature is valid for work.
     * <p>
     * If connected to heat network, this always returns true.
     *
     * @return whether the temperature is valid
     */
    public boolean isTemperatureValid() {
        double effective = temperature + temperatureModifier;
        return effective >= MIN_TEMP_HOUSE && effective <= MAX_TEMP_HOUSE;
    }

    public double getEffectiveTemperature() {
        return temperature + temperatureModifier;
    }

    /**
     * Get a comfort rating based on how the house is built.
     * <p>
     * This would affect the mood of the residents on the next day.
     *
     * @return a rating in range of zero to one
     */
    private double computeRating() {
        if(this.isValid()){
            return (calculateSpaceRating(this.volume, this.area) * (1+calculateDecorationRating(this.decorations, this.area))
                    + calculateTemperatureRating(this.temperature + this.temperatureModifier)) / 3;
        }
        else return 0;
    }
    public static double calculateTemperatureRating(double temperature) {
        double tempDiff = Math.abs(COMFORTABLE_TEMP_HOUSE - temperature);
        return 0.017 + 1 / (1 + Math.exp(0.4 * (tempDiff - 10)));
    }
    public static double calculateDecorationRating(Map<?, Integer> decorations, int area) {
        double score = 0;
        for (Integer num : decorations.values()) {
            if (num + 0.32 > 0) { // Ensure the argument for log is positive
                score += Math.log(num + 0.32) * 1.75 + 0.9;
            } else {
                // Handle the case where num + 0.32 <= 0
                // For example, you could add a minimal score or skip adding to the score.
                score += 0; // Or some other handling logic
            }
        }
        return Math.min(1, score / (6 + area / 16.0f));
    }
    public static double calculateSpaceRating(int volume, int area) {
        double height = volume / (float) area;
        double score = area * (1.55 + Math.log(height - 1.6) * 0.6);
        return 1 - Math.exp(-0.024 * Math.pow(score, 1.11));
    }
    private int calculateMaxResidents() {
        if(this.isValid()){
            return (int) (calculateSpaceRating(this.volume, this.area) / 16 * this.area);
        }
        else return 0;
    }

    @Override
    public void tick() {
        assert level != null;
        if (!level.isClientSide) {
            if (endpoint.tryDrainHeat(1)) {
                temperatureModifier = Math.max(endpoint.getTempLevel() * 10, COMFORTABLE_TEMP_HOUSE);
                if (setActive(true)) {
                    setChanged();
                }
            } else {
                temperatureModifier = 0;
                if (setActive(false)) {
                    setChanged();
                }
            }
        } else if (getIsActive()) {
            FHClientUtils.spawnSteamParticles(level, worldPosition);
        }
        this.addToSchedulerQueue();
    }

    @Override
    public void readCustomNBT(CompoundTag compoundNBT, boolean isPacket) {
        endpoint.load(compoundNBT, isPacket);
    }

    @Override
    public void writeCustomNBT(CompoundTag compoundNBT, boolean isPacket) {
        endpoint.save(compoundNBT, isPacket);
    }

    LazyOptional<HeatEndpoint> endpointCap = LazyOptional.of(()-> endpoint);
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if(capability== FHCapabilities.HEAT_EP.capability() && facing == Direction.NORTH) {
            return endpointCap.cast();
        }
        return super.getCapability(capability, facing);
    }
	@Override
	public void invalidateCaps() {
		endpointCap.invalidate();
		super.invalidateCaps();
	}
}