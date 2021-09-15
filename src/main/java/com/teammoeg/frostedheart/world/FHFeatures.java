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

package com.teammoeg.frostedheart.world;

import java.util.ArrayList;

import com.cannolicatfish.rankine.init.RankineBlocks;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class FHFeatures {
    public static final Feature<FHOreFeatureConfig> FHORE = new FHOreFeature(FHOreFeatureConfig.CODEC);
    public static ArrayList<ConfiguredFeature> FH_ORES = new ArrayList();
    public static final ConfiguredFeature<?, ?> ore_magnetite = register("ore_magnetite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.magnetite, RankineBlocks.MAGNETITE_ORE.get().getStateContainer().getBaseState(), 40)).range(64).square().chance(3));
    public static final ConfiguredFeature<?, ?> ore_pyrite = register("ore_pyrite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.pyrite, RankineBlocks.PYRITE_ORE.get().getStateContainer().getBaseState(), 40)).range(35).square().chance(4));
    public static final ConfiguredFeature<?, ?> ore_native_copper = register("ore_native_copper", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.native_copper, RankineBlocks.NATIVE_COPPER_ORE.get().getStateContainer().getBaseState(), 40)).range(65).square()).chance(2);
    public static final ConfiguredFeature<?, ?> ore_malachite = register("ore_malachite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.malachite, RankineBlocks.MALACHITE_ORE.get().getStateContainer().getBaseState(), 45)).range(65).square().chance(2));
    public static final ConfiguredFeature<?, ?> ore_pentlandite = register("ore_pentlandite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.pentlandite, RankineBlocks.PENTLANDITE_ORE.get().getStateContainer().getBaseState(), 40)).range(65).square().chance(7));
    public static final ConfiguredFeature<?, ?> ore_native_tin = register("ore_native_tin", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.native_tin, RankineBlocks.NATIVE_TIN_ORE.get().getStateContainer().getBaseState(), 40)).range(65).square().chance(2));
    public static final ConfiguredFeature<?, ?> ore_cassiterite = register("ore_cassiterite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.cassiterite, RankineBlocks.CASSITERITE_ORE.get().getStateContainer().getBaseState(), 45)).range(65).square().chance(2));
    public static final ConfiguredFeature<?, ?> ore_bituminous = register("ore_bituminous", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.bituminous, RankineBlocks.BITUMINOUS_ORE.get().getStateContainer().getBaseState(), 55)).range(80).square().chance(12));
    public static final ConfiguredFeature<?, ?> ore_lignite = register("ore_lignite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.lignite, RankineBlocks.LIGNITE_ORE.get().getStateContainer().getBaseState(), 40)).range(80).square().chance(2));
    public static final ConfiguredFeature<?, ?> ore_bauxite = register("ore_bauxite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.bauxite, RankineBlocks.BAUXITE_ORE.get().getStateContainer().getBaseState(), 50)).range(60).square().chance(2));
    public static final ConfiguredFeature<?, ?> ore_stibnite = register("ore_stibnite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.stibnite, RankineBlocks.STIBNITE_ORE.get().getStateContainer().getBaseState(), 35)).range(65).square().chance(12));
    public static final ConfiguredFeature<?, ?> ore_cinnabar = register("ore_cinnabar", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.pyrite, RankineBlocks.CINNABAR_ORE.get().getStateContainer().getBaseState(), 40)).range(30).square().chance(6));
    public static final ConfiguredFeature<?, ?> ore_magnesite = register("ore_magnesite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.magnesite, RankineBlocks.MAGNESITE_ORE.get().getStateContainer().getBaseState(), 40)).range(65).square().chance(10));
    public static final ConfiguredFeature<?, ?> ore_galena = register("ore_galena", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.pyrite, RankineBlocks.GALENA_ORE.get().getStateContainer().getBaseState(), 40)).range(40).square().chance(7));
    public static final ConfiguredFeature<?, ?> ore_halite = register("ore_halite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.bauxite, RankineBlocks.HALITE_ORE.get().getStateContainer().getBaseState(), 40)).range(65).square().chance(7));
    public static final ConfiguredFeature<?, ?> ore_fluorite = register("ore_fluorite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.gold, RankineBlocks.FLUORITE_ORE.get().getStateContainer().getBaseState(), 35)).range(65).square().chance(10));
    public static final ConfiguredFeature<?, ?> ore_silver = register("ore_silver", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.gold, RankineBlocks.NATIVE_SILVER_ORE.get().getStateContainer().getBaseState(), 35)).range(30).square().chance(12));
    public static final ConfiguredFeature<?, ?> ore_gold = register("ore_gold", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.gold, RankineBlocks.NATIVE_GOLD_ORE.get().getStateContainer().getBaseState(), 35)).range(30).square().chance(12));
    public static final ConfiguredFeature<?, ?> ore_sphalerite = register("ore_sphalerite", FHORE.withConfiguration(new FHOreFeatureConfig(FHOreFeatureConfig.FillerBlockType.pyrite, RankineBlocks.SPHALERITE_ORE.get().getStateContainer().getBaseState(), 40)).range(65).square().chance(4));

    private static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> register(String key, ConfiguredFeature<FC, ?> configuredFeature) {
        FH_ORES.add(configuredFeature);
        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, key, configuredFeature);
    }

}
