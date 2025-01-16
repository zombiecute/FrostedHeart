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

package com.teammoeg.frostedheart.content.research.network;

import java.util.function.Supplier;

import com.teammoeg.chorda.network.FHMessage;
import com.teammoeg.chorda.team.TeamDataClosure;
import com.teammoeg.frostedheart.content.research.FHResearch;
import com.teammoeg.frostedheart.content.research.api.ResearchDataAPI;
import com.teammoeg.frostedheart.content.research.data.ResearchData;
import com.teammoeg.frostedheart.content.research.data.TeamResearchData;
import com.teammoeg.frostedheart.content.research.research.Research;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class FHResearchControlPacket implements FHMessage {
    public enum Operator {
        COMMIT_ITEM,
        START,
        PAUSE
    }

    public final Operator status;
    private final int researchID;


    public FHResearchControlPacket(Operator status, Research research) {
        super();
        this.status = status;
        this.researchID = FHResearch.researches.getIntId(research);
    }

    public FHResearchControlPacket(FriendlyByteBuf buffer) {
        researchID = buffer.readVarInt();
        status = Operator.values()[buffer.readByte()];
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(researchID);
        buffer.writeByte(status.ordinal());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {

        context.get().enqueueWork(() -> {
            Research r = FHResearch.researches.getById(researchID);
            if(r==null)return;
            ServerPlayer spe = context.get().getSender();
            TeamDataClosure<TeamResearchData> trd = ResearchDataAPI.getData(spe);
            switch (status) {
                case COMMIT_ITEM:

                    ResearchData rd = trd.get().getData(r);
                    if (rd.canResearch()) return;
                    if (trd.get().commitItem(spe,trd.team(),r)) {
                        trd.get().setCurrentResearch(trd.team(),r);
                    }
                    return;
                case START:
                    trd.get().setCurrentResearch(trd.team(),r);
                    return;
                case PAUSE:
                    trd.get().clearCurrentResearch(trd.team(),true);
            }
        });
        context.get().setPacketHandled(true);
    }
}
