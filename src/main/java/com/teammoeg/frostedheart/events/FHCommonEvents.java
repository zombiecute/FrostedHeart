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

import com.google.common.collect.Sets;
import com.teammoeg.chorda.util.lang.Components;
import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.bootstrap.common.FHCapabilities;
import com.teammoeg.frostedheart.bootstrap.common.FHMobEffects;
import com.teammoeg.frostedheart.bootstrap.reference.FHTags;
import com.teammoeg.frostedheart.compat.tetra.TetraCompat;
import com.teammoeg.frostedheart.content.steamenergy.HeatStatContainer;
import com.teammoeg.frostedheart.content.utility.DeathInventoryData;
import com.teammoeg.frostedheart.content.utility.ignition.IgnitionHandler;
import com.teammoeg.frostedheart.content.utility.oredetect.CoreSpade;
import com.teammoeg.frostedheart.content.utility.oredetect.GeologistsHammer;
import com.teammoeg.frostedheart.content.utility.oredetect.ProspectorPick;
import com.teammoeg.frostedheart.infrastructure.config.FHConfig;
import com.teammoeg.frostedheart.infrastructure.data.FHRecipeCachingReloadListener;
import com.teammoeg.frostedheart.infrastructure.data.FHRecipeReloadListener;
import com.teammoeg.chorda.util.CUtils;
import com.teammoeg.chorda.util.Constants;
import com.teammoeg.chorda.util.RegistryUtils;
import com.teammoeg.frostedheart.util.client.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent.PickupXp;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.items.modular.IModularItem;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Miscellaneous common events that are not specific to any particular module.
 * Examples: steam energy, utility, data, compat
 */
@Mod.EventBusSubscriber(modid = FHMain.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FHCommonEvents {

    private static final Set<EntityType<?>> VANILLA_ENTITIES = Sets.newHashSet(EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN);
    private static final ResourceLocation DRAWERS = new ResourceLocation("storagedrawers:drawers");

    @SubscribeEvent
    public static void attachToPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayer) {//server-side only capabilities
            ServerPlayer player = (ServerPlayer) event.getObject();
            if (!(player instanceof FakePlayer)) {
                event.addCapability(new ResourceLocation(FHMain.MODID, "death_inventory"), FHCapabilities.DEATH_INV.provider());
            }
        }
        //Common capabilities

    }

    @SubscribeEvent
    public static void loginReminder(@Nonnull PlayerEvent.PlayerLoggedInEvent event) {
        CompoundTag nbt = event.getEntity().getPersistentData();
        CompoundTag persistent;

        if (nbt.contains(Player.PERSISTED_NBT_TAG)) {
            persistent = nbt.getCompound(Player.PERSISTED_NBT_TAG);
        } else {
            nbt.put(Player.PERSISTED_NBT_TAG, (persistent = new CompoundTag()));
        }
        if (!persistent.contains(Constants.FIRST_LOGIN_GIVE_MANUAL)) {
            persistent.putBoolean(Constants.FIRST_LOGIN_GIVE_MANUAL, false);
            event.getEntity().getInventory().add(
                    new ItemStack(RegistryUtils.getItem(new ResourceLocation("ftbquests", "book"))));
            event.getEntity().getInventory().armor.set(3, CUtils.ArmorLiningNBT(new ItemStack(Items.IRON_HELMET)
                    .setHoverName(Lang.translateKey("itemname.frostedheart.start_head"))));
            event.getEntity().getInventory().armor.set(2, CUtils.ArmorLiningNBT(new ItemStack(Items.IRON_CHESTPLATE)
                    .setHoverName(Lang.translateKey("itemname.frostedheart.start_chest"))));
            event.getEntity().getInventory().armor.set(1, CUtils.ArmorLiningNBT(new ItemStack(Items.IRON_LEGGINGS)
                    .setHoverName(Lang.translateKey("itemname.frostedheart.start_leg"))));
            event.getEntity().getInventory().armor.set(0, CUtils.ArmorLiningNBT(new ItemStack(Items.IRON_BOOTS)
                    .setHoverName(Lang.translateKey("itemname.frostedheart.start_foot"))));
            if (event.getEntity().getAbilities().instabuild) {
                event.getEntity().sendSystemMessage(Lang.translateKey("message.frostedheart.creative_help")
                        .setStyle(Style.EMPTY.applyFormat(ChatFormatting.YELLOW)
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Components.str("Click to use command")))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/frostedheart research complete all"))));
            }

            event.getEntity().sendSystemMessage(Lang.translateKey("message.frostedheart.temperature_help"));
        }
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        ReloadableServerResources dataPackRegistries = event.getServerResources();
        // IReloadableResourceManager resourceManager = (IReloadableResourceManager)
        // dataPackRegistries.getResourceManager();
        event.addListener(new FHRecipeReloadListener(dataPackRegistries));
//            resourceManager.addReloadListener(ChunkCacheInvalidationReloaderListener.INSTANCE);
    }

    @SubscribeEvent
    public static void addReloadListenersLowest(AddReloadListenerEvent event) {
        ReloadableServerResources dataPackRegistries = event.getServerResources();
        event.addListener(new FHRecipeCachingReloadListener(dataPackRegistries));
    }

    /**
     * Lights the block on fire if it can be lit, otherwise places a fire block.
     */
    @SubscribeEvent
    public static void lightingFire(PlayerInteractEvent.RightClickBlock event) {
        ItemStack handStack = event.getEntity().getMainHandItem();
        ItemStack offHandStack = event.getEntity().getOffhandItem();
        Player player = event.getEntity();
        Level level = event.getLevel();
        RandomSource rand = level.random;
        BlockPos blockpos = event.getPos();
        BlockState blockstate = level.getBlockState(blockpos);

        if (!handStack.isEmpty() && !offHandStack.isEmpty() && !handStack.is(ItemTags.CREEPER_IGNITERS) &&
                (handStack.is(Tags.Items.RODS_WOODEN) && offHandStack.is(Tags.Items.RODS_WOODEN) ||
                        handStack.is(FHTags.Items.IGNITION_METAL.tag) && offHandStack.is(FHTags.Items.IGNITION_MATERIAL.tag) ||
                        handStack.is(FHTags.Items.IGNITION_MATERIAL.tag) && offHandStack.is(FHTags.Items.IGNITION_METAL.tag))) {
            // place fire block
            if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) && !CandleCakeBlock.canLight(blockstate)) {
                BlockPos blockpos1 = blockpos.relative(event.getHitVec().getDirection());
                if (BaseFireBlock.canBePlacedAt(level, blockpos1, player.getDirection())) {
                    player.swing(InteractionHand.MAIN_HAND);
                    level.playSound(player, blockpos1, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                    if (level.isClientSide()) {
                        for (int i = 0; i < 5; i++) {
                            level.addParticle(ParticleTypes.SMOKE, player.getX() + player.getLookAngle().x() + rand.nextFloat() * 0.25, player.getY() + 0.5f + rand.nextFloat() * 0.25, player.getZ() + player.getLookAngle().z() + rand.nextFloat() * 0.25, 0, 0.01, 0);
                        }
                        level.addParticle(ParticleTypes.FLAME, player.getX() + player.getLookAngle().x() + rand.nextFloat() * 0.25, player.getY() + 0.5f + rand.nextFloat() * 0.25, player.getZ() + player.getLookAngle().z() + rand.nextFloat() * 0.25, 0, 0.01, 0);
                    }
                    if (IgnitionHandler.tryIgnition(rand, handStack, offHandStack)) {
                        BlockState blockstate1 = BaseFireBlock.getState(level, blockpos1);
                        level.setBlock(blockpos1, blockstate1, 11);
                        level.gameEvent(player, GameEvent.BLOCK_PLACE, blockpos);
                        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                    } else {
                        event.setCancellationResult(InteractionResult.PASS);
                        event.setCanceled(true);
                    }

                } else {
                    event.setCancellationResult(InteractionResult.FAIL);
                    event.setCanceled(true);
                }
            }
            // light the block
            else {
                player.swing(InteractionHand.MAIN_HAND);
                level.playSound(player, blockpos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

                if (level.isClientSide()) {
                    for (int i = 0; i < 5; i++) {
                        level.addParticle(ParticleTypes.SMOKE, player.getX() + player.getLookAngle().x() + rand.nextFloat() * 0.25, player.getY() + 0.5f + rand.nextFloat() * 0.25, player.getZ() + player.getLookAngle().z() + rand.nextFloat() * 0.25, 0, 0.01, 0);
                    }
                    level.addParticle(ParticleTypes.FLAME, player.getX() + player.getLookAngle().x() + rand.nextFloat() * 0.25, player.getY() + 0.5f + rand.nextFloat() * 0.25, player.getZ() + player.getLookAngle().z() + rand.nextFloat() * 0.25, 0, 0.01, 0);
                }

                if (IgnitionHandler.tryIgnition(rand, handStack, offHandStack)) {
                    level.setBlock(blockpos, blockstate.setValue(BlockStateProperties.LIT, true), 11);
                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
                    event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                    event.setCanceled(true);
                } else {
                    event.setCancellationResult(InteractionResult.PASS);
                    event.setCanceled(true);
                }

            }
        }

    }

    @SubscribeEvent
    public static void prospecting(PlayerInteractEvent.RightClickBlock event) {
        if (ModList.get().isLoaded("tetra") && event.getItemStack().getItem() instanceof IModularItem) {
            int type = 0;
            if (event.getItemStack().canPerformAction(TetraCompat.coreSpade))
                type = 1;
            else if (event.getItemStack().canPerformAction(TetraCompat.geoHammer))
                type = 2;
            else if (event.getItemStack().canPerformAction(TetraCompat.proPick))
                type = 3;
            if (type != 0)
                if (!event.getEntity().getCooldowns().isOnCooldown(event.getItemStack().getItem())) {
                    event.getEntity().getCooldowns().addCooldown(event.getItemStack().getItem(), 10);
                    if ((type == 3 && event.getLevel().getRandom().nextBoolean()) || (type != 3 && event.getLevel().getRandom().nextBoolean()))
                        ((IModularItem) event.getItemStack().getItem()).tickProgression(event.getEntity(), event.getItemStack(), 1);
                    switch (type) {
                        case 1:
                            CoreSpade.doProspect(event.getEntity(), event.getLevel(), event.getPos(), event.getItemStack(), event.getHand());
                            break;
                        case 2:
                            GeologistsHammer.doProspect(event.getEntity(), event.getLevel(), event.getPos(), event.getItemStack(), event.getHand());
                            break;
                        case 3:
                            ProspectorPick.doProspect(event.getEntity(), event.getLevel(), event.getPos(), event.getItemStack(), event.getHand());
                            break;
                    }
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
        }
    }

    @SubscribeEvent
    public static void death(PlayerEvent.Clone ev) {
        CUtils.clonePlayerCapability(FHCapabilities.WANTED_FOOD.capability(),ev.getOriginal(),ev.getEntity());
        CUtils.clonePlayerCapability(FHCapabilities.ENERGY,ev.getOriginal(),ev.getEntity());
        CUtils.clonePlayerCapability(FHCapabilities.SCENARIO,ev.getOriginal(),ev.getEntity());
        CUtils.clonePlayerCapability(FHCapabilities.WAYPOINT,ev.getOriginal(),ev.getEntity());
        //CUtils.clonePlayerCapability(PlayerTemperatureData.CAPABILITY,ev.getOriginal(),ev.getEntity());
        //FHMain.LOGGER.info("clone");
        if (!ev.getEntity().level().isClientSide) {
            DeathInventoryData orig = DeathInventoryData.get(ev.getOriginal());
            DeathInventoryData nw = DeathInventoryData.get(ev.getEntity());

            if (nw != null && orig != null)
                nw.copy(orig);
            nw.calledClone();
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof FakePlayer) {
            if (ForgeRegistries.BLOCKS.getHolder(event.getState().getBlock()).get().is(DRAWERS))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCuriosDrop(DropRulesEvent cde) {
        if ((cde.getEntity() instanceof Player) && FHConfig.SERVER.keepEquipments.get()) {
            cde.addOverride(e -> true, DropRule.ALWAYS_KEEP);
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        MobEffectInstance ei = event.getEntity().getEffect(FHMobEffects.SCURVY.get());
        if (ei != null)
            event.setAmount(event.getAmount() * (0.2f / (ei.getAmplifier() + 1)));
    }

    //not allow repair
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.LOWEST)
    public static void onItemRepair(AnvilUpdateEvent event) {
        if (event.getLeft().hasTag()) {
            if (event.getLeft().getTag().getBoolean("inner_bounded"))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPotionRemove(MobEffectEvent.Remove event) {
        if (event.getEffect() == FHMobEffects.ION.get())
            event.setCanceled(true);

    }

    @SubscribeEvent
    public static void tickPlayer(PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == Phase.END
                && event.player instanceof ServerPlayer player) {
            // Heat network statistics update
            if (player.containerMenu instanceof HeatStatContainer) {
            	((HeatStatContainer)player.containerMenu).tick();
            }
        }
    }

    @SubscribeEvent
    public static void disableSleepJumpTimeInStorm(SleepingTimeCheckEvent event) {
        // Disable sleep jumping time if it is day time (thunder)
        long ttime = event.getEntity().getCommandSenderWorld().getDayTime() % 24000;
        if (ttime < 12000)
            event.setResult(Result.DENY);
    }

    @SubscribeEvent
    public static void playerXPPickUp(PickupXp event) {
        Player player = event.getEntity();
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty()) {
                CompoundTag cn = stack.getTag();
                if (cn == null)
                    continue;
                String inner = cn.getString("inner_cover");
                if (inner.isEmpty() || cn.getBoolean("inner_bounded"))
                    continue;
                CompoundTag cnbt = cn.getCompound("inner_cover_tag");
                int crdmg = cnbt.getInt("Damage");
                if (crdmg > 0 && CUtils.getEnchantmentLevel(Enchantments.MENDING, cnbt) > 0) {
                    event.setCanceled(true);
                    ExperienceOrb orb = event.getOrb();
                    player.takeXpDelay = 2;
                    player.take(orb, 1);

                    int toRepair = Math.min(orb.value * 2, crdmg);
                    orb.value -= toRepair / 2;
                    crdmg = crdmg - toRepair;
                    cnbt.putInt("Damage", crdmg);
                    cn.put("inner_cover_tag", cnbt);
                    if (orb.value > 0) {
                        player.giveExperiencePoints(orb.value);
                    }
                    orb.remove(RemovalReason.DISCARDED);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer && !(event.getEntity() instanceof FakePlayer)) {
            DeathInventoryData dit = DeathInventoryData.get(event.getEntity());
            dit.tryCallClone(event.getEntity());
            if (FHConfig.SERVER.keepEquipments.get() && !event.getEntity().level().isClientSide) {
                if (dit != null)
                    dit.alive(event.getEntity().getInventory());
            }
        }
    }

}
