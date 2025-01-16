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

package com.teammoeg.frostedheart.content.steamenergy;

import com.teammoeg.chorda.network.FHMessage;
import com.teammoeg.chorda.util.io.SerializeUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.function.Supplier;

public class EndPointDataPacket implements FHMessage {
    private final Collection<HeatEndpoint> data;

    public EndPointDataPacket(HeatNetwork network) {

        this.data = network.getEndpoints();
    }

    public EndPointDataPacket(FriendlyByteBuf buffer) {
        data = SerializeUtil.readList(buffer, HeatEndpoint::readNetwork);
    }

    public void encode(FriendlyByteBuf buffer) {
        SerializeUtil.writeList(buffer, data, HeatEndpoint::writeNetwork);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientHeatHandler.loadEndPoint(data));
        context.get().setPacketHandled(true);
    }
}
