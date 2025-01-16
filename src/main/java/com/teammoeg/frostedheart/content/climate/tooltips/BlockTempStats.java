package com.teammoeg.frostedheart.content.climate.tooltips;

import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.teammoeg.frostedheart.util.client.KeyControlledDesc;
import com.teammoeg.frostedheart.content.climate.data.BlockTempData;
import com.teammoeg.frostedheart.infrastructure.data.FHDataManager;
import com.teammoeg.frostedheart.util.client.FHTextIcon;
import com.teammoeg.frostedheart.util.client.Lang;
import com.teammoeg.frostedheart.content.climate.TemperatureDisplayHelper;
import com.teammoeg.chorda.util.lang.Components;
import com.teammoeg.chorda.util.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BlockTempStats implements TooltipModifier {
    protected final Block block;
    public BlockTempStats(Block block) {
        this.block = block;
    }

    @Nullable
    public static BlockTempStats create(Item item) {
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            return new BlockTempStats(block);
        }
        return null;
    }

    @Override
    public void modify(ItemTooltipEvent context) {
        List<Component> stats = getStats(block, context.getItemStack(), context.getEntity());
        KeyControlledDesc desc = new KeyControlledDesc(stats, new ArrayList<>(),
                GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_LEFT_CONTROL,
                "S", "Ctrl",
                "holdForTemperature", "holdForControls"
        );
        if (!stats.isEmpty()) {
            List<Component> tooltip = context.getToolTip();
            tooltip.add(Components.immutableEmpty());
            tooltip.addAll(desc.getCurrentLines());
        }
    }

    public static List<Component> getStats(Block block, @Nullable ItemStack stack, @Nullable Player player) {
        List<Component> list = new ArrayList<>();
        BlockTempData data = FHDataManager.getBlockData(block);
        if (data != null) {
            float heat = data.getTemp();
            heat = (Math.round(heat * 10)) / 10.0F;// round
            String s = TemperatureDisplayHelper.toTemperatureDeltaFloatString(heat);
            Lang.translate("tooltip", "temp.block")
                    .style(ChatFormatting.GRAY)
                    .addTo(list);

            int progress = Mth.ceil(Mth.clamp(Math.abs(heat) * 0.1, 0, 3));

            LangBuilder builder = Lang.builder()
                    .add(FHTextIcon.thermometer.getIcon())
                    .add(Lang.text(" " + s + " " + TooltipHelper.makeProgressBar(3, progress))
                            .style(heat < 0 ? ChatFormatting.AQUA : ChatFormatting.GOLD));
            builder.addTo(list);
        }
        return list;
    }
}
