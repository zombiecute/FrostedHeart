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

package com.teammoeg.frostedheart.content.research.research;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import com.teammoeg.chorda.util.lang.Components;
import com.teammoeg.frostedheart.content.research.FHResearch;
import com.teammoeg.frostedheart.content.research.gui.FHIcons.IconEditor;
import com.teammoeg.frostedheart.content.research.gui.editor.BaseEditDialog;
import com.teammoeg.frostedheart.content.research.gui.editor.EditListDialog;
import com.teammoeg.frostedheart.content.research.gui.editor.EditUtils;
import com.teammoeg.frostedheart.content.research.gui.editor.Editor;
import com.teammoeg.frostedheart.content.research.gui.editor.IngredientEditor;
import com.teammoeg.frostedheart.content.research.gui.editor.LabeledSelection;
import com.teammoeg.frostedheart.content.research.gui.editor.LabeledTextBox;
import com.teammoeg.frostedheart.content.research.gui.editor.LabeledTextBoxAndBtn;
import com.teammoeg.frostedheart.content.research.gui.editor.NumberBox;
import com.teammoeg.frostedheart.content.research.gui.editor.OpenEditorButton;
import com.teammoeg.frostedheart.content.research.gui.editor.SelectDialog;
import com.teammoeg.frostedheart.content.research.research.clues.ClueEditor;
import com.teammoeg.frostedheart.content.research.research.effects.EffectEditor;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.GuiGraphics;

public class ResearchEditorDialog extends BaseEditDialog {
    public static final Editor<Collection<Research>> RESEARCH_LIST = (p, l, v, c) -> new EditListDialog<>(p, l, v, null, SelectDialog.EDITOR_RESEARCH, e -> e.getName().getString(), Research::getFTBIcon, c).open();
    Research r;
    LabeledTextBox id, name;
    LabeledSelection<ResearchCategory> cat;
    NumberBox pts;
    LabeledSelection<Boolean> hide, alt, hidden, locked, showed, inf;
    boolean removed;

    public ResearchEditorDialog(Widget panel, Research r, ResearchCategory def) {
        super(panel);
        if (r == null) {
            r = new Research();
            r.setCategory(def == null ? ResearchCategory.RESCUE : def);
        }
        this.r = r;
        this.setY(-panel.getGui().getY() + 10);
        id = new LabeledTextBoxAndBtn(this, "id", r.getId(), "Random", t -> t.accept(Long.toHexString(UUID.randomUUID().getMostSignificantBits())));

        cat = new LabeledSelection<>(this, "category", r.getCategory(), ResearchCategory.values(), ResearchCategory::name);
        name = new LabeledTextBox(this, "name", r.name);
        pts = new NumberBox(this, "points", r.points);
        showed = LabeledSelection.createBool(this, "Keep this research show in list", r.alwaysShow);
        hide = LabeledSelection.createBool(this, "Hide effects before complete", r.hideEffects);
        alt = LabeledSelection.createBool(this, "Show alt description before complete", r.showfdesc);
        hidden = LabeledSelection.createBool(this, "Hide this research in list", r.isHidden);
        locked = LabeledSelection.createBool(this, "Lock this research", r.isInCompletable());
        inf = LabeledSelection.createBool(this, "Infinite", r.isInfinite());

    }


    @Override
    public void addWidgets() {
        add(EditUtils.getTitle(this, "Edit/New Research"));
        add(id);
        add(new SimpleTextButton(this, Components.str("Reset id"), Icon.empty()) {
            @Override
            public void onClicked(MouseButton arg0) {
                id.setText(r.getId());
            }
        });
        add(name);
        add(pts);
        add(new OpenEditorButton<>(this, "Edit minigame", ClueEditor.RESEARCH_GAME, r, s -> {
        }));
        add(new OpenEditorButton<>(this, "Set Icon", IconEditor.EDITOR, r.icon, r.icon.asFtbIcon(), s -> r.icon = s));
        add(cat);

        add(new OpenEditorButton<>(this, "Edit Description", EditListDialog.STRING_LIST, r.desc, s -> r.desc = new ArrayList<>(s)));
        add(new OpenEditorButton<>(this, "Edit Alternative Description", EditListDialog.STRING_LIST, r.fdesc, s -> r.fdesc = new ArrayList<>(s)));
        add(new OpenEditorButton<>(this, "Edit Parents", ResearchEditorDialog.RESEARCH_LIST, r.getParents(), s -> r.setParents(s.stream().map(Research::getId).collect(Collectors.toList()))));
        add(new OpenEditorButton<>(this, "Edit Children", ResearchEditorDialog.RESEARCH_LIST, r.getChildren(), s -> {
            r.getChildren().forEach(e -> e.removeParent(r));
            s.forEach(e -> {
                e.addParent(r);
                e.doIndex();
            });

        }));
        add(new OpenEditorButton<>(this, "Edit Ingredients", IngredientEditor.LIST_EDITOR, r.getRequiredItems(), s -> r.requiredItems = new ArrayList<>(s)));
        add(new OpenEditorButton<>(this, "Edit Effects", EffectEditor.EFFECT_LIST, r.getEffects(), s -> {
            //r.getEffects().forEach(Effect::deleteSelf);
            r.getEffects().clear();
            r.getEffects().addAll(s);
            r.doIndex();
        }));
        add(new OpenEditorButton<>(this, "Edit Clues", ClueEditor.EDITOR_LIST, r.getClues().stream().map(to->to.getClueClosure(r)).collect(Collectors.toList()), s -> {
           // r.getClues().forEach(Clue::deleteSelf);
            r.getClues().clear();
            s.forEach(t->r.getClues().add(t.clue()));
            r.doIndex();
        }));
        add(new SimpleTextButton(this, Components.str("Remove"), Icon.empty()) {

            @Override
            public void onClicked(MouseButton arg0) {
                removed = true;
                close();
            }

        });
        add(showed);
        add(hide);
        add(alt);
        add(inf);
        add(hidden);
        add(locked);
    }

    @Override
    public void draw(GuiGraphics matrixStack, Theme theme, int x, int y, int w, int h) {
        super.draw(matrixStack, theme, x, y, w, h);
        Research r = FHResearch.researches.getByName(id.getText());
        if (r != null && r != this.r)
            theme.drawString(matrixStack, "ID Existed!", x + id.width + 10, y + 10, Color4I.RED, 0);
    }


    @Override
    public void onClose() {
        if (removed) {
            r.delete();

        } else {
            r.name = name.getText();
            r.setCategory(cat.getSelection());
            r.points = (int)pts.getNum();
            r.alwaysShow = showed.getSelection();
            r.hideEffects = hide.getSelection();
            r.showfdesc = alt.getSelection();
            r.isHidden = hidden.getSelection();
            r.setInfinite(inf.getSelection());
            r.setInCompletable(locked.getSelection());

            if (!id.getText().isEmpty()) {
                r.setId(id.getText());
                //FHResearch.register(r);
            }

            EditUtils.saveResearch(r);
            FHResearch.load(r);
        }
        FHResearch.reindex();
    }

}
