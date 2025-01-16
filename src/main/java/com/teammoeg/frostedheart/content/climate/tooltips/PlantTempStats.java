package com.teammoeg.frostedheart.content.climate.tooltips;

import com.simibubi.create.foundation.item.TooltipModifier;
import com.teammoeg.frostedheart.util.client.KeyControlledDesc;
import com.teammoeg.frostedheart.content.climate.TemperatureDisplayHelper;
import com.teammoeg.frostedheart.content.climate.WorldTemperature;
import com.teammoeg.frostedheart.content.climate.data.PlantTempData;
import com.teammoeg.chorda.util.CTooltipHelper;
import com.teammoeg.chorda.util.lang.Components;
import com.teammoeg.frostedheart.util.client.FHTextIcon;
import com.teammoeg.frostedheart.util.client.Lang;
import com.teammoeg.chorda.util.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class PlantTempStats implements TooltipModifier {
    protected final Block block;
    public PlantTempStats(Block block) {
        this.block = block;
    }

    @Nullable
    public static PlantTempStats create(Item item) {
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            // TODO: I don't know better way to do this
            // In general, BonemeableBlock is growable
            // IPlantable is plantable
            // but there are exceptions...
            if (block instanceof BonemealableBlock || block instanceof IPlantable) {
                return new PlantTempStats(block);
            }
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


    public static LangBuilder getTempProgressBar(float min, float max) {
        // remap from -30 to 30 to 0 to 6
        int low = Mth.ceil(Mth.clampedMap(min, -30, 30, 0, 6));
        int high = Mth.ceil(Mth.clampedMap(max, -30, 30, 0, 6));

        // bar
        String s = CTooltipHelper.makeProgressBarInterval(6, low, high);
        String s1 = s.substring(0, 3);
        String s2 = s.substring(3);

        LangBuilder builder = Lang.builder()
                .add(FHTextIcon.SOIL_THERMOMETER.getIcon())
                .add(Lang.text(" " + TemperatureDisplayHelper.toTemperatureFloatString(min))
                        .style(min <= 0 ? ChatFormatting.AQUA : ChatFormatting.GOLD))
                .add(Lang.text(" - ").style(ChatFormatting.GRAY))
                .add(Lang.text(TemperatureDisplayHelper.toTemperatureFloatString(max))
                        .style(max <= 0 ? ChatFormatting.AQUA : ChatFormatting.GOLD))
                .add(Lang.text(" "))
                .add(Lang.text(s1).style(ChatFormatting.AQUA))
                .add(Lang.text(s2).style(ChatFormatting.GOLD));

        return builder;
    }

    public static List<Component> getStats(Block block, @Nullable ItemStack stack, @Nullable Player player) {
        List<Component> list = new ArrayList<>();
        PlantTempData data = WorldTemperature.getPlantDataWithDefault(block);
        boolean bonemealable = block instanceof BonemealableBlock;

        Lang.translate("tooltip", "temp.plant.survive").style(ChatFormatting.GRAY).addTo(list);
        getTempProgressBar(data.getMinSurvive(), data.getMaxSurvive()).addTo(list);

        Lang.translate("tooltip", "temp.plant.grow").style(ChatFormatting.GRAY).addTo(list);
        getTempProgressBar(data.getMinGrow(), data.getMaxGrow()).addTo(list);

        if (bonemealable) {
            Lang.translate("tooltip", "temp.plant.fertilize").style(ChatFormatting.GRAY).addTo(list);
            getTempProgressBar(data.getMinFertilize(), data.getMaxFertilize()).addTo(list);
        }

        boolean vulnerableSnow = data.isSnowVulnerable();
        if (vulnerableSnow) {
            Lang.translate("tooltip", "temp.plant.snow_vulnerable")
                    .style(ChatFormatting.RED)
                    .addTo(list);
        } else {
            Lang.translate("tooltip", "temp.plant.snow_resistant")
                    .style(ChatFormatting.GREEN)
                    .addTo(list);
        }

        boolean vulnerableBlizzard = data.isBlizzardVulnerable();
        if (vulnerableBlizzard) {
            Lang.translate("tooltip", "temp.plant.blizzard_vulnerable")
                    .style(ChatFormatting.RED)
                    .addTo(list);
        } else {
            Lang.translate("tooltip", "temp.plant.blizzard_resistant")
                    .style(ChatFormatting.GREEN)
                    .addTo(list);
        }

        return list;
    }
}
