package com.teammoeg.frostedheart.content.steamenergy.creative;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;
import com.teammoeg.frostedheart.content.steamenergy.HeatEndpoint;
import com.teammoeg.frostedheart.util.client.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CreativeHeaterBlockEntity extends HeatManagerBlockEntity {
    public static final int DEFAULT_HEAT = 256;
    public static final int MAX_HEAT = 8192;
    protected ScrollValueBehaviour generatedHeat;
    public CreativeHeaterBlockEntity(BlockEntityType<? extends BlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        endpoint = new HeatEndpoint(-1, MAX_HEAT * 4, DEFAULT_HEAT, 0);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        generatedHeat = new HeatScrollValueBehavior(Lang.gui("creative_heater.heat").component(), this, new HeaterValueBox());
        generatedHeat.between(0, MAX_HEAT);
        generatedHeat.value = DEFAULT_HEAT;
        generatedHeat.withCallback(i -> updateHeat());
        behaviours.add(generatedHeat);
    }

    @Override
    public void tick() {
        super.tick();
        endpoint.fill();
    }

    public void updateHeat() {
        endpoint.setMaxOutput(generatedHeat.getValue());
    }

    class HeaterValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16.5);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis()
                    .isHorizontal();
        }

    }
}
