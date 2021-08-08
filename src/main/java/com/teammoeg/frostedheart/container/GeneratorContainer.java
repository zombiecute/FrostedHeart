package com.teammoeg.frostedheart.container;

import blusunrize.immersiveengineering.common.gui.IEBaseContainer;
import blusunrize.immersiveengineering.common.gui.IESlot;
import com.teammoeg.frostedheart.recipe.GeneratorRecipe;
import com.teammoeg.frostedheart.tileentity.GeneratorTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class GeneratorContainer extends IEBaseContainer<GeneratorTileEntity> {
    public GeneratorTileEntity.GeneratorData data;

    public GeneratorContainer(int id, PlayerInventory inventoryPlayer, GeneratorTileEntity tile) {
        super(inventoryPlayer, tile, id);

        this.addSlot(new IESlot(this, this.inv, 0, 80, 12) {
            @Override
            public boolean isItemValid(ItemStack itemStack) {
                return GeneratorRecipe.findRecipe(itemStack) != null;
            }
        });

        this.addSlot(new IESlot.Output(this, this.inv, 1, 80, 51));

        slotCount = 2;

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 9; j++)
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        data = tile.guiData;
        trackIntArray(data);
    }
}
