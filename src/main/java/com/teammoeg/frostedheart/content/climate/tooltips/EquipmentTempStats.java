package com.teammoeg.frostedheart.content.climate.tooltips;

import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.teammoeg.frostedheart.util.client.KeyControlledDesc;
import com.teammoeg.frostedheart.content.climate.player.IHeatingEquipment;
import com.teammoeg.frostedheart.util.client.FHTextIcon;
import com.teammoeg.frostedheart.util.client.Lang;
import com.teammoeg.frostedheart.content.climate.TemperatureDisplayHelper;
import com.teammoeg.chorda.util.lang.Components;
import com.teammoeg.chorda.util.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class EquipmentTempStats implements TooltipModifier {
    protected final IHeatingEquipment item;
    public EquipmentTempStats(IHeatingEquipment item) {
        this.item = item;
    }

    @Nullable
    public static EquipmentTempStats create(Item item) {
        if (item instanceof IHeatingEquipment heatingEquipment) {
            return new EquipmentTempStats(heatingEquipment);
        }
        return null;
    }

    @Override
    public void modify(ItemTooltipEvent context) {
        List<Component> stats = getStats(item, context.getItemStack(), context.getEntity());
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

    public static List<Component> getStats(IHeatingEquipment item, ItemStack stack, Player player) {
        List<Component> list = new ArrayList<>();
        float heat = item.getEffectiveTempAdded(null, stack,0, 0);
        heat = (Math.round(heat * 2000)) / 1000.0F;
        if (heat != 0) {
            String s = TemperatureDisplayHelper.toTemperatureDeltaFloatString(heat);
            Lang.translate("tooltip", "temp.item")
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
