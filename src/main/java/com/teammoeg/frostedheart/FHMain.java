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

package com.teammoeg.frostedheart;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.teammoeg.frostedheart.bootstrap.common.FHSpecialDataTypes;
import com.teammoeg.frostedheart.bootstrap.client.FHTabs;
import com.teammoeg.frostedheart.bootstrap.client.FHTooltips;
import com.teammoeg.frostedheart.bootstrap.common.FHAttributes;
import com.teammoeg.frostedheart.bootstrap.common.FHBlockEntityTypes;
import com.teammoeg.frostedheart.bootstrap.common.FHBlocks;
import com.teammoeg.frostedheart.bootstrap.common.FHCapabilities;
import com.teammoeg.frostedheart.bootstrap.common.FHEntityTypes;
import com.teammoeg.frostedheart.bootstrap.common.FHFluids;
import com.teammoeg.frostedheart.bootstrap.common.FHItems;
import com.teammoeg.frostedheart.bootstrap.common.FHLoot;
import com.teammoeg.frostedheart.bootstrap.common.FHMenuTypes;
import com.teammoeg.frostedheart.bootstrap.common.FHMobEffects;
import com.teammoeg.frostedheart.bootstrap.common.FHMultiblocks;
import com.teammoeg.frostedheart.bootstrap.common.FHPredicates;
import com.teammoeg.frostedheart.bootstrap.common.FHRecipes;
import com.teammoeg.frostedheart.bootstrap.reference.FHParticleTypes;
import com.teammoeg.frostedheart.bootstrap.reference.FHProps;
import com.teammoeg.frostedheart.bootstrap.reference.FHSoundEvents;
import com.teammoeg.frostedheart.bootstrap.reference.FHTags;
import com.teammoeg.frostedheart.compat.create.CreateCompat;
import com.teammoeg.frostedheart.compat.curios.CuriosCompat;
import com.teammoeg.frostedheart.compat.ftbq.FHRewardTypes;
import com.teammoeg.frostedheart.compat.tetra.TetraCompat;
import com.teammoeg.frostedheart.content.climate.tooltips.BlockTempStats;
import com.teammoeg.frostedheart.content.climate.tooltips.EquipmentTempStats;
import com.teammoeg.frostedheart.content.climate.tooltips.FoodTempStats;
import com.teammoeg.frostedheart.content.climate.tooltips.PlantTempStats;
import com.teammoeg.frostedheart.content.health.tooltip.FoodNutritionStats;
import com.teammoeg.frostedheart.infrastructure.data.FHRecipeReloadListener;
import com.teammoeg.frostedheart.infrastructure.gen.FHRegistrate;
import com.teammoeg.frostedheart.compat.ftbteams.FTBTeamsEvents;
import com.teammoeg.frostedheart.infrastructure.config.FHConfig;
import com.teammoeg.frostedheart.content.world.FHBiomeModifiers;
import com.teammoeg.frostedheart.content.world.FHBiomes;
import com.teammoeg.frostedheart.content.world.FHFeatures;
import com.teammoeg.frostedheart.util.FHRemote;
import com.teammoeg.frostedheart.util.FHVersion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;

@Mod(FHMain.MODID)
public class FHMain {

    // Constants
    public static final String MODID = "frostedheart";
    public static final String ALIAS = "fh";
    public static final String TWRID = "twr";
    public static final String MODNAME = "Frosted Heart";
    public static final String MODPACK = "The Winter Rescue";

    // Logger
    public static final Logger LOGGER = LogManager.getLogger(MODNAME + " (" + MODPACK + ")");
    public static final Marker VERSION_CHECK = MarkerManager.getMarker("Version Check");
    public static final Marker INIT = MarkerManager.getMarker("Init");
    public static final Marker SETUP = MarkerManager.getMarker("Setup");
    public static final Marker COMMON_INIT = MarkerManager.getMarker("Common").addParents(INIT);
    public static final Marker CLIENT_INIT = MarkerManager.getMarker("Client").addParents(INIT);
    public static final Marker COMMON_SETUP = MarkerManager.getMarker("Common").addParents(SETUP);
    public static final Marker CLIENT_SETUP = MarkerManager.getMarker("Client").addParents(SETUP);

    // Remote
    public static FHRemote remote;
    public static FHRemote local;
    public static FHRemote pre;

    // Update
    public static File lastbkf;
    public static File lastServerConfig;
    public static boolean saveNeedUpdate;

    // Registrate
    public static final FHRegistrate REGISTRATE = FHRegistrate.create(MODID);
    static {
        REGISTRATE.setTooltipModifierFactory(item -> {
            return new ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE)
                    .andThen(new FoodTempStats(item))
                    .andThen(TooltipModifier.mapNull(FoodNutritionStats.create(item)))
                    .andThen(TooltipModifier.mapNull(PlantTempStats.create(item)))
                    .andThen(TooltipModifier.mapNull(BlockTempStats.create(item)))
                    .andThen(TooltipModifier.mapNull(EquipmentTempStats.create(item)))
                    .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
                    ;
        });
        FHTooltips.registerTooltipModifiers();
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

    /**
     * Do all initialization that does not require deferred registers to be filled.
     */
    public FHMain() {
        IEventBus mod = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forge = MinecraftForge.EVENT_BUS;

        // Config
        LOGGER.info(COMMON_INIT, "Loading Config");
        FHConfig.register();

        // FH Remote Version Check
        LOGGER.info(VERSION_CHECK, "Checking for updates");
        local = new FHRemote.FHLocal();
        remote = new FHRemote();
        if (local.fetchVersion().resolve().orElse(FHVersion.empty).getOriginal().contains("pre"))
            pre = new FHRemote.FHPreRemote();
        LOGGER.info(VERSION_CHECK, "Check completes. Running on version " + local.fetchVersion().resolve().orElse(FHVersion.empty).getOriginal());

        // Registrate
        LOGGER.info(COMMON_INIT, "Registering Registrate");
        REGISTRATE.registerEventListeners(mod);

        // Init
        LOGGER.info(COMMON_INIT, "Initializing " + MODNAME);
        FHTags.init();
        FHItems.init();
        FHProps.init();
        FHBlocks.init();
        FHBlockEntityTypes.init();
        FHBiomes.init();
        FHMultiblocks.Multiblock.init();
        FHPredicates.init();

        // Compat init
        LOGGER.info(COMMON_INIT, "Initializing Mod Compatibilities");
        FHSpecialDataTypes.init();
        if (ModList.get().isLoaded("create"))
            CreateCompat.init();
        if (ModList.get().isLoaded("tetra"))
            TetraCompat.init();
        if (ModList.get().isLoaded("ftbquests"))
            FHRewardTypes.init();
        if (ModList.get().isLoaded("ftbteams"))
            FTBTeamsEvents.init();

        // Deferred Registration
        // Order doesn't matter here, as that's why we use deferred registers
        // See ForgeRegistries for more info
        LOGGER.info(COMMON_INIT, "Registering Deferred Registers");
        FHEntityTypes.ENTITY_TYPES.register(mod);
        FHFluids.FLUIDS.register(mod);
        FHFluids.FLUID_TYPES.register(mod);
        FHMobEffects.EFFECTS.register(mod);
        FHParticleTypes.REGISTER.register(mod);
        FHBlockEntityTypes.REGISTER.register(mod);
        FHMenuTypes.CONTAINERS.register(mod);
        FHTabs.TABS.register(mod);
        FHItems.ITEMS.register(mod);
        FHBlocks.BLOCKS.register(mod);
        FHSoundEvents.SOUNDS.register(mod);
        FHRecipes.RECIPE_SERIALIZERS.register(mod);
        FHRecipes.RECIPE_TYPES.register(mod);
        FHFeatures.FEATURES.register(mod);
        FHBiomes.BIOME_REGISTER.register(mod);
        FHBiomeModifiers.BIOME_MODIFIERS.register(mod);
        FHAttributes.REGISTER.register(mod);
        FHLoot.LC_REGISTRY.register(mod);
        FHLoot.LM_REGISTRY.register(mod);

        // Forge bus
        LOGGER.info(COMMON_INIT, "Registering Forge Event Listeners");
        forge.register(new FHRecipeReloadListener(null));

        // Mod bus
        LOGGER.info(COMMON_INIT, "Registering Mod Event Listeners");
        mod.addListener(this::setup);
        mod.addListener(this::processIMC);
        mod.addListener(this::enqueueIMC);
        mod.addListener(this::loadComplete);

        // Client setup
        LOGGER.info(COMMON_INIT, "Proceeding to Client Initialization");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FHClient.init();
        }
    }

    /**
     * Setup stuff that requires deferred registers to be filled.
     * @param event The event
     */
    private void setup(final FMLCommonSetupEvent event) {
        FHNetwork.register();
        FHCapabilities.setup();
        // modify default value
        GameRules.GAME_RULE_TYPES.put(GameRules.RULE_SPAWN_RADIUS, IntegerValue.create(0));

    }

    /**
     * Enqueue Inter-Mod Communication
     * @param event The event
     */
    private void enqueueIMC(final InterModEnqueueEvent event) {
        CuriosCompat.sendIMCS();
    }

    /**
     * Process Inter-Mod Communication
     * @param event The event
     */
    private void processIMC(final InterModProcessEvent event) {

    }

    /**
     * Stuff that needs to be done after everything is loaded.
     * In general, not used.
     * @param event The event
     */
    private void loadComplete(FMLLoadCompleteEvent event) {

    }
}
