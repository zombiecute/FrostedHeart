/*
 * Copyright (c) 2023-2024 TeamMoeg
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

package com.teammoeg.chorda.util.evaluator;

class ConstNode implements Node {
    double val;

    public ConstNode(double val) {
        this.val = val;
    }

    @Override
    public double eval(IEnvironment env) {
        return val;
    }

    @Override
    public boolean isPrimary() {
        return true;
    }

    @Override
    public Node simplify() {
        return this;
    }

    @Override
    public String toString() {
        return "" + val;
    }
}