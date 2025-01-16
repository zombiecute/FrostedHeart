package com.teammoeg.frostedheart.mixin.minecraft.temperature;

import com.teammoeg.frostedheart.content.climate.event.PerformBonemealEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {
        NyliumBlock.class, FungusBlock.class, NetherrackBlock.class,
        SeagrassBlock.class, MangroveLeavesBlock.class, CocoaBlock.class, BigDripleafBlock.class,
        PitcherCropBlock.class, PinkPetalsBlock.class, GrassBlock.class, GlowLichenBlock.class,
        SweetBerryBushBlock.class, CropBlock.class, TallFlowerBlock.class, TallGrassBlock.class,
        MossBlock.class, BigDripleafStemBlock.class, RootedDirtBlock.class, RootedDirtBlock.class,
        StemBlock.class, AzaleaBlock.class, SaplingBlock.class, MangrovePropaguleBlock.class,
        CaveVinesPlantBlock.class, GrowingPlantBodyBlock.class, CaveVinesBlock.class,
        BambooStalkBlock.class, SeaPickleBlock.class, GrowingPlantHeadBlock.class,
        SmallDripleafBlock.class, MushroomBlock.class, BambooSaplingBlock.class
})
public class BonemealableBlockMixin_CheckTemp {

    /**
     * @reason Check if the block can be bonemealed
     * @author yuesha-yc
     */
    @Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true)
    private void fh$checkTempForBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new PerformBonemealEvent(pLevel, pPos, pState, pRandom))) {
            ci.cancel();
        }
    }

}
