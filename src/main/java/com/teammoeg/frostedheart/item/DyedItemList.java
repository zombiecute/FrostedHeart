/*
 * Copyright (c) 2024 TeamMoeg
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

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class DyedItemList<T extends Item> implements Iterable<ItemEntry<T>> {
    private static final int COLOR_AMOUNT = DyeColor.values().length;

    private final ItemEntry<?>[] values = new ItemEntry<?>[COLOR_AMOUNT];

    public DyedItemList(Function<DyeColor, ItemEntry<? extends T>> filler) {
        for (DyeColor color : DyeColor.values()) {
            values[color.ordinal()] = filler.apply(color);
        }
    }

    @SuppressWarnings("unchecked")
    public ItemEntry<T> get(DyeColor color) {
        return (ItemEntry<T>) values[color.ordinal()];
    }

    public boolean contains(Block block) {
        for (ItemEntry<?> entry : values) {
            if (entry.is(block)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public ItemEntry<T>[] toArray() {
        return (ItemEntry<T>[]) Arrays.copyOf(values, values.length);
    }

    @Override
    public Iterator<ItemEntry<T>> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < values.length;
            }

            @SuppressWarnings("unchecked")
            @Override
            public ItemEntry<T> next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return (ItemEntry<T>) values[index++];
            }
        };
    }
}
