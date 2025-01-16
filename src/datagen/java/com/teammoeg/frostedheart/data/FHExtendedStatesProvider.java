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

package com.teammoeg.frostedheart.data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blusunrize.immersiveengineering.data.models.NongeneratedModels;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.teammoeg.chorda.util.RegistryUtils;

import blusunrize.immersiveengineering.data.DataGenUtils;
import blusunrize.immersiveengineering.data.models.IEOBJBuilder;
import blusunrize.immersiveengineering.data.models.SplitModelBuilder;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.ObjModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class FHExtendedStatesProvider extends BlockStateProvider {
    protected static final List<Vec3i> COLUMN_THREE = ImmutableList.of(BlockPos.ZERO, BlockPos.ZERO.above(), BlockPos.ZERO.above(2));
    protected static final ExistingFileHelper.ResourceType MODEL = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".json", "models");
    protected static final Map<ResourceLocation, String> generatedParticleTextures = new HashMap<>();
    protected final ExistingFileHelper existingFileHelper;

    String modid;

    public FHExtendedStatesProvider(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
        super(gen.getPackOutput(), modid, exFileHelper);
        this.modid = modid;
        this.existingFileHelper = exFileHelper;
    }


    protected String name(Block b)
    {
        return RegistryUtils.getRegistryName(b).getPath();
    }

    public void simpleBlockItem(Block b, ModelFile model)
    {
        simpleBlockItem(b, new ConfiguredModel(model));
    }

    protected void simpleBlockItem(Block b, ConfiguredModel model)
    {
        simpleBlock(b, model);
        itemModel(b, model.model);
    }

    protected void cubeSideVertical(Block b, ResourceLocation side, ResourceLocation vertical)
    {
        simpleBlockItem(b, models().cubeBottomTop(name(b), side, vertical, vertical));
    }

    protected void cubeAll(Block b, ResourceLocation texture)
    {
        simpleBlockItem(b, models().cubeAll(name(b), texture));
    }

    public void layeredBlock(SnowLayerBlock block, ModelFile... models) {
        getVariantBuilder(block)
                .partialState().with(SnowLayerBlock.LAYERS, 1)
                .modelForState().modelFile(models[0]).addModel()
                .partialState().with(SnowLayerBlock.LAYERS, 2)
                .modelForState().modelFile(models[1]).addModel()
                .partialState().with(SnowLayerBlock.LAYERS, 3)
                .modelForState().modelFile(models[2]).addModel()
                .partialState().with(SnowLayerBlock.LAYERS, 4)
                .modelForState().modelFile(models[3]).addModel()
                .partialState().with(SnowLayerBlock.LAYERS, 5)
                .modelForState().modelFile(models[4]).addModel()
                .partialState().with(SnowLayerBlock.LAYERS, 6)
                .modelForState().modelFile(models[5]).addModel()
                .partialState().with(SnowLayerBlock.LAYERS, 7)
                .modelForState().modelFile(models[6]).addModel()
                .partialState().with(SnowLayerBlock.LAYERS, 8)
                .modelForState().modelFile(models[7]).addModel();
    }

    protected void layered(SnowLayerBlock layer, Block block, ResourceLocation texture)
    {
        layeredBlock(
                layer,
                models().withExistingParent(name(layer) + "_height2", mcLoc("block/snow_height2"))
                        .texture("particle", texture)
                        .texture("texture", texture),
                models().withExistingParent(name(layer) + "_height4", mcLoc("block/snow_height4"))
                        .texture("particle", texture)
                        .texture("texture", texture),
                models().withExistingParent(name(layer) + "_height6", mcLoc("block/snow_height6"))
                        .texture("particle", texture)
                        .texture("texture", texture),
                models().withExistingParent(name(layer) + "_height8", mcLoc("block/snow_height8"))
                        .texture("particle", texture)
                        .texture("texture", texture),
                models().withExistingParent(name(layer) + "_height10", mcLoc("block/snow_height10"))
                        .texture("particle", texture)
                        .texture("texture", texture),
                models().withExistingParent(name(layer) + "_height12", mcLoc("block/snow_height12"))
                        .texture("particle", texture)
                        .texture("texture", texture),
                models().withExistingParent(name(layer) + "_height14", mcLoc("block/snow_height14"))
                        .texture("particle", texture)
                        .texture("texture", texture),
                models().withExistingParent(name(block), mcLoc("block/cube_all"))
                        .texture("all", texture)
        );

        // one layer item model
        itemModel(
                layer,
                models().withExistingParent(name(layer) + "_height2", mcLoc("block/snow_height2"))
                        .texture("particle", texture)
                        .texture("texture", texture)
        );

        // block item model
        simpleBlockItem(block, models().cubeAll(name(block), texture));
    }
//
//    protected void snowCovered(Block b, ResourceLocation top, ResourceLocation side, ResourceLocation bottom)
//    {
//        ModelFile model = models().grassBlock(name(b), top, side, bottom);
//
//        simpleBlockItem(b, model);
//    }

    protected void scaffold(Block b, ResourceLocation others, ResourceLocation top)
    {
        simpleBlockItem(
                b,
                models().withExistingParent(name(b), modLoc("block/ie_scaffolding"))
                        .texture("side", others)
                        .texture("bottom", others)
                        .texture("top", top)
        );
    }
    protected void slab(SlabBlock b, ResourceLocation texture)
    {
        slab(b, texture, texture, texture);
    }

    protected void slab(SlabBlock b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom)
    {
        ModelFile mainModel = models().slab(name(b)+"_bottom", side, bottom, top);
        slabBlock(
                b, mainModel,
                models().slabTop(name(b)+"_top", side, bottom, top),
                models().cubeBottomTop(name(b)+"_double", side, bottom, top)
        );
        itemModel(b, mainModel);
    }

    protected void stairs(StairBlock b, ResourceLocation texture)
    {
        stairs(b, texture, texture, texture);
    }

    protected void stairs(StairBlock b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom)
    {
        String baseName = name(b);
        ModelFile stairs = models().stairs(baseName, side, bottom, top);
        ModelFile stairsInner = models().stairsInner(baseName+"_inner", side, bottom, top);
        ModelFile stairsOuter = models().stairsOuter(baseName+"_outer", side, bottom, top);
        // TODO: ?
        //        StairBlock(b, stairs, stairsInner, stairsOuter);
        itemModel(b, stairs);
    }

    protected ResourceLocation forgeLoc(String path)
    {
        return new ResourceLocation("forge", path);
    }

    protected ResourceLocation addModelsPrefix(ResourceLocation in)
    {
        return new ResourceLocation(in.getNamespace(), "models/"+in.getPath());
    }

    protected void itemModel(Block block, ModelFile model)
    {
        itemModels().getBuilder(name(block)).parent(model);
    }

    protected BlockModelBuilder obj(String loc)
    {
        Preconditions.checkArgument(loc.endsWith(".obj"));
        return obj(loc.substring(0, loc.length()-4), modLoc(loc));
    }

    protected BlockModelBuilder obj(String name, ResourceLocation model)
    {
        return obj(name, model, ImmutableMap.of());
    }

    protected BlockModelBuilder obj(String name, ResourceLocation model, Map<String, ResourceLocation> textures)
    {
        assertModelExists(model);
        BlockModelBuilder ret = models().withExistingParent(name, mcLoc("block"))
                .customLoader(ObjModelBuilder::begin)
                .automaticCulling(false)
                .modelLocation(addModelsPrefix(model))
                .flipV(true)
                .end();
        String particleTex = DataGenUtils.getTextureFromObj(model, existingFileHelper);
        if(particleTex.charAt(0)=='#')
            particleTex = textures.get(particleTex.substring(1)).toString();
        ret.texture("particle", particleTex);
        generatedParticleTextures.put(ret.getLocation(), particleTex);
        for(Map.Entry<String, ResourceLocation> e : textures.entrySet())
            ret.texture(e.getKey(), e.getValue());
        return ret;
    }

    protected BlockModelBuilder splitModel(String name, ModelFile model, List<Vec3i> parts, boolean dynamic)
    {
        BlockModelBuilder result = models().withExistingParent(name, mcLoc("block"))
                .customLoader(SplitModelBuilder::begin)
                .innerModel((NongeneratedModels.NongeneratedModel) model)
                .parts(parts)
                .dynamic(dynamic)
                .end();
        addParticleTextureFrom(result, model);
        return result;
    }

    protected ModelFile split(ModelFile baseModel, List<Vec3i> parts, boolean dynamic)
    {
        return splitModel(baseModel.getLocation().getPath()+"_split", baseModel, parts, dynamic);
    }

    protected ModelFile split(ModelFile baseModel, List<Vec3i> parts)
    {
        return split(baseModel, parts, false);
    }

    protected ModelFile splitDynamic(ModelFile baseModel, List<Vec3i> parts)
    {
        return split(baseModel, parts, true);
    }

    protected void addParticleTextureFrom(BlockModelBuilder result, ModelFile model)
    {
        String particles = generatedParticleTextures.get(model.getLocation());
        if(particles!=null)
        {
            result.texture("particle", particles);
            generatedParticleTextures.put(result.getLocation(), particles);
        }
    }

    protected ConfiguredModel emptyWithParticles(String name, String particleTexture)
    {
        ModelFile model = models().withExistingParent(name, modLoc("block/ie_empty"))
                .texture("particle", particleTexture);
        generatedParticleTextures.put(modLoc(name), particleTexture);
        return new ConfiguredModel(model);
    }

    public void assertModelExists(ResourceLocation name)
    {
        String suffix = name.getPath().contains(".")?"": ".json";
        Preconditions.checkState(
                existingFileHelper.exists(name, PackType.CLIENT_RESOURCES, suffix, "models"),
                "Model \""+name+"\" does not exist");
    }

    protected BlockModelBuilder ieObj(String loc)
    {
        Preconditions.checkArgument(loc.endsWith(".obj.ie"));
        return ieObj(loc.substring(0, loc.length()-7), modLoc(loc));
    }

    protected BlockModelBuilder ieObj(String name, ResourceLocation model)
    {
        final String particle = DataGenUtils.getTextureFromObj(model, existingFileHelper);
        generatedParticleTextures.put(modLoc(name), particle);
        return models().withExistingParent(name, mcLoc("block"))
                .customLoader(IEOBJBuilder::begin)
                .modelLocation(addModelsPrefix(model))
                .dynamic(true) // TODO: Was flipV, is this correct?
                .end()
                .texture("particle", particle);
    }

    protected int getAngle(Direction dir, int offset)
    {
        return (int)((dir.toYRot()+offset)%360);
    }

    protected static String getName(RenderStateShard state)
    {
        //TODO clean up/speed up
        try
        {
            // Datagen should only ever run in a deobf environment, so no need to use unreadable SRG names here
            // This is a workaround for the fact that client-side Mixins are not applied in datagen
            Field f = RenderStateShard.class.getDeclaredField("name");
            f.setAccessible(true);
            return (String)f.get(state);
        } catch(Exception e)
        {
            throw new RuntimeException("Failed to get name of RenderStateShard instance", e);
        }
    }
}
