/*
 * Copyright (c) 2022-2024 TeamMoeg
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

package com.teammoeg.frostedheart.content.research.gui.tech;

import java.text.DecimalFormat;

import com.teammoeg.chorda.util.lang.Components;
import com.teammoeg.frostedheart.content.research.api.ClientResearchDataAPI;
import com.teammoeg.frostedheart.content.research.data.TeamResearchData;
import com.teammoeg.frostedheart.util.client.Lang;
import net.minecraft.client.gui.GuiGraphics;
import com.teammoeg.frostedheart.content.research.FHResearch;
import com.teammoeg.frostedheart.content.research.data.ResearchData;
import com.teammoeg.frostedheart.content.research.gui.RTextField;
import com.teammoeg.frostedheart.content.research.gui.TechIcons;
import com.teammoeg.frostedheart.content.research.gui.TechTextButton;
import com.teammoeg.frostedheart.content.research.gui.editor.EditUtils;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;

public class ResearchDashboardPanel extends Panel {

    final static String read = "kmgtpezyh";
    final static DecimalFormat df1 = new DecimalFormat("#.#");

    final static DecimalFormat df2 = new DecimalFormat("#.##");

    final static DecimalFormat df3 = new DecimalFormat("#.##");

    ResearchDetailPanel detailPanel;
    RTextField techpoint;
    RTextField availableInsightLevel;
    public static synchronized String toReadable(long num) {
        int unit = -1;
        double lnum = num;
        while (lnum > 1999) {
            unit++;
            lnum /= 1000;
        }
        if (unit < 0)
            return String.valueOf(num);
        if (lnum >= 1000) {
            return "" + ((long) lnum) + read.charAt(unit);
        } else if (lnum >= 100) {
            return df1.format(lnum) + read.charAt(unit);
        } else if (lnum >= 10) {
            return df2.format(lnum) + read.charAt(unit);
        } else {
            return df3.format(lnum) + read.charAt(unit);
        }
    }
    public ResearchDashboardPanel(ResearchDetailPanel panel) {
        super(panel);
        this.setOnlyInteractWithWidgetsInside(true);
        this.setOnlyRenderWidgetsInside(true);
        detailPanel = panel;
        techpoint = new RTextField(this).setMaxWidth(100).setMaxLine(1).setColor(TechIcons.text);
        techpoint.setPos(40, 28);
        availableInsightLevel = new RTextField(this).setMaxWidth(100).setMaxLine(1).setColor(TechIcons.text);
        availableInsightLevel.setPos(60, 28);
    }

    @Override
    public void addWidgets() {
        // close panel button
       /* Button closePanel = new SimpleTextButton(this, new StringTextComponent("Close"), Icon.EMPTY) {
            @Override
            public void onClicked(MouseButton mouseButton) {
                detailPanel.close();
                //closeGui();
            }
        };
        closePanel.setPosAndSize(width-PADDING, 0, PADDING, PADDING);
        add(closePanel);*/
        RTextField tf = new RTextField(this);
        tf.setPos(0, 0);
        add(tf);
        tf.setMaxWidth(140).setMinWidth(140).setMaxLine(2).setColor(TechIcons.text).addFlags(4);
        tf.setText(detailPanel.research.getName());
        if (FHResearch.editor) {
            Button create = new TechTextButton(this, Components.str("edit"),
                    Icon.empty()) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    EditUtils.editResearch(this, detailPanel.research);
                }
            };
            create.setPos(40, 30);
            add(create);
        }
        techpoint.setText(toReadable(detailPanel.research.getRequiredPoints()) + "IOPS");
        add(techpoint);
        TeamResearchData data = ClientResearchDataAPI.getData().get();
        availableInsightLevel.setText(data.getAvailableInsightLevel() + "Insight Points");
        add(availableInsightLevel);
    }

    @Override
    public void alignWidgets() {

    }

    @Override
    public void draw(GuiGraphics matrixStack, Theme theme, int x, int y, int w, int h) {
        ResearchData rd = detailPanel.research.getData();

        techpoint.setColor(TechIcons.text);
        if (rd.canResearch()) {
            if (!rd.canComplete(detailPanel.research))
                techpoint.setColor(TechIcons.text_red);
            techpoint.setText(toReadable(rd.getTotalCommitted(detailPanel.research)) + "/" + toReadable(detailPanel.research.getRequiredPoints()) + "IOPS");
        }
        techpoint.setX(140 - techpoint.width);

        TeamResearchData data = ClientResearchDataAPI.getData().get();
        if (data.getAvailableInsightLevel() == 0) {
            availableInsightLevel.setColor(TechIcons.text_red);
        } else {
            availableInsightLevel.setColor(TechIcons.text);
        }
        availableInsightLevel.setX(160 - availableInsightLevel.width);
        super.draw(matrixStack, theme, x, y, w, h);

        // name
        //theme.drawString(matrixStack, detailPanel.research.getName(), x+7, y+8);
        // icon
        TechIcons.SHADOW.draw(matrixStack, x + 1, y + 36, 36, 9);
        detailPanel.icon.draw(matrixStack, x + 3, y + 10, 32, 32);
        theme.drawString(matrixStack, Lang.translateGui("research.points"), x + 40, y + 19, TechIcons.text, 0);
        if (rd.canResearch() && !rd.canComplete(detailPanel.research))
            theme.drawString(matrixStack, Lang.translateGui("research.required_clue"), x + 40, y + 38, TechIcons.text_red, 0);
        GuiHelper.setupDrawing();
        TechIcons.HLINE_L.draw(matrixStack, x, y + 49, 140, 3);

        // TODO: research progress
        // ResearchData data = ResearchDataAPI.getData((ServerPlayerEntity) detailPanel.researchScreen.player).getData(detailPanel.research);
        // theme.drawString(matrixStack, data.getProgress()*100 + "%", x+theme.getStringWidth(detailPanel.research.getName())+5, y);
    }
}
