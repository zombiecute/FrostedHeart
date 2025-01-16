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

package com.teammoeg.frostedheart.content.research.network;

import java.util.function.Supplier;

import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.chorda.team.FHClientTeamDataManager;
import com.teammoeg.chorda.network.FHMessage;
import com.teammoeg.frostedheart.bootstrap.common.FHSpecialDataTypes;
import com.teammoeg.frostedheart.compat.jei.JEICompat;
import com.teammoeg.frostedheart.content.research.data.TeamResearchData;
import com.teammoeg.chorda.util.io.codec.DataOps;
import com.teammoeg.chorda.util.io.codec.ObjectWriter;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

// send when player join
public class FHResearchDataSyncPacket implements FHMessage {
	Object dat;


	public FHResearchDataSyncPacket(FriendlyByteBuf buffer) {
		this.dat = (ObjectWriter.readObject(buffer));
    }

    public FHResearchDataSyncPacket(TeamResearchData team) {
        try {
            this.dat = (FHSpecialDataTypes.RESEARCH_DATA.saveData(DataOps.COMPRESSED, team));
        } catch (Exception e) {
            FHMain.LOGGER.error("Failed to save research data when syncing research data", e);
        }
    }


    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            try {
                FHClientTeamDataManager.INSTANCE.getInstance().setData(FHSpecialDataTypes.RESEARCH_DATA, FHSpecialDataTypes.RESEARCH_DATA.loadData(DataOps.COMPRESSED, dat));
            } catch (Exception e) {
                FHMain.LOGGER.error("Failed to load data when syncing research data", e);
            }
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> JEICompat::syncJEI);
        });
        context.get().setPacketHandled(true);
    }

	@Override
	public void encode(FriendlyByteBuf buffer) {
		ObjectWriter.writeObject(buffer, dat);
	}
}
