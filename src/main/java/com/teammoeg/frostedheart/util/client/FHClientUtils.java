package com.teammoeg.frostedheart.util.client;

import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.bootstrap.reference.FHParticleTypes;
import com.teammoeg.chorda.util.client.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FHClientUtils {
    public static void spawnInvertedConeSteam(Level worldIn, BlockPos generatorTopPos, Vec3 windVelocity) {
        RandomSource random = worldIn.getRandom();
        double vYMean = 0.02; // Mean speed of the particles upwards
        double speedVar = 0.01; // Variance in speed of the particles

        // To ensure the particle spawn at the center of the block and introduce a bit of randomness
        double x = 0.5 + random.nextGaussian() * 0.1 * 2;
        double y = 0.5 + random.nextGaussian() * 0.1; // to prevent particles from being stuck in the tower
        double z = 0.5 + random.nextGaussian() * 0.1 * 2;

        // Incorporating wind effect in particle velocity
        double vX = windVelocity.x + random.nextGaussian() * speedVar;
        double vY = windVelocity.y + random.nextGaussian() * speedVar + vYMean; // Ensure upward movement with variability
        double vZ = windVelocity.z + random.nextGaussian() * speedVar;

        // Spawn the smoke particles with wind effect
        ClientUtils.spawnCustomParticles(worldIn, generatorTopPos, FHParticleTypes.STEAM.get(),
                x, y, z, vX, vY, vZ);
    }

    public static void spawnSteamParticles(Level worldIn, BlockPos pos) {
        RandomSource random = worldIn.getRandom();
        worldIn.addAlwaysVisibleParticle(FHParticleTypes.STEAM.get(), true, pos.getX() + 0.5D + random.nextDouble() / 3.0D * (random.nextBoolean() ? 1 : -1), pos.getY() + 0.5D + random.nextDouble() + random.nextDouble(), pos.getZ() + 0.5D + random.nextDouble() / 3.0D * (random.nextBoolean() ? 1 : -1), 0.0D, 0.12D, 0.0D);
        worldIn.addParticle(FHParticleTypes.STEAM.get(), pos.getX() + 0.5D + random.nextDouble() / 2.0D * (random.nextBoolean() ? 1 : -1), pos.getY() + 0.4D, pos.getZ() + 0.5D + random.nextDouble() / 2.0D * (random.nextBoolean() ? 1 : -1), 0.0D, 0.05D, 0.0D);
    }

    public static ResourceLocation makeTextureLocation(String name) {
        return FHMain.rl("textures/gui/" + name + ".png");
    }

    public static String rawQuestReward(String name, Object... args) {
        return "ftbquests.reward." + FHMain.MODID + "." + name;
    }
}
