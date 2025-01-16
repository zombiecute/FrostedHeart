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

package com.teammoeg.frostedheart.content.climate.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teammoeg.chorda.util.io.CodecUtil;
import lombok.Getter;

@Getter
public class PlantTempData {
	public static final float DEFAULT_BONEMEAL_TEMP = 10;
	public static final float DEFAULT_GROW_TEMP = 0;
	public static final float DEFAULT_SURVIVE_TEMP = -10;
	public static final float DEFAULT_BONEMEAL_MAX_TEMP = 40;
	public static final float DEFAULT_GROW_MAX_TEMP = 40;
	public static final float DEFAULT_SURVIVE_MAX_TEMP = 40;
	public static final boolean DEFAULT_SNOW_VULNERABLE = true;
	public static final boolean DEFAULT_BLIZZARD_VULNERABLE = true;

	public static final MapCodec<PlantTempData> CODEC=RecordCodecBuilder.mapCodec(t->t.group(
			// min
			CodecUtil.defaultValue(Codec.FLOAT,DEFAULT_BONEMEAL_TEMP).fieldOf("min_fertilize").forGetter(o->o.minFertilize),
			CodecUtil.defaultValue(Codec.FLOAT,DEFAULT_GROW_TEMP).fieldOf("min_grow").forGetter(o->o.minGrow),
			CodecUtil.defaultValue(Codec.FLOAT,DEFAULT_SURVIVE_TEMP).fieldOf("min_survive").forGetter(o->o.minSurvive),
			// max
			CodecUtil.defaultValue(Codec.FLOAT,DEFAULT_BONEMEAL_MAX_TEMP).fieldOf("max_fertilize").forGetter(o->o.maxFertilize),
			CodecUtil.defaultValue(Codec.FLOAT,DEFAULT_GROW_MAX_TEMP).fieldOf("max_grow").forGetter(o->o.maxGrow),
			CodecUtil.defaultValue(Codec.FLOAT,DEFAULT_SURVIVE_MAX_TEMP).fieldOf("max_survive").forGetter(o->o.maxSurvive),
			// vulnerability
			CodecUtil.defaultValue(Codec.BOOL,DEFAULT_SNOW_VULNERABLE).fieldOf("snow_vulnerable").forGetter(o->o.snowVulnerable),
			CodecUtil.defaultValue(Codec.BOOL,DEFAULT_BLIZZARD_VULNERABLE).fieldOf("blizzard_vulnerable").forGetter(o->o.blizzardVulnerable)
	).apply(t, PlantTempData::new));

	// the temperature at which bonemeal can be used on the plant
	private final float minFertilize;
	// above this temperature, the plant will not grow
	private final float minGrow;
	// above this temperature, the plant will shrink to default state but not die
	// below this temperature, the plant will die
	private final float minSurvive;
	private final float maxFertilize;
	private final float maxGrow;
	private final float maxSurvive;
	private final boolean snowVulnerable;
	private final boolean blizzardVulnerable;

    public PlantTempData(float minFertilize, float minGrow, float minSurvive,
						 float maxFertilize, float maxGrow, float maxSurvive,
						 boolean snowVulnerable, boolean blizzardVulnerable) {
		this.minFertilize = minFertilize;
		this.minGrow = minGrow;
		this.minSurvive = minSurvive;
		this.maxFertilize = maxFertilize;
		this.maxGrow = maxGrow;
		this.maxSurvive = maxSurvive;
		this.snowVulnerable = snowVulnerable;
		this.blizzardVulnerable = blizzardVulnerable;
	}

	// default constructor
	public PlantTempData() {
		this(DEFAULT_BONEMEAL_TEMP, DEFAULT_GROW_TEMP, DEFAULT_SURVIVE_TEMP,
				DEFAULT_BONEMEAL_MAX_TEMP, DEFAULT_GROW_MAX_TEMP, DEFAULT_SURVIVE_MAX_TEMP,
				DEFAULT_SNOW_VULNERABLE, DEFAULT_BLIZZARD_VULNERABLE);
	}

}
