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

package com.teammoeg.frostedheart.research.gui.editor;

import dev.ftb.mods.ftblibrary.ui.Panel;

public class RealBox extends LabeledTextBox {

    public RealBox(Panel panel, String lab, double val) {
        super(panel, lab, String.valueOf(val));
    }

    public void setNum(double number) {
        super.setText(String.valueOf(number));
    }

    ;

    public double getNum() {
        try {
            return Double.parseDouble(getText());
        } catch (NumberFormatException ex) {

        }
        return Double.parseDouble(orig);
    }

    ;
}
