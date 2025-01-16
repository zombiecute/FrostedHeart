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

package com.teammoeg.frostedheart.content.scenario.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.teammoeg.chorda.network.CMessage;
import com.teammoeg.frostedheart.content.scenario.client.ClientScene;
import com.teammoeg.frostedheart.content.scenario.client.FHScenarioClient;
import com.teammoeg.chorda.util.io.SerializeUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ServerScenarioCommandPacket implements CMessage {
    private String commandName;
    Map<String, String> params;

    public ServerScenarioCommandPacket(FriendlyByteBuf buffer) {
        commandName = buffer.readUtf();
        params = SerializeUtil.readStringMap(buffer, new HashMap<>(), FriendlyByteBuf::readUtf);
    }

    
    public ServerScenarioCommandPacket(String commandName, Map<String, String> params) {
        super();
        this.commandName = commandName;
        this.params = params;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(commandName);
        SerializeUtil.writeStringMap(buffer, params, (v, p) -> p.writeUtf(v));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // Update client-side nbt
        	//System.out.println(this);
            FHScenarioClient.callCommand(commandName, ClientScene.INSTANCE, params);
        });
        context.get().setPacketHandled(true);
    }

	@Override
	public String toString() {
		return "ServerScenarioCommandPacket [commandName=" + commandName + ", params=" + params + "]";
	}
}
