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

package com.teammoeg.chorda.util.noise;

import net.minecraft.util.Mth;

/**
 * Wrapper for a 3D Noise Layer
 */
@FunctionalInterface
public interface INoise3D {
    /**
     * Takes the absolute value of a noise function. Does not scale the result
     *
     * @return a new noise function
     */
    default INoise3D abs() {
        return (x, y, z) -> Math.abs(INoise3D.this.noise(x, y, z));
    }

    default INoise3D add(INoise3D other) {
        return (x, y, z) -> INoise3D.this.noise(x, y, z) + other.noise(x, y, z);
    }

    default INoise3D flattened(float min, float max) {
        return (x, y, z) -> Mth.clamp(INoise3D.this.noise(x, y, z), min, max);
    }

    default INoise3D map(FloatUnaryFunction mappingFunction) {
        return (x, y, z) -> mappingFunction.applyAsFloat(INoise3D.this.noise(x, y, z));
    }

    float noise(float x, float y, float z);

    default INoise3D octaves(int octaves) {
        return octaves(octaves, 0.5f);
    }

    /**
     * Creates simple octaves, where the octaves all use the same base noise function
     *
     * @param octaves     The number of octaves
     * @param persistence The base for each octave's amplitude
     * @return A new noise function
     */
    default INoise3D octaves(int octaves, float persistence) {
        final float[] frequency = new float[octaves];
        final float[] amplitude = new float[octaves];
        for (int i = 0; i < octaves; i++) {
            frequency[i] = 1 << i;
            amplitude[i] = (float) Math.pow(persistence, octaves - i);
        }
        return (x, y, z) -> {
            float value = 0;
            for (int i = 0; i < octaves; i++) {
                value += INoise3D.this.noise(x / frequency[i], y / frequency[i], z / frequency[i]) * amplitude[i];
            }
            return value;
        };
    }

    /**
     * Creates ridged noise using absolute value
     *
     * @return a new noise function
     */
    default INoise3D ridged() {
        return (x, y, z) -> {
            float value = INoise3D.this.noise(x, y, z);
            value = value < 0 ? -value : value;
            return 1f - 2f * value;
        };
    }

    default INoise3D scaled(float min, float max) {
        return scaled(-1, 1, min, max);
    }

    /**
     * Re-scales the output of the noise to a new range
     *
     * @param oldMin the old minimum value (typically -1)
     * @param oldMax the old maximum value (typically 1)
     * @param min    the new minimum value
     * @param max    the new maximum value
     * @return a new noise function
     */
    default INoise3D scaled(float oldMin, float oldMax, float min, float max) {
        return (x, y, z) -> {
            float value = INoise3D.this.noise(x, y, z);
            return (value - oldMin) / (oldMax - oldMin) * (max - min) + min;
        };
    }

    /**
     * Spreads out the noise via the input parameters
     *
     * @param scaleFactor The scale for the input params
     * @return a new noise function
     */
    default INoise3D spread(float scaleFactor) {
        return (x, y, z) -> INoise3D.this.noise(x * scaleFactor, y * scaleFactor, z * scaleFactor);
    }

    /**
     * Creates "terraces" by taking the nearest level and rounding
     *
     * @param levels The number of levels to round to
     * @return a new noise function
     */
    default INoise3D terraces(int levels) {
        return (x, y, z) -> {
            float value = 0.5f * INoise3D.this.noise(x, y, z) + 0.5f;
            float rounded = (int) (value * levels); // In range [0, levels)
            return (rounded * 2f) / levels - 1f;
        };
    }

    /**
     * Applies domain warping to each input coordinate using the two input noise functions
     *
     * @param warpX the x warp noise
     * @param warpY the y warp noise
     * @return a new noise function
     */
    default INoise3D warped(INoise3D warpX, INoise3D warpY, INoise3D warpZ) {
        return (x, y, z) -> {
            float x0 = x + warpX.noise(x, y, z);
            float y0 = y + warpY.noise(x, y, z);
            float z0 = z + warpZ.noise(x, y, z);
            return INoise3D.this.noise(x0, y0, z0);
        };
    }
}