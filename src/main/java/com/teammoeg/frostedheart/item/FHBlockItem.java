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

package com.teammoeg.frostedheart.item;

import com.teammoeg.chorda.creativeTab.CreativeTabItemHelper;
import com.teammoeg.chorda.creativeTab.ICreativeModeTabItem;
import com.teammoeg.chorda.creativeTab.TabType;
import com.teammoeg.frostedheart.bootstrap.client.FHTabs;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class FHBlockItem extends BlockItem implements ICreativeModeTabItem{
	TabType tab;
    public FHBlockItem(Block block) {
        this(block, new Item.Properties(), FHTabs.itemGroup);
        
    }
    public FHBlockItem(Block block, Item.Properties props) {
    	this(block, props, FHTabs.itemGroup);
    }
    public FHBlockItem(Block block, Item.Properties props,TabType tab) {
        super(block, props);
        this.tab=tab;
    }


	@Override
	public void fillItemCategory(CreativeTabItemHelper helper) {
		if(getBlock() instanceof ICreativeModeTabItem item)
			item.fillItemCategory(helper);
		else if(helper.isType(tab))
			helper.accept(this);
	}
}
