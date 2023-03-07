/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.diagram.module;

import com.intellij.diagram.AbstractUmlVisibilityManager;
import com.intellij.diagram.VisibilityLevel;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * @author Eugene.Kudelevsky
 */
public final class ModuleDepDiagramVisibilityManager extends AbstractUmlVisibilityManager {

    public static final VisibilityLevel SMALL = new VisibilityLevel("Small");
    public static final VisibilityLevel MEDIUM = new VisibilityLevel("Medium");
    public static final VisibilityLevel LARGE = new VisibilityLevel("Large");

    private static final VisibilityLevel[] LEVELS = {SMALL, MEDIUM, LARGE};

    private final Comparator<VisibilityLevel> COMPARATOR = Comparator.comparingInt(level -> ArrayUtil.indexOf(LEVELS, level));

    public ModuleDepDiagramVisibilityManager() {
        setCurrentVisibilityLevel(SMALL);
    }

    @Override
    public VisibilityLevel @NotNull [] getVisibilityLevels() {
        return LEVELS.clone();
    }

    @Nullable
    @Override
    public VisibilityLevel getVisibilityLevel(final Object o) {
        return null;
    }

    @Override
    public @NotNull Comparator<VisibilityLevel> getComparator() {
        return COMPARATOR;
    }

    @Override
    public boolean isRelayoutNeeded() {
        return true;
    }
}
