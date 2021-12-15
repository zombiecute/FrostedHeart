/*
 * Copyright (c) 2021 TeamMoeg
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
 */

package com.teammoeg.frostedheart.network;

import com.teammoeg.frostedheart.client.util.ClientUtils;
import com.teammoeg.frostedheart.climate.ClimateData;
import com.teammoeg.frostedheart.climate.TemperatureCore;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class FHClimatePacket {
    private final CompoundNBT data;

    public FHClimatePacket(ClimateData climateData) {
        data = climateData.serializeNBT();
    }

    FHClimatePacket(PacketBuffer buffer) {
        data = buffer.readCompoundTag();
    }

    void encode(PacketBuffer buffer) {
        buffer.writeCompoundTag(data);
    }

    void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // Update client-side nbt
            World world = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientUtils::getWorld);
            if (world != null) {
                world.getCapability(ClimateData.CAPABILITY).ifPresent((cap) -> {
                    cap.deserializeNBT(data);
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}