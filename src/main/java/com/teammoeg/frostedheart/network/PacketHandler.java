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

import com.teammoeg.frostedheart.FHMain;

import blusunrize.immersiveengineering.common.network.MessageTileSync;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
	private static final String VERSION = Integer.toString(1);
	private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(FHMain.rl("network"), () -> VERSION,
			VERSION::equals, VERSION::equals);

	public static void send(PacketDistributor.PacketTarget target, Object message) {
		CHANNEL.send(target, message);
	}

	public static void sendToServer(Object message) {
		CHANNEL.sendToServer(message);
	}

	public static SimpleChannel get() {
		return CHANNEL;
	}

	@SuppressWarnings("UnusedAssignment")
	public static void register() {
		int id = 0;

		// CHANNEL.registerMessage(id++, ChunkWatchPacket.class,
		// ChunkWatchPacket::encode, ChunkWatchPacket::new, ChunkWatchPacket::handle);
		// CHANNEL.registerMessage(id++, ChunkUnwatchPacket.class,
		// ChunkUnwatchPacket::encode, ChunkUnwatchPacket::new,
		// ChunkUnwatchPacket::handle);
		CHANNEL.registerMessage(id++, MessageTileSync.class, MessageTileSync::toBytes, MessageTileSync::new,
				(t, ctx) -> {
					t.process(ctx);
					ctx.get().setPacketHandled(true);
				});
		// CHANNEL.registerMessage(id++, TemperatureChangePacket.class,
		// TemperatureChangePacket::encode, TemperatureChangePacket::new,
		// TemperatureChangePacket::handle);
		CHANNEL.registerMessage(id++, FHDataSyncPacket.class, FHDataSyncPacket::encode, FHDataSyncPacket::new,
				FHDataSyncPacket::handle);
		CHANNEL.registerMessage(id++, FHDatapackSyncPacket.class, FHDatapackSyncPacket::encode,
				FHDatapackSyncPacket::new, FHDatapackSyncPacket::handle);
		CHANNEL.registerMessage(id++, FHResearchRegistrtySyncPacket.class, FHResearchRegistrtySyncPacket::encode,
				FHResearchRegistrtySyncPacket::new, FHResearchRegistrtySyncPacket::handle);
		CHANNEL.registerMessage(id++, FHResearchDataSyncPacket.class, FHResearchDataSyncPacket::encode,
				FHResearchDataSyncPacket::new, FHResearchDataSyncPacket::handle);
		CHANNEL.registerMessage(id++, FHResearchProgressSyncPacket.class, FHResearchProgressSyncPacket::encode,
				FHResearchProgressSyncPacket::new, FHResearchProgressSyncPacket::handle);
		CHANNEL.registerMessage(id++, FHClueProgressSyncPacket.class, FHClueProgressSyncPacket::encode,
				FHClueProgressSyncPacket::new, FHClueProgressSyncPacket::handle);
		CHANNEL.registerMessage(id++, FHClimatePacket.class, FHClimatePacket::encode, FHClimatePacket::new,
				FHClimatePacket::handle);
		CHANNEL.registerMessage(id++, FHEffectTriggerPacket.class, FHEffectTriggerPacket::encode,
				FHEffectTriggerPacket::new, FHEffectTriggerPacket::handle);
	}

}