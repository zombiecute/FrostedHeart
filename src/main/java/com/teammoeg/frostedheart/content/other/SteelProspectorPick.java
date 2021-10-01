package com.teammoeg.frostedheart.content.other;


import com.teammoeg.frostedheart.base.item.FHBaseItem;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;

public class SteelProspectorPick extends FHBaseItem {
    public SteelProspectorPick(String name, Properties properties) {
        super(name, properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        IWorld world = context.getWorld();
        BlockPos blockpos = context.getPos();
        int x = blockpos.getX();
        int y = blockpos.getY();
        int z = blockpos.getZ();
        int count = 0;
        Block ore;
        String ore_name = null;
        boolean found = false;
        if (player != null) {
            context.getItem().damageItem(1, player, (player2) -> player2.sendBreakAnimation(context.getHand()));
        }
        for (int x2 = -8; x2 < 8; x2++)
            for (int y2 = -4; y2 < 4; y2++)
                for (int z2 = -8; z2 < 8; z2++) {
                    int BlockX = x + x2;
                    int BlockY = y + y2;
                    int BlockZ = z + z2;
                    ore = world.getBlockState(new BlockPos(BlockX, BlockY, BlockZ)).getBlock();
                    if (ore.getTags().contains(ProspectorPick.tag)) {
                        count += 1;
                        if (!found) ;
                        {
                            ore_name = ore.getTranslationKey();
                            found = true;
                        }
                    }
                }
        if (player != null) {
            if (ore_name != null) {
                player.sendStatusMessage(new TranslationTextComponent(ore_name).appendString(" Count:" + count), true);
            } else {
                player.sendStatusMessage(new TranslationTextComponent("frostedheart.nothing"), true);
            }
        }
        return ActionResultType.SUCCESS;
    }
}