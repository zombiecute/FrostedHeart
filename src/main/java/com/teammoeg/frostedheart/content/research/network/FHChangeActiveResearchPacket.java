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

import com.teammoeg.chorda.network.CMessage;
import com.teammoeg.frostedheart.content.research.FHResearch;
import com.teammoeg.frostedheart.content.research.ResearchUtils;
import com.teammoeg.frostedheart.content.research.api.ClientResearchDataAPI;
import com.teammoeg.frostedheart.content.research.research.Research;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

// send when data update
public class FHChangeActiveResearchPacket implements CMessage {
    private final int id;

    public FHChangeActiveResearchPacket() {
        this.id = 0;
    }

    public FHChangeActiveResearchPacket(int rid) {
        this.id = rid;
    }

    public FHChangeActiveResearchPacket(FriendlyByteBuf buffer) {
        id = buffer.readVarInt();
    }

    public FHChangeActiveResearchPacket(Research rs) {
        this.id = FHResearch.researches.getIntId(rs);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(id);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientResearchDataAPI.getData().get().setCurrentResearch(id);
            ResearchUtils.refreshResearchGui();
        });
        context.get().setPacketHandled(true);
    }
}
