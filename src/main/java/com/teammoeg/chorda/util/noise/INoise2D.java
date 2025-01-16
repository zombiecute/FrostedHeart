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
 * Wrapper for a 2D noise layer
 */
@FunctionalInterface
public interface INoise2D {
    /**
     * Takes the absolute value of a noise function. Does not scale the result
     *
     * @return a new noise function
     */
    default INoise2D abs() {
        return (x, y) -> Math.abs(INoise2D.this.noise(x, y));
    }

    default INoise2D add(INoise2D other) {
        return (x, y) -> INoise2D.this.noise(x, y) + other.noise(x, y);
    }

    /**
     * Creates flattened noise by cutting off values above or below a threshold
     *
     * @param min the minimum noise value
     * @param max the maximum noise value
     * @return a new noise function
     */
    default INoise2D flattened(float min, float max) {
        return (x, y) -> Mth.clamp(INoise2D.this.noise(x, y), min, max);
    }

    default INoise2D map(FloatUnaryFunction mappingFunction) {
        return (x, y) -> mappingFunction.applyAsFloat(INoise2D.this.noise(x, y));
    }

    float noise(float x, float z);

    default INoise2D octaves(int octaves) {
        return octaves(octaves, 0.5f);
    }

    /**
     * Creates simple octaves, where the octaves all use the same base noise function
     *
     * @param octaves     The number of octaves
     * @param persistence The base for each octave's amplitude
     * @return A new noise function
     */
    default INoise2D octaves(int octaves, float persistence) {
        final float[] frequency = new float[octaves];
        final float[] amplitude = new float[octaves];
        for (int i = 0; i < octaves; i++) {
            frequency[i] = 1 << i;
            amplitude[i] = (float) Math.pow(persistence, octaves - i);
        }
        return (x, y) -> {
            float value = 0;
            for (int i = 0; i < octaves; i++) {
                value += INoise2D.this.noise(x / frequency[i], y / frequency[i]) * amplitude[i];
            }
            return value;
        };
    }

    /**
     * Creates ridged noise using absolute value
     *
     * @return a new noise function
     */
    default INoise2D ridged() {
        return (x, y) -> {
            float value = INoise2D.this.noise(x, y);
            value = value < 0 ? -value : value;
            return 1f - 2f * value;
        };
    }

    default INoise2D scaled(float min, float max) {
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
    default INoise2D scaled(float oldMin, float oldMax, float min, float max) {
        final float scale = (max - min) / (oldMax - oldMin);
        final float shift = min - oldMin * scale;
        return (x, y) -> INoise2D.this.noise(x, y) * scale + shift;
    }

    /**
     * Spreads out the noise via the input parameters
     *
     * @param scaleFactor The scale for the input params
     * @return a new noise function
     */
    default INoise2D spread(float scaleFactor) {
        return (x, y) -> INoise2D.this.noise(x * scaleFactor, y * scaleFactor);
    }

    /**
     * Creates "terraces" by taking the nearest level and rounding
     * Input must be in range [-1, 1]
     *
     * @param levels The number of levels to round to
     * @return a new noise function
     */
    default INoise2D terraces(int levels) {
        return (x, y) -> {
            float value = 0.5f * INoise2D.this.noise(x, y) + 0.5f;
            float rounded = (int) (value * levels); // In range [0, levels)
            return (rounded * 2f) / levels - 1f;
        };
    }

    /**
     * Applies a transformation to the input coordinates. This is similar to {@link INoise2D#warped(INoise2D, INoise2D)} except it does not add values to the coordinates. This makes it useful for clamp / scale operations on the input coordinates.
     *
     * @param transformX the x transformation
     * @param transformY the y transformation
     * @return a new noise function
     */
    default INoise2D transformed(INoise2D transformX, INoise2D transformY) {
        return (x, y) -> INoise2D.this.noise(transformX.noise(x, y), transformY.noise(x, y));
    }

    /**
     * Applies domain warping to each input coordinate using the two input noise functions
     *
     * @param warpX the x warp noise
     * @param warpY the y warp noise
     * @return a new noise function
     */
    default INoise2D warped(INoise2D warpX, INoise2D warpY) {
        return (x, y) -> INoise2D.this.noise(x + warpX.noise(x, y), y + warpY.noise(x, y));
    }
}