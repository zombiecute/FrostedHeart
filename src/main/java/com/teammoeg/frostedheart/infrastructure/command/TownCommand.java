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

import java.util.Arrays;
import java.util.stream.IntStream;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.teammoeg.chorda.util.lang.Components;
import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.content.town.TeamTown;
import com.teammoeg.frostedheart.content.town.resident.Resident;
import com.teammoeg.frostedheart.util.client.Lang;
import com.teammoeg.frostedheart.content.town.resource.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.item.ItemStack;

@Mod.EventBusSubscriber(modid = FHMain.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TownCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> name =
                Commands.literal("name")
                        .executes(ct -> {
                            TeamTown town = TeamTown.from(ct.getSource().getPlayerOrException());
                            ct.getSource().sendSuccess(()-> Components.str(town.getName()), true);
                            return Command.SINGLE_SUCCESS;
                        });

        LiteralArgumentBuilder<CommandSourceStack> listItemStackResources =
                Commands.literal("list_items")
                        .executes(ct -> {
                            TeamTown town = TeamTown.from(ct.getSource().getPlayerOrException());
                            ct.getSource().sendSuccess(()-> Components.str(town.getResourceManager().resourceHolder.getAllItems() ), true);
                            return Command.SINGLE_SUCCESS;
                        });

        LiteralArgumentBuilder<CommandSourceStack> listVirtualResources =
                Commands.literal("list_virtual")
                        .executes(ct -> {
                            TeamTown town = TeamTown.from(ct.getSource().getPlayerOrException());
                            System.out.println(town.getResourceManager().resourceHolder.getAllVirtualResources());
                            ct.getSource().sendSuccess(()-> Components.str(town.getResourceManager().resourceHolder.getAllVirtualResources() ), true);
                            return Command.SINGLE_SUCCESS;
                        });

        LiteralArgumentBuilder<CommandSourceStack> addVirtualResources =
                Commands.literal("addVirtual")
                        .then(Commands.argument("type", StringArgumentType.string())
                                .suggests((ct, s) -> {
                                    // Get all TownResourceType enum values
                                    Arrays.stream(VirtualResourceType.values()).forEach(t -> s.suggest(t.getKey()));
                                    return s.buildFuture();
                                })
                                .then(Commands.argument("level", IntegerArgumentType.integer())
                                        .suggests((ct, s) -> {
                                            IntStream.rangeClosed(0, VirtualResourceType.from(StringArgumentType.getString(ct, "type")).maxLevel)
                                                    .forEach(s::suggest);
                                            return s.buildFuture();
                                        })
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                .executes(ct -> {
                                                    double amount = DoubleArgumentType.getDouble(ct, "amount");
                                                    String type = StringArgumentType.getString(ct, "type");
                                                    int level = IntegerArgumentType.getInteger(ct, "level");
                                                    if(amount < 0){
                                                        ct.getSource().sendFailure(Components.str("Invalid amount: Amount must be positive."));
                                                        return Command.SINGLE_SUCCESS;
                                                    }
                                                    TeamTown town = TeamTown.from(ct.getSource().getPlayerOrException());
                                                    ResourceActionResult result = town.getResourceManager().addIfHaveCapacity(VirtualResourceType.from(type).generateKey(level), amount);
                                                    if(result.allSuccess()){
                                                        ct.getSource().sendSuccess(()-> Components.str("Resource added"), true);
                                                    } else ct.getSource().sendSuccess(()-> Components.str("Resource added failed: No enough capacity."), true);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        );

        LiteralArgumentBuilder<CommandSourceStack> costResource =
                Commands.literal("cost")
                        .then(Commands.argument("type", StringArgumentType.string())
                                .suggests((ct, s) -> {
                                    // Get all TownResourceType enum values
                                    Arrays.stream(VirtualResourceType.values()).forEach(t -> s.suggest(t.getKey()));
                                    Arrays.stream(ItemResourceType.values()).forEach(t -> s.suggest(t.getKey()));
                                    return s.buildFuture();
                                })
                                .then(Commands.argument("level", IntegerArgumentType.integer())
                                        .suggests((ct, s) -> {
                                            ITownResourceType type = ITownResourceType.from(StringArgumentType.getString(ct, "type"));
                                            if(type == null) return s.buildFuture();
                                            IntStream.rangeClosed(0, type.getMaxLevel())
                                                    .forEach(s::suggest);
                                            return s.buildFuture();
                                        })
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                .executes(ct -> {
                                                    double amount = DoubleArgumentType.getDouble(ct, "amount");
                                                    String typeString = StringArgumentType.getString(ct, "type");
                                                    int level = IntegerArgumentType.getInteger(ct, "level");
                                                    ITownResourceType type = ITownResourceType.from(typeString);
                                                    if(type == null){
                                                        ct.getSource().sendFailure(Components.str("Invalid type"));
                                                        return Command.SINGLE_SUCCESS;
                                                    }
                                                    if(amount < 0){
                                                        ct.getSource().sendFailure(Components.str("Invalid amount: Amount must be positive."));
                                                        return Command.SINGLE_SUCCESS;
                                                    }
                                                    TeamTown town = TeamTown.from(ct.getSource().getPlayerOrException());
                                                    ResourceActionResult result = null;
                                                    result = town.getResourceManager().costIfHaveEnough(type.generateKey(level), amount);
                                                    if(result.allSuccess()){
                                                        ct.getSource().sendSuccess(()-> Components.str("Resource costed."), true);
                                                    } else ct.getSource().sendSuccess(()-> Components.str("Resource cost failed: No enough resource."), true);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        );

        LiteralArgumentBuilder<CommandSourceStack> addItemOnHand =
                Commands.literal("addItemOnHand")
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                .executes(ct -> {
                                    double amount = DoubleArgumentType.getDouble(ct, "amount");
                                    TeamTown town = TeamTown.from(ct.getSource().getPlayerOrException());
                                    ItemStack itemStack = ct.getSource().getPlayerOrException().getMainHandItem();
                                    ct.getSource().sendSuccess(()-> Components.str("Adding ItemStack: " + itemStack), true);
                                    ResourceActionResult result = town.getResourceManager().addIfHaveCapacity(itemStack, amount);
                                    if(result.allSuccess()){
                                        ct.getSource().sendSuccess(()-> Components.str("Resource added"), true);
                                        return Command.SINGLE_SUCCESS;
                                    } else ct.getSource().sendSuccess(()-> Components.str("Resource added failed: No enough capacity."), true);
                                    return Command.SINGLE_SUCCESS;
                                })

                        );

        LiteralArgumentBuilder<CommandSourceStack> listResidents =
                Commands.literal("list").executes(ct -> {
                    TeamTown town = TeamTown.from(ct.getSource().getPlayerOrException());
                            int size = town.getResidents().values().size();
                            ct.getSource().sendSuccess(()-> Components.str("Total residents: " + size), true);
                            ct.getSource().sendSuccess(()-> Components.str(town.getResidents().values()), true);
                            return Command.SINGLE_SUCCESS;
                        });

        LiteralArgumentBuilder<CommandSourceStack> addResident =
                Commands.literal("add")
                        .then(Commands.argument("first_name", StringArgumentType.string())
                                .then(Commands.argument("last_name", StringArgumentType.string()).executes(ct -> {
                                    TeamTown town = TeamTown.from(ct.getSource().getPlayerOrException());
                                    town.addResident(new Resident(StringArgumentType.getString(ct, "first_name"), StringArgumentType.getString(ct, "last_name")));
                                    ct.getSource().sendSuccess(()-> Components.str("Resident added"), true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        );

        LiteralArgumentBuilder<CommandSourceStack> listBlocks =
                Commands.literal("list").executes(ct -> {
                    TeamTown town = TeamTown.from(ct.getSource().getPlayerOrException());
                    ct.getSource().sendSuccess(()-> Components.str("Total blocks: " + town.getTownBlocks().size()), true);
                    town.getTownBlocks().forEach((k, v) -> {
                        String blockName = v.getType().getBlock().getDescriptionId();
                        ct.getSource().sendSuccess(()-> Lang.translateKey(blockName).append(Components.str(" at " + k)), true);
                    });
                    return Command.SINGLE_SUCCESS;
                });

        for (String string : new String[]{FHMain.MODID, FHMain.ALIAS, FHMain.TWRID}) {
            dispatcher.register(Commands.literal(string)
                    .requires(s -> s.hasPermission(2))
                    .then(Commands.literal("town")
                            .then(name)
                            .then(Commands.literal("resources")
                                    .then(listItemStackResources)
                                .then(listVirtualResources)
                                .then(addVirtualResources)
                                    .then(addItemOnHand)
                                .then(costResource)
                            )
                            .then(Commands.literal("residents")
                                    .then(listResidents)
                                    .then(addResident)
                            )
                            .then(Commands.literal("blocks")
                                    .then(listBlocks)
                            )
                    )
            );
        }

        // alias without modid
        dispatcher.register(Commands.literal("town")
                .requires(s -> s.hasPermission(2))
                .then(name)
                .then(Commands.literal("resources")
                        .then(listVirtualResources)
                        .then(listItemStackResources)
                        .then(addVirtualResources)
                        .then(addItemOnHand)
                        .then(costResource)
                )
                .then(Commands.literal("residents")
                        .then(listResidents)
                        .then(addResident)
                )
                .then(Commands.literal("blocks")
                        .then(listBlocks)
                )
        );
    }
}
