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

public class OpenSimplex3D implements INoise3D {
    private static final float SKEW_XY = (float) 0.211324865405187;
    private static final float SKEW_Z = (float) 0.577350269189626;

    private final int seed;

    public OpenSimplex3D(long seed) {
        this.seed = (int) seed;
    }

    @Override
    @SuppressWarnings({"UnnecessaryLocalVariable", "NumericOverflow"})
    public float noise(float x, float y, float z) {
        // ImproveXYPlanes option
        float xy = x + y;
        float s2 = xy * -SKEW_XY;
        z *= SKEW_Z;
        x += s2 - z;
        y = y + s2 - z;
        z += xy * SKEW_Z;

        // 3D OpenSimplex2S case uses two offset rotated cube grids.

        int i = NoiseUtil.fastFloor(x);
        int j = NoiseUtil.fastFloor(y);
        int k = NoiseUtil.fastFloor(z);
        float xi = x - i;
        float yi = y - j;
        float zi = z - k;

        i *= NoiseUtil.PRIME_X;
        j *= NoiseUtil.PRIME_Y;
        k *= NoiseUtil.PRIME_Z;
        int seed2 = seed + 1293373;

        int xNMask = (int) (-0.5f - xi);
        int yNMask = (int) (-0.5f - yi);
        int zNMask = (int) (-0.5f - zi);

        float x0 = xi + xNMask;
        float y0 = yi + yNMask;
        float z0 = zi + zNMask;
        float a0 = 0.75f - x0 * x0 - y0 * y0 - z0 * z0;
        float value = (a0 * a0) * (a0 * a0) * NoiseUtil.gradientCoord(seed, i + (xNMask & NoiseUtil.PRIME_X), j + (yNMask & NoiseUtil.PRIME_Y), k + (zNMask & NoiseUtil.PRIME_Z), x0, y0, z0);

        float x1 = xi - 0.5f;
        float y1 = yi - 0.5f;
        float z1 = zi - 0.5f;
        float a1 = 0.75f - x1 * x1 - y1 * y1 - z1 * z1;
        value += (a1 * a1) * (a1 * a1) * NoiseUtil.gradientCoord(seed2, i + NoiseUtil.PRIME_X, j + NoiseUtil.PRIME_Y, k + NoiseUtil.PRIME_Z, x1, y1, z1);

        float xAFlipMask0 = ((xNMask | 1) << 1) * x1;
        float yAFlipMask0 = ((yNMask | 1) << 1) * y1;
        float zAFlipMask0 = ((zNMask | 1) << 1) * z1;
        float xAFlipMask1 = (-2 - (xNMask << 2)) * x1 - 1.0f;
        float yAFlipMask1 = (-2 - (yNMask << 2)) * y1 - 1.0f;
        float zAFlipMask1 = (-2 - (zNMask << 2)) * z1 - 1.0f;

        boolean skip5 = false;
        float a2 = xAFlipMask0 + a0;
        if (a2 > 0) {
            float x2 = x0 - (xNMask | 1);
            float y2 = y0;
            float z2 = z0;
            value += (a2 * a2) * (a2 * a2) * NoiseUtil.gradientCoord(seed, i + (~xNMask & NoiseUtil.PRIME_X), j + (yNMask & NoiseUtil.PRIME_Y), k + (zNMask & NoiseUtil.PRIME_Z), x2, y2, z2);
        } else {
            float a3 = yAFlipMask0 + zAFlipMask0 + a0;
            if (a3 > 0) {
                float x3 = x0;
                float y3 = y0 - (yNMask | 1);
                float z3 = z0 - (zNMask | 1);
                value += (a3 * a3) * (a3 * a3) * NoiseUtil.gradientCoord(seed, i + (xNMask & NoiseUtil.PRIME_X), j + (~yNMask & NoiseUtil.PRIME_Y), k + (~zNMask & NoiseUtil.PRIME_Z), x3, y3, z3);
            }

            float a4 = xAFlipMask1 + a1;
            if (a4 > 0) {
                float x4 = (xNMask | 1) + x1;
                float y4 = y1;
                float z4 = z1;
                value += (a4 * a4) * (a4 * a4) * NoiseUtil.gradientCoord(seed2, i + (xNMask & (NoiseUtil.PRIME_X * 2)), j + NoiseUtil.PRIME_Y, k + NoiseUtil.PRIME_Z, x4, y4, z4);
                skip5 = true;
            }
        }

        boolean skip9 = false;
        float a6 = yAFlipMask0 + a0;
        if (a6 > 0) {
            float x6 = x0;
            float y6 = y0 - (yNMask | 1);
            float z6 = z0;
            value += (a6 * a6) * (a6 * a6) * NoiseUtil.gradientCoord(seed, i + (xNMask & NoiseUtil.PRIME_X), j + (~yNMask & NoiseUtil.PRIME_Y), k + (zNMask & NoiseUtil.PRIME_Z), x6, y6, z6);
        } else {
            float a7 = xAFlipMask0 + zAFlipMask0 + a0;
            if (a7 > 0) {
                float x7 = x0 - (xNMask | 1);
                float y7 = y0;
                float z7 = z0 - (zNMask | 1);
                value += (a7 * a7) * (a7 * a7) * NoiseUtil.gradientCoord(seed, i + (~xNMask & NoiseUtil.PRIME_X), j + (yNMask & NoiseUtil.PRIME_Y), k + (~zNMask & NoiseUtil.PRIME_Z), x7, y7, z7);
            }

            float a8 = yAFlipMask1 + a1;
            if (a8 > 0) {
                float x8 = x1;
                float y8 = (yNMask | 1) + y1;
                float z8 = z1;
                value += (a8 * a8) * (a8 * a8) * NoiseUtil.gradientCoord(seed2, i + NoiseUtil.PRIME_X, j + (yNMask & (NoiseUtil.PRIME_Y << 1)), k + NoiseUtil.PRIME_Z, x8, y8, z8);
                skip9 = true;
            }
        }

        boolean skipD = false;
        float aA = zAFlipMask0 + a0;
        if (aA > 0) {
            float xA = x0;
            float yA = y0;
            float zA = z0 - (zNMask | 1);
            value += (aA * aA) * (aA * aA) * NoiseUtil.gradientCoord(seed,
                    i + (xNMask & NoiseUtil.PRIME_X), j + (yNMask & NoiseUtil.PRIME_Y), k + (~zNMask & NoiseUtil.PRIME_Z), xA, yA, zA);
        } else {
            float aB = xAFlipMask0 + yAFlipMask0 + a0;
            if (aB > 0) {
                float xB = x0 - (xNMask | 1);
                float yB = y0 - (yNMask | 1);
                float zB = z0;
                value += (aB * aB) * (aB * aB) * NoiseUtil.gradientCoord(seed,
                        i + (~xNMask & NoiseUtil.PRIME_X), j + (~yNMask & NoiseUtil.PRIME_Y), k + (zNMask & NoiseUtil.PRIME_Z), xB, yB, zB);
            }

            float aC = zAFlipMask1 + a1;
            if (aC > 0) {
                float xC = x1;
                float yC = y1;
                float zC = (zNMask | 1) + z1;
                value += (aC * aC) * (aC * aC) * NoiseUtil.gradientCoord(seed2, i + NoiseUtil.PRIME_X, j + NoiseUtil.PRIME_Y, k + (zNMask & (NoiseUtil.PRIME_Z << 1)), xC, yC, zC);
                skipD = true;
            }
        }

        if (!skip5) {
            float a5 = yAFlipMask1 + zAFlipMask1 + a1;
            if (a5 > 0) {
                float x5 = x1;
                float y5 = (yNMask | 1) + y1;
                float z5 = (zNMask | 1) + z1;
                value += (a5 * a5) * (a5 * a5) * NoiseUtil.gradientCoord(seed2, i + NoiseUtil.PRIME_X, j + (yNMask & (NoiseUtil.PRIME_Y << 1)), k + (zNMask & (NoiseUtil.PRIME_Z << 1)), x5, y5, z5);
            }
        }

        if (!skip9) {
            float a9 = xAFlipMask1 + zAFlipMask1 + a1;
            if (a9 > 0) {
                float x9 = (xNMask | 1) + x1;
                float y9 = y1;
                float z9 = (zNMask | 1) + z1;
                value += (a9 * a9) * (a9 * a9) * NoiseUtil.gradientCoord(seed2, i + (xNMask & (NoiseUtil.PRIME_X * 2)), j + NoiseUtil.PRIME_Y, k + (zNMask & (NoiseUtil.PRIME_Z << 1)), x9, y9, z9);
            }
        }

        if (!skipD) {
            float aD = xAFlipMask1 + yAFlipMask1 + a1;
            if (aD > 0) {
                float xD = (xNMask | 1) + x1;
                float yD = (yNMask | 1) + y1;
                float zD = z1;
                value += (aD * aD) * (aD * aD) * NoiseUtil.gradientCoord(seed2, i + (xNMask & (NoiseUtil.PRIME_X << 1)), j + (yNMask & (NoiseUtil.PRIME_Y << 1)), k + NoiseUtil.PRIME_Z, xD, yD, zD);
            }
        }

        return value * 9.046026385208288f;
    }
}
