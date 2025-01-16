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

package com.teammoeg.frostedheart.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.teammoeg.chorda.util.lang.Components;
import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.content.water.capability.WaterLevelCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FHMain.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WaterCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        // Get waterlevel from WaterLevelCapability
        LiteralArgumentBuilder<CommandSourceStack> water = Commands.literal("water")
                .then(Commands.literal("level")
                        .then(Commands.literal("get").executes(ct -> {
                            WaterLevelCapability.getCapability(ct.getSource().getPlayer()).ifPresent(data -> {
                                int waterLevel = data.getWaterLevel();
                                ct.getSource().sendSuccess(()-> Components.str("Water Level: " + waterLevel).withStyle(ChatFormatting.BLUE), false);
                            });
                            return Command.SINGLE_SUCCESS;
                        }))
                        // add
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 20)).executes(ct -> {
                                    WaterLevelCapability.getCapability(ct.getSource().getPlayer()).ifPresent(data -> {
                                        int amount = ct.getArgument("amount", Integer.class);
                                        data.addWaterLevel(ct.getSource().getPlayer(), amount);
                                        ct.getSource().sendSuccess(()-> Components.str("Water Level Added").withStyle(ChatFormatting.BLUE), false);
                                    });
                                    return Command.SINGLE_SUCCESS;
                                }))
                        )
                        // fill, do not take in amount, just fill up to 20
                        .then(Commands.literal("fill").executes(ct -> {
                            WaterLevelCapability.getCapability(ct.getSource().getPlayer()).ifPresent(data -> {
                                data.setWaterLevel(20);
                                ct.getSource().sendSuccess(()-> Components.str("Water Level Filled").withStyle(ChatFormatting.BLUE), false);
                            });
                            return Command.SINGLE_SUCCESS;
                        }))
                        // set
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 20)).executes(ct -> {
                                    WaterLevelCapability.getCapability(ct.getSource().getPlayer()).ifPresent(data -> {
                                        int amount = ct.getArgument("amount", Integer.class);
                                        data.setWaterLevel(amount);
                                        ct.getSource().sendSuccess(()-> Components.str("Water Level Set: " + amount).withStyle(ChatFormatting.BLUE), false);
                                    });
                                    return Command.SINGLE_SUCCESS;
                                }))
                        )
                );

        for (String string : new String[]{FHMain.MODID, FHMain.ALIAS, FHMain.TWRID}) {
            dispatcher.register(Commands.literal(string).requires(s -> s.hasPermission(2)).then(water));
        }

        // create a simple alias command /drink for /fh water level fill
        dispatcher.register(Commands.literal("drink")
                .requires(s -> s.hasPermission(2))
                .executes(ct -> {
                    WaterLevelCapability.getCapability(ct.getSource().getPlayer()).ifPresent(data -> {
                        data.setWaterLevel(20);
                        ct.getSource().sendSuccess(()-> Components.str("Water Level Filled").withStyle(ChatFormatting.BLUE), false);
                    });
                    return Command.SINGLE_SUCCESS;
                })
        );

    }
}
