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

package com.teammoeg.frostedheart.content.steamenergy.debug;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.teammoeg.chorda.block.CTickableBlockEntity;
import com.teammoeg.frostedheart.bootstrap.common.FHBlockEntityTypes;
import com.teammoeg.frostedheart.bootstrap.common.FHCapabilities;
import com.teammoeg.frostedheart.content.steamenergy.*;

import com.teammoeg.frostedheart.util.client.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;

import static net.minecraft.ChatFormatting.GRAY;

public class DebugHeaterTileEntity extends IEBaseBlockEntity implements CTickableBlockEntity, HeatNetworkProvider, NetworkConnector, IHaveGoggleInformation {

    HeatNetwork manager;
    HeatEndpoint endpoint;
    LazyOptional<HeatEndpoint> heatcap;
    ConnectorNetworkRevalidator<DebugHeaterTileEntity> networkHandler=new ConnectorNetworkRevalidator<>(this);

    public DebugHeaterTileEntity(BlockPos pos, BlockState state) {
        super(FHBlockEntityTypes.DEBUGHEATER.get(), pos, state);
        manager = new HeatNetwork( () -> {
            for (Direction d : Direction.values()) {
            	manager.connectTo(level, worldPosition.relative(d),getBlockPos(), d.getOpposite());
            }
        });
        endpoint = new HeatEndpoint(-1, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        heatcap = LazyOptional.of(() -> endpoint);
    }

    @Override
    public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
    }

    @Override
    public void tick() {
        endpoint.addHeat(Integer.MAX_VALUE);
        if(!endpoint.hasValidNetwork())
        	manager.addEndpoint(heatcap.cast(), 0, getLevel(), getBlockPos());
        manager.tick(level);
        networkHandler.tick();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == FHCapabilities.HEAT_EP.capability())
            return heatcap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
    }

	@Override
	public void invalidateCaps() {
		heatcap.invalidate();
		super.invalidateCaps();
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		endpoint.unload();
	}

    @Override
    public HeatNetwork getNetwork() {
        return networkHandler.hasNetwork()?networkHandler.getNetwork():manager;
    }

    @Override
    public boolean canConnectTo(Direction to) {
        return true;
    }

    @Override
    public void setNetwork(HeatNetwork network) {
        networkHandler.setNetwork(network);
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        float output = 0;
        float intake = 0;

        Lang.tooltip("heat_stats").forGoggles(tooltip);

        if (networkHandler.hasNetwork()) {
            output = networkHandler.getNetwork().getTotalEndpointOutput();
            intake = networkHandler.getNetwork().getTotalEndpointIntake();
            Lang.translate("tooltip", "pressure")
                    .style(GRAY)
                    .forGoggles(tooltip);
        } else {
            Lang.translate("tooltip", "pressure.no_network")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
        }

        Lang.number(intake)
                .translate("generic", "unit.pressure")
                .style(ChatFormatting.AQUA)
                .space()
                .add(Lang.translate("tooltip", "pressure.intake")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        Lang.number(output)
                .translate("generic", "unit.pressure")
                .style(ChatFormatting.AQUA)
                .space()
                .add(Lang.translate("tooltip", "pressure.output")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;

    }
}
