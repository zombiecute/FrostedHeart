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

package com.teammoeg.frostedheart.events;

import com.teammoeg.frostedheart.*;
import com.teammoeg.chorda.creativeTab.CreativeTabItemHelper;
import com.teammoeg.chorda.creativeTab.ICreativeModeTabItem;
import com.teammoeg.frostedheart.compat.ftbq.FHGuiProviders;
import com.teammoeg.frostedheart.compat.ie.FHManual;
import com.teammoeg.frostedheart.compat.tetra.TetraClient;
import com.teammoeg.frostedheart.content.climate.particle.SnowParticle;
import com.teammoeg.frostedheart.content.climate.heatdevice.generator.t1.T1GeneratorRenderer;
import com.teammoeg.frostedheart.content.climate.heatdevice.generator.t2.T2GeneratorRenderer;
import com.teammoeg.frostedheart.content.research.blocks.MechCalcRenderer;
import com.teammoeg.frostedheart.content.scenario.client.gui.layered.font.KGlyphProvider;
import com.teammoeg.frostedheart.content.town.resident.WanderingRefugeeRenderer;
import com.teammoeg.frostedheart.content.utility.heatervest.HeaterVestExtension;
import com.teammoeg.frostedheart.content.utility.heatervest.HeaterVestModel;
import com.teammoeg.chorda.model.DynamicBlockModelReference;
import com.teammoeg.frostedheart.bootstrap.client.FHKeyMappings;
import com.teammoeg.frostedheart.bootstrap.client.FHScreens;
import com.teammoeg.frostedheart.bootstrap.common.FHBlockEntityTypes;
import com.teammoeg.frostedheart.bootstrap.common.FHEntityTypes;
import com.teammoeg.frostedheart.bootstrap.common.FHMultiblocks;
import com.teammoeg.frostedheart.bootstrap.reference.FHParticleTypes;
import com.teammoeg.frostedheart.content.climate.model.LiningModel;
import com.teammoeg.frostedheart.content.climate.particle.BreathParticle;
import com.teammoeg.frostedheart.content.climate.particle.SteamParticle;
import com.teammoeg.frostedheart.content.climate.particle.WetSteamParticle;
import com.teammoeg.frostedheart.content.world.entities.CuriosityEntityModel;
import com.teammoeg.frostedheart.content.world.entities.CuriosityEntityRenderer;
import com.teammoeg.chorda.util.CRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import static com.teammoeg.frostedheart.FHMain.CLIENT_SETUP;
import static com.teammoeg.frostedheart.FHMain.LOGGER;

/**
 * Client side events fired on mod bus.
 */
@Mod.EventBusSubscriber(modid = FHMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FHClientEventsMod {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        LOGGER.info(CLIENT_SETUP, "FML Client setup event started");

        LOGGER.info(CLIENT_SETUP, "Initializing Key Mappings");
        FHKeyMappings.init();
        LOGGER.info(CLIENT_SETUP, "Initializing Screens");
        FHScreens.init();

        if (ModList.get().isLoaded("immersiveengineering")) {
            LOGGER.info(CLIENT_SETUP, "Initializing IE Manual");
            FHManual.init();
        }
        if (ModList.get().isLoaded("tetra")) {
            LOGGER.info(CLIENT_SETUP, "Initializing Tetra Client");
            TetraClient.init();
        }
        if (ModList.get().isLoaded("ftbquests")) {
            LOGGER.info(CLIENT_SETUP, "Initializing FTB Quests");
            FHGuiProviders.setRewardGuiProviders();
        }
        LOGGER.info(CLIENT_SETUP, "FML Client setup event finished");
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(KGlyphProvider.INSTANCE);
    }

	@SubscribeEvent
	public static void onCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
		CreativeTabItemHelper helper = new CreativeTabItemHelper(event.getTabKey(), event.getTab());
		ForgeRegistries.ITEMS.forEach(e -> {
			if (e instanceof ICreativeModeTabItem item) {
				item.fillItemCategory(helper);
			}
		});
		helper.register(event);

	}

    @SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent ev) {
        FHKeyMappings.key_skipDialog.get().setKeyConflictContext(KeyConflictContext.IN_GAME);
        FHKeyMappings.key_InfraredView.get().setKeyConflictContext(KeyConflictContext.IN_GAME);
        FHKeyMappings.key_health.get().setKeyConflictContext(KeyConflictContext.IN_GAME);
		ev.register(FHKeyMappings.key_skipDialog.get());
        ev.register(FHKeyMappings.key_InfraredView.get());
        ev.register(FHKeyMappings.key_health.get());
	}

	@SubscribeEvent
	public static void onLayerRegister(final RegisterLayerDefinitions event) {
		event.registerLayerDefinition(HeaterVestModel.HEATER_VEST_LAYER, () -> HeaterVestModel.createLayer());
	} 

	@SubscribeEvent
	public static void onLayerAdd(final AddLayers event) {
		HeaterVestExtension.MODEL=new HeaterVestModel(Minecraft.getInstance().getEntityModels().bakeLayer(HeaterVestModel.HEATER_VEST_LAYER));
	}

	@SubscribeEvent
	public static void registerModels(ModelEvent.RegisterAdditional ev)
	{
		FHMain.LOGGER.info("===========Dynamic Model Register========");
		DynamicBlockModelReference.registeredModels.forEach(rl->{
			ev.register(rl);
			FHMain.LOGGER.info(rl);
		});
	}
	@SubscribeEvent
	public static void registerBERenders(RegisterRenderers event){
		FHMain.LOGGER.info("===========Dynamic Block Renderers========");
        event.registerBlockEntityRenderer(FHMultiblocks.Logic.GENERATOR_T1.masterBE().get(), T1GeneratorRenderer::new);
        event.registerBlockEntityRenderer(FHMultiblocks.Logic.GENERATOR_T2.masterBE().get(), T2GeneratorRenderer::new);
        event.registerBlockEntityRenderer(FHBlockEntityTypes.MECH_CALC.get(), MechCalcRenderer::new);
	}

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        for (ResourceLocation location : event.getModels().keySet()) {
            // Now find all armors
            ResourceLocation item = new ResourceLocation(location.getNamespace(), location.getPath());
            if (CRegistries.getItem(item) instanceof ArmorItem) {
                ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(item, "inventory");
                BakedModel model = event.getModels().get(itemModelResourceLocation);
                if (model == null) {
                    FHMain.LOGGER.warn("Did not find the expected vanilla baked model for " + item + " in registry");
                } else if (model instanceof LiningModel) {
                	FHMain.LOGGER.warn("Tried to replace " + item + " twice");
                } else {
                    // Replace the model with our IBakedModel
                    LiningModel customModel = new LiningModel(model);
                    event.getModels().put(itemModelResourceLocation, customModel);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent event) {
       /* if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
        	
        	SpriteLoader.create(event.getAtlas()).loadAndStitch(null, null, 0, null)
            event.getAtlas().(LiningFinalizedModel.buffCoatFeetTexture);
            event.addSprite(LiningFinalizedModel.buffCoatLegsTexture);
            event.addSprite(LiningFinalizedModel.buffCoatHelmetTexture);
            event.addSprite(LiningFinalizedModel.buffCoatTorsoTexture);
            event.addSprite(LiningFinalizedModel.gambesonLegsTexture);
            event.addSprite(LiningFinalizedModel.gambesonFeetTexture);
            event.addSprite(LiningFinalizedModel.gambesonHelmetTexture);
            event.addSprite(LiningFinalizedModel.gambesonTorsoTexture);
            event.addSprite(LiningFinalizedModel.kelpLiningLegsTexture);
            event.addSprite(LiningFinalizedModel.kelpLiningFeetTexture);
            event.addSprite(LiningFinalizedModel.kelpLiningHelmetTexture);
            event.addSprite(LiningFinalizedModel.kelpLiningTorsoTexture);
            event.addSprite(LiningFinalizedModel.strawLiningLegsTexture);
            event.addSprite(LiningFinalizedModel.strawLiningFeetTexture);
            event.addSprite(LiningFinalizedModel.strawLiningHelmetTexture);
            event.addSprite(LiningFinalizedModel.strawLiningTorsoTexture);
        }*/
    }
/*
    @SubscribeEvent
    public static void provideTextures(final TextureStitchEvent.Pre event) {
        if (InventoryMenu.BLOCK_ATLAS.equals(event.getMap().location())) {
            Minecraft.getInstance().getResourceManager().listResources("textures/item/module", s -> s.endsWith(".png")).stream()
                    .filter(resourceLocation -> FHMain.MODID.equals(resourceLocation.getNamespace()))
                    // 9 is the length of "textures/" & 4 is the length of ".png"

                    .map(rl -> new ResourceLocation(rl.getNamespace(), rl.getPath().substring(9, rl.getPath().length() - 4)))
                    .peek(rl -> FHMain.LOGGER.info("stitching texture" + rl))
                    .forEach(event::addSprite);
        }
    }
*/


    /*public static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> void
    registerIEScreen(ResourceLocation containerName, MenuScreens.ScreenConstructor<C, S> factory) {
        @SuppressWarnings("unchecked")
        MenuType<C> type = (MenuType<C>) GuiHandler.getContainerType(containerName);
        MenuScreens.register(type, factory);
    }*/

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
    	event.registerSpriteSet(FHParticleTypes.STEAM.get(), SteamParticle.Factory::new);
    	event.registerSpriteSet(FHParticleTypes.BREATH.get(), BreathParticle.Factory::new);
    	event.registerSpriteSet(FHParticleTypes.WET_STEAM.get(), WetSteamParticle.Factory::new);
        event.registerSpriteSet(FHParticleTypes.SNOW.get(), SnowParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FHEntityTypes.CURIOSITY.get(), CuriosityEntityRenderer::new);
        event.registerEntityRenderer(FHEntityTypes.WANDERING_REFUGEE.get(), WanderingRefugeeRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CuriosityEntityModel.LAYER_LOCATION, CuriosityEntityModel::createBodyLayer);
    }

}
